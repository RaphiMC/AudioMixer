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
package net.raphimc.audiomixer.dsp.resampler.impl;

import net.raphimc.audiomixer.dsp.resampler.Resampler;

public class PointResampler implements Resampler {

    public static final PointResampler INSTANCE = new PointResampler();

    private PointResampler() {
    }

    @Override
    public double resampleMonoToMono(final float[] src, final float[] dst, final float pitch, double srcPosition) {
        for (int dstPosition = 0; dstPosition < dst.length && srcPosition < src.length; dstPosition++) {
            final int index = (int) srcPosition;
            dst[dstPosition] = src[index];
            srcPosition += pitch;
        }
        return srcPosition;
    }

    @Override
    public double resampleStereoToStereo(final float[] src, final float[] dst, final float pitch, double srcPosition) {
        final int srcFrameLength = src.length / 2;
        for (int dstPosition = 0; dstPosition < dst.length && srcPosition < srcFrameLength; dstPosition += 2) {
            final int index = (int) srcPosition * 2;
            dst[dstPosition] = src[index];
            dst[dstPosition + 1] = src[index + 1];
            srcPosition += pitch;
        }
        return srcPosition;
    }

    @Override
    public double resampleMonoToStereo(final float[] src, final float[] dst, final float pitch, double srcPosition) {
        for (int dstPosition = 0; dstPosition < dst.length && srcPosition < src.length; dstPosition += 2) {
            final int index = (int) srcPosition;
            dst[dstPosition] = dst[dstPosition + 1] = src[index];
            srcPosition += pitch;
        }
        return srcPosition;
    }

    @Override
    public double resampleStereoToMono(final float[] src, final float[] dst, final float pitch, double srcPosition) {
        final int srcFrameLength = src.length / 2;
        for (int dstPosition = 0; dstPosition < dst.length && srcPosition < srcFrameLength; dstPosition++) {
            final int index = (int) srcPosition * 2;
            dst[dstPosition] = (src[index] + src[index + 1]) / 2F;
            srcPosition += pitch;
        }
        return srcPosition;
    }

}
