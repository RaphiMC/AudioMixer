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
    public static final double INV_PI = 1D / Math.PI;
    public static final double INV_SQRT_2 = 1D / Math.sqrt(2D);
    public static final float BUTTERWORTH_Q = (float) INV_SQRT_2;

    private static final double SIN_C1 = Double.longBitsToDouble(-4628199217061079959L);
    private static final double SIN_C2 = Double.longBitsToDouble(4575957461383549981L);
    private static final double SIN_C3 = Double.longBitsToDouble(-4671919876307284301L);
    private static final double SIN_C4 = Double.longBitsToDouble(4523617213632129738L);
    private static final double SIN_C5 = Double.longBitsToDouble(-4730215344060517252L);
    private static final double SIN_C6 = Double.longBitsToDouble(4460268259291226124L);
    private static final double SIN_C7 = Double.longBitsToDouble(-4798040743777455072L);
    private static final boolean USE_FMA;
    private static final boolean USE_FAST_SIN;

    static {
        boolean supportsFma = false;
        if (!Boolean.parseBoolean(System.getProperty("audioMixer.disableFma", "false"))) {
            try {
                supportsFma = Boolean.parseBoolean(ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class).getVMOption("UseFMA").getValue());
            } catch (final Throwable ignored) {
            }
        }
        USE_FMA = supportsFma;
        USE_FAST_SIN = !Boolean.parseBoolean(System.getProperty("audioMixer.disableFastSin", "false"));
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

    public static double lerp(final double a, final double b, final double t) {
        return multiplyAndAdd(b - a, t, a);
    }

    public static int roundDownToMultiple(final int value, final int multiple) {
        return Math.multiplyExact(Math.floorDiv(value, multiple), multiple);
    }

    public static int roundUpToMultiple(final int value, final int multiple) {
        if (Math.floorMod(value, multiple) != 0) {
            return Math.multiplyExact(Math.floorDiv(value, multiple) + 1, multiple);
        } else {
            return value;
        }
    }

    public static float dbToGain(final float db) {
        return (float) Math.pow(10D, db / 20D);
    }

    public static float gainToDb(final float gain) {
        return (float) (20D * Math.log10(gain));
    }

    public static double sin(final double v) {
        if (USE_FAST_SIN) {
            return fastSin(v);
        } else {
            return Math.sin(v);
        }
    }

    public static double cos(final double v) {
        if (USE_FAST_SIN) {
            return fastCos(v);
        } else {
            return Math.cos(v);
        }
    }

    public static float multiplyAndAdd(final float a, final float b, final float c) {
        if (USE_FMA) {
            return Math.fma(a, b, c);
        } else {
            return a * b + c;
        }
    }

    public static double multiplyAndAdd(final double a, final double b, final double c) {
        if (USE_FMA) {
            return Math.fma(a, b, c);
        } else {
            return a * b + c;
        }
    }

    /**
     * Copyright <a href="https://github.com/JOML-CI/JOML/blob/c8f2ec39d9f138f9708bc7ac27a23e9603f14751/src/main/java/org/joml/Math.java#L176">JOML</a>.<br>
     * Licensed under the <a href="https://github.com/JOML-CI/JOML/blob/c8f2ec39d9f138f9708bc7ac27a23e9603f14751/LICENSE">MIT</a> license.
     */
    public static double fastSin(final double v) {
        final double i = Math.rint(v * INV_PI);
        final double x0 = multiplyAndAdd(-i, Math.PI, v);
        final double sign = 1D - 2D * ((int) i & 1);
        final double x = sign * x0;
        final double x2 = x * x;
        double r = SIN_C7;
        r = multiplyAndAdd(r, x2, SIN_C6);
        r = multiplyAndAdd(r, x2, SIN_C5);
        r = multiplyAndAdd(r, x2, SIN_C4);
        r = multiplyAndAdd(r, x2, SIN_C3);
        r = multiplyAndAdd(r, x2, SIN_C2);
        r = multiplyAndAdd(r, x2, SIN_C1);
        return multiplyAndAdd(x * x2, r, x);
    }

    public static double fastCos(final double v) {
        return fastSin(v + HALF_PI);
    }

}
