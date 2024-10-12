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
package net.raphimc.audiomixer.util;

import java.util.Arrays;

public class GrowableArray {

    private int[] array;
    private int size;

    public GrowableArray(final int initialSize) {
        this.array = new int[initialSize];
    }

    public void add(final int value) {
        this.ensureSize(this.size + 1);
        this.array[this.size++] = value;
    }

    public void add(final int[] value) {
        this.ensureSize(this.size + value.length);
        System.arraycopy(value, 0, this.array, this.size, value.length);
        this.size += value.length;
    }

    public void set(final int index, final int[] values) {
        this.ensureSize(index + values.length);
        System.arraycopy(values, 0, this.array, index, values.length);
        this.size = Math.max(this.size, index + values.length);
    }

    public int[] getArrayDirect() {
        return this.array;
    }

    public int[] getArray() {
        return Arrays.copyOf(this.array, this.size);
    }

    private void ensureSize(final int size) {
        if (size <= this.array.length) {
            return;
        }

        int newSize = this.array.length << 1;
        if (newSize - size < 0) {
            newSize = size;
        }
        this.array = Arrays.copyOf(this.array, newSize);
    }

}
