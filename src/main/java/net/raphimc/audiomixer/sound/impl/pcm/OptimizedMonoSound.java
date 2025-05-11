/*
 * This file is part of AudioMixer - https://github.com/RaphiMC/AudioMixer
 * Copyright (C) 2024-2025 RK_01/RaphiMC and contributors
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

import net.raphimc.audiomixer.pcmsource.MonoPcmSource;
import net.raphimc.audiomixer.sound.Sound;
import net.raphimc.audiomixer.util.ArrayUtil;
import net.raphimc.audiomixer.util.PcmFloatAudioFormat;

import java.util.Arrays;

public class OptimizedMonoSound extends Sound {

    private final MonoPcmSource pcmSource;
    private float pitch;
    private float volume;
    private float panning;

    public OptimizedMonoSound(final MonoPcmSource pcmSource) {
        this(pcmSource, 1F, 1F, 0F);
    }

    public OptimizedMonoSound(final MonoPcmSource pcmSource, final float pitch, final float volume, final float panning) {
        this.pcmSource = pcmSource;
        this.setPitch(pitch);
        this.setVolume(volume);
        this.setPanning(panning);
    }

    @Override
    public void render(final PcmFloatAudioFormat audioFormat, final float[] renderedSamples) {
        final int numChannels = audioFormat.getChannels();
        final int numSamples = renderedSamples.length / numChannels;

        final float leftVolume = numChannels == 2 ? (1F - this.panning) * this.volume : 0F;
        final float rightVolume = numChannels == 2 ? this.panning * this.volume : 0F;

        int renderedIndex = 0;
        for (int i = 0; i < numSamples && !this.isFinished(); i++) {
            final float sample = this.pcmSource.consumeSample(this.pitch);
            if (numChannels == 2) {
                renderedSamples[renderedIndex++] = sample * leftVolume;
                renderedSamples[renderedIndex++] = sample * rightVolume;
            } else {
                ArrayUtil.fillFast(renderedSamples, renderedIndex, numChannels, sample * this.volume);
                renderedIndex += numChannels;
            }
        }
        Arrays.fill(renderedSamples, renderedIndex, renderedSamples.length, 0F);

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

    public OptimizedMonoSound setPitch(final float pitch) {
        if (pitch <= 0) {
            throw new IllegalArgumentException("Pitch must be greater than 0");
        }

        this.pitch = pitch;
        return this;
    }

    public float getVolume() {
        return this.volume;
    }

    public OptimizedMonoSound setVolume(final float volume) {
        this.volume = Math.max(0, volume);
        return this;
    }

    public float getPanning() {
        return this.panning * 2F - 1F;
    }

    public OptimizedMonoSound setPanning(final float panning) {
        this.panning = (Math.max(-1F, Math.min(1F, panning)) + 1) / 2F;
        return this;
    }

}
