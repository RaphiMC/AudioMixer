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
package net.raphimc.audiomixer.resampler;

import net.raphimc.audiomixer.util.AudioFormat;
import net.raphimc.audiomixer.util.buffer.AudioBuffer;
import net.raphimc.audiomixer.util.math.MathUtil;

public abstract class Resampler {

    private final int lookBehindFrameCount;
    private final int lookAheadFrameCount;
    private int lastOutputFrameCount;

    protected Resampler(final int lookBehindFrameCount, final int lookAheadFrameCount) {
        if (lookBehindFrameCount < 0) {
            throw new IllegalArgumentException("Look behind frame count must be >= 0: " + lookBehindFrameCount);
        }
        if (lookAheadFrameCount < 0) {
            throw new IllegalArgumentException("Look ahead frame count must be >= 0: " + lookAheadFrameCount);
        }
        this.lookBehindFrameCount = lookBehindFrameCount;
        this.lookAheadFrameCount = lookAheadFrameCount;
    }

    public AudioBuffer resample(final AudioBuffer src, final AudioFormat dstFormat) {
        if (!src.format().equals(dstFormat)) {
            final double srcStep = (double) src.format().sampleRate() / (double) dstFormat.sampleRate();
            final AudioBuffer dst = new AudioBuffer(dstFormat, computeOutputFrameCount(src.frameCount(), Integer.MAX_VALUE, srcStep, 0));
            this.resample(src, dst, 0);
            return dst;
        } else {
            this.lastOutputFrameCount = src.frameCount();
            return src;
        }
    }

    public double resample(final AudioBuffer src, final AudioBuffer dst, final double srcFramePosition) {
        return this.resample(src.samples(), src.format(), dst.samples(), dst.format(), srcFramePosition);
    }

    public double resample(final float[] src, final AudioFormat srcFormat, final float[] dst, final AudioFormat dstFormat, final double srcFramePosition) {
        final int srcFrameCount = srcFormat.sampleCountToFrameCount(src.length);
        final int dstFrameCount = dstFormat.sampleCountToFrameCount(dst.length);
        if (srcFormat.sampleRate() != dstFormat.sampleRate() || srcFramePosition % 1 != 0) {
            final double srcStep = (double) srcFormat.sampleRate() / (double) dstFormat.sampleRate();
            this.lastOutputFrameCount = computeOutputFrameCount(srcFrameCount, dstFrameCount, srcStep, srcFramePosition);
            if (srcFormat.channelCount() == 1 && dstFormat.channelCount() == 1) {
                this.resampleMonoToMono(src, dst, this.lastOutputFrameCount, srcStep, srcFramePosition);
            } else if (srcFormat.channelCount() == 2 && dstFormat.channelCount() == 2) {
                this.resampleStereoToStereo(src, dst, this.lastOutputFrameCount, srcStep, srcFramePosition);
            } else if (srcFormat.channelCount() == 1 && dstFormat.channelCount() == 2) {
                this.resampleMonoToStereo(src, dst, this.lastOutputFrameCount, srcStep, srcFramePosition);
            } else if (srcFormat.channelCount() == 2 && dstFormat.channelCount() == 1) {
                this.resampleStereoToMono(src, dst, this.lastOutputFrameCount, srcStep, srcFramePosition);
            } else {
                throw new IllegalArgumentException("Unsupported channel conversion: " + srcFormat.channelCount() + " -> " + dstFormat.channelCount());
            }
            return MathUtil.multiplyAndAdd(this.lastOutputFrameCount, srcStep, srcFramePosition);
        } else {
            this.lastOutputFrameCount = computeOutputFrameCount(srcFrameCount, dstFrameCount, 1D, srcFramePosition);
            if (srcFormat.channelCount() == dstFormat.channelCount()) {
                System.arraycopy(src, (int) srcFramePosition * srcFormat.channelCount(), dst, 0, this.lastOutputFrameCount * srcFormat.channelCount());
            } else if (srcFormat.channelCount() == 1 && dstFormat.channelCount() == 2) {
                remapMonoToStereo(src, dst, this.lastOutputFrameCount, (int) srcFramePosition);
            } else if (srcFormat.channelCount() == 2 && dstFormat.channelCount() == 1) {
                remapStereoToMono(src, dst, this.lastOutputFrameCount, (int) srcFramePosition);
            } else {
                throw new IllegalArgumentException("Unsupported channel conversion: " + srcFormat.channelCount() + " -> " + dstFormat.channelCount());
            }
            return srcFramePosition + this.lastOutputFrameCount;
        }
    }

