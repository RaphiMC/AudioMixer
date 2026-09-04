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
package net.raphimc.audiomixer.processor.filter.eq;

import net.raphimc.audiomixer.parameter.FloatParameter;
import net.raphimc.audiomixer.processor.filter.BiquadFilterProcessor;
import net.raphimc.audiomixer.util.AudioFormat;
import net.raphimc.audiomixer.util.math.MathUtil;

public class LowShelfFilterProcessor extends BiquadFilterProcessor<LowShelfFilterProcessor.InternalProcessor> {

    private final FloatParameter frequency = FloatParameter.of(0F).withConstraint(FloatParameter.Constraint.GREATER_THAN_ZERO).withChangeListener(this::applyParameters);
    private final FloatParameter gain = FloatParameter.of(1F).withConstraint(FloatParameter.Constraint.GREATER_THAN_ZERO).withChangeListener(this::applyParameters);
    private final FloatParameter gainDb = this.gain.withMapping(MathUtil::gainToDb, MathUtil::dbToGain);
    private final FloatParameter slope = FloatParameter.of(1F).withConstraint(FloatParameter.Constraint.GREATER_THAN_ZERO).withChangeListener(this::applyParameters);

    public LowShelfFilterProcessor(final float frequency) {
        this.frequency.set(frequency);
    }

    public LowShelfFilterProcessor(final float frequency, final float gain) {
        this.frequency.set(frequency);
        this.gain.set(gain);
    }

    public LowShelfFilterProcessor(final float frequency, final float gain, final float slope) {
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
    protected InternalProcessor createInternalProcessor(final AudioFormat format) {
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

    protected static final class InternalProcessor extends BiquadFilterProcessor.InternalProcessor {

        private InternalProcessor(final AudioFormat format) {
            super(format);
        }

        private void applyParameters(final float frequency, final float gain, final float slope) {
            final double omega = MathUtil.TWO_PI * (frequency / this.format.sampleRate());
            final double sin = MathUtil.sin(omega);
            final double cos = MathUtil.cos(omega);
            final double a = Math.sqrt(gain);
            final double sqrtA = Math.sqrt(a);
            final double alpha = sin * 0.5D * Math.sqrt((a + 1D / a) * (1D / slope - 1D) + 2D);

            final double b0 = a * ((a + 1D) - (a - 1D) * cos + 2D * sqrtA * alpha);
            final double b1 = 2D * a * ((a - 1D) - (a + 1D) * cos);
            final double b2 = a * ((a + 1D) - (a - 1D) * cos - 2D * sqrtA * alpha);
            final double a0 = (a + 1D) + (a - 1D) * cos + 2D * sqrtA * alpha;
            final double a1 = -2D * ((a - 1D) + (a + 1D) * cos);
            final double a2 = (a + 1D) + (a - 1D) * cos - 2D * sqrtA * alpha;
            this.setCoefficients(b0, b1, b2, a0, a1, a2);
        }

    }

}
