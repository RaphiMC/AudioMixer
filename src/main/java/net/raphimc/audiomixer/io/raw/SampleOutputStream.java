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
package net.raphimc.audiomixer.io.raw;

import net.raphimc.audiomixer.util.PcmFloatAudioFormat;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SampleOutputStream extends OutputStream {

    private final OutputStream os;
    private final AudioInputStream is;
    private final byte[] writeBuffer;
    private final byte[] readBuffer;
    private int bufferIndex;

    public SampleOutputStream(final OutputStream os, final AudioFormat targetAudioFormat) {
        final AudioFormat sourceAudioFormat = new PcmFloatAudioFormat(targetAudioFormat.getSampleRate(), targetAudioFormat.getChannels());
        AudioInputStream audioInputStream = new AudioInputStream(new BufferInputStream(), sourceAudioFormat, AudioSystem.NOT_SPECIFIED);
        if (!sourceAudioFormat.matches(targetAudioFormat)) {
            audioInputStream = AudioSystem.getAudioInputStream(targetAudioFormat, audioInputStream);
        }

        this.os = os;
        this.is = audioInputStream;
        this.writeBuffer = new byte[sourceAudioFormat.getFrameSize()];
        this.readBuffer = new byte[targetAudioFormat.getFrameSize()];
    }

    @Override
    public void write(final int b) throws IOException {
        this.os.write(b);
    }

    @Override
    public void close() throws IOException {
        this.os.close();
    }

    public void writeSample(final float sample) throws IOException {
        final int intBits = Float.floatToIntBits(sample);
        this.writeBuffer[this.bufferIndex++] = (byte) ((intBits >> 24) & 0xFF);
        this.writeBuffer[this.bufferIndex++] = (byte) ((intBits >> 16) & 0xFF);
        this.writeBuffer[this.bufferIndex++] = (byte) ((intBits >> 8) & 0xFF);
        this.writeBuffer[this.bufferIndex++] = (byte) (intBits & 0xFF);
        if (this.bufferIndex >= this.writeBuffer.length) {
            this.bufferIndex = 0;
            final int actualReadBytes = this.is.readNBytes(this.readBuffer, 0, this.readBuffer.length);
            if (actualReadBytes != this.readBuffer.length) {
                throw new IOException("Failed to read from input stream, expected " + this.readBuffer.length + " bytes but got " + actualReadBytes);
            }
            this.bufferIndex = 0;
            this.os.write(this.readBuffer);
        }
    }

    public AudioFormat getFormat() {
        return this.is.getFormat();
    }

    private class BufferInputStream extends InputStream {

        @Override
        public int read() {
            if (SampleOutputStream.this.bufferIndex < SampleOutputStream.this.writeBuffer.length) {
                return SampleOutputStream.this.writeBuffer[SampleOutputStream.this.bufferIndex++] & 0xFF;
            } else {
                return -1;
            }
        }

    }

}
