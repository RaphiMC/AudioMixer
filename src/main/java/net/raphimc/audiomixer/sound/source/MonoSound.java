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
import net.raphimc.audiomixer.util.InterpolationUtil;

import javax.sound.sampled.AudioFormat;

public class MonoSound implements Sound {

    private final int[] samples;
    private float pitch;
    private float volume;
    private float panning;
    private double sampleIndex;

    public MonoSound(final int[] samples) {
        this(samples, 1F, 1F, 0F);
    }

    public MonoSound(final int[] samples, final float pitch, final float volume, final float panning) {
        if (samples == null || samples.length == 0) {
            throw new IllegalArgumentException("Samples must not be null or empty");
        }

        this.samples = samples;
        this.setPitch(pitch);
        this.setVolume(volume);
        this.setPanning(panning);
    }

    @Override
    public void render(final AudioFormat audioFormat, final int[] renderedSamples, final int renderedSamplesLength) {
        final int numChannels = audioFormat.getChannels();
        final int numSamples = renderedSamplesLength / numChannels;

        int renderedIndex = 0;
        for (int i = 0; i < numSamples; i++) {
            final int sample = InterpolationUtil.interpolateLinear(this.samples, this.sampleIndex);

            if (numChannels == 2) {
                renderedSamples[renderedIndex++] = (int) (sample * (1F - this.panning) * this.volume);
                renderedSamples[renderedIndex++] = (int) (sample * this.panning * this.volume);
            } else {
                for (int j = 0; j < numChannels; j++) {
                    renderedSamples[renderedIndex++] = (int) (sample * this.volume);
                }
            }

            this.sampleIndex += this.pitch;
            if (this.isFinished()) {
                break;
            }
        }

        while (renderedIndex < renderedSamplesLength) {
            renderedSamples[renderedIndex++] = 0;
        }
    }

    @Override
    public boolean isFinished() {
        return (int) this.sampleIndex >= this.samples.length;
    }

    public int[] getSamples() {
        return this.samples;
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

    public float getVolume() {
        return this.volume;
    }

    public void setVolume(final float volume) {
        this.volume = Math.max(0, volume);
    }

    public float getPanning() {
        return this.panning * 2F - 1F;
    }

    public void setPanning(final float panning) {
        this.panning = (Math.max(-1F, Math.min(1F, panning)) + 1) / 2F;
    }

}
