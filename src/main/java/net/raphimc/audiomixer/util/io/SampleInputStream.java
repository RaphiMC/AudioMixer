/*
 * This file is part of AudioMixer - https://github.com/RaphiMC/AudioMixer
 * Copyright (C) 2024-2024 RK_01/RaphiMC and contributors
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
package net.raphimc.audiomixer.util.io;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.io.InputStream;

public class SampleInputStream extends InputStream {

    private final InputStream is;
    private final AudioFormat audioFormat;

    public SampleInputStream(final InputStream is, final AudioFormat audioFormat) {
        if (audioFormat.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
            throw new IllegalArgumentException("Unsupported audio format: " + audioFormat);
        }

        this.is = is;
        this.audioFormat = audioFormat;
    }

    @Override
    public int read() throws IOException {
        return this.is.read();
    }

    @Override
    public void close() throws IOException {
        this.is.close();
    }

    public int readSample() throws IOException {
        switch (this.audioFormat.getSampleSizeInBits()) {
            case 8:
                return (byte) this.read();
            case 16:
                return this.read16Bit();
            case 32:
                return this.read32Bit();
            default:
                throw new UnsupportedOperationException("Unsupported sample size: " + this.audioFormat.getSampleSizeInBits());
        }
    }

    private short read16Bit() throws IOException {
        final int b1 = this.read();
        final int b2 = this.read();
        if (b1 == -1 || b2 == -1) return -1;
        if (this.audioFormat.isBigEndian()) {
            return (short) ((b1 << 8) | b2);
        } else {
            return (short) ((b2 << 8) | b1);
        }
    }

    private int read32Bit() throws IOException {
        final int b1 = this.read();
        final int b2 = this.read();
        final int b3 = this.read();
        final int b4 = this.read();
        if (b1 == -1 || b2 == -1 || b3 == -1 || b4 == -1) return -1;
        if (this.audioFormat.isBigEndian()) {
            return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
        } else {
            return (b4 << 24) | (b3 << 16) | (b2 << 8) | b1;
        }
    }

}
