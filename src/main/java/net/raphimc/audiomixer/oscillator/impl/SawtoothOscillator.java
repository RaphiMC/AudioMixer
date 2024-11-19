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

public class SawtoothOscillator extends Oscillator {

    private static final double TWO_PI = 2 * Math.PI;

    private double angle;

    public SawtoothOscillator(final float frequency) {
        super(frequency);
    }

    @Override
    public float getNextNormalizedValue(final float referenceFrequency) {
        final float value = (float) (2 * (this.angle / TWO_PI) - 1);

        if (this.frequencyOscillator == null) {
            this.angle += TWO_PI * this.frequency / referenceFrequency;
        } else {
            this.angle += TWO_PI * this.frequencyOscillator.modifyValue(this.frequency, referenceFrequency) / referenceFrequency;
        }
        if (this.angle > TWO_PI) {
            this.angle -= TWO_PI;
        }

        return value;
    }

}
