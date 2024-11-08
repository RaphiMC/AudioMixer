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

public class DynamicMonoSound implements Sound {

    private final PcmSource pcmSource;
    private float pitch;

    @Deprecated(forRemoval = true)
    public DynamicMonoSound(final int[] samples, final float pitch) {
        this(new IntPcmSource(samples), pitch);
    }

    public DynamicMonoSound(final PcmSource pcmSource, final float pitch) {
        this.pcmSource = pcmSource;
        this.setPitch(pitch);
    }

    @Override
    public void render(final AudioFormat audioFormat, final int[] renderedSamples) {
        final int numChannels = audioFormat.getChannels();
        final int numSamples = renderedSamples.length / numChannels;

        int renderedIndex = 0;
        for (int i = 0; i < numSamples && !this.isFinished(); i++) {
            final int sample = this.pcmSource.getCurrentSample();
            for (int j = 0; j < numChannels; j++) {
                renderedSamples[renderedIndex++] = sample;
            }

            this.pcmSource.incrementPosition(this.pitch);
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

    public float getPitch() {
        return this.pitch;
    }

    public void setPitch(final float pitch) {
        if (pitch <= 0) {
            throw new IllegalArgumentException("Pitch must be greater than 0");
        }

        this.pitch = pitch;
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
