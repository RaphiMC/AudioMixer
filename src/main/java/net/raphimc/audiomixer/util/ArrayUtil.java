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
package net.raphimc.audiomixer.util;

import java.util.Arrays;

public class ArrayUtil {

    public static void fillFast(final float[] arr, final int index, final int length, final float value) {
        switch (length) {
            case 0 -> {
            }
            case 1 -> arr[index] = value;
            case 2 -> {
                arr[index] = value;
                arr[index + 1] = value;
            }
            case 3 -> {
                arr[index] = value;
                arr[index + 1] = value;
                arr[index + 2] = value;
            }
            case 4 -> {
                arr[index] = value;
                arr[index + 1] = value;
                arr[index + 2] = value;
                arr[index + 3] = value;
            }
            default -> Arrays.fill(arr, index, index + length, value);
        }
    }

}
