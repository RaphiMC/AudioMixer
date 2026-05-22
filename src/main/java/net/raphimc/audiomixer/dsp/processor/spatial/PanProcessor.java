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
package net.raphimc.audiomixer.dsp.processor.spatial;

import net.raphimc.audiomixer.dsp.parameter.FloatParameter;
import net.raphimc.audiomixer.dsp.processor.Processor;
import net.raphimc.audiomixer.dsp.processor.dynamics.StereoGainProcessor;
import net.raphimc.audiomixer.util.buffer.AudioBuffer;
import net.raphimc.audiomixer.util.math.MathUtil;

public class PanProcessor implements Processor {

    private final StereoGainProcessor internalProcessor = new StereoGainProcessor();
    private final FloatParameter pan = FloatParameter.of(0F).withConstraint(FloatParameter.Constraint.SIGNED_NORMALIZED).withChangeListener(this::applyPan);

    public PanProcessor() {
        this.applyPan();
    }

    public PanProcessor(final float pan) {
        this.applyPan();
        this.pan.set(pan);
    }

    @Override
    public void process(final AudioBuffer buffer) {
        this.internalProcessor.process(buffer);
    }

    public FloatParameter pan() {
        return this.pan;
    }

    private void applyPan() {
        final float normalizedPan = (this.pan.get() + 1F) / 2F;
        this.internalProcessor.leftGain().set((float) Math.cos(normalizedPan * MathUtil.HALF_PI));
        this.internalProcessor.rightGain().set((float) Math.sin(normalizedPan * MathUtil.HALF_PI));
    }

}
