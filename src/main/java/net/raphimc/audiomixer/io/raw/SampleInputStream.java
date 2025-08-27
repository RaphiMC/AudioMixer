/*
 * This file is part of AudioMixer - https://github.com/RaphiMC/AudioMixer
 * Copyright (C) 2024-2025 RK_01/RaphiMC and contributors
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
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SampleInputStream extends InputStream {

    private final InputStream is;
    private final byte[] buffer;
    private int bufferIndex;
    private int bufferLength;

    public SampleInputStream(final InputStream is, final PcmFloatAudioFormat targetAudioFormat) throws UnsupportedAudioFileException, IOException {
        this(new BufferedInputStream(is), targetAudioFormat);
    }

    public SampleInputStream(final BufferedInputStream is, final PcmFloatAudioFormat targetAudioFormat) throws UnsupportedAudioFileException, IOException {
        this(AudioSystem.getAudioInputStream(is), targetAudioFormat);
    }

    public SampleInputStream(AudioInputStream is, final PcmFloatAudioFormat targetAudioFormat) {
        final AudioFormat sourceAudioFormat = is.getFormat();
        if (!sourceAudioFormat.matches(targetAudioFormat)) {
            is = AudioSystem.getAudioInputStream(targetAudioFormat, is);
        }

        this.is = is;
        this.buffer = new byte[targetAudioFormat.getFrameSize()];
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

    public float readSample() throws IOException {
        final int b1 = this.read();
        final int b2 = this.read();
        final int b3 = this.read();
        final int b4 = this.read();
        if (b1 == -1 || b2 == -1 || b3 == -1 || b4 == -1) {
            return Float.NaN;
        } else {
            return Float.intBitsToFloat((b1 << 24) | (b2 << 16) | (b3 << 8) | b4);
        }
    }

}
