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
package net.raphimc.audiomixer.util.buffer;

import net.raphimc.audiomixer.util.FloatAudioFormat;
import net.raphimc.audiomixer.util.math.MathUtil;

import java.util.Arrays;

public record AudioBuffer(FloatAudioFormat format, float[] samples) {

    public AudioBuffer {
        if (samples.length % format.channels() != 0) {
            throw new IllegalArgumentException("Sample count must be a multiple of the channel count");
        }
    }

    public AudioBuffer(final FloatAudioFormat format, final int sampleCount) {
        this(format, new float[sampleCount]);
    }

    public void mix(final AudioBuffer other) {
        if (!other.format().equals(this.format)) {
            throw new IllegalArgumentException("Format mismatch: " + other.format() + " != " + this.format);
        }
        final float[] otherSamples = other.samples();
        if (otherSamples.length != this.samples.length) {
            throw new IllegalArgumentException("Sample count mismatch: " + otherSamples.length + " != " + this.samples.length);
        }
        for (int sampleIndex = 0; sampleIndex < otherSamples.length; sampleIndex++) {
            this.samples[sampleIndex] += otherSamples[sampleIndex];
        }
    }

    public void multiply(final float factor) {
        if (factor == 0F) {
            this.clear();
        } else if (factor != 1F) {
            for (int sampleIndex = 0; sampleIndex < this.samples.length; sampleIndex++) {
                this.samples[sampleIndex] *= factor;
            }
        }
    }

    public void limitToUnitRange() {
        final float peak = this.peakAmplitude();
        if (peak > 1F) {
            this.multiply(1F / peak);
        }
    }

    public void normalizePeak() {
        final float peak = this.peakAmplitude();
        if (peak != 0F) {
            this.multiply(1F / peak);
        }
    }

    public void clear() {
        Arrays.fill(this.samples, 0F);
    }

    public float peakAmplitude() {
        float peak = 0F;
        for (float sample : this.samples) {
            peak = Math.max(Math.abs(sample), peak);
        }
        return peak;
    }

    public int getSampleCount() {
        return this.samples.length;
    }

    public int getFrameCount() {
        return this.format.sampleCountToFrameCount(this.getSampleCount());
    }

    public float getMillisecondLength() {
        return this.format.sampleCountToMillis(this.getSampleCount());
    }

    public AudioBuffer append(final AudioBuffer other) {
        if (other.format().channels() != this.format.channels()) {
            throw new IllegalArgumentException("Channel count mismatch: " + other.format().channels() + " != " + this.format.channels());
        }
        final float[] newSamples = new float[this.samples.length + other.samples().length];
        System.arraycopy(this.samples, 0, newSamples, 0, this.samples.length);
        System.arraycopy(other.samples(), 0, newSamples, this.samples.length, other.samples().length);
        return new AudioBuffer(this.format, newSamples);
    }

    public AudioBuffer slice(final int from, final int to) {
        if (from != 0 || to != this.samples.length) {
            return new AudioBuffer(this.format, Arrays.copyOfRange(this.samples, from, to));
        } else {
            return this;
        }
    }

    public AudioBuffer trimLeadingSilence() {
        int i = 0;
        while (i < this.samples.length && this.samples[i] == 0) {
            i++;
        }
        i = MathUtil.roundDownToMultiple(i, this.format.channels());
        return this.slice(i, this.samples.length);
    }

    public AudioBuffer trimTrailingSilence() {
        int i = this.samples.length - 1;
        while (i >= 0 && this.samples[i] == 0) {
            i--;
        }
        i = MathUtil.roundUpToMultiple(i + 1, this.format.channels()) - 1;
        return this.slice(0, i + 1);
    }

}
