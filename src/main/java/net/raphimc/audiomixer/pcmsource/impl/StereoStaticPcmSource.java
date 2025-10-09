/*
 * This file is part of AudioMixer - https://github.com/RaphiMC/AudioMixer
 * Copyright (C) 2024-2025 RK_01/RaphiMC and contributors
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

import net.raphimc.audiomixer.interpolator.Interpolator;
import net.raphimc.audiomixer.interpolator.impl.LinearInterpolator;
import net.raphimc.audiomixer.pcmsource.StaticPcmSource;
import net.raphimc.audiomixer.pcmsource.StereoPcmSource;

public class StereoStaticPcmSource implements StereoPcmSource, StaticPcmSource {

    private final float[] samples;
    private final int sampleCount;
    private final float[] buffer = new float[2];
    private final Interpolator interpolator;
    private double position;

    public StereoStaticPcmSource(final float[] samples) {
        this(samples, LinearInterpolator.INSTANCE);
    }

    public StereoStaticPcmSource(final float[] samples, final Interpolator interpolator) {
        if (samples == null || samples.length == 0) {
            throw new IllegalArgumentException("Samples must not be null or empty");
        }
        if (samples.length % 2 != 0) {
            throw new IllegalArgumentException("Sample count must be a multiple of 2");
        }

        this.samples = samples;
        this.sampleCount = samples.length / 2;
        this.interpolator = interpolator;
    }

    @Override
    public float[] consumeSample(final float increment) {
        this.buffer[0] = this.interpolator.interpolate(this.samples, this.position, 0, 2);
        this.buffer[1] = this.interpolator.interpolate(this.samples, this.position, 1, 2);
        this.position += increment;
        return this.buffer;
    }

    @Override
    public int consumeSamples(final float[] buffer, final int offset, final int length) {
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
        if (position < 0 || position > this.samples.length) {
            throw new IllegalArgumentException("Position must be between 0 and " + this.samples.length);
        }
        this.position = position;
        return this;
    }

}
