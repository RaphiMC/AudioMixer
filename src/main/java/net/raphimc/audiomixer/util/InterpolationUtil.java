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

public class InterpolationUtil {

    public static float interpolateLinear(final float[] arr, final double index) {
        return interpolateLinear(arr, index, 0, 1);
    }

    public static float interpolateLinear(final float[] arr, final double index, final int offset, final int channels) {
        final int floorIndex = (int) index * channels + offset;
        final int ceilIndex = floorIndex + channels;

        if (ceilIndex >= arr.length) {
            return arr[floorIndex];
        }

        final double fraction = index * channels + offset - floorIndex;
        final float floorValue = arr[floorIndex];
        final float ceilValue = arr[ceilIndex];
        return floorValue + (float) ((ceilValue - floorValue) * fraction);
    }

}
