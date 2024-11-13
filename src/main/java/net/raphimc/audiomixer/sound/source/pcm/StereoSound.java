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
package net.raphimc.audiomixer.sound.source.pcm;

import net.raphimc.audiomixer.pcmsource.StereoPcmSource;
import net.raphimc.audiomixer.sound.Sound;

import javax.sound.sampled.AudioFormat;

public class StereoSound implements Sound {

    private final StereoPcmSource pcmSource;
    private float pitch;

    public StereoSound(final StereoPcmSource pcmSource) {
        this(pcmSource, 1F);
    }

    public StereoSound(final StereoPcmSource pcmSource, final float pitch) {
        this.pcmSource = pcmSource;
        this.setPitch(pitch);
    }

    @Override
    public void render(final AudioFormat audioFormat, final int[] renderedSamples) {
        int renderedIndex = 0;
        if (this.pitch == 1F && audioFormat.getChannels() == 2) {
            renderedIndex += this.pcmSource.consumeSamples(renderedSamples);
        } else {
            final int numChannels = audioFormat.getChannels();
            final int numSamples = renderedSamples.length / numChannels;

            if (numChannels == 2) {
                for (int i = 0; i < numSamples && !this.isFinished(); i++) {
                    final int[] sample = this.pcmSource.consumeSample(this.pitch);
                    renderedSamples[renderedIndex++] = sample[0];
                    renderedSamples[renderedIndex++] = sample[1];
                }
            } else {
                for (int i = 0; i < numSamples && !this.isFinished(); i++) {
                    final int[] sample = this.pcmSource.consumeSample(this.pitch);
                    final int monoSample = (sample[0] + sample[1]) / 2;
                    for (int j = 0; j < numChannels; j++) {
                        renderedSamples[renderedIndex++] = monoSample;
                    }
                }
            }
        }

        while (renderedIndex < renderedSamples.length) {
            renderedSamples[renderedIndex++] = 0;
        }
    }

    @Override
    public boolean isFinished() {
        return this.pcmSource.hasReachedEnd();
    }

    public StereoPcmSource getPcmSource() {
        return this.pcmSource;
    }

    public float getPitch() {
        return this.pitch;
    }

    public void setPitch(final float pitch) {
        if (pitch <= 0) {
            throw new IllegalArgumentException("Pitch must be greater than 0");
        }

        this.pitch = pitch;
    }

}
