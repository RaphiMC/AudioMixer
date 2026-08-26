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
package net.raphimc.audiomixer.util.math;

public final class MathUtil {

    public static final int MEDIUM_BYTES = 3;
    public static final int MEDIUM_MIN_VALUE = -8388608;
    public static final int MEDIUM_MAX_VALUE = 8388607;

    public static final double HALF_PI = Math.PI / 2D;
    public static final double TWO_PI = Math.PI * 2D;
    public static final double INV_SQRT_2 = 1D / Math.sqrt(2D);
    public static final float BUTTERWORTH_Q = (float) INV_SQRT_2;

    private MathUtil() {
    }

    public static int clamp(final int value, final int min, final int max) {
        return Math.max(min, Math.min(value, max));
    }

    public static long clamp(final long value, final long min, final long max) {
        return Math.max(min, Math.min(value, max));
    }

    public static float clamp(final float value, final float min, final float max) {
        return Math.max(min, Math.min(value, max));
    }

    public static float map(final float value, final float inMin, final float inMax, final float outMin, final float outMax) {
        return (value - inMin) / (inMax - inMin) * (outMax - outMin) + outMin;
    }

    public static int roundDownToMultiple(final int value, final int multiple) {
        return (value / multiple) * multiple;
    }

    public static int roundUpToMultiple(final int value, final int multiple) {
        return (int) (Math.ceil((double) value / multiple) * multiple);
    }

    public static float dbToGain(final float db) {
        return (float) Math.pow(10D, db / 20D);
    }

    public static float gainToDb(final float gain) {
        return (float) (20D * Math.log10(gain));
    }

}
