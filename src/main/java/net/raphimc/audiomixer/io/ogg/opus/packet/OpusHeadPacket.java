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
package net.raphimc.audiomixer.io.ogg.opus.packet;

import net.raphimc.audiomixer.util.io.BinaryInputStream;
import net.raphimc.audiomixer.util.io.BinaryOutputStream;

import java.io.IOException;
import java.util.Arrays;

public class OpusHeadPacket {

    private static final byte[] MAGIC = new byte[]{(byte) 'O', (byte) 'p', (byte) 'u', (byte) 's', (byte) 'H', (byte) 'e', (byte) 'a', (byte) 'd'};

    private final byte majorVersion;
    private final byte minorVersion;
    private final int outputChannels;
    private final int preSkip;
    private final long inputSampleRate;
    private final short outputGain;

    public OpusHeadPacket(final BinaryInputStream inputStream) throws IOException {
        final byte[] magic = inputStream.readBytes(MAGIC.length);
        if (!Arrays.equals(magic, MAGIC)) {
            throw new IOException("Invalid OpusHead packet: Incorrect magic bytes");
        }
        final int version = inputStream.readUnsignedByte();
        this.minorVersion = (byte) (version & 0xF);
        this.majorVersion = (byte) (version >> 4);
        if (this.majorVersion > 0) {
            throw new IOException("Unsupported version: " + this.majorVersion + "." + this.minorVersion);
        }
        this.outputChannels = inputStream.readUnsignedByte();
        this.preSkip = inputStream.readUnsignedShort();
        this.inputSampleRate = inputStream.readUnsignedInt();
        this.outputGain = inputStream.readShort();
        final int channelMappingFamily = inputStream.readUnsignedByte();
        if (channelMappingFamily == 0) {
            if (this.outputChannels < 1 || this.outputChannels > 2) {
                throw new IOException("Unsupported output channel count: " + this.outputChannels);
            }
        } else {
            throw new IOException("Unsupported channel mapping family: " + channelMappingFamily);
        }
    }

    public OpusHeadPacket(final int outputChannels, final int preSkip, final long inputSampleRate) {
        this((byte) 0, (byte) 1, outputChannels, preSkip, inputSampleRate, (short) 0);
    }

    public OpusHeadPacket(final byte majorVersion, final byte minorVersion, final int outputChannels, final int preSkip, final long inputSampleRate, final short outputGain) {
        if (majorVersion > 0) {
            throw new IllegalArgumentException("Unsupported version: " + majorVersion + "." + minorVersion);
        }
        if (outputChannels < 1 || outputChannels > 2) {
            throw new IllegalArgumentException("Unsupported output channel count: " + outputChannels);
        }
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.outputChannels = outputChannels;
        this.preSkip = preSkip;
        this.inputSampleRate = inputSampleRate;
        this.outputGain = outputGain;
    }

    public void write(final BinaryOutputStream outputStream) throws IOException {
        outputStream.write(MAGIC);
        outputStream.writeUnsignedByte((this.majorVersion << 4) | (this.minorVersion & 0xF));
        outputStream.writeUnsignedByte(this.outputChannels);
        outputStream.writeUnsignedShort(this.preSkip);
        outputStream.writeUnsignedInt(this.inputSampleRate);
        outputStream.writeShort(this.outputGain);
        outputStream.writeUnsignedByte(0); // channel mapping family
    }

    public byte majorVersion() {
        return this.majorVersion;
    }

    public byte minorVersion() {
        return this.minorVersion;
    }

    public int outputChannels() {
        return this.outputChannels;
    }

    public int preSkip() {
        return this.preSkip;
    }

    public long inputSampleRate() {
        return this.inputSampleRate;
    }

    public short outputGain() {
        return this.outputGain;
    }

}
