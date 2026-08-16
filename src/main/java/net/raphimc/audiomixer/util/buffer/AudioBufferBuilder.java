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
package net.raphimc.audiomixer.util.buffer;

import net.raphimc.audiomixer.util.AudioFormat;

import java.util.Arrays;

public class AudioBufferBuilder {

    private final AudioFormat format;
    private float[] array;
    private int size;

    public AudioBufferBuilder(final AudioFormat format) {
        this(format, 128 * 1024);
    }

    public AudioBufferBuilder(final AudioFormat format, final int initialCapacity) {
        this.format = format;
        this.array = new float[initialCapacity];
    }

    public void append(final float value) {
        this.ensureHasEnoughSpace(1);
        this.array[this.size++] = value;
    }

    public void append(final float[] samples) {
        this.append(samples, 0, samples.length);
    }

    public void append(final float[] samples, final int offset, final int length) {
        this.ensureHasEnoughSpace(length);
        System.arraycopy(samples, offset, this.array, this.size, length);
        this.size += length;
    }

    public void append(final AudioBuffer buffer) {
        if (!buffer.format().equals(this.format)) {
            throw new IllegalArgumentException("Format mismatch: " + buffer.format() + " != " + this.format);
        }
        this.append(buffer.samples());
    }

    public AudioBuffer build() {
        return new AudioBuffer(this.format, Arrays.copyOf(this.array, this.size));
    }

    public int size() {
        return this.size;
    }

    private void ensureHasEnoughSpace(final int length) {
        if (this.size + length > this.array.length) {
            this.array = Arrays.copyOf(this.array, this.size + Math.max(length, this.size));
        }
    }

}
