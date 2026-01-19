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

import net.raphimc.audiomixer.pcmsource.StereoPcmSource;
import net.raphimc.audiomixer.sound.Sound;
import net.raphimc.audiomixer.util.ArrayUtil;
import net.raphimc.audiomixer.util.PcmFloatAudioFormat;
import net.raphimc.audiomixer.valuemodifier.ValueModifier;

import java.util.Arrays;

public class StereoSound extends Sound {

    private final StereoPcmSource pcmSource;
    private float pitch;
    private ValueModifier pitchModifier;

    public StereoSound(final StereoPcmSource pcmSource) {
        this(pcmSource, 1F);
    }

    public StereoSound(final StereoPcmSource pcmSource, final float pitch) {
        this.pcmSource = pcmSource;
        this.setPitch(pitch);
    }

    @Override
    public void render(final PcmFloatAudioFormat audioFormat, final float[] renderedSamples) {
        int renderedIndex = 0;
        if (this.pitch == 1F && audioFormat.getChannels() == 2 && this.pitchModifier == null) {
            renderedIndex += this.pcmSource.consumeSamples(renderedSamples);
        } else {
            final int channelCount = audioFormat.getChannels();
            final int frameCount = renderedSamples.length / channelCount;
            final boolean hasPitchModifier = this.pitchModifier != null;

            for (int i = 0; i < frameCount && !this.isFinished(); i++) {
                final float pitch;
                if (!hasPitchModifier) {
                    pitch = this.pitch;
                } else {
                    pitch = Math.max(this.pitchModifier.modify(this.pitch, audioFormat.getSampleRate()), 0F);
                }

                final float[] frame = this.pcmSource.consumeFrame(pitch);
                if (channelCount == 2) {
                    renderedSamples[renderedIndex++] = frame[0];
                    renderedSamples[renderedIndex++] = frame[1];
                } else {
                    final float monoSample = (frame[0] + frame[1]) / 2F;
                    ArrayUtil.fillFast(renderedSamples, renderedIndex, channelCount, monoSample);
                    renderedIndex += channelCount;
                }
            }
        }
        Arrays.fill(renderedSamples, renderedIndex, renderedSamples.length, 0F);

        this.getSoundModifiers().modify(audioFormat, renderedSamples);
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

    public StereoSound setPitch(final float pitch) {
        if (pitch < 0) {
            throw new IllegalArgumentException("Pitch must be greater than or equal to 0");
        }
        this.pitch = pitch;
        return this;
    }

    public ValueModifier getPitchModifier() {
        return this.pitchModifier;
    }

    public StereoSound setPitchModifier(final ValueModifier pitchModifier) {
        this.pitchModifier = pitchModifier;
        return this;
    }

}
