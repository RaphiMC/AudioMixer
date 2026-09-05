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
            throw new IllegalArgumentException("Look behind frame count must be >= 0");
        }
        if (lookAheadFrameCount < 0) {
            throw new IllegalArgumentException("Look ahead frame count must be >= 0");
        }
        this.lookBehindFrameCount = lookBehindFrameCount;
        this.lookAheadFrameCount = lookAheadFrameCount;
    }

    public AudioBuffer resample(final AudioBuffer src, final AudioFormat dstFormat) {
        if (!src.format().equals(dstFormat) && src.frameCount() > 0) {
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
        if (!srcFormat.equals(dstFormat) || srcFramePosition % 1 != 0) {
            final int srcFrameCount = srcFormat.sampleCountToFrameCount(src.length);
            final int dstFrameCount = dstFormat.sampleCountToFrameCount(dst.length);
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
                throw new IllegalArgumentException("Unsupported channel configuration: " + srcFormat.channelCount() + " -> " + dstFormat.channelCount());
            }
            return MathUtil.multiplyAndAdd(this.lastOutputFrameCount, srcStep, srcFramePosition);
        } else {
            final int offset = (int) srcFramePosition * srcFormat.channelCount();
            final int length = MathUtil.clamp(src.length - offset, 0, dst.length);
            System.arraycopy(src, offset, dst, 0, length);
            this.lastOutputFrameCount = srcFormat.sampleCountToFrameCount(length);
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
            throw new IllegalArgumentException("Output frame count must be >= 0");
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
