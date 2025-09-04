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
import net.raphimc.audiomixer.util.MathUtil;

public class SincInterpolator implements Interpolator {

    public static final SincInterpolator INSTANCE = new SincInterpolator(4);

    private final int radius;

    public SincInterpolator(final int radius) {
        this.radius = radius;
    }

    @Override
    public float interpolate(final float[] arr, final double position, final int offset, final int channels) {
        final int centerPosition = (int) position;
        final int firstSamplePosition = centerPosition - this.radius + 1;
        final int lastSamplePosition = centerPosition + this.radius;
        if (firstSamplePosition < 0 || (lastSamplePosition) * channels + offset >= arr.length) {
            return CubicInterpolator.INSTANCE.interpolate(arr, position, offset, channels);
        }

        float sum = 0F;
        float weightSum = 0F;
        for (int tap = firstSamplePosition; tap <= lastSamplePosition; tap++) {
            final float distance = (float) (position - tap);
            final float w = sinc(distance) * lanczosWindow(distance / this.radius);
            sum += arr[tap * channels + offset] * w;
            weightSum += w;
        }
        return sum / weightSum;
    }

    private static float lanczosWindow(final float x) {
        if (x <= -1F || x >= 1F) {
            return 0F;
        }
        return sinc(x);
    }

    private static float sinc(final float x) {
        if (x == 0F) {
            return 1F;
        }
        final double pix = Math.PI * x;
        return (float) (MathUtil.sin_roquen_newk(pix) / pix);
    }

}
