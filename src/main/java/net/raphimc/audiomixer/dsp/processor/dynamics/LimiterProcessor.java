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
package net.raphimc.audiomixer.dsp.processor.dynamics;

import net.raphimc.audiomixer.dsp.parameter.FloatParameter;
import net.raphimc.audiomixer.dsp.processor.Processor;
import net.raphimc.audiomixer.util.buffer.AudioBuffer;
import net.raphimc.audiomixer.util.math.MathUtil;

import java.util.ArrayDeque;

public class LimiterProcessor extends Processor {

    private final FloatParameter attackMillis = FloatParameter.of(1F).withConstraint(FloatParameter.Constraint.GREATER_THAN_ZERO);
    private final FloatParameter releaseMillis = FloatParameter.of(100F).withConstraint(FloatParameter.Constraint.GREATER_THAN_ZERO);
    private final FloatParameter lookaheadMillis = FloatParameter.of(5F).withConstraint(FloatParameter.Constraint.GREATER_THAN_ZERO);

    private float currentGain = 1F;

    public LimiterProcessor() {
    }

    public LimiterProcessor(final float attackMillis, final float releaseMillis, final float lookaheadMillis) {
        this.attackMillis.set(attackMillis);
        this.releaseMillis.set(releaseMillis);
        this.lookaheadMillis.set(lookaheadMillis);
    }

    @Override
    protected void processInternal(final AudioBuffer buffer) {
        final float millisPerFrame = buffer.format().frameCountToMillis(1);
        final float attackCoefficient = computeCoefficient(millisPerFrame, this.attackMillis.get());
        final float releaseCoefficient = computeCoefficient(millisPerFrame, this.releaseMillis.get());
        final int lookaheadFrameCount = buffer.format().millisToFrameCount(this.lookaheadMillis.get());
        final int channels = buffer.format().channels();
        final float[] samples = buffer.samples();

        final float[] framePeaks = new float[buffer.getFrameCount()];
        for (int frame = 0; frame < framePeaks.length; frame++) {
            for (int channel = 0; channel < channels; channel++) {
                framePeaks[frame] = Math.max(Math.abs(samples[frame * channels + channel]), framePeaks[frame]);
            }
        }
        final LookaheadPeakWindow window = new LookaheadPeakWindow(framePeaks, lookaheadFrameCount);
        for (int frame = 0; frame < framePeaks.length; frame++) {
            final float peakAhead = window.getMax(frame);
            final float targetGain = peakAhead > 1F ? 1F / peakAhead : 1F;
            final float coefficient = targetGain < this.currentGain ? attackCoefficient : releaseCoefficient;
            this.currentGain += (targetGain - this.currentGain) * coefficient;
            for (int channel = 0; channel < channels; channel++) {
                samples[frame * channels + channel] *= this.currentGain;
            }
        }
    }

    public FloatParameter attackMillis() {
        return this.attackMillis;
    }

    public FloatParameter releaseMillis() {
        return this.releaseMillis;
    }

    public FloatParameter lookaheadMillis() {
        return this.lookaheadMillis;
    }

    public float getCurrentGain() {
        return this.currentGain;
    }

    public float getCurrentGainDb() {
        return MathUtil.gainToDb(this.currentGain);
    }

    private static float computeCoefficient(final float millisPerFrame, final float timeConstantMillis) {
        return 1F - (float) Math.exp(-millisPerFrame / timeConstantMillis);
    }

    private static class LookaheadPeakWindow {

        private final float[] framePeaks;
        private final int lookaheadFrameCount;
        private final ArrayDeque<Integer> maxIndices = new ArrayDeque<>();
        private int expandedUntil = -1;

        private LookaheadPeakWindow(final float[] framePeaks, final int lookaheadFrameCount) {
            this.framePeaks = framePeaks;
            this.lookaheadFrameCount = lookaheadFrameCount;
        }

        private float getMax(final int frameIndex) {
            final int windowEnd = Math.min(frameIndex + this.lookaheadFrameCount, this.framePeaks.length - 1);
            while (this.expandedUntil < windowEnd) { // Expand window to the right
                this.expandedUntil++;
                while (!this.maxIndices.isEmpty() && this.framePeaks[this.maxIndices.peekLast()] <= this.framePeaks[this.expandedUntil]) { // Maintain decreasing order (max at front)
                    this.maxIndices.removeLast();
                }
                this.maxIndices.addLast(this.expandedUntil);
            }
            while (!this.maxIndices.isEmpty() && this.maxIndices.peekFirst() < frameIndex) { // Remove elements outside the left side of the window
                this.maxIndices.removeFirst();
            }
            return this.maxIndices.isEmpty() ? 0F : this.framePeaks[this.maxIndices.peekFirst()];
        }

    }

}
