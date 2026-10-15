/*
 * This file is part of AudioMixer - https://github.com/RaphiMC/AudioMixer
 * Copyright (C) 2024-2026 RK_01/RaphiMC and contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.raphimc.audiomixer.io.mp3;

import de.sciss.jump3r.mpg.Common;
import de.sciss.jump3r.mpg.MPG123;
import net.raphimc.audiomixer.util.io.BinaryInputStream;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.Arrays;

public class Mp3InputStream implements Closeable {

    private static final byte[] ID3V1_MAGIC = new byte[]{'T', 'A', 'G'};
    private static final byte[] ID3V2_MAGIC = new byte[]{'I', 'D', '3'};
    private static final byte[] APE_MAGIC = new byte[]{'A', 'P', 'E', 'T', 'A', 'G', 'E', 'X'};

    private final BinaryInputStream inputStream;
    private final byte[] buffer = new byte[MPG123.MAXFRAMESIZE + 1];

    public Mp3InputStream(final InputStream inputStream) throws IOException {
        this.inputStream = new BinaryInputStream(inputStream instanceof BufferedInputStream ? inputStream : new BufferedInputStream(inputStream));
        this.skipTags();
    }

    public Frame readFrameOrThrow() throws IOException {
        final Frame frame = this.readFrame();
        if (frame == null) {
            throw new EOFException();
        }
        return frame;
    }

    public Frame readFrame() throws IOException {
        if (this.isEndOfStream()) {
            return null;
        }
        final int header = this.peekInt();
        if (((header >>> 21) & 0x7FF) != 0x7FF) {
            if (this.skipTags() && this.isEndOfStream()) {
                return null;
            }
            throw new IOException("Invalid frame sync");
        }
        final Frame.MpegVersion mpegVersion = Frame.MpegVersion.fromHeader((header >>> 19) & 3);
        if (mpegVersion == null) {
            throw new IOException("Invalid MPEG version");
        }
        final Frame.Layer layer = Frame.Layer.fromHeader((header >>> 17) & 3);
        if (layer == null) {
            throw new IOException("Invalid layer");
        }
        final int bitrateIndex = (header >>> 12) & 15;
        if (bitrateIndex == 0) {
            throw new IOException("Free-format bitrate is not supported");
        }
        if (bitrateIndex == 15) {
            throw new IOException("Invalid bitrate index");
        }
        final int bitrate = Common.tabsel_123[mpegVersion.major() - 1][layer.ordinal()][bitrateIndex];
        final int sampleRateIndex = (header >>> 10) & 3;
        if (sampleRateIndex == 3) {
            throw new IOException("Invalid sample rate index");
        }
        final int sampleRate = Common.freqs[mpegVersion.ordinal() * Frame.MpegVersion.values().length + sampleRateIndex];
        final int padding = (header >>> 9) & 1;
        final Frame.ChannelMode channelMode = Frame.ChannelMode.values()[(header >>> 6) & 3];

        final int length = switch (layer) {
            case LAYER_1 -> (12 * bitrate * 1000 / sampleRate + padding) * 4;
            case LAYER_2 -> 144 * bitrate * 1000 / sampleRate + padding;
            case LAYER_3 -> switch (mpegVersion) {
                case MPEG_1 -> 144 * bitrate * 1000 / sampleRate + padding;
                case MPEG_2, MPEG_2_5 -> 72 * bitrate * 1000 / sampleRate + padding;
            };
        };
        this.inputStream.readBytes(this.buffer, 0, length);
        return new Frame(this.buffer, 0, length, mpegVersion, layer, bitrate, sampleRate, channelMode);
    }

    @Override
    public void close() throws IOException {
        this.inputStream.close();
    }

    private boolean skipTags() throws IOException {
        boolean skipped = false;
        while (true) {
            if (this.hasId3v2Tag()) {
                this.skipId3v2Tag();
            } else if (this.hasId3v1Tag()) {
                this.skipId3v1Tag();
            } else if (this.hasApeTag()) {
                this.skipApeTag();
            } else {
                break;
            }
            skipped = true;
        }
        return skipped;
    }

    private boolean hasId3v1Tag() throws IOException {
        this.inputStream.mark(ID3V1_MAGIC.length);
        try {
            return Arrays.equals(this.inputStream.readNBytes(ID3V1_MAGIC.length), ID3V1_MAGIC);
        } finally {
            this.inputStream.reset();
        }
    }

    private void skipId3v1Tag() throws IOException {
        if (!Arrays.equals(this.inputStream.readBytes(ID3V1_MAGIC.length), ID3V1_MAGIC)) {
            throw new IOException("Invalid ID3v1 tag: Incorrect magic bytes");
        }
        this.inputStream.skipNBytes(125);
    }

    private boolean hasId3v2Tag() throws IOException {
        this.inputStream.mark(ID3V2_MAGIC.length);
        try {
            return Arrays.equals(this.inputStream.readNBytes(ID3V2_MAGIC.length), ID3V2_MAGIC);
        } finally {
            this.inputStream.reset();
        }
    }

    private void skipId3v2Tag() throws IOException {
        if (!Arrays.equals(this.inputStream.readBytes(ID3V2_MAGIC.length), ID3V2_MAGIC)) {
            throw new IOException("Invalid ID3v2 tag: Incorrect magic bytes");
        }
        final int majorVersion = this.inputStream.readUnsignedByte();
        final int revisionNumber = this.inputStream.readUnsignedByte();
        if (majorVersion == 0xFF || revisionNumber == 0xFF) {
            throw new IOException("Invalid version");
        }
        if (majorVersion < 2 || majorVersion > 4) {
            throw new IOException("Unsupported version: 2." + majorVersion + "." + revisionNumber);
        }
        final int flags = this.inputStream.readUnsignedByte();
        this.inputStream.skipNBytes(this.readSyncSafeInt()); // skip tag
        if (majorVersion == 4 && (flags & 0x10) != 0) {
            this.inputStream.skipNBytes(10); // skip footer
        }
    }

    private boolean hasApeTag() throws IOException {
        this.inputStream.mark(APE_MAGIC.length);
        try {
            return Arrays.equals(this.inputStream.readNBytes(APE_MAGIC.length), APE_MAGIC);
        } finally {
            this.inputStream.reset();
        }
    }

    private void skipApeTag() throws IOException {
        if (!Arrays.equals(this.inputStream.readBytes(APE_MAGIC.length), APE_MAGIC)) {
            throw new IOException("Invalid APE tag: Incorrect magic bytes");
        }
        this.inputStream.skipNBytes(Integer.BYTES); // version
        this.inputStream.skipNBytes(Math.addExact(this.inputStream.readUnsignedInt(ByteOrder.LITTLE_ENDIAN), 16));
    }

    private int readSyncSafeInt() throws IOException {
        int value = 0;
        for (int i = 0; i < Integer.BYTES; i++) {
            final int b = this.inputStream.readUnsignedByte();
            if ((b & 0x80) != 0) {
                throw new IOException("Invalid sync-safe integer");
            }
            value = (value << 7) | b;
        }
        return value;
    }

    private int peekInt() throws IOException {
        this.inputStream.mark(Integer.BYTES);
        try {
            return this.inputStream.readInt();
        } finally {
            this.inputStream.reset();
        }
    }

    private boolean isEndOfStream() throws IOException {
        this.inputStream.mark(1);
        try {
            return this.inputStream.read() == -1;
        } finally {
            this.inputStream.reset();
        }
    }

    public record Frame(byte[] data, int offset, int length, MpegVersion mpegVersion, Layer layer, int bitrate, int sampleRate, Frame.ChannelMode channelMode) {

        public enum MpegVersion {

            MPEG_1(1),
            MPEG_2(2),
            MPEG_2_5(2);

            private final int major;

            private static MpegVersion fromHeader(final int value) {
                return switch (value) {
                    case 0 -> MPEG_2_5;
                    case 2 -> MPEG_2;
                    case 3 -> MPEG_1;
                    default -> null;
                };
            }

            MpegVersion(final int major) {
                this.major = major;
            }

            public int major() {
                return this.major;
            }

        }

        public enum Layer {

            LAYER_1,
            LAYER_2,
            LAYER_3;

            private static Layer fromHeader(final int value) {
                return switch (value) {
                    case 1 -> LAYER_3;
                    case 2 -> LAYER_2;
                    case 3 -> LAYER_1;
                    default -> null;
                };
            }

        }

        public enum ChannelMode {

            STEREO(2),
            JOINT_STEREO(2),
            DUAL_CHANNEL(2),
            SINGLE_CHANNEL(1);

            private final int channelCount;

            ChannelMode(final int channelCount) {
                this.channelCount = channelCount;
            }

            public int channelCount() {
                return this.channelCount;
            }

        }

    }

}
