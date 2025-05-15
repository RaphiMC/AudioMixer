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
import net.raphimc.audiomixer.pcmsource.MonoPcmSource;
import net.raphimc.audiomixer.pcmsource.StaticPcmSource;

public class MonoStaticPcmSource implements MonoPcmSource, StaticPcmSource {

    private final float[] samples;
    private final Interpolator interpolator;
    private double position;

    public MonoStaticPcmSource(final float[] samples) {
        this(samples, LinearInterpolator.INSTANCE);
    }

    public MonoStaticPcmSource(final float[] samples, final Interpolator interpolator) {
        if (samples == null || samples.length == 0) {
            throw new IllegalArgumentException("Samples must not be null or empty");
        }
        if (interpolator == null) {
            throw new IllegalArgumentException("Interpolator must not be null");
        }

        this.samples = samples;
        this.interpolator = interpolator;
    }

    @Override
    public float consumeSample(final float increment) {
        final float sample = this.interpolator.interpolate(this.samples, this.position, 0, 1);
        this.position += increment;
        return sample;
    }

    @Override
    public int consumeSamples(final float[] buffer, final int offset, final int length) {
        final int numSamples = Math.min(length, this.samples.length - (int) this.position);
        System.arraycopy(this.samples, (int) this.position, buffer, offset, numSamples);
        this.position += numSamples;
        return numSamples;
    }

    @Override
    public boolean hasReachedEnd() {
        return (int) this.position >= this.samples.length;
    }

    @Override
    public int getSampleCount() {
        return this.samples.length;
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
        return (float) (this.position / this.samples.length);
    }

    @Override
    public StaticPcmSource setProgress(final float progress) {
        this.position = (int) ((double) progress * this.samples.length);
        return this;
    }

}
