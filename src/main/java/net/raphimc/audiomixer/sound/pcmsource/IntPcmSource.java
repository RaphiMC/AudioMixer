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
package net.raphimc.audiomixer.sound.pcmsource;

import net.raphimc.audiomixer.sound.PcmSource;
import net.raphimc.audiomixer.util.InterpolationUtil;

public class IntPcmSource implements PcmSource {

    private final int[] samples;
    private double position;

    public IntPcmSource(final int[] samples) {
        if (samples == null || samples.length == 0) {
            throw new IllegalArgumentException("Samples must not be null or empty");
        }

        this.samples = samples;
    }

    @Override
    public int getCurrentSample() {
        return InterpolationUtil.interpolateLinear(this.samples, this.position);
    }

    @Override
    public int consumeSamples(final int[] buffer, final int offset, final int length) {
        final int numSamples = Math.min(length, this.samples.length - (int) this.position);
        System.arraycopy(this.samples, (int) this.position, buffer, offset, numSamples);
        this.position += numSamples;
        return numSamples;
    }

    @Override
    public double getPosition() {
        return this.position;
    }

    @Override
    public void setPosition(final double position) {
        this.position = position;
    }

    @Override
    public void incrementPosition(final double increment) {
        this.position += increment;
    }

    @Override
    public float getProgress() {
        return (float) (this.position / this.samples.length);
    }

    @Override
    public void setProgress(final float progress) {
        this.position = (int) ((double) progress * this.samples.length);
    }

    @Override
    public boolean hasReachedEnd() {
        return (int) this.position >= this.samples.length;
    }

}