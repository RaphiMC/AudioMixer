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

import net.raphimc.audiomixer.sound.PcmSource;
import net.raphimc.audiomixer.sound.Sound;
import net.raphimc.audiomixer.sound.pcmsource.IntPcmSource;

import javax.sound.sampled.AudioFormat;

public class StaticMonoSound implements Sound {

    private final PcmSource pcmSource;

    @Deprecated(forRemoval = true)
    public StaticMonoSound(final int[] samples) {
        this(new IntPcmSource(samples));
    }

    public StaticMonoSound(final PcmSource pcmSource) {
        this.pcmSource = pcmSource;
    }

    @Override
    public void render(final AudioFormat audioFormat, final int[] renderedSamples) {
        int renderedIndex = 0;
        if (audioFormat.getChannels() == 1) {
            renderedIndex += this.pcmSource.consumeSamples(renderedSamples);
        } else {
            final int numChannels = audioFormat.getChannels();
            final int numSamples = renderedSamples.length / numChannels;

            for (int i = 0; i < numSamples && !this.isFinished(); i++) {
                final int sample = this.pcmSource.getCurrentSample();
                for (int j = 0; j < numChannels; j++) {
                    renderedSamples[renderedIndex++] = sample;
                }

                this.pcmSource.incrementPosition(1);
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

    public PcmSource getPcmSource() {
        return this.pcmSource;
    }

    @Deprecated(forRemoval = true)
    public float getProgress() {
        return this.pcmSource.getProgress();
    }

    @Deprecated(forRemoval = true)
    public void setProgress(final float progress) {
        this.pcmSource.setProgress(progress);
    }

}
