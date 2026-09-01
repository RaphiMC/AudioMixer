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

import net.raphimc.audiomixer.util.math.MathUtil;

public record AudioFormat(float sampleRate, int channels) {

    public AudioFormat {
        if (!Float.isFinite(sampleRate) || sampleRate <= 0) {
            throw new IllegalArgumentException("Sample rate must be finite and > 0");
        }
        if (channels <= 0) {
            throw new IllegalArgumentException("Channel count must be > 0");
        }
    }

    public AudioFormat withSampleRate(final float sampleRate) {
        return new AudioFormat(sampleRate, this.channels);
    }

    public AudioFormat withChannels(final int channels) {
        return new AudioFormat(this.sampleRate, channels);
    }

    public int millisToFrameCount(final float millis) {
        return MathUtil.ceil(this.sampleRate / 1000F * millis);
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

}
