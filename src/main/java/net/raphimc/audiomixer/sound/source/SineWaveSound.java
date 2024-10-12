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

public class SineWaveSound implements Sound {

    private int frequency;
    private float volume;
    private double angle;

    public SineWaveSound(final int frequency, final float volume) {
        this.setFrequency(frequency);
        this.setVolume(volume);
    }

    @Override
    public void render(final AudioFormat audioFormat, final int[] renderedSamples, final int renderedSamplesLength) {
        final int maxValue = (int) Math.pow(2, audioFormat.getSampleSizeInBits() - 1) - 1;
        final int numChannels = audioFormat.getChannels();
        final double angularVelocity = 2 * Math.PI * this.frequency / audioFormat.getSampleRate();

        for (int i = 0; i < renderedSamplesLength; i += numChannels) {
            final int sample = (int) (Math.sin(this.angle) * this.volume * maxValue);
            for (int channel = 0; channel < numChannels; channel++) {
                renderedSamples[i + channel] = sample;
            }

            this.angle += angularVelocity;
            if (this.angle > 2 * Math.PI) {
                this.angle -= 2 * Math.PI;
            }
        }
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    public int getFrequency() {
        return this.frequency;
    }

    public void setFrequency(final int frequency) {
        if (frequency <= 0) {
            throw new IllegalArgumentException("Frequency must be greater than 0");
        }

        this.frequency = frequency;
    }

    public float getVolume() {
        return this.volume;
    }

    public void setVolume(final float volume) {
        this.volume = Math.max(0, volume);
    }

}
