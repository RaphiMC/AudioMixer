/*
 * This file is part of AudioMixer - https://github.com/RaphiMC/AudioMixer
 * Copyright (C) 2024-2024 RK_01/RaphiMC and contributors
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
package net.raphimc.audiomixer.soundmodifier.impl;

import net.raphimc.audiomixer.soundmodifier.SoundModifier;

import javax.sound.sampled.AudioFormat;
import java.util.Arrays;

public class BassBoostModifier implements SoundModifier {

    private float factor;
    private float rc;
    private float[] previousResult = new float[2];

    public BassBoostModifier(final float cutoffFrequency) {
        this(cutoffFrequency, 1);
    }

    public BassBoostModifier(final float cutoffFrequency, final float factor) {
        this.setCutoffFrequency(cutoffFrequency);
        this.setFactor(factor);
    }

    @Override
    public void modify(final AudioFormat audioFormat, final int[] renderedSamples) {
        final int channels = audioFormat.getChannels();
        if (this.previousResult.length < channels) {
            this.previousResult = Arrays.copyOf(this.previousResult, channels);
        }
        final float dt = 1F / audioFormat.getSampleRate();
        final float alpha = dt / (this.rc + dt);

        for (int i = 0; i < renderedSamples.length; i++) {
            final int channelIndex = i % channels;
            final int sample = renderedSamples[i];
            final float result = (1 - alpha) * this.previousResult[channelIndex] + alpha * sample;
            this.previousResult[channelIndex] = result;
            renderedSamples[i] = (int) (sample + result * this.factor);
        }
    }

    public BassBoostModifier setCutoffFrequency(final float cutoffFrequency) {
        this.rc = (float) (1D / (2 * Math.PI * cutoffFrequency));
        return this;
    }

    public float getFactor() {
        return this.factor;
    }

    public BassBoostModifier setFactor(final float factor) {
        if (factor < 0) {
            throw new IllegalArgumentException("Factor must be greater than or equal to 0");
        }

        this.factor = factor;
        return this;
    }

}
