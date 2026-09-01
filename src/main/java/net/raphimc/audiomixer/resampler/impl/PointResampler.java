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

public final class PointResampler extends Resampler {

    public PointResampler() {
        super(0, 0);
    }

    @Override
    protected void resampleMonoToMono(final float[] src, final float[] dst, final int outputFrameCount, final double srcStep, final double baseSrcFramePosition) {
        for (int dstFrameIndex = 0; dstFrameIndex < outputFrameCount; dstFrameIndex++) {
            dst[dstFrameIndex] = src[(int) MathUtil.multiplyAndAdd(dstFrameIndex, srcStep, baseSrcFramePosition)];
        }
    }

    @Override
    protected void resampleStereoToStereo(final float[] src, final float[] dst, final int outputFrameCount, final double srcStep, final double baseSrcFramePosition) {
        for (int dstFrameIndex = 0; dstFrameIndex < outputFrameCount; dstFrameIndex++) {
            final int srcIndex = (int) MathUtil.multiplyAndAdd(dstFrameIndex, srcStep, baseSrcFramePosition) * 2;
            dst[dstFrameIndex * 2] = src[srcIndex];
            dst[dstFrameIndex * 2 + 1] = src[srcIndex + 1];
        }
    }

    @Override
    protected void resampleMonoToStereo(final float[] src, final float[] dst, final int outputFrameCount, final double srcStep, final double baseSrcFramePosition) {
        for (int dstFrameIndex = 0; dstFrameIndex < outputFrameCount; dstFrameIndex++) {
            dst[dstFrameIndex * 2] = dst[dstFrameIndex * 2 + 1] = src[(int) MathUtil.multiplyAndAdd(dstFrameIndex, srcStep, baseSrcFramePosition)];
        }
    }

    @Override
    protected void resampleStereoToMono(final float[] src, final float[] dst, final int outputFrameCount, final double srcStep, final double baseSrcFramePosition) {
        for (int dstFrameIndex = 0; dstFrameIndex < outputFrameCount; dstFrameIndex++) {
            final int srcIndex = (int) MathUtil.multiplyAndAdd(dstFrameIndex, srcStep, baseSrcFramePosition) * 2;
            dst[dstFrameIndex] = (src[srcIndex] + src[srcIndex + 1]) / 2F;
        }
    }

}
