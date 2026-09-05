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

import net.raphimc.audiomixer.util.io.BinaryInputStream;
import net.raphimc.audiomixer.util.io.BoundedInputStream;
import net.raphimc.audiomixer.util.io.riff.RiffChunk;
import net.raphimc.audiomixer.util.io.riff.RiffInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.UUID;

public class WavInputStream extends RiffInputStream {

    private static final int WAVE_FORMAT_EXTENSIBLE = 0xFFFE;
    private static final long WAVEFORMATEX_GUID_MSB = 0x0000000000000010L;
    private static final long WAVEFORMATEX_GUID_LSB = 0x800000AA00389B71L;

    private final UUID format;
    private final int channelCount;
    private final long sampleRate;
    private final long byteRate;
    private final int blockAlign;
    private final int bitsPerSample;
    private final int extensibleSampleInfo;
    private final long channelMask;
    private final byte[] formatMetadata;

    public WavInputStream(final InputStream in) throws IOException {
        super(in);
        if (!this.getRootChunk().identifier().equals("WAVE")) {
            throw new IOException("Invalid WAV stream: Expected 'WAVE' but got '" + this.getRootChunk().identifier() + "'");
        }
        try (RiffChunk fmtChunk = this.readUntilChunk("fmt ")) {
            final int formatTag = this.readUnsignedShort();
            this.channelCount = this.readUnsignedShort();
            this.sampleRate = this.readUnsignedInt();
            this.byteRate = this.readUnsignedInt();
            this.blockAlign = this.readUnsignedShort();
            this.bitsPerSample = this.readUnsignedShort();
            if (formatTag == WAVE_FORMAT_EXTENSIBLE) {
                final BoundedInputStream boundedInputStream = new BoundedInputStream(this, this.readUnsignedShort());
                final BinaryInputStream metadataInputStream = new BinaryInputStream(boundedInputStream, ByteOrder.LITTLE_ENDIAN);
                this.extensibleSampleInfo = metadataInputStream.readUnsignedShort();
                this.channelMask = metadataInputStream.readUnsignedInt();
                this.format = readGuid(metadataInputStream);
                this.formatMetadata = metadataInputStream.readBytes(Math.toIntExact(boundedInputStream.getRemaining()));
            } else {
                this.format = new UUID(((long) formatTag << 32) | WAVEFORMATEX_GUID_MSB, WAVEFORMATEX_GUID_LSB);
                this.extensibleSampleInfo = -1;
                this.channelMask = 0L;
                this.formatMetadata = fmtChunk.remaining() > 0 ? this.readBytes(this.readUnsignedShort()) : new byte[0];
            }
        }
        this.readUntilChunk("data");
    }

    public UUID getFormat() {
        return this.format;
    }

    public int getChannelCount() {
        return this.channelCount;
    }

    public long getSampleRate() {
        return this.sampleRate;
    }

    public long getByteRate() {
        return this.byteRate;
    }

    public int getBlockAlign() {
        return this.blockAlign;
    }

    public int getBitsPerSample() {
        return this.bitsPerSample;
    }

    public int getExtensibleSampleInfo() {
        return this.extensibleSampleInfo;
    }

    public long getChannelMask() {
        return this.channelMask;
    }

    public BinaryInputStream getFormatMetadataInputStream() {
        return new BinaryInputStream(new ByteArrayInputStream(this.formatMetadata), ByteOrder.LITTLE_ENDIAN);
    }

    private static UUID readGuid(final BinaryInputStream inputStream) throws IOException {
        final long data1 = inputStream.readUnsignedInt();
        final long data2 = inputStream.readUnsignedShort();
        final long data3 = inputStream.readUnsignedShort();
        final long msb = (data1 << 32) | (data2 << 16) | data3;
        final long lsb = inputStream.readLong(ByteOrder.BIG_ENDIAN);
        return new UUID(msb, lsb);
    }

}
