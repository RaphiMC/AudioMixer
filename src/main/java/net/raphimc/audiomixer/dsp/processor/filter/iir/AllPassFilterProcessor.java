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
package net.raphimc.audiomixer.dsp.processor.filter.iir;

import net.raphimc.audiomixer.dsp.parameter.FloatParameter;
import net.raphimc.audiomixer.dsp.processor.filter.BiquadFilterProcessor;
import net.raphimc.audiomixer.util.FloatAudioFormat;
import net.raphimc.audiomixer.util.math.MathUtil;

public class AllPassFilterProcessor extends BiquadFilterProcessor<AllPassFilterProcessor.InternalProcessor> {

    private final FloatParameter frequency = FloatParameter.of(0F).withConstraint(FloatParameter.Constraint.GREATER_THAN_ZERO).withChangeListener(this::applyParameters);
    private final FloatParameter q = FloatParameter.of(MathUtil.BUTTERWORTH_Q).withConstraint(FloatParameter.Constraint.GREATER_THAN_ZERO).withChangeListener(this::applyParameters);

    public AllPassFilterProcessor(final float frequency) {
        this.frequency.set(frequency);
    }

    public AllPassFilterProcessor(final float frequency, final float q) {
        this.frequency.set(frequency);
        this.q.set(q);
    }

    public FloatParameter frequency() {
        return this.frequency;
    }

    public FloatParameter q() {
        return this.q;
    }

    @Override
    protected InternalProcessor createInternalProcessor(final FloatAudioFormat format) {
        final InternalProcessor internalProcessor = new InternalProcessor(format);
        internalProcessor.applyParameters(this.frequency.get(), this.q.get());
        return internalProcessor;
    }

    private void applyParameters() {
        final InternalProcessor internalProcessor = this.getInternalProcessor();
        if (internalProcessor != null) {
            internalProcessor.applyParameters(this.frequency.get(), this.q.get());
        }
    }

    protected static class InternalProcessor extends BiquadFilterProcessor.InternalProcessor {

        private InternalProcessor(final FloatAudioFormat format) {
            super(format);
        }

        private void applyParameters(final float frequency, final float q) {
            final double omega = MathUtil.TWO_PI * (frequency / this.format.sampleRate());
            final float sin = (float) Math.sin(omega);
            final float cos = (float) Math.cos(omega);
            final float alpha = sin / (2F * q);

            final float b0 = 1F - alpha;
            final float b1 = -2F * cos;
            final float b2 = 1F + alpha;
            final float a0 = 1F + alpha;
            final float a1 = -2F * cos;
            final float a2 = 1F - alpha;
            this.setCoefficients(b0, b1, b2, a0, a1, a2);
        }

    }

}
