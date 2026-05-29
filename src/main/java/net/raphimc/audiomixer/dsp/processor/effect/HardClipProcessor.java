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
package net.raphimc.audiomixer.dsp.processor.effect;

import net.raphimc.audiomixer.dsp.parameter.FloatParameter;
import net.raphimc.audiomixer.dsp.processor.Processor;
import net.raphimc.audiomixer.util.buffer.AudioBuffer;
import net.raphimc.audiomixer.util.math.MathUtil;

public class HardClipProcessor extends Processor {

    private final FloatParameter drive = FloatParameter.of(1F).withConstraint(FloatParameter.Constraint.AT_LEAST_ONE);

    public HardClipProcessor() {
    }

    public HardClipProcessor(final float drive) {
        this.drive.set(drive);
    }

    @Override
    protected void processInternal(final AudioBuffer buffer) {
        final float drive = this.drive.get();
        final float[] samples = buffer.samples();
        for (int sampleIndex = 0; sampleIndex < samples.length; sampleIndex++) {
            samples[sampleIndex] = MathUtil.clamp(samples[sampleIndex] * drive, -1F, 1F);
        }
    }

}
