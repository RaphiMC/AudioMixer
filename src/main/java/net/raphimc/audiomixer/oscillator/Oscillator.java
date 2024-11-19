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

public abstract class Oscillator {

    protected float frequency;
    protected Oscillator frequencyOscillator;
    private float multiplier;

    public Oscillator() {
    }

    public Oscillator(final float frequency) {
        this.setFrequency(frequency);
        this.setMultiplier(1F);
    }

    public float getNextValue(final float referenceFrequency) {
        return this.getNextNormalizedValue(referenceFrequency) * this.multiplier;
    }

    public int getNextValueInt(final float referenceFrequency) {
        return Math.round(this.getNextValue(referenceFrequency));
    }

    protected abstract float getNextNormalizedValue(final float referenceFrequency);

    public float modifyValue(final float value, final float referenceFrequency) {
        return value + this.getNextValue(referenceFrequency);
    }

    public int modifyValueInt(final int value, final float referenceFrequency) {
        return value + this.getNextValueInt(referenceFrequency);
    }

    public float getFrequency() {
        return this.frequency;
    }

    public void setFrequency(final float frequency) {
        if (frequency <= 0) {
            throw new IllegalArgumentException("Frequency must be greater than 0");
        }

        this.frequency = frequency;
    }

    public Oscillator getFrequencyOscillator() {
        return this.frequencyOscillator;
    }

    public void setFrequencyOscillator(final Oscillator frequencyOscillator) {
        this.frequencyOscillator = frequencyOscillator;
    }

    public float getMultiplier() {
        return this.multiplier;
    }

    public void setMultiplier(final float multiplier) {
        this.multiplier = multiplier;
    }

}
