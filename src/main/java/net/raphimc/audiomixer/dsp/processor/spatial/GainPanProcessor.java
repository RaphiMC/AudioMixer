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

public class GainPanProcessor extends Processor {

    private final StereoGainProcessor internalProcessor = new StereoGainProcessor();
    private final FloatParameter gain = FloatParameter.of(1F).withConstraint(FloatParameter.Constraint.POSITIVE).withChangeListener(this::applyParameters);
    private final FloatParameter gainDb = this.gain.withMapping(MathUtil::gainToDb, MathUtil::dbToGain);
    private final FloatParameter pan = FloatParameter.of(0F).withConstraint(FloatParameter.Constraint.SIGNED_NORMALIZED).withChangeListener(this::applyParameters);

    public GainPanProcessor() {
        this.applyParameters();
    }

    public GainPanProcessor(final float gain, final float pan) {
        this.applyParameters();
        this.gain.set(gain);
        this.pan.set(pan);
    }

    @Override
    protected void processInternal(final AudioBuffer buffer) {
        this.internalProcessor.process(buffer);
    }

    public FloatParameter gain() {
        return this.gain;
    }

    public FloatParameter gainDb() {
        return this.gainDb;
    }

    public FloatParameter pan() {
        return this.pan;
    }

    private void applyParameters() {
        final float gain = this.gain.get();
        final float normalizedPan = (this.pan.get() + 1F) / 2F;
        this.internalProcessor.leftGain().set(gain * (float) Math.cos(normalizedPan * MathUtil.HALF_PI));
        this.internalProcessor.rightGain().set(gain * (float) Math.sin(normalizedPan * MathUtil.HALF_PI));
    }

}
