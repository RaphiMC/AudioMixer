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

// Catmull-Rom cubic resampler
public class CubicResampler implements Resampler {

    public static final CubicResampler INSTANCE = new CubicResampler();

    private CubicResampler() {
    }

    @Override
    public double resampleMonoToMono(final float[] src, final float[] dst, final float pitch, double srcPosition) {
        final int srcLastIndex = src.length - 1;
        for (int dstPosition = 0; dstPosition < dst.length && srcPosition < src.length; dstPosition++) {
            final float f = (float) (srcPosition - (int) srcPosition);
            final float f2 = f * f;
            final float f3 = f2 * f;
            final int i1 = (int) srcPosition;
            final int i0 = reflectIfNeeded(i1 - 1, srcLastIndex);
            final int i2 = reflectIfNeeded(i1 + 1, srcLastIndex);
            final int i3 = reflectIfNeeded(i1 + 2, srcLastIndex);
            final float s0 = src[i0];
            final float s1 = src[i1];
            final float s2 = src[i2];
            final float s3 = src[i3];
            final float c0 = 2F * s1;
            final float c1 = (-s0 + s2) * f;
            final float c2 = (2F * s0 - 5F * s1 + 4F * s2 - s3) * f2;
            final float c3 = (-s0 + 3F * s1 - 3F * s2 + s3) * f3;
            dst[dstPosition] = 0.5F * (c0 + c1 + c2 + c3);
            srcPosition += pitch;
        }
        return srcPosition;
    }

    @Override
    public double resampleStereoToStereo(final float[] src, final float[] dst, final float pitch, double srcPosition) {
        final int srcFrameLength = src.length / 2;
        final int srcLastIndex = src.length - 2;
        for (int dstPosition = 0; dstPosition < dst.length && srcPosition < srcFrameLength; dstPosition += 2) {
            final float f = (float) (srcPosition - (int) srcPosition);
            final float f2 = f * f;
            final float f3 = f2 * f;
            final int i1 = (int) srcPosition * 2;
            final int i0 = reflectIfNeeded(i1 - 2, srcLastIndex);
            final int i2 = reflectIfNeeded(i1 + 2, srcLastIndex);
            final int i3 = reflectIfNeeded(i1 + 4, srcLastIndex);
            {
                final float s0 = src[i0];
                final float s1 = src[i1];
                final float s2 = src[i2];
                final float s3 = src[i3];
                final float c0 = 2F * s1;
                final float c1 = (-s0 + s2) * f;
                final float c2 = (2F * s0 - 5F * s1 + 4F * s2 - s3) * f2;
                final float c3 = (-s0 + 3F * s1 - 3F * s2 + s3) * f3;
                dst[dstPosition] = 0.5F * (c0 + c1 + c2 + c3);
            }
            {
                final float s0 = src[i0 + 1];
                final float s1 = src[i1 + 1];
                final float s2 = src[i2 + 1];
                final float s3 = src[i3 + 1];
                final float c0 = 2F * s1;
                final float c1 = (-s0 + s2) * f;
                final float c2 = (2F * s0 - 5F * s1 + 4F * s2 - s3) * f2;
                final float c3 = (-s0 + 3F * s1 - 3F * s2 + s3) * f3;
                dst[dstPosition + 1] = 0.5F * (c0 + c1 + c2 + c3);
            }
            srcPosition += pitch;
        }
        return srcPosition;
    }

    @Override
    public double resampleMonoToStereo(final float[] src, final float[] dst, final float pitch, double srcPosition) {
        final int srcLastIndex = src.length - 1;
        for (int dstPosition = 0; dstPosition < dst.length && srcPosition < src.length; dstPosition += 2) {
            final float f = (float) (srcPosition - (int) srcPosition);
            final float f2 = f * f;
            final float f3 = f2 * f;
            final int i1 = (int) srcPosition;
            final int i0 = reflectIfNeeded(i1 - 1, srcLastIndex);
            final int i2 = reflectIfNeeded(i1 + 1, srcLastIndex);
            final int i3 = reflectIfNeeded(i1 + 2, srcLastIndex);
            final float s0 = src[i0];
            final float s1 = src[i1];
            final float s2 = src[i2];
            final float s3 = src[i3];
            final float c0 = 2F * s1;
            final float c1 = (-s0 + s2) * f;
            final float c2 = (2F * s0 - 5F * s1 + 4F * s2 - s3) * f2;
            final float c3 = (-s0 + 3F * s1 - 3F * s2 + s3) * f3;
            dst[dstPosition] = dst[dstPosition + 1] = 0.5F * (c0 + c1 + c2 + c3);
            srcPosition += pitch;
        }
        return srcPosition;
    }

    @Override
    public double resampleStereoToMono(final float[] src, final float[] dst, final float pitch, double srcPosition) {
        final int srcFrameLength = src.length / 2;
        final int srcLastIndex = src.length - 2;
        for (int dstPosition = 0; dstPosition < dst.length && srcPosition < srcFrameLength; dstPosition++) {
            final float f = (float) (srcPosition - (int) srcPosition);
            final float f2 = f * f;
            final float f3 = f2 * f;
            final int i1 = (int) srcPosition * 2;
            final int i0 = reflectIfNeeded(i1 - 2, srcLastIndex);
            final int i2 = reflectIfNeeded(i1 + 2, srcLastIndex);
            final int i3 = reflectIfNeeded(i1 + 4, srcLastIndex);
            final float s0 = (src[i0] + src[i0 + 1]) / 2F;
            final float s1 = (src[i1] + src[i1 + 1]) / 2F;
            final float s2 = (src[i2] + src[i2 + 1]) / 2F;
            final float s3 = (src[i3] + src[i3 + 1]) / 2F;
            final float c0 = 2F * s1;
            final float c1 = (-s0 + s2) * f;
            final float c2 = (2F * s0 - 5F * s1 + 4F * s2 - s3) * f2;
            final float c3 = (-s0 + 3F * s1 - 3F * s2 + s3) * f3;
            dst[dstPosition] = 0.5F * (c0 + c1 + c2 + c3);
            srcPosition += pitch;
        }
        return srcPosition;
    }

    private static int reflectIfNeeded(int index, final int lastIndex) {
        if (lastIndex <= 0) {
            return 0;
        }
        while (index < 0 || index > lastIndex) {
            index = index < 0 ? -index : (lastIndex << 1) - index;
        }
        return index;
    }

}
