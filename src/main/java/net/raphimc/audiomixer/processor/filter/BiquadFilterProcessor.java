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
package net.raphimc.audiomixer.processor.filter;

import net.raphimc.audiomixer.processor.FormatDependentProcessor;
import net.raphimc.audiomixer.util.AudioFormat;
import net.raphimc.audiomixer.util.buffer.AudioBuffer;
import net.raphimc.audiomixer.util.math.MathUtil;

// Direct Form 2 Transposed Biquad filter
public abstract class BiquadFilterProcessor<IP extends BiquadFilterProcessor.InternalProcessor> extends FormatDependentProcessor<IP> {

    protected abstract static class InternalProcessor extends FormatDependentProcessor.InternalProcessor {

        private float b0 = 1F;
        private float b1;
        private float b2;
        private float a1;
        private float a2;
        private final float[] s1;
        private final float[] s2;

        protected InternalProcessor(final AudioFormat format) {
            super(format);
            this.s1 = new float[format.channels()];
            this.s2 = new float[format.channels()];
        }

        @Override
        protected void processInternal(final AudioBuffer buffer) {
            final float b0 = this.b0;
            final float b1 = this.b1;
            final float b2 = this.b2;
            final float a1 = this.a1;
            final float a2 = this.a2;
            final int channels = buffer.format().channels();
            final float[] samples = buffer.samples();
            for (int sampleIndex = 0; sampleIndex < samples.length; sampleIndex += channels) {
                for (int channel = 0; channel < channels; channel++) {
                    final float x0 = samples[sampleIndex + channel];
                    final float y0 = MathUtil.multiplyAndAdd(b0, x0, this.s1[channel]);
                    this.s1[channel] = MathUtil.multiplyAndAdd(-a1, y0, MathUtil.multiplyAndAdd(b1, x0, this.s2[channel]));
                    this.s2[channel] = MathUtil.multiplyAndAdd(-a2, y0, b2 * x0);
                    samples[sampleIndex + channel] = y0;
                }
            }
        }

        protected void setCoefficients(final double b0, final double b1, final double b2, final double a0, final double a1, final double a2) {
            this.setCoefficients(b0 / a0, b1 / a0, b2 / a0, a1 / a0, a2 / a0);
        }

        protected void setCoefficients(final double b0, final double b1, final double b2, final double a1, final double a2) {
            if (!Double.isFinite(b0) || !Double.isFinite(b1) || !Double.isFinite(b2) || !Double.isFinite(a1) || !Double.isFinite(a2)) {
                throw new IllegalArgumentException("Coefficients must be finite");
            }
            if ((1D + a1 + a2) <= 0D || (1D - a1 + a2) <= 0D || (1D - a2) <= 0D) {
                throw new IllegalArgumentException("Unstable coefficients (poles outside the unit circle)");
            }
            this.b0 = (float) b0;
            this.b1 = (float) b1;
            this.b2 = (float) b2;
            this.a1 = (float) a1;
            this.a2 = (float) a2;
        }

    }

}
