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
package net.raphimc.audiomixer.soundmodifier.impl;

import net.raphimc.audiomixer.soundmodifier.SoundModifier;
import net.raphimc.audiomixer.util.PcmFloatAudioFormat;

import java.util.Arrays;

public class BitCrushModifier implements SoundModifier {

    private int decimationRate;

    private int counter;
    private float[] lastSample = new float[2];

    public BitCrushModifier() {
        this(15);
    }

    public BitCrushModifier(final int decimationRate) {
        this.setDecimationRate(decimationRate);
    }

    @Override
    public void modify(final PcmFloatAudioFormat audioFormat, final float[] renderedSamples) {
        final int channels = audioFormat.getChannels();
        if (this.lastSample.length < channels) {
            this.lastSample = Arrays.copyOf(this.lastSample, channels);
        }

        for (int i = 0; i < renderedSamples.length; i += channels) {
            if (this.counter == 0) {
                this.counter = this.decimationRate;
                for (int channelIndex = 0; channelIndex < channels; channelIndex++) {
                    final int intSample = (int) (renderedSamples[i + channelIndex] * Short.MAX_VALUE);
                    this.lastSample[channelIndex] = (intSample & 0xFFFFFFFC) / (float) Short.MAX_VALUE;
                }
            } else {
                this.counter--;
            }

            System.arraycopy(this.lastSample, 0, renderedSamples, i, channels);
        }
    }

    public int getDecimationRate() {
        return this.decimationRate;
    }

    public BitCrushModifier setDecimationRate(final int decimationRate) {
        if (decimationRate <= 0) {
            throw new IllegalArgumentException("Decimation rate must be greater than 0");
        }
        this.decimationRate = decimationRate;
        return this;
    }

}
