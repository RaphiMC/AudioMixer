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
import net.raphimc.audiomixer.util.MathUtil;
import net.raphimc.audiomixer.util.buffer.AudioBuffer;

import java.util.Arrays;

public class BassBoostProcessor implements Processor {

    private float factor;
    private float rc;
    private float[] previousResult = new float[2];

    public BassBoostProcessor(final float cutoffFrequency) {
        this(cutoffFrequency, 1F);
    }

    public BassBoostProcessor(final float cutoffFrequency, final float factor) {
        this.setCutoffFrequency(cutoffFrequency);
        this.setFactor(factor);
    }

    @Override
    public void process(final AudioBuffer buffer) {
        final int channels = buffer.format().channels();
        if (this.previousResult.length < channels) {
            this.previousResult = Arrays.copyOf(this.previousResult, channels);
        }
        final float dt = 1F / buffer.format().sampleRate();
        final float alpha = dt / (this.rc + dt);

        final float[] samples = buffer.samples();
        for (int i = 0; i < samples.length; i++) {
            final int channelIndex = i % channels;
            final float sample = samples[i];
            final float result = (1 - alpha) * this.previousResult[channelIndex] + alpha * sample;
            this.previousResult[channelIndex] = result;
            samples[i] = sample + result * this.factor;
        }
    }

    public void setCutoffFrequency(final float cutoffFrequency) {
        if (cutoffFrequency <= 0) {
            throw new IllegalArgumentException("Cutoff frequency must be > 0");
        }
        this.rc = 1F / (MathUtil.TWO_PI * cutoffFrequency);
    }

    public float getFactor() {
        return this.factor;
    }

    public void setFactor(final float factor) {
        if (factor < 0) {
            throw new IllegalArgumentException("Factor must be >= 0");
        }
        this.factor = factor;
    }

}
