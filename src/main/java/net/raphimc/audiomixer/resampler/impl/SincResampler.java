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

import java.util.Arrays;

public abstract class SincResampler extends Resampler {

    private final int tapCount;
    private final int phaseCount;
    private final double rolloff;
    private final float[][] coefficientsTable;
    private double cutoff;

    public SincResampler() {
        this(64);
    }

    public SincResampler(final int tapCount) {
        this(tapCount, 128);
    }

    public SincResampler(final int tapCount, final int phaseCount) {
        this(tapCount, phaseCount, 0.95D);
    }

    public SincResampler(final int tapCount, final int phaseCount, final double rolloff) {
        super(tapCount / 2 - 1, tapCount / 2);
        if (tapCount % 2 != 0 || tapCount < 2) {
            throw new IllegalArgumentException("Tap count must be even and >= 2");
        }
        if (phaseCount <= 0) {
            throw new IllegalArgumentException("Phase count must be > 0");
        }
        if (!Double.isFinite(rolloff) || rolloff <= 0D || rolloff > 1D) {
            throw new IllegalArgumentException("Rolloff must be finite and in (0, 1]");
        }
        this.tapCount = tapCount;
        this.phaseCount = phaseCount;
        this.rolloff = rolloff;
        this.coefficientsTable = new float[this.phaseCount + 1][];
    }

    @Override
    protected void resampleMonoToMono(final float[] src, final float[] dst, final int outputFrameCount, final double srcStep, final double baseSrcFramePosition) {
        this.updateCutoff(srcStep);
        for (int dstFrameIndex = 0; dstFrameIndex < outputFrameCount; dstFrameIndex++) {
            dst[dstFrameIndex] = this.sample(src, MathUtil.multiplyAndAdd(dstFrameIndex, srcStep, baseSrcFramePosition), 1, 0);
        }
    }

    @Override
    protected void resampleStereoToStereo(final float[] src, final float[] dst, final int outputFrameCount, final double srcStep, final double baseSrcFramePosition) {
        this.updateCutoff(srcStep);
        for (int dstFrameIndex = 0; dstFrameIndex < outputFrameCount; dstFrameIndex++) {
            final double srcFramePosition = MathUtil.multiplyAndAdd(dstFrameIndex, srcStep, baseSrcFramePosition);
            dst[dstFrameIndex * 2] = this.sample(src, srcFramePosition, 2, 0);
            dst[dstFrameIndex * 2 + 1] = this.sample(src, srcFramePosition, 2, 1);
        }
    }

    @Override
    protected void resampleMonoToStereo(final float[] src, final float[] dst, final int outputFrameCount, final double srcStep, final double baseSrcFramePosition) {
        this.updateCutoff(srcStep);
        for (int dstFrameIndex = 0; dstFrameIndex < outputFrameCount; dstFrameIndex++) {
            final float sample = this.sample(src, MathUtil.multiplyAndAdd(dstFrameIndex, srcStep, baseSrcFramePosition), 1, 0);
            dst[dstFrameIndex * 2] = sample;
            dst[dstFrameIndex * 2 + 1] = sample;
        }
    }

