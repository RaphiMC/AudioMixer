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
package net.raphimc.audiomixer.dsp.processor.filter.eq;

import net.raphimc.audiomixer.dsp.parameter.FloatParameter;
import net.raphimc.audiomixer.dsp.processor.filter.BiquadFilterProcessor;
import net.raphimc.audiomixer.util.FloatAudioFormat;
import net.raphimc.audiomixer.util.math.MathUtil;

public class HighShelfFilterProcessor extends BiquadFilterProcessor<HighShelfFilterProcessor.InternalProcessor> {

    private final FloatParameter frequency = FloatParameter.of(0F).withConstraint(FloatParameter.Constraint.GREATER_THAN_ZERO).withChangeListener(this::applyParameters);
    private final FloatParameter gain = FloatParameter.of(1F).withConstraint(FloatParameter.Constraint.GREATER_THAN_ZERO).withChangeListener(this::applyParameters);
    private final FloatParameter gainDb = this.gain.withMapping(MathUtil::gainToDb, MathUtil::dbToGain);
    private final FloatParameter slope = FloatParameter.of(1F).withConstraint(FloatParameter.Constraint.GREATER_THAN_ZERO).withChangeListener(this::applyParameters);

    public HighShelfFilterProcessor(final float frequency) {
        this.frequency.set(frequency);
    }

    public HighShelfFilterProcessor(final float frequency, final float gain) {
        this.frequency.set(frequency);
        this.gain.set(gain);
    }

    public HighShelfFilterProcessor(final float frequency, final float gain, final float slope) {
        this.frequency.set(frequency);
        this.gain.set(gain);
        this.slope.set(slope);
    }

    public FloatParameter frequency() {
        return this.frequency;
    }

    public FloatParameter gain() {
        return this.gain;
    }

    public FloatParameter gainDb() {
        return this.gainDb;
    }

    public FloatParameter slope() {
        return this.slope;
    }

    @Override
    protected InternalProcessor createInternalProcessor(final FloatAudioFormat format) {
        final InternalProcessor internalProcessor = new InternalProcessor(format);
        internalProcessor.applyParameters(this.frequency.get(), this.gain.get(), this.slope.get());
        return internalProcessor;
    }

    private void applyParameters() {
        final InternalProcessor internalProcessor = this.getInternalProcessor();
        if (internalProcessor != null) {
            internalProcessor.applyParameters(this.frequency.get(), this.gain.get(), this.slope.get());
        }
    }

    protected static class InternalProcessor extends BiquadFilterProcessor.InternalProcessor {

        private InternalProcessor(final FloatAudioFormat format) {
            super(format);
        }

        private void applyParameters(final float frequency, final float gain, final float slope) {
            final double omega = MathUtil.TWO_PI * (frequency / this.format.sampleRate());
            final float sin = (float) Math.sin(omega);
            final float cos = (float) Math.cos(omega);
            final float a = (float) Math.sqrt(gain);
            final float sqrtA = (float) Math.sqrt(a);
            final float alpha = sin * 0.5F * (float) Math.sqrt((a + 1F / a) * (1F / slope - 1F) + 2F);

            final float b0 = a * ((a + 1F) + (a - 1F) * cos + 2F * sqrtA * alpha);
            final float b1 = -2F * a * ((a - 1F) + (a + 1F) * cos);
            final float b2 = a * ((a + 1F) + (a - 1F) * cos - 2F * sqrtA * alpha);
            final float a0 = (a + 1F) - (a - 1F) * cos + 2F * sqrtA * alpha;
            final float a1 = 2F * ((a - 1F) - (a + 1F) * cos);
            final float a2 = (a + 1F) - (a - 1F) * cos - 2F * sqrtA * alpha;
            this.setCoefficients(b0, b1, b2, a0, a1, a2);
        }

    }

}
