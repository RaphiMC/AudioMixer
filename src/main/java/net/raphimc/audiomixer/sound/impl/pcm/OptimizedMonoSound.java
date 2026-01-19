/*
 * This file is part of AudioMixer - https://github.com/RaphiMC/AudioMixer
 * Copyright (C) 2024-2026 RK_01/RaphiMC and contributors
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
        this(pcmSource, 1F);
    }

    public OptimizedMonoSound(final MonoPcmSource pcmSource, final float pitch) {
        this(pcmSource, pitch, 1F);
    }

    public OptimizedMonoSound(final MonoPcmSource pcmSource, final float pitch, final float volume) {
        this(pcmSource, pitch, volume, 0F);
    }

    public OptimizedMonoSound(final MonoPcmSource pcmSource, final float pitch, final float volume, final float panning) {
        this.pcmSource = pcmSource;
        this.setPitch(pitch);
        this.setVolume(volume);
        this.setPanning(panning);
    }

    @Override
    public void render(final PcmFloatAudioFormat audioFormat, final float[] renderedSamples) {
        final int channelCount = audioFormat.getChannels();
        final int frameCount = renderedSamples.length / channelCount;

        final float leftVolume = channelCount == 2 ? (1F - this.panning) * this.volume : 0F;
        final float rightVolume = channelCount == 2 ? this.panning * this.volume : 0F;

        int renderedIndex = 0;
        for (int i = 0; i < frameCount && !this.isFinished(); i++) {
            final float sample = this.pcmSource.consumeFrame(this.pitch);
            if (channelCount == 2) {
                renderedSamples[renderedIndex++] = sample * leftVolume;
                renderedSamples[renderedIndex++] = sample * rightVolume;
            } else {
                ArrayUtil.fillFast(renderedSamples, renderedIndex, channelCount, sample * this.volume);
                renderedIndex += channelCount;
            }
        }
        Arrays.fill(renderedSamples, renderedIndex, renderedSamples.length, 0F);

        this.getSoundModifiers().modify(audioFormat, renderedSamples);
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
        if (pitch < 0) {
            throw new IllegalArgumentException("Pitch must be greater than or equal to 0");
        }
        this.pitch = pitch;
        return this;
    }

    public float getVolume() {
        return this.volume;
    }

    public OptimizedMonoSound setVolume(final float volume) {
        if (volume < 0F || volume > 1F) {
            throw new IllegalArgumentException("Volume must be between 0 and 1");
        }
        this.volume = volume;
        return this;
    }

    public float getPanning() {
        return this.panning * 2F - 1F;
    }

    public OptimizedMonoSound setPanning(final float panning) {
        if (panning < -1F || panning > 1F) {
            throw new IllegalArgumentException("Panning must be between -1 and 1");
        }
        this.panning = (panning + 1F) / 2F;
        return this;
    }

}
