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
package net.raphimc.audiomixer.sound.impl.pcm;

import net.raphimc.audiomixer.modulator.Modulator;
import net.raphimc.audiomixer.pcmsource.StereoPcmSource;
import net.raphimc.audiomixer.sound.Sound;
import net.raphimc.audiomixer.util.ArrayUtil;

import javax.sound.sampled.AudioFormat;
import java.util.Arrays;

public class StereoSound extends Sound {

    private final StereoPcmSource pcmSource;
    private float pitch;
    private Modulator pitchModulator;

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
        if (this.pitch == 1F && audioFormat.getChannels() == 2 && this.pitchModulator == null) {
            renderedIndex += this.pcmSource.consumeSamples(renderedSamples);
        } else {
            final int numChannels = audioFormat.getChannels();
            final int numSamples = renderedSamples.length / numChannels;
            final boolean hasPitchModulator = this.pitchModulator != null;

            for (int i = 0; i < numSamples && !this.isFinished(); i++) {
                final int[] sample = this.pcmSource.consumeSample(!hasPitchModulator ? this.pitch : this.pitchModulator.modifyValue(this.pitch, audioFormat.getSampleRate()));
                if (numChannels == 2) {
                    renderedSamples[renderedIndex++] = sample[0];
                    renderedSamples[renderedIndex++] = sample[1];
                } else {
                    final int monoSample = (sample[0] + sample[1]) / 2;
                    ArrayUtil.fillFast(renderedSamples, renderedIndex, numChannels, monoSample);
                    renderedIndex += numChannels;
                }
            }
        }
        Arrays.fill(renderedSamples, renderedIndex, renderedSamples.length, 0);

        this.soundModifiers.modify(audioFormat, renderedSamples);
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

    public Modulator getPitchModulator() {
        return this.pitchModulator;
    }

    public void setPitchModulator(final Modulator pitchModulator) {
        this.pitchModulator = pitchModulator;
    }

}
