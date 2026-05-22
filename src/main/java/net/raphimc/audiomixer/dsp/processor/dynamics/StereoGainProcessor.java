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

public class StereoGainProcessor implements Processor {

    private final FloatParameter leftGain = FloatParameter.of(1F).withConstraint(FloatParameter.Constraint.POSITIVE);
    private final FloatParameter leftGainDb = this.leftGain.withMapping(MathUtil::gainToDb, MathUtil::dbToGain);
    private final FloatParameter rightGain = FloatParameter.of(1F).withConstraint(FloatParameter.Constraint.POSITIVE);
    private final FloatParameter rightGainDb = this.rightGain.withMapping(MathUtil::gainToDb, MathUtil::dbToGain);

    public StereoGainProcessor() {
    }

    public StereoGainProcessor(final float leftGain, final float rightGain) {
        this.leftGain.set(leftGain);
        this.rightGain.set(rightGain);
    }

    @Override
    public void process(final AudioBuffer buffer) {
        if (buffer.format().channels() != 2) {
            throw new IllegalArgumentException("Target audio format must have 2 channels");
        }
        final float leftGain = this.leftGain.get();
        final float rightGain = this.rightGain.get();
        final float[] samples = buffer.samples();
        for (int sampleIndex = 0; sampleIndex < samples.length; sampleIndex += 2) {
            samples[sampleIndex] *= leftGain;
            samples[sampleIndex + 1] *= rightGain;
        }
    }

    public FloatParameter leftGain() {
        return this.leftGain;
    }

    public FloatParameter leftGainDb() {
        return this.leftGainDb;
    }

    public FloatParameter rightGain() {
        return this.rightGain;
    }

    public FloatParameter rightGainDb() {
        return this.rightGainDb;
    }

}
