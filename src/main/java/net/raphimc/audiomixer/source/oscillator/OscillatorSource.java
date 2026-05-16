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
package net.raphimc.audiomixer.source.oscillator;

import net.raphimc.audiomixer.source.Source;

public abstract class OscillatorSource extends Source {

    private float frequency;

    public OscillatorSource(final float frequency) {
        this.setFrequency(frequency);
    }

    public float getFrequency() {
        return this.frequency;
    }

    public void setFrequency(final float frequency) {
        if (frequency <= 0) {
            throw new IllegalArgumentException("Frequency must be > 0");
        }
        this.frequency = frequency;
    }

}
