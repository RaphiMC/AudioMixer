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
package net.raphimc.audiomixer.io.wav;

import net.raphimc.audiomixer.io.pcm.PcmAudioInputStream;
import net.raphimc.audiomixer.io.wav.riff.RiffChunk;
import net.raphimc.audiomixer.io.wav.riff.RiffInputStream;
import net.raphimc.audiomixer.util.PcmSampleEncoding;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HexFormat;

public class WavPcmAudioInputStream extends PcmAudioInputStream {

    private static final int WAVE_FORMAT_PCM = 0x0001;
    private static final int WAVE_FORMAT_IEEE_FLOAT = 0x0003;
    private static final int WAVE_FORMAT_EXTENSIBLE = 0xFFFE;

    private static final int SUBTYPE_LENGTH = 16;
    private static final byte[] SUBTYPE_PCM = {
        (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x10, (byte) 0x00,
        (byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0xAA, (byte) 0x00, (byte) 0x38, (byte) 0x9B, (byte) 0x71
    };
    private static final byte[] SUBTYPE_IEEE_FLOAT = {
        (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x10, (byte) 0x00,
        (byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0xAA, (byte) 0x00, (byte) 0x38, (byte) 0x9B, (byte) 0x71
    };

    private final long channelMask;

    public WavPcmAudioInputStream(final InputStream inputStream) throws IOException {
        this(new CodeBeforeSuper(inputStream));
    }

    private WavPcmAudioInputStream(final CodeBeforeSuper codeBeforeSuper) {
        super(codeBeforeSuper.riffInputStream, codeBeforeSuper.sampleRate, codeBeforeSuper.channels, codeBeforeSuper.encoding);
        this.channelMask = codeBeforeSuper.channelMask;
    }

    public long getChannelMask() {
        return this.channelMask;
    }

    private static final class CodeBeforeSuper {

        private final RiffInputStream riffInputStream;
        private final long sampleRate;
        private final int channels;
        private final PcmSampleEncoding encoding;
        private final long channelMask;

        private CodeBeforeSuper(final InputStream inputStream) throws IOException {
            this.riffInputStream = new RiffInputStream(inputStream);
            if (!this.riffInputStream.getRootChunk().identifier().equals("WAVE")) {
                throw new IOException("Invalid WAV stream: Expected 'WAVE' but got '" + this.riffInputStream.getRootChunk().identifier() + "'");
            }
            try (RiffChunk fmtChunk = this.riffInputStream.findNextChunk("fmt ")) {
                int format = this.riffInputStream.readUnsignedShort();
                this.channels = this.riffInputStream.readUnsignedShort();
                this.sampleRate = this.riffInputStream.readUnsignedInt();
                this.riffInputStream.readUnsignedInt(); // byte rate
                this.riffInputStream.readUnsignedShort(); // frame size
                final int bitsPerSample = this.riffInputStream.readUnsignedShort();

                if (format == WAVE_FORMAT_EXTENSIBLE) {
                    final int size = this.riffInputStream.readUnsignedShort();
                    if (size < 22) {
                        throw new IOException("Invalid WAVE_FORMAT_EXTENSIBLE size: Expected at least 22 but got " + size);
                    }
                    final int validBitsPerSample = this.riffInputStream.readUnsignedShort();
                    if (validBitsPerSample != bitsPerSample) {
                        throw new IOException("Unsupported valid bits per sample: " + validBitsPerSample);
                    }
                    this.channelMask = this.riffInputStream.readUnsignedInt();
                    final byte[] subFormat = this.riffInputStream.readBytes(SUBTYPE_LENGTH);
                    if (Arrays.equals(subFormat, SUBTYPE_PCM)) {
                        format = WAVE_FORMAT_PCM;
                    } else if (Arrays.equals(subFormat, SUBTYPE_IEEE_FLOAT)) {
                        format = WAVE_FORMAT_IEEE_FLOAT;
                    } else {
                        throw new IOException("Unsupported subformat: " + HexFormat.of().formatHex(subFormat));
                    }
                } else {
                    this.channelMask = 0;
                }

                this.encoding = switch (format) {
                    case WAVE_FORMAT_PCM -> switch (bitsPerSample) {
                        case 8 -> PcmSampleEncoding.U8;
                        case 16 -> PcmSampleEncoding.S16_LE;
                        case 24 -> PcmSampleEncoding.S24_LE;
                        case 32 -> PcmSampleEncoding.S32_LE;
                        default -> throw new IOException("Unsupported PCM bit depth: " + bitsPerSample);
                    };
                    case WAVE_FORMAT_IEEE_FLOAT -> switch (bitsPerSample) {
                        case 32 -> PcmSampleEncoding.F32_LE;
                        case 64 -> PcmSampleEncoding.F64_LE;
                        default -> throw new IOException("Unsupported IEEE float bit depth: " + bitsPerSample);
                    };
                    default -> throw new IOException("Unsupported format: " + format);
                };
            }
            this.riffInputStream.findNextChunk("data");
        }

    }

}
