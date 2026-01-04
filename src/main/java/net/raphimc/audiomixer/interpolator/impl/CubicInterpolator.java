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
package net.raphimc.audiomixer.interpolator.impl;

import net.raphimc.audiomixer.interpolator.Interpolator;

// Catmull-Rom cubic interpolation
public class CubicInterpolator implements Interpolator {

    public static final CubicInterpolator INSTANCE = new CubicInterpolator();

    @Override
    public float interpolate(final float[] arr, final double position, final int offset, final int channels) {
        final int centerPosition = (int) position;
        if (centerPosition < 1 || (centerPosition + 2) * channels + offset >= arr.length) {
            return LinearInterpolator.INSTANCE.interpolate(arr, position, offset, channels);
        }

        final float x0 = arr[(centerPosition - 1) * channels + offset];
        final float x1 = arr[centerPosition * channels + offset];
        final float x2 = arr[(centerPosition + 1) * channels + offset];
        final float x3 = arr[(centerPosition + 2) * channels + offset];

        final float a0 = -0.5F * x0 + 1.5F * x1 - 1.5F * x2 + 0.5F * x3;
        final float a1 = x0 - 2.5F * x1 + 2.0F * x2 - 0.5F * x3;
        final float a2 = -0.5F * x0 + 0.5F * x2;
        final float a3 = x1;

        final float t = (float) (position - centerPosition);
        return ((a0 * t + a1) * t + a2) * t + a3;
    }

}
