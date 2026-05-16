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
package net.raphimc.audiomixer.dsp.processor.dynamics;

import net.raphimc.audiomixer.dsp.processor.Processor;
import net.raphimc.audiomixer.util.buffer.AudioBuffer;

public class GainProcessor implements Processor {

    private float gain;

    public GainProcessor(final float gain) {
        this.setGain(gain);
    }

    @Override
    public void process(final AudioBuffer buffer) {
        buffer.scale(this.gain);
    }

    public float getGain() {
        return this.gain;
    }

    public void setGain(final float gain) {
        if (gain < 0F) {
            throw new IllegalArgumentException("Gain must be >= 0");
        }
        this.gain = gain;
    }

    public void setGainPercent(final float gainPercent) {
        this.setGain(gainPercent / 100F);
    }

}
