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
package net.raphimc.audiomixer.sound.source;

import net.raphimc.audiomixer.sound.Sound;

import javax.sound.sampled.AudioFormat;

public class StaticMonoSound implements Sound {

    private final int[] samples;
    private int sampleIndex;

    public StaticMonoSound(final int[] samples) {
        if (samples == null || samples.length == 0) {
            throw new IllegalArgumentException("Samples must not be null or empty");
        }

        this.samples = samples;
    }

    @Override
    public void render(final AudioFormat audioFormat, final int[] renderedSamples, final int renderedSamplesLength) {
        final int numChannels = audioFormat.getChannels();
        final int numSamples = renderedSamplesLength / numChannels;

        int renderedIndex = 0;
        if (numChannels == 1) {
            final int length = Math.min(numSamples, this.samples.length - this.sampleIndex);
            System.arraycopy(this.samples, this.sampleIndex, renderedSamples, 0, length);
            this.sampleIndex += length;
            renderedIndex += length;
        } else {
            for (int i = 0; i < numSamples; i++) {
                final int sample = this.samples[this.sampleIndex];
                for (int j = 0; j < numChannels; j++) {
                    renderedSamples[renderedIndex++] = sample;
                }

                this.sampleIndex++;
                if (this.isFinished()) {
                    break;
                }
            }
        }

        while (renderedIndex < renderedSamplesLength) {
            renderedSamples[renderedIndex++] = 0;
        }
    }

    @Override
    public boolean isFinished() {
        return this.sampleIndex >= this.samples.length;
    }

    public int[] getSamples() {
        return this.samples;
    }

}
