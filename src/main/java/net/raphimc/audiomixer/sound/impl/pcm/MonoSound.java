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

import net.raphimc.audiomixer.oscillator.Oscillator;
import net.raphimc.audiomixer.pcmsource.MonoPcmSource;
import net.raphimc.audiomixer.sound.Sound;

import javax.sound.sampled.AudioFormat;
import java.util.Arrays;

public class MonoSound extends Sound {

    private final MonoPcmSource pcmSource;
    private float pitch;
    private Oscillator pitchOscillator;

    public MonoSound(final MonoPcmSource pcmSource) {
        this(pcmSource, 1F);
    }

    public MonoSound(final MonoPcmSource pcmSource, final float pitch) {
        this.pcmSource = pcmSource;
        this.setPitch(pitch);
    }

    @Override
    public void render(final AudioFormat audioFormat, final int[] renderedSamples) {
        int renderedIndex = 0;
        if (this.pitch == 1F && audioFormat.getChannels() == 1 && this.pitchOscillator == null) {
            renderedIndex += this.pcmSource.consumeSamples(renderedSamples);
        } else {
            final int numChannels = audioFormat.getChannels();
            final int numSamples = renderedSamples.length / numChannels;
            final boolean hasPitchOscillator = this.pitchOscillator != null;

            for (int i = 0; i < numSamples && !this.isFinished(); i++) {
                final int sample = this.pcmSource.consumeSample(!hasPitchOscillator ? this.pitch : this.pitchOscillator.modifyValue(this.pitch, audioFormat.getSampleRate()));
                for (int j = 0; j < numChannels; j++) {
                    renderedSamples[renderedIndex++] = sample;
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

    public MonoPcmSource getPcmSource() {
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

    public Oscillator getPitchOscillator() {
        return this.pitchOscillator;
    }

    public void setPitchOscillator(final Oscillator pitchOscillator) {
        this.pitchOscillator = pitchOscillator;
    }

}
