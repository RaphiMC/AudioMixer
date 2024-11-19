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
package net.raphimc.audiomixer.oscillator.impl;

import net.raphimc.audiomixer.oscillator.Oscillator;

public class SquareOscillator extends Oscillator {

    private float dutyCycle;
    private double phase;

    public SquareOscillator(final float frequency) {
        this(frequency, 0.5F);
    }

    public SquareOscillator(final float frequency, final float dutyCycle) {
        super(frequency);
        this.setDutyCycle(dutyCycle);
    }

    @Override
    public float getNextNormalizedValue(final float referenceFrequency) {
        float value = this.phase < this.dutyCycle ? 1F : -1F;

        if (this.frequencyOscillator == null) {
            this.phase += this.frequency / referenceFrequency;
        } else {
            this.phase += this.frequencyOscillator.modifyValue(this.frequency, referenceFrequency) / referenceFrequency;
        }
        if (this.phase >= 1.0) {
            this.phase -= 1.0;
        }

        return value;
    }

    public float getDutyCycle() {
        return this.dutyCycle;
    }

    public void setDutyCycle(final float dutyCycle) {
        if (dutyCycle <= 0 || dutyCycle >= 1) {
            throw new IllegalArgumentException("Duty cycle must be between 0 and 1 (exclusive)");
        }

        this.dutyCycle = dutyCycle;
    }

}
