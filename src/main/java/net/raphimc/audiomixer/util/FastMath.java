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

public class FastMath {

    private static final double ONE_OVER_PI = 1D / Math.PI;
    private static final double k1 = Double.longBitsToDouble(-4628199217061079959L);
    private static final double k2 = Double.longBitsToDouble(4575957461383549981L);
    private static final double k3 = Double.longBitsToDouble(-4671919876307284301L);
    private static final double k4 = Double.longBitsToDouble(4523617213632129738L);
    private static final double k5 = Double.longBitsToDouble(-4730215344060517252L);
    private static final double k6 = Double.longBitsToDouble(4460268259291226124L);
    private static final double k7 = Double.longBitsToDouble(-4798040743777455072L);

    /**
     * Copyright <a href="https://github.com/JOML-CI/JOML/blob/c8f2ec39d9f138f9708bc7ac27a23e9603f14751/src/main/java/org/joml/Math.java#L176">JOML</a>.<br>
     * Licensed under the <a href="https://github.com/JOML-CI/JOML/blob/c8f2ec39d9f138f9708bc7ac27a23e9603f14751/LICENSE">MIT</a> license.
     */
    public static double sin_roquen_newk(final double v) {
        double i = Math.rint(v * ONE_OVER_PI);
        double x = v - i * Math.PI;
        double qs = 1 - 2 * ((int) i & 1);
        double x2 = x * x;
        double r;
        x = qs * x;
        r = k7;
        r = r * x2 + k6;
        r = r * x2 + k5;
        r = r * x2 + k4;
        r = r * x2 + k3;
        r = r * x2 + k2;
        r = r * x2 + k1;
        return x + x * x2 * r;
    }

}
