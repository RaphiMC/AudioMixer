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
package net.raphimc.audiomixer.dsp.processor.filter;

import net.raphimc.audiomixer.dsp.processor.FormatDependentProcessor;
import net.raphimc.audiomixer.util.FloatAudioFormat;
import net.raphimc.audiomixer.util.buffer.AudioBuffer;

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

        protected InternalProcessor(final FloatAudioFormat format) {
            super(format);
            this.s1 = new float[format.channels()];
            this.s2 = new float[format.channels()];
        }

        @Override
        public void process(final AudioBuffer buffer) {
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
                    final float y0 = b0 * x0 + this.s1[channel];
                    this.s1[channel] = b1 * x0 + this.s2[channel] - a1 * y0;
                    this.s2[channel] = b2 * x0 - a2 * y0;
                    samples[sampleIndex + channel] = y0;
                }
            }
        }

        protected void setCoefficients(final float b0, final float b1, final float b2, final float a0, final float a1, final float a2) {
            this.setCoefficients(b0 / a0, b1 / a0, b2 / a0, a1 / a0, a2 / a0);
        }

        protected void setCoefficients(final float b0, final float b1, final float b2, final float a1, final float a2) {
            if (!Float.isFinite(b0) || !Float.isFinite(b1) || !Float.isFinite(b2) || !Float.isFinite(a1) || !Float.isFinite(a2)) {
                throw new IllegalArgumentException("Coefficients must be finite");
            }
            if ((1F + a1 + a2) <= 0F || (1F - a1 + a2) <= 0F || (1F - a2) <= 0F) {
                throw new IllegalArgumentException("Unstable coefficients (poles outside the unit circle)");
            }
            this.b0 = b0;
            this.b1 = b1;
            this.b2 = b2;
            this.a1 = a1;
            this.a2 = a2;
        }

    }

}
