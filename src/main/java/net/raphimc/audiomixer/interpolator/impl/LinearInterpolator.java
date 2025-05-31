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
package net.raphimc.audiomixer.interpolator.impl;

import net.raphimc.audiomixer.interpolator.Interpolator;

public class LinearInterpolator implements Interpolator {

    public static final LinearInterpolator INSTANCE = new LinearInterpolator();

    @Override
    public float interpolate(final float[] arr, final double position, final int offset, final int channels) {
        final int floorPosition = (int) position;
        if ((floorPosition + 1) * channels + offset >= arr.length) {
            return arr[(int) position * channels + offset]; // PointInterpolator#interpolate
        }

        final float floorValue = arr[floorPosition * channels + offset];
        final float ceilValue = arr[(floorPosition + 1) * channels + offset];
        return floorValue + (ceilValue - floorValue) * (float) (position - floorPosition);
    }

}
