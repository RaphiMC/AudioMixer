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
package net.raphimc.audiomixer.util;

import java.util.Arrays;

public class GrowableArray {

    private float[] array;
    private int size;

    public GrowableArray(final int initialSize) {
        this.array = new float[initialSize];
    }

    public void add(final float value) {
        this.ensureHasEnoughSpace(1);
        this.array[this.size++] = value;
    }

    public void add(final float[] value) {
        this.ensureHasEnoughSpace(value.length);
        System.arraycopy(value, 0, this.array, this.size, value.length);
        this.size += value.length;
    }

    public float[] getArrayDirect() {
        return this.array;
    }

    public float[] getArray() {
        return Arrays.copyOf(this.array, this.size);
    }

    private void ensureHasEnoughSpace(final int bytes) {
        if (this.size + bytes <= this.array.length) {
            return;
        }

        final int newSize = this.array.length + Math.max(bytes, this.array.length);
        this.array = Arrays.copyOf(this.array, newSize);
    }

}
