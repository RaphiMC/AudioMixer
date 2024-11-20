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
package net.raphimc.audiomixer.oscillator;

import net.raphimc.audiomixer.modulator.Modulator;

public abstract class Oscillator extends Modulator {

    protected static final double TWO_PI = 2 * Math.PI;

    private float frequency;
    private Modulator frequencyModulator;
    protected double angle;

    public Oscillator() {
    }

    public Oscillator(final float frequency) {
        this.setFrequency(frequency);
    }

    @Override
    protected float getNextNormalizedValue(final float referenceFrequency) {
        final float value = this.computeNextValue();

        if (this.frequencyModulator == null) {
            this.angle += TWO_PI * (this.frequency / referenceFrequency);
        } else {
            this.angle += TWO_PI * (Math.max(this.frequencyModulator.modifyValue(this.frequency, referenceFrequency), 0.0001) / referenceFrequency);
        }
        if (this.angle > TWO_PI) {
            this.angle -= TWO_PI;
        }

        return value;
    }

    protected abstract float computeNextValue();

    public float getFrequency() {
        return this.frequency;
    }

    public void setFrequency(final float frequency) {
        if (frequency <= 0) {
            throw new IllegalArgumentException("Frequency must be greater than 0");
        }

        this.frequency = frequency;
    }

    public Modulator getFrequencyModulator() {
        return this.frequencyModulator;
    }

    public void setFrequencyModulator(final Modulator frequencyModulator) {
        this.frequencyModulator = frequencyModulator;
    }

}
