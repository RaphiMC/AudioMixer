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
import javax.sound.sampled.AudioInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class SampleInputStream extends InputStream {

    private final InputStream is;
    private final AudioFormat audioFormat;
    private final byte[] buffer;
    private int bufferIndex;
    private int bufferLength;

    public SampleInputStream(final AudioInputStream audioInputStream) {
        this(audioInputStream, audioInputStream.getFormat());
    }

    public SampleInputStream(final InputStream is, final AudioFormat audioFormat) {
        if (audioFormat.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
            throw new IllegalArgumentException("Unsupported audio format: " + audioFormat);
        }

        this.is = is;
        this.audioFormat = audioFormat;
        this.buffer = new byte[(audioFormat.getSampleSizeInBits() / 8) * audioFormat.getChannels()];
    }

    @Override
    public int read() throws IOException {
        if (this.bufferIndex >= this.bufferLength) {
            this.bufferLength = this.is.readNBytes(this.buffer, 0, this.buffer.length);
            if (this.bufferLength == 0) {
                return -1;
            }
            this.bufferIndex = 0;
        }

        return this.buffer[this.bufferIndex++] & 0xFF;
    }

    @Override
    public void close() throws IOException {
        this.is.close();
    }

    public int readSample() throws IOException {
        switch (this.audioFormat.getSampleSizeInBits()) {
            case 8:
                final int b1 = this.read();
                if (b1 == -1) throw new EOFException();
                return (byte) b1;
            case 16:
                return this.read16Bit();
            case 32:
                return this.read32Bit();
            default:
                throw new UnsupportedOperationException("Unsupported sample size: " + this.audioFormat.getSampleSizeInBits());
        }
    }

    public AudioFormat getAudioFormat() {
        return this.audioFormat;
    }

    private short read16Bit() throws IOException {
        final int b1 = this.read();
        final int b2 = this.read();
        if (b1 == -1 || b2 == -1) throw new EOFException();
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
        if (b1 == -1 || b2 == -1 || b3 == -1 || b4 == -1) throw new EOFException();
        if (this.audioFormat.isBigEndian()) {
            return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
        } else {
            return (b4 << 24) | (b3 << 16) | (b2 << 8) | b1;
        }
    }

}
