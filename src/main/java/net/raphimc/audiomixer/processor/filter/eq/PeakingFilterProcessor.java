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

public class PeakingFilterProcessor extends BiquadFilterProcessor<PeakingFilterProcessor.InternalProcessor> {

    private final FloatParameter frequency = FloatParameter.of(0F).withConstraint(FloatParameter.Constraint.GREATER_THAN_ZERO).withChangeListener(this::applyParameters);
    private final FloatParameter gain = FloatParameter.of(1F).withConstraint(FloatParameter.Constraint.GREATER_THAN_ZERO).withChangeListener(this::applyParameters);
    private final FloatParameter gainDb = this.gain.withMapping(MathUtil::gainToDb, MathUtil::dbToGain);
    private final FloatParameter q = FloatParameter.of(MathUtil.BUTTERWORTH_Q).withConstraint(FloatParameter.Constraint.GREATER_THAN_ZERO).withChangeListener(this::applyParameters);

    public PeakingFilterProcessor(final float frequency) {
        this.frequency.set(frequency);
    }

    public PeakingFilterProcessor(final float frequency, final float gain) {
        this.frequency.set(frequency);
        this.gain.set(gain);
    }

    public PeakingFilterProcessor(final float frequency, final float gain, final float q) {
        this.frequency.set(frequency);
        this.gain.set(gain);
        this.q.set(q);
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

    public FloatParameter q() {
        return this.q;
    }

    @Override
    protected InternalProcessor createInternalProcessor(final AudioFormat format) {
        final InternalProcessor internalProcessor = new InternalProcessor(format);
        internalProcessor.applyParameters(this.frequency.get(), this.gain.get(), this.q.get());
        return internalProcessor;
    }

    private void applyParameters() {
        final InternalProcessor internalProcessor = this.getInternalProcessor();
        if (internalProcessor != null) {
            internalProcessor.applyParameters(this.frequency.get(), this.gain.get(), this.q.get());
        }
    }

    protected static final class InternalProcessor extends BiquadFilterProcessor.InternalProcessor {

        private InternalProcessor(final AudioFormat format) {
            super(format);
        }

        private void applyParameters(final float frequency, final float gain, final float q) {
            final double omega = MathUtil.TWO_PI * (frequency / this.format.sampleRate());
            final double sin = MathUtil.sin(omega);
            final double cos = MathUtil.cos(omega);
            final double a = Math.sqrt(gain);
            final double alpha = sin / (2D * q);

            final double b0 = 1D + alpha * a;
            final double b1 = -2D * cos;
            final double b2 = 1D - alpha * a;
            final double a0 = 1D + alpha / a;
            final double a1 = -2D * cos;
            final double a2 = 1D - alpha / a;
            this.setCoefficients(b0, b1, b2, a0, a1, a2);
        }

    }

}
