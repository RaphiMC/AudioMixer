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
package net.raphimc.audiomixer.source.audio;

import net.raphimc.audiomixer.dsp.resampler.Resampler;
import net.raphimc.audiomixer.dsp.resampler.impl.LinearResampler;
import net.raphimc.audiomixer.source.Source;
import net.raphimc.audiomixer.util.FloatAudioFormat;
import net.raphimc.audiomixer.util.buffer.AudioBuffer;

public abstract class AudioSource extends Source {

    private final FloatAudioFormat originalFormat;
    private final Resampler resampler;
    protected AudioBuffer buffer;
    protected double position;

    public AudioSource(final AudioBuffer buffer) {
        this(buffer, LinearResampler.INSTANCE);
    }

    public AudioSource(final AudioBuffer buffer, final Resampler resampler) {
        this.originalFormat = buffer.format();
        this.resampler = resampler;
        this.buffer = buffer;
    }

    @Override
    protected void renderDry(final AudioBuffer buffer) {
        if (this.getFormat().equals(buffer.format()) && this.position % 1 == 0) {
            final int offset = (int) this.position * this.getFormat().channels();
            final int count = Math.min(buffer.getSampleCount(), this.buffer.getSampleCount() - offset);
            System.arraycopy(this.buffer.samples(), offset, buffer.samples(), 0, count);
            this.position += this.getFormat().sampleCountToFrameCount(count);
        } else {
            this.position = Math.min(this.resampler.resample(this.buffer, buffer, this.position), this.buffer.getFrameCount());
        }
    }

    public FloatAudioFormat getFormat() {
        return this.buffer.format();
    }

    public int getRemainingFrameCount() {
        return this.buffer.getFrameCount() - (int) this.position;
    }

    public float getRemainingMillisecondLength() {
        return this.getFormat().frameCountToMillis(this.getRemainingFrameCount());
    }

    public float getSampleRate() {
        return this.getFormat().sampleRate();
    }

    public void setSampleRate(final float sampleRate) {
        if (sampleRate <= 0) {
            throw new IllegalArgumentException("Sample rate must be > 0");
        }
        this.buffer = new AudioBuffer(this.originalFormat.withSampleRate(sampleRate), this.buffer.samples());
    }

    public float getPitch() {
        return this.getSampleRate() / this.originalFormat.sampleRate();
    }

    public void setPitch(final float pitch) {
        if (pitch <= 0) {
            throw new IllegalArgumentException("Pitch must be > 0");
        }
        this.setSampleRate(this.originalFormat.sampleRate() * pitch);
    }

}
