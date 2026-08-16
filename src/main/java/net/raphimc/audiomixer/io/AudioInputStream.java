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
package net.raphimc.audiomixer.io;

import net.raphimc.audiomixer.util.AudioFormat;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AudioInputStream implements Closeable {

    private final AudioFormat format;

    public AudioInputStream(final AudioFormat format) {
        this.format = format;
    }

    public abstract float read() throws IOException;

    public float[] read(final int length) throws IOException {
        final float[] samples = new float[length];
        final int read = this.read(samples);
        if (read < length) {
            return Arrays.copyOf(samples, read);
        } else {
            return samples;
        }
    }

    public int read(final float[] samples) throws IOException {
        return this.read(samples, 0, samples.length);
    }

    public int read(final float[] samples, final int offset, final int length) throws IOException {
        for (int i = 0; i < length; i++) {
            try {
                samples[offset + i] = this.read();
            } catch (final EOFException e) {
                return i;
            }
        }
        return length;
    }

    public float[] readFully() throws IOException {
        final int bufferLength = this.format.millisToSampleCount(1000F);
        final List<float[]> buffers = new ArrayList<>();
        int totalLength = 0;
        while (true) {
            final float[] buffer = this.read(bufferLength);
            buffers.add(buffer);
            totalLength = Math.addExact(totalLength, buffer.length);
            if (buffer.length < bufferLength) {
                break;
            }
        }
        final float[] samples = new float[totalLength];
        int offset = 0;
        for (float[] buffer : buffers) {
            System.arraycopy(buffer, 0, samples, offset, buffer.length);
            offset += buffer.length;
        }
        return samples;
    }

    public AudioFormat getFormat() {
        return this.format;
    }

}
