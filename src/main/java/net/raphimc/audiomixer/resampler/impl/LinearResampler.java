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

public final class LinearResampler extends Resampler {

    public LinearResampler() {
        super(0, 1);
    }

    @Override
    protected void resampleMonoToMono(final float[] src, final float[] dst, final int outputFrameCount, final double srcStep, final double baseSrcFramePosition) {
        for (int dstFrameIndex = 0; dstFrameIndex < outputFrameCount; dstFrameIndex++) {
            final double srcFramePosition = MathUtil.multiplyAndAdd(dstFrameIndex, srcStep, baseSrcFramePosition);
            final float fraction = (float) (srcFramePosition - (int) srcFramePosition);
            final int srcIndex = (int) srcFramePosition;
            dst[dstFrameIndex] = MathUtil.lerp(src[srcIndex], sampleOrZero(src, srcIndex + 1), fraction);
        }
    }

    @Override
    protected void resampleStereoToStereo(final float[] src, final float[] dst, final int outputFrameCount, final double srcStep, final double baseSrcFramePosition) {
        for (int dstFrameIndex = 0; dstFrameIndex < outputFrameCount; dstFrameIndex++) {
            final double srcFramePosition = MathUtil.multiplyAndAdd(dstFrameIndex, srcStep, baseSrcFramePosition);
            final float fraction = (float) (srcFramePosition - (int) srcFramePosition);
            final int srcIndex = (int) srcFramePosition * 2;
            dst[dstFrameIndex * 2] = MathUtil.lerp(src[srcIndex], sampleOrZero(src, srcIndex + 2), fraction);
            dst[dstFrameIndex * 2 + 1] = MathUtil.lerp(src[srcIndex + 1], sampleOrZero(src, srcIndex + 3), fraction);
        }
    }

    @Override
    protected void resampleMonoToStereo(final float[] src, final float[] dst, final int outputFrameCount, final double srcStep, final double baseSrcFramePosition) {
        for (int dstFrameIndex = 0; dstFrameIndex < outputFrameCount; dstFrameIndex++) {
            final double srcFramePosition = MathUtil.multiplyAndAdd(dstFrameIndex, srcStep, baseSrcFramePosition);
            final float fraction = (float) (srcFramePosition - (int) srcFramePosition);
            final int srcIndex = (int) srcFramePosition;
            final float sample = MathUtil.lerp(src[srcIndex], sampleOrZero(src, srcIndex + 1), fraction);
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
            dst[dstFrameIndex] = MathUtil.lerp(
                (src[srcIndex] + src[srcIndex + 1]) / 2F,
                (sampleOrZero(src, srcIndex + 2) + sampleOrZero(src, srcIndex + 3)) / 2F,
                fraction
            );
        }
    }

    private static float sampleOrZero(final float[] src, final int index) {
        return index >= 0 && index < src.length ? src[index] : 0F;
    }

}
