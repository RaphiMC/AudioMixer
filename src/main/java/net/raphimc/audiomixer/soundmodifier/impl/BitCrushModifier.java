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

public class BitCrushModifier implements SoundModifier {

    private int interval;

    private int counter;
    private int[] lastSample = new int[2];

    public BitCrushModifier() {
        this(15);
    }

    public BitCrushModifier(final int interval) {
        this.setInterval(interval);
    }

    @Override
    public void modify(final AudioFormat audioFormat, final int[] renderedSamples) {
        final int channels = audioFormat.getChannels();
        if (this.lastSample.length < channels) {
            this.lastSample = Arrays.copyOf(this.lastSample, channels);
        }

        for (int i = 0; i < renderedSamples.length; i += channels) {
            if (this.counter == 0) {
                this.counter = this.interval;
                for (int j = 0; j < channels; j++) {
                    this.lastSample[j] = (renderedSamples[i + j] & 0xFFFFFFFC);
                }
            } else {
                this.counter--;
            }

            System.arraycopy(this.lastSample, 0, renderedSamples, i, channels);
        }
    }

    public int getInterval() {
        return this.interval;
    }

    public void setInterval(final int interval) {
        if (interval <= 0) {
            throw new IllegalArgumentException("Interval must be greater than 0");
        }

        this.interval = interval;
    }

}
