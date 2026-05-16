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

import net.raphimc.audiomixer.dsp.processor.Processor;
import net.raphimc.audiomixer.util.buffer.AudioBuffer;

import java.util.Arrays;

public class BitCrushProcessor implements Processor {

    private int decimationRate;

    private int counter;
    private float[] lastSample = new float[2];

    public BitCrushProcessor() {
        this(15);
    }

    public BitCrushProcessor(final int decimationRate) {
        this.setDecimationRate(decimationRate);
    }

    @Override
    public void process(final AudioBuffer buffer) {
        final int channels = buffer.format().channels();
        if (this.lastSample.length < channels) {
            this.lastSample = Arrays.copyOf(this.lastSample, channels);
        }

        final float[] samples = buffer.samples();
        for (int i = 0; i < samples.length; i += channels) {
            if (this.counter == 0) {
                this.counter = this.decimationRate;
                for (int channel = 0; channel < channels; channel++) {
                    final int intSample = (int) (samples[i + channel] * Short.MAX_VALUE);
                    this.lastSample[channel] = (intSample & 0xFFFFFFFC) / (float) Short.MAX_VALUE;
                }
            } else {
                this.counter--;
            }

            System.arraycopy(this.lastSample, 0, samples, i, channels);
        }
    }

    public int getDecimationRate() {
        return this.decimationRate;
    }

    public void setDecimationRate(final int decimationRate) {
        if (decimationRate <= 0) {
            throw new IllegalArgumentException("Decimation rate must be > 0");
        }
        this.decimationRate = decimationRate;
    }

}
