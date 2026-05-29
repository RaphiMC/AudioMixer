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
import net.raphimc.audiomixer.dsp.processor.FormatDependentProcessor;
import net.raphimc.audiomixer.util.FloatAudioFormat;
import net.raphimc.audiomixer.util.buffer.AudioBuffer;

public class SampleAndHoldProcessor extends FormatDependentProcessor<SampleAndHoldProcessor.InternalProcessor> {

    private final FloatParameter holdMillis = FloatParameter.of(0.125F).withConstraint(FloatParameter.Constraint.POSITIVE);

    public SampleAndHoldProcessor() {
    }

    public SampleAndHoldProcessor(final float holdMillis) {
        this.holdMillis.set(holdMillis);
    }

    public FloatParameter holdMillis() {
        return this.holdMillis;
    }

    @Override
    protected InternalProcessor createInternalProcessor(final FloatAudioFormat format) {
        return new InternalProcessor(format);
    }

    protected class InternalProcessor extends FormatDependentProcessor.InternalProcessor {

        private final float[] heldSamples;
        private int holdFramesRemaining;

        private InternalProcessor(final FloatAudioFormat format) {
            super(format);
            this.heldSamples = new float[format.channels()];
        }

        @Override
        protected void processInternal(final AudioBuffer buffer) {
            final int holdFrameCount = buffer.format().millisToFrameCount(SampleAndHoldProcessor.this.holdMillis.get());
            final int channels = buffer.format().channels();
            final float[] samples = buffer.samples();
            for (int sampleIndex = 0; sampleIndex < samples.length; sampleIndex += channels) {
                if (this.holdFramesRemaining == 0) {
                    this.holdFramesRemaining = holdFrameCount - 1;
                    for (int channel = 0; channel < channels; channel++) {
                        this.heldSamples[channel] = samples[sampleIndex + channel];
                    }
                } else {
                    this.holdFramesRemaining--;
                    for (int channel = 0; channel < channels; channel++) {
                        samples[sampleIndex + channel] = this.heldSamples[channel];
                    }
                }
            }
        }

        @Override
        protected boolean supports(final FloatAudioFormat other) {
            return this.format.channels() == other.channels();
        }

    }

}
