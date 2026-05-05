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
package net.raphimc.audiomixer.soundmodifier.impl;

import net.raphimc.audiomixer.soundmodifier.SoundModifier;
import net.raphimc.audiomixer.util.MathUtil;
import net.raphimc.audiomixer.util.PcmFloatAudioFormat;

import java.util.ArrayDeque;

public class LimiterModifier implements SoundModifier {

    private float attackMillis;
    private float releaseMillis;
    private float lookaheadMillis;

    private float currentGain = 1F;

    public LimiterModifier() {
        this(1F, 100F, 5F);
    }

    public LimiterModifier(final float attackMillis, final float releaseMillis, final float lookaheadMillis) {
        this.setAttackMillis(attackMillis);
        this.setReleaseMillis(releaseMillis);
        this.setLookaheadMillis(lookaheadMillis);
    }

    @Override
    public void modify(final PcmFloatAudioFormat audioFormat, final float[] renderedSamples) {
        final int channelCount = audioFormat.getChannels();
        final int frameCount = renderedSamples.length / channelCount;
        final float millisPerFrame = MathUtil.frameCountToMillis(audioFormat, 1);
        final float attackCoefficient = computeCoefficient(millisPerFrame, this.attackMillis);
        final float releaseCoefficient = computeCoefficient(millisPerFrame, this.releaseMillis);
        final int lookaheadFrameCount = MathUtil.millisToFrameCount(audioFormat, this.lookaheadMillis);

        final float[] framePeaks = new float[frameCount];
        for (int frame = 0; frame < frameCount; frame++) {
            for (int channel = 0; channel < channelCount; channel++) {
                framePeaks[frame] = Math.max(Math.abs(renderedSamples[frame * channelCount + channel]), framePeaks[frame]);
            }
        }
        final LookaheadPeakWindow window = new LookaheadPeakWindow(framePeaks, lookaheadFrameCount);
        for (int frame = 0; frame < frameCount; frame++) {
            final float peakAhead = window.getMax(frame);
            final float targetGain = peakAhead > 1F ? 1F / peakAhead : 1F;
            final float coefficient = targetGain < this.currentGain ? attackCoefficient : releaseCoefficient;
            this.currentGain += (targetGain - this.currentGain) * coefficient;
            for (int channel = 0; channel < channelCount; channel++) {
                renderedSamples[frame * channelCount + channel] *= this.currentGain;
            }
        }
    }

    public void reset() {
        this.currentGain = 1F;
    }

    public float getAttackMillis() {
        return this.attackMillis;
    }

    public LimiterModifier setAttackMillis(final float attackMillis) {
        if (attackMillis <= 0) {
            throw new IllegalArgumentException("Attack millis must be greater than 0");
        }
        this.attackMillis = attackMillis;
        return this;
    }

    public float getReleaseMillis() {
        return this.releaseMillis;
    }

    public LimiterModifier setReleaseMillis(final float releaseMillis) {
        if (releaseMillis <= 0) {
            throw new IllegalArgumentException("Release millis must be greater than 0");
        }
        this.releaseMillis = releaseMillis;
        return this;
    }

    public float getLookaheadMillis() {
        return this.lookaheadMillis;
    }

    public LimiterModifier setLookaheadMillis(final float lookaheadMillis) {
        if (lookaheadMillis <= 0) {
            throw new IllegalArgumentException("Lookahead millis must be greater than 0");
        }
        this.lookaheadMillis = lookaheadMillis;
        return this;
    }

    public float getCurrentGain() {
        return this.currentGain;
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
