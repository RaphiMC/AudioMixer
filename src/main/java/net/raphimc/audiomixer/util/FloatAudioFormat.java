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
package net.raphimc.audiomixer.util;

import javax.sound.sampled.AudioFormat;

public record FloatAudioFormat(float sampleRate, int channels) {

    public FloatAudioFormat {
        if (sampleRate <= 0) {
            throw new IllegalArgumentException("Sample rate must be > 0");
        }
        if (channels <= 0) {
            throw new IllegalArgumentException("Channel count must be > 0");
        }
    }

    public FloatAudioFormat(final AudioFormat audioFormat) {
        this(audioFormat.getSampleRate(), audioFormat.getChannels());
    }

    public FloatAudioFormat withSampleRate(final float sampleRate) {
        return new FloatAudioFormat(sampleRate, this.channels);
    }

    public FloatAudioFormat withChannels(final int channels) {
        return new FloatAudioFormat(this.sampleRate, channels);
    }

    public int millisToFrameCount(final float millis) {
        return (int) Math.ceil(this.sampleRate / 1000F * millis);
    }

    public float frameCountToMillis(final int frameCount) {
        return (frameCount / this.sampleRate) * 1000F;
    }

    public int millisToSampleCount(final float millis) {
        return this.millisToFrameCount(millis) * this.channels;
    }

    public float sampleCountToMillis(final int sampleCount) {
        return this.frameCountToMillis(this.sampleCountToFrameCount(sampleCount));
    }

    public int sampleCountToFrameCount(final int sampleCount) {
        if (sampleCount % this.channels != 0) {
            throw new IllegalArgumentException("Sample count must be a multiple of the channel count");
        }
        return sampleCount / this.channels;
    }

    public AudioFormat toJavaAudioFormat() {
        return new AudioFormat(AudioFormat.Encoding.PCM_FLOAT, this.sampleRate, Float.SIZE, this.channels, this.channels * Float.BYTES, this.sampleRate, true);
    }

    public AudioFormat toJavaPcmAudioFormat(final int sampleSizeInBits) {
        return new AudioFormat(this.sampleRate, sampleSizeInBits, this.channels, true, false);
    }

}
