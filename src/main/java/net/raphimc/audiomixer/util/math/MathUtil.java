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

import com.sun.management.HotSpotDiagnosticMXBean;

import java.lang.management.ManagementFactory;

public final class MathUtil {

    public static final int MEDIUM_BYTES = 3;
    public static final int MEDIUM_MIN_VALUE = -8388608;
    public static final int MEDIUM_MAX_VALUE = 8388607;

    public static final double HALF_PI = Math.PI / 2D;
    public static final double TWO_PI = Math.PI * 2D;
    public static final double INV_SQRT_2 = 1D / Math.sqrt(2D);
    public static final float BUTTERWORTH_Q = (float) INV_SQRT_2;

    private static final boolean SUPPORTS_FMA;

    static {
        boolean supportsFma = false;
        try {
            if (!Boolean.parseBoolean(System.getProperty("audioMixer.disableFma", "false"))) {
                supportsFma = Boolean.parseBoolean(ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class).getVMOption("UseFMA").getValue());
            }
        } catch (final Throwable ignored) {
        }
        SUPPORTS_FMA = supportsFma;
    }

    private MathUtil() {
    }

    public static int floor(final float value) {
        return Math.toIntExact((long) Math.floor(value));
    }

    public static int floor(final double value) {
        return Math.toIntExact((long) Math.floor(value));
    }

    public static int ceil(final float value) {
        return Math.toIntExact((long) Math.ceil(value));
    }

    public static int ceil(final double value) {
        return Math.toIntExact((long) Math.ceil(value));
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

    public static float lerp(final float a, final float b, final float t) {
        return multiplyAndAdd(b - a, t, a);
    }

    public static float multiplyAndAdd(final float a, final float b, final float c) {
        if (SUPPORTS_FMA) {
            return Math.fma(a, b, c);
        } else {
            return a * b + c;
        }
    }

    public static double multiplyAndAdd(final double a, final double b, final double c) {
        if (SUPPORTS_FMA) {
            return Math.fma(a, b, c);
        } else {
            return a * b + c;
        }
    }

    public static int roundDownToMultiple(final int value, final int multiple) {
        return (value / multiple) * multiple;
    }

    public static int roundUpToMultiple(final int value, final int multiple) {
        return Math.multiplyExact(ceil((double) value / multiple), multiple);
    }

    public static float dbToGain(final float db) {
        return (float) Math.pow(10D, db / 20D);
    }

    public static float gainToDb(final float gain) {
        return (float) (20D * Math.log10(gain));
    }

}
