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

import net.raphimc.audiomixer.util.AudioFormat;
import net.raphimc.audiomixer.util.math.MathUtil;

import java.util.Arrays;

public record AudioBuffer(AudioFormat format, float[] samples) {

    public AudioBuffer {
        if (samples.length % format.channelCount() != 0) {
            throw new IllegalArgumentException("Sample count must be a multiple of the channel count");
        }
    }

    public AudioBuffer(final AudioFormat format, final int frameCount) {
        this(format, new float[frameCount * format.channelCount()]);
    }

    public void add(final AudioBuffer other) {
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

    public AudioBuffer copy() {
        return new AudioBuffer(this.format, Arrays.copyOf(this.samples, this.samples.length));
    }

    public AudioBuffer createWorkBuffer() {
        return new AudioBuffer(this.format, this.frameCount());
    }

    public float peakAmplitude() {
        float peak = 0F;
        for (float sample : this.samples) {
            peak = Math.max(Math.abs(sample), peak);
        }
        return peak;
    }

    public int sampleCount() {
        return this.samples.length;
    }

    public int frameCount() {
        return this.format.sampleCountToFrameCount(this.sampleCount());
    }

    public float millisecondLength() {
        return this.format.sampleCountToMillis(this.sampleCount());
    }

    public AudioBuffer append(final AudioBuffer other) {
        if (other.format().channelCount() != this.format.channelCount()) {
            throw new IllegalArgumentException("Channel count mismatch: " + other.format().channelCount() + " != " + this.format.channelCount());
        }
        final float[] newSamples = new float[this.samples.length + other.samples().length];
        System.arraycopy(this.samples, 0, newSamples, 0, this.samples.length);
        System.arraycopy(other.samples(), 0, newSamples, this.samples.length, other.samples().length);
        return new AudioBuffer(this.format, newSamples);
    }

    public AudioBuffer slice(final int from, final int to) {
        if (from != 0 || to != this.frameCount()) {
            return new AudioBuffer(this.format, Arrays.copyOfRange(this.samples, from * this.format.channelCount(), to * this.format.channelCount()));
        } else {
            return this;
        }
    }

    public AudioBuffer trimLeadingSilence() {
        int i = 0;
        while (i < this.samples.length && this.samples[i] == 0) {
            i++;
        }
        i = MathUtil.roundDownToMultiple(i, this.format.channelCount());
        return this.slice(i / this.format.channelCount(), this.frameCount());
    }

    public AudioBuffer trimTrailingSilence() {
        int i = this.samples.length - 1;
        while (i >= 0 && this.samples[i] == 0) {
            i--;
        }
        i = MathUtil.roundUpToMultiple(i + 1, this.format.channelCount());
        return this.slice(0, i / this.format.channelCount());
    }

}