    protected abstract void resampleMonoToMono(final float[] src, final float[] dst, final int outputFrameCount, final double srcStep, final double srcFramePosition);

    protected abstract void resampleStereoToStereo(final float[] src, final float[] dst, final int outputFrameCount, final double srcStep, final double srcFramePosition);

    protected abstract void resampleMonoToStereo(final float[] src, final float[] dst, final int outputFrameCount, final double srcStep, final double srcFramePosition);

    protected abstract void resampleStereoToMono(final float[] src, final float[] dst, final int outputFrameCount, final double srcStep, final double srcFramePosition);

    public int getLookBehindFrameCount() {
        return this.lookBehindFrameCount;
    }

    public int getLookAheadFrameCount() {
        return this.lookAheadFrameCount;
    }

    public int getLastOutputFrameCount() {
        return this.lastOutputFrameCount;
    }

    public static int computeMaxRequiredInputFrameCount(final AudioFormat srcFormat, final AudioFormat dstFormat, final int outputFrameCount) {
        if (outputFrameCount > 0) {
            final double srcStep = (double) srcFormat.sampleRate() / (double) dstFormat.sampleRate();
            final double maxLastSrcFramePosition = MathUtil.multiplyAndAdd(outputFrameCount - 1, srcStep, Math.nextDown(1D));
            return Math.addExact(MathUtil.floor(maxLastSrcFramePosition), 1);
        } else if (outputFrameCount == 0) {
            return 0;
        } else {
            throw new IllegalArgumentException("Output frame count must be >= 0: " + outputFrameCount);
        }
    }

    private static void remapMonoToStereo(final float[] src, final float[] dst, final int outputFrameCount, final int srcFrameIndex) {
        for (int dstFrameIndex = 0; dstFrameIndex < outputFrameCount; dstFrameIndex++) {
            dst[dstFrameIndex * 2] = dst[dstFrameIndex * 2 + 1] = src[srcFrameIndex + dstFrameIndex];
        }
    }

    private static void remapStereoToMono(final float[] src, final float[] dst, final int outputFrameCount, final int srcFrameIndex) {
        for (int dstFrameIndex = 0; dstFrameIndex < outputFrameCount; dstFrameIndex++) {
            final int srcIndex = (srcFrameIndex + dstFrameIndex) * 2;
            dst[dstFrameIndex] = (src[srcIndex] + src[srcIndex + 1]) / 2F;
        }
    }

    private static int computeOutputFrameCount(final int srcFrameCount, final int dstFrameCount, final double srcStep, final double srcFramePosition) {
        if (dstFrameCount > 0 && srcFramePosition < srcFrameCount) {
            int outputFrameCount = MathUtil.ceil(Math.min((srcFrameCount - srcFramePosition) / srcStep, dstFrameCount));
            while (outputFrameCount > 0 && MathUtil.multiplyAndAdd(outputFrameCount - 1, srcStep, srcFramePosition) >= srcFrameCount) {
                outputFrameCount--;
            }
            while (outputFrameCount < dstFrameCount && MathUtil.multiplyAndAdd(outputFrameCount, srcStep, srcFramePosition) < srcFrameCount) {
                outputFrameCount++;
            }
            return outputFrameCount;
        } else {
            return 0;
        }
    }

}