    @Override
    protected void resampleStereoToMono(final float[] src, final float[] dst, final int outputFrameCount, final double srcStep, final double baseSrcFramePosition) {
        this.updateCutoff(srcStep);
        for (int dstFrameIndex = 0; dstFrameIndex < outputFrameCount; dstFrameIndex++) {
            final int srcFrameCount = src.length / 2;
            final double srcFramePosition = MathUtil.multiplyAndAdd(dstFrameIndex, srcStep, baseSrcFramePosition);
            final int srcFrameIndex = (int) srcFramePosition;
            final int firstFrameIndex = srcFrameIndex - this.getLookBehindFrameCount();
            final double phasePosition = (srcFramePosition - srcFrameIndex) * this.phaseCount;
            final int phaseIndex = (int) phasePosition;
            final float[] lowerPhaseCoefficients = this.getPhaseCoefficients(phaseIndex);
            final float[] upperPhaseCoefficients = this.getPhaseCoefficients(phaseIndex + 1);
            float sample = 0F;
            for (int tapIndex = 0; tapIndex < lowerPhaseCoefficients.length; tapIndex++) {
                final int tapSrcFrameIndex = firstFrameIndex + tapIndex;
                if (tapSrcFrameIndex >= 0 && tapSrcFrameIndex < srcFrameCount) {
                    final float coefficient = MathUtil.lerp(lowerPhaseCoefficients[tapIndex], upperPhaseCoefficients[tapIndex], (float) (phasePosition - phaseIndex));
                    sample = MathUtil.multiplyAndAdd((src[tapSrcFrameIndex * 2] + src[tapSrcFrameIndex * 2 + 1]) / 2F, coefficient, sample);
                }
            }
            dst[dstFrameIndex] = sample;
        }
    }

    private void updateCutoff(final double srcStep) {
        final double cutoff = this.rolloff / Math.max(1D, srcStep);
        if (this.cutoff != cutoff) {
            this.cutoff = cutoff;
            Arrays.fill(this.coefficientsTable, null);
        }
    }

    private float[] getPhaseCoefficients(final int phaseIndex) {
        if (this.coefficientsTable[phaseIndex] == null) {
            final double phase = (double) phaseIndex / this.phaseCount;
            final float[] phaseCoefficients = new float[this.tapCount];
            float coefficientsSum = 0F;
            for (int tapIndex = 0; tapIndex < phaseCoefficients.length; tapIndex++) {
                final double distance = tapIndex - this.getLookBehindFrameCount() - phase;
                final float coefficient = (float) (this.cutoff * sinc(this.cutoff * distance) * this.evaluateWindow(distance / this.getLookAheadFrameCount()));
                phaseCoefficients[tapIndex] = coefficient;
                coefficientsSum += coefficient;
            }
            for (int tapIndex = 0; tapIndex < phaseCoefficients.length; tapIndex++) {
                phaseCoefficients[tapIndex] /= coefficientsSum;
            }
            this.coefficientsTable[phaseIndex] = phaseCoefficients;
        }
        return this.coefficientsTable[phaseIndex];
    }

    protected abstract double evaluateWindow(final double normalizedDistance);

    private float sample(final float[] samples, final double framePosition, final int channelCount, final int channelIndex) {
        final int frameIndex = (int) framePosition;
        final double phasePosition = (framePosition - frameIndex) * this.phaseCount;
        final int phaseIndex = (int) phasePosition;
        return convolve(
            samples, frameIndex - this.getLookBehindFrameCount(),
            channelCount, channelIndex,
            this.getPhaseCoefficients(phaseIndex), this.getPhaseCoefficients(phaseIndex + 1),
            (float) (phasePosition - phaseIndex)
        );
    }

    private static float convolve(final float[] samples, final int firstFrameIndex, final int channelCount, final int channelIndex, final float[] lowerPhaseCoefficients, final float[] upperPhaseCoefficients, final float phaseFraction) {
        float sample = 0F;
        final int frameCount = samples.length / channelCount;
        for (int tapIndex = 0; tapIndex < lowerPhaseCoefficients.length; tapIndex++) {
            final int tapFrameIndex = firstFrameIndex + tapIndex;
            if (tapFrameIndex >= 0 && tapFrameIndex < frameCount) {
                final float coefficient = MathUtil.lerp(lowerPhaseCoefficients[tapIndex], upperPhaseCoefficients[tapIndex], phaseFraction);
                sample = MathUtil.multiplyAndAdd(samples[tapFrameIndex * channelCount + channelIndex], coefficient, sample);
            }
        }
        return sample;
    }

    private static double sinc(final double x) {
        if (x == 0D) {
            return 1D;
        }
        final double pix = Math.PI * x;
        return MathUtil.sin(pix) / pix;
    }

}
