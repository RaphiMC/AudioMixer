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

public class StaticStereoSound implements Sound {

    private final int[] samples;
    private int sampleIndex;

    public StaticStereoSound(final int[] samples) {
        if (samples == null || samples.length == 0) {
            throw new IllegalArgumentException("Samples must not be null or empty");
        }

        this.samples = samples;
    }

    @Override
    public void render(final AudioFormat audioFormat, final int[] renderedSamples) {
        int renderedIndex = 0;
        if (audioFormat.getChannels() == 2) {
            final int numSamples = Math.min(renderedSamples.length, this.samples.length - this.sampleIndex);
            System.arraycopy(this.samples, this.sampleIndex, renderedSamples, 0, numSamples);
            this.sampleIndex += numSamples;
            renderedIndex += numSamples;
        } else {
            final int numChannels = audioFormat.getChannels();
            final int numSamples = renderedSamples.length / numChannels;

            for (int i = 0; i < numSamples && !this.isFinished(); i++) {
                final int sample1 = this.samples[this.sampleIndex];
                final int sample2 = this.samples[this.sampleIndex + 1];
                final int monoSample = (sample1 + sample2) / 2;
                for (int j = 0; j < numChannels; j++) {
                    renderedSamples[renderedIndex++] = monoSample;
                }

                this.sampleIndex += 2;
            }
        }

        while (renderedIndex < renderedSamples.length) {
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
