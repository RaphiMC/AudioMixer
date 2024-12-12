/*
 * This file is part of AudioMixer - https://github.com/RaphiMC/AudioMixer
 * Copyright (C) 2024-2024 RK_01/RaphiMC and contributors
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
package net.raphimc.audiomixer.pcmsource.impl;

import net.raphimc.audiomixer.pcmsource.StaticPcmSource;
import net.raphimc.audiomixer.pcmsource.StereoPcmSource;
import net.raphimc.audiomixer.util.InterpolationUtil;

public class StereoIntPcmSource implements StereoPcmSource, StaticPcmSource {

    private final int[] samples;
    private final int sampleCount;
    private final int[] buffer = new int[2];
    private double position;

    public StereoIntPcmSource(final int[] samples) {
        if (samples == null || samples.length == 0) {
            throw new IllegalArgumentException("Samples must not be null or empty");
        }
        if (samples.length % 2 != 0) {
            throw new IllegalArgumentException("Sample count must be a multiple of 2");
        }

        this.samples = samples;
        this.sampleCount = samples.length / 2;
    }

    @Override
    public int[] consumeSample(final float increment) {
        this.buffer[0] = InterpolationUtil.interpolateLinear(this.samples, this.position, 0, 2);
        this.buffer[1] = InterpolationUtil.interpolateLinear(this.samples, this.position, 1, 2);
        this.position += increment;
        return this.buffer;
    }

    @Override
    public int consumeSamples(final int[] buffer, final int offset, final int length) {
        final int numSamples = Math.min(length / 2, this.sampleCount - (int) this.position);
        System.arraycopy(this.samples, (int) this.position * 2, buffer, offset * 2, numSamples * 2);
        this.position += numSamples;
        return numSamples * 2;
    }

    @Override
    public boolean hasReachedEnd() {
        return (int) this.position >= this.sampleCount;
    }

    @Override
    public int getSampleCount() {
        return this.sampleCount;
    }

    @Override
    public double getPosition() {
        return this.position;
    }

    @Override
    public StaticPcmSource setPosition(final double position) {
        this.position = position;
        return this;
    }

    @Override
    public float getProgress() {
        return (float) (this.position / this.sampleCount);
    }

    @Override
    public StaticPcmSource setProgress(final float progress) {
        this.position = (int) ((double) progress * this.sampleCount);
        return this;
    }

}
