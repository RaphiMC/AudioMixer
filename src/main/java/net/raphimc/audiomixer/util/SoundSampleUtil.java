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

import java.util.Arrays;

public class SoundSampleUtil {

    public static void normalize(final float[] samples) {
        normalize(samples, getMax(samples));
    }

    public static void normalize(final float[] samples, final float max) {
        if (max <= 1F) return;

        final float factor = 1F / max;
        for (int i = 0; i < samples.length; i++) {
            samples[i] *= factor;
        }
    }

    public static void clip(final float[] samples) {
        for (int i = 0; i < samples.length; i++) {
            if (samples[i] > 1F) {
                samples[i] = 1F;
            } else if (samples[i] < -1F) {
                samples[i] = -1F;
            }
        }
    }

    public static float getMax(final float[] samples) {
        float max = 0F;
        for (float sample : samples) {
            max = Math.max(Math.abs(sample), max);
        }
        return max;
    }

    public static float[] trimZeroesAtStart(final float[] samples) {
        int i = 0;
        while (i < samples.length && samples[i] == 0) i++;
        return i == 0 ? samples : i == samples.length ? new float[0] : Arrays.copyOfRange(samples, i, samples.length);
    }

    public static float[] trimZeroesAtEnd(final float[] samples) {
        int i = samples.length - 1;
        while (i >= 0 && samples[i] == 0) i--;
        return i == samples.length - 1 ? samples : i < 0 ? new float[0] : Arrays.copyOf(samples, i + 1);
    }

}
