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

import net.raphimc.audiomixer.parameter.FloatParameter;
import net.raphimc.audiomixer.resampler.Resampler;
import net.raphimc.audiomixer.resampler.impl.LinearResampler;
import net.raphimc.audiomixer.source.FiniteSource;
import net.raphimc.audiomixer.util.AudioFormat;
import net.raphimc.audiomixer.util.buffer.AudioBuffer;

public abstract class AudioSource extends FiniteSource {

    private final AudioFormat originalFormat;
    protected final Resampler resampler;
    protected AudioBuffer buffer;
    private final FloatParameter sampleRate;
    private final FloatParameter pitch;
    protected double position;

    public AudioSource(final AudioBuffer buffer) {
        this(buffer, new LinearResampler());
    }

    public AudioSource(final AudioBuffer buffer, final Resampler resampler) {
        this.originalFormat = buffer.format();
        this.resampler = resampler;
        this.buffer = buffer;
        this.sampleRate = FloatParameter.of(this::getSampleRate, this::setSampleRate).withConstraint(FloatParameter.Constraint.GREATER_THAN_ZERO);
        this.pitch = this.sampleRate.withMapping(value -> value / this.originalFormat.sampleRate(), value -> value * this.originalFormat.sampleRate());
    }

    @Override
    protected void renderInternal(final AudioBuffer buffer) {
        this.position = Math.min(this.resampler.resample(this.buffer, buffer, this.position), this.buffer.frameCount());
    }

    @Override
    public boolean isFinished() {
        return this.position >= this.buffer.frameCount();
    }

    public AudioFormat getFormat() {
        return this.buffer.format();
    }

    public int getRemainingFrameCount() {
        return this.buffer.frameCount() - (int) this.position;
    }

    public float getRemainingMillisecondLength() {
        return this.getFormat().frameCountToMillis(this.getRemainingFrameCount());
    }

    public FloatParameter sampleRate() {
        return this.sampleRate;
    }

    public FloatParameter pitch() {
        return this.pitch;
    }

    protected float getSampleRate() {
        return this.getFormat().sampleRate();
    }

    protected void setSampleRate(final float sampleRate) {
        this.buffer = new AudioBuffer(this.originalFormat.withSampleRate(sampleRate), this.buffer.samples());
    }

}
