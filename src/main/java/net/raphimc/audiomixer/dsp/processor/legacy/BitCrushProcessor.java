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
package net.raphimc.audiomixer.dsp.processor.legacy;

import net.raphimc.audiomixer.dsp.parameter.IntParameter;
import net.raphimc.audiomixer.dsp.processor.Processor;
import net.raphimc.audiomixer.util.buffer.AudioBuffer;

import java.util.Arrays;

public class BitCrushProcessor implements Processor {

    private final IntParameter decimationRate = IntParameter.of(15).withConstraint(IntParameter.Constraint.GREATER_THAN_ZERO);

    private int counter;
    private float[] lastSample = new float[2];

    public BitCrushProcessor() {
    }

    public BitCrushProcessor(final int decimationRate) {
        this.decimationRate.set(decimationRate);
    }

    @Override
    public void process(final AudioBuffer buffer) {
        final int channels = buffer.format().channels();
        if (this.lastSample.length < channels) {
            this.lastSample = Arrays.copyOf(this.lastSample, channels);
        }

        final int decimationRate = this.decimationRate.get();
        final float[] samples = buffer.samples();
        for (int sampleIndex = 0; sampleIndex < samples.length; sampleIndex += channels) {
            if (this.counter == 0) {
                this.counter = decimationRate;
                for (int channel = 0; channel < channels; channel++) {
                    final int intSample = (int) (samples[sampleIndex + channel] * Short.MAX_VALUE);
                    this.lastSample[channel] = (intSample & 0xFFFFFFFC) / (float) Short.MAX_VALUE;
                }
            } else {
                this.counter--;
            }

            System.arraycopy(this.lastSample, 0, samples, sampleIndex, channels);
        }
    }

    public IntParameter decimationRate() {
        return this.decimationRate;
    }

}
