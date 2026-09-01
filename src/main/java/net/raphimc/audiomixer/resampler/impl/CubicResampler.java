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
package net.raphimc.audiomixer.resampler.impl;

import net.raphimc.audiomixer.resampler.Resampler;
import net.raphimc.audiomixer.util.math.MathUtil;

// Catmull-Rom cubic resampler
public final class CubicResampler extends Resampler {

    public CubicResampler() {
        super(1, 2);
    }

    @Override
    protected void resampleMonoToMono(final float[] src, final float[] dst, final int outputFrameCount, final double srcStep, final double baseSrcFramePosition) {
        for (int dstFrameIndex = 0; dstFrameIndex < outputFrameCount; dstFrameIndex++) {
            final double srcFramePosition = MathUtil.multiplyAndAdd(dstFrameIndex, srcStep, baseSrcFramePosition);
            final float fraction = (float) (srcFramePosition - (int) srcFramePosition);
            final int srcIndex = (int) srcFramePosition;
            dst[dstFrameIndex] = interpolate(
                sampleOrZero(src, srcIndex - 1),
                src[srcIndex],
                sampleOrZero(src, srcIndex + 1),
                sampleOrZero(src, srcIndex + 2),
                fraction
            );
        }
    }

    @Override
    protected void resampleStereoToStereo(final float[] src, final float[] dst, final int outputFrameCount, final double srcStep, final double baseSrcFramePosition) {
        for (int dstFrameIndex = 0; dstFrameIndex < outputFrameCount; dstFrameIndex++) {
            final double srcFramePosition = MathUtil.multiplyAndAdd(dstFrameIndex, srcStep, baseSrcFramePosition);
            final float fraction = (float) (srcFramePosition - (int) srcFramePosition);
            final int srcIndex = (int) srcFramePosition * 2;
            dst[dstFrameIndex * 2] = interpolate(
                sampleOrZero(src, srcIndex - 2),
                src[srcIndex],
                sampleOrZero(src, srcIndex + 2),
                sampleOrZero(src, srcIndex + 4),
                fraction
            );
            dst[dstFrameIndex * 2 + 1] = interpolate(
                sampleOrZero(src, srcIndex - 1),
                src[srcIndex + 1],
                sampleOrZero(src, srcIndex + 3),
                sampleOrZero(src, srcIndex + 5),
                fraction
            );
        }
    }

    @Override
    protected void resampleMonoToStereo(final float[] src, final float[] dst, final int outputFrameCount, final double srcStep, final double baseSrcFramePosition) {
        for (int dstFrameIndex = 0; dstFrameIndex < outputFrameCount; dstFrameIndex++) {
            final double srcFramePosition = MathUtil.multiplyAndAdd(dstFrameIndex, srcStep, baseSrcFramePosition);
            final float fraction = (float) (srcFramePosition - (int) srcFramePosition);
            final int srcIndex = (int) srcFramePosition;
            final float sample = interpolate(
                sampleOrZero(src, srcIndex - 1),
                src[srcIndex],
                sampleOrZero(src, srcIndex + 1),
                sampleOrZero(src, srcIndex + 2),
                fraction
            );
            dst[dstFrameIndex * 2] = sample;
            dst[dstFrameIndex * 2 + 1] = sample;
        }
    }

    @Override
    protected void resampleStereoToMono(final float[] src, final float[] dst, final int outputFrameCount, final double srcStep, final double baseSrcFramePosition) {
        for (int dstFrameIndex = 0; dstFrameIndex < outputFrameCount; dstFrameIndex++) {
            final double srcFramePosition = MathUtil.multiplyAndAdd(dstFrameIndex, srcStep, baseSrcFramePosition);
            final float fraction = (float) (srcFramePosition - (int) srcFramePosition);
            final int srcIndex = (int) srcFramePosition * 2;
            dst[dstFrameIndex] = interpolate(
                (sampleOrZero(src, srcIndex - 2) + sampleOrZero(src, srcIndex - 1)) / 2F,
                (src[srcIndex] + src[srcIndex + 1]) / 2F,
                (sampleOrZero(src, srcIndex + 2) + sampleOrZero(src, srcIndex + 3)) / 2F,
                (sampleOrZero(src, srcIndex + 4) + sampleOrZero(src, srcIndex + 5)) / 2F,
                fraction
            );
        }
    }

    private static float interpolate(final float s0, final float s1, final float s2, final float s3, final float t) {
        final float c1 = s2 - s0;
        final float c2 = MathUtil.multiplyAndAdd(4F, s2, MathUtil.multiplyAndAdd(-5F, s1, 2F * s0)) - s3;
        final float c3 = MathUtil.multiplyAndAdd(-3F, s2, MathUtil.multiplyAndAdd(3F, s1, -s0)) + s3;
        return MathUtil.multiplyAndAdd(0.5F * t, MathUtil.multiplyAndAdd(t, MathUtil.multiplyAndAdd(t, c3, c2), c1), s1);
    }

    private static float sampleOrZero(final float[] src, final int index) {
        return index >= 0 && index < src.length ? src[index] : 0F;
    }

}
