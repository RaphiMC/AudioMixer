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
package net.raphimc.audiomixer.oscillator.impl;

import net.raphimc.audiomixer.oscillator.Oscillator;

public class SquareOscillator extends Oscillator {

    private float dutyCycle;

    public SquareOscillator(final float frequency) {
        this(frequency, 0.5F);
    }

    public SquareOscillator(final float frequency, final float dutyCycle) {
        super(frequency);
        this.setDutyCycle(dutyCycle);
    }

    @Override
    protected float computeNextValue() {
        return this.angle < this.dutyCycle * TWO_PI ? 1F : -1F;
    }

    public float getDutyCycle() {
        return this.dutyCycle;
    }

    public SquareOscillator setDutyCycle(final float dutyCycle) {
        if (dutyCycle <= 0 || dutyCycle >= 1) {
            throw new IllegalArgumentException("Duty cycle must be between 0 and 1 (exclusive)");
        }

        this.dutyCycle = dutyCycle;
        return this;
    }

}
