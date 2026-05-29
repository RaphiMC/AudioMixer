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

import net.raphimc.audiomixer.dsp.parameter.FloatParameter;
import net.raphimc.audiomixer.dsp.processor.Processor;
import net.raphimc.audiomixer.util.buffer.AudioBuffer;
import net.raphimc.audiomixer.util.math.MathUtil;

public class GainProcessor extends Processor {

    private final FloatParameter gain = FloatParameter.of(1F).withConstraint(FloatParameter.Constraint.POSITIVE);
    private final FloatParameter gainDb = this.gain.withMapping(MathUtil::gainToDb, MathUtil::dbToGain);

    public GainProcessor() {
    }

    public GainProcessor(final float gain) {
        this.gain.set(gain);
    }

    @Override
    protected void processInternal(final AudioBuffer buffer) {
        buffer.multiply(this.gain.get());
    }

    public FloatParameter gain() {
        return this.gain;
    }

    public FloatParameter gainDb() {
        return this.gainDb;
    }

}
