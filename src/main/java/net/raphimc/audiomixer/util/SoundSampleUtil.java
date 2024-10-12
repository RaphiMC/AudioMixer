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

public class SoundSampleUtil {

    public static void normalize(final int[] samples, final int maxValue) {
        normalize(samples, maxValue, getMax(samples));
    }

    public static void normalize(final int[] samples, final int maxValue, final int max) {
        final float factor = Math.min(1F, (float) maxValue / max);
        for (int i = 0; i < samples.length; i++) {
            samples[i] = (int) (samples[i] * factor);
        }
    }

    public static int getMax(final int[] samples) {
        int max = 1;
        for (int sample : samples) max = Math.max(max, Math.abs(sample));
        return max;
    }

    public static int[] trimZeroesAtEnd(final int[] samples) {
        int i = samples.length - 1;
        while (i >= 0 && samples[i] == 0) i--;
        return i == samples.length - 1 ? samples : i < 0 ? new int[0] : Arrays.copyOf(samples, i + 1);
    }

}
