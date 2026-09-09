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
package net.raphimc.audiomixer.io.special;

import net.raphimc.audiomixer.io.AudioOutputStream;
import net.raphimc.audiomixer.resampler.Resampler;
import net.raphimc.audiomixer.resampler.impl.LinearResampler;
import net.raphimc.audiomixer.util.AudioFormat;

import java.io.IOException;
import java.util.Arrays;

public class ResamplingAudioOutputStream extends AudioOutputStream {

    private static final float DEFAULT_BUFFER_MILLIS = 10F;

    private final AudioOutputStream delegate;
    private final Resampler resampler;
    private final float[] inputSamples;
    private double inputFramePosition;
    private int inputSampleCount;
    private final float[] outputSamples;

    public ResamplingAudioOutputStream(final AudioOutputStream delegate, final AudioFormat sourceFormat) {
        this(delegate, sourceFormat, DEFAULT_BUFFER_MILLIS);
    }

    public ResamplingAudioOutputStream(final AudioOutputStream delegate, final AudioFormat sourceFormat, final Resampler resampler) {
        this(delegate, sourceFormat, DEFAULT_BUFFER_MILLIS, resampler);
    }

    public ResamplingAudioOutputStream(final AudioOutputStream delegate, final AudioFormat sourceFormat, final float bufferMillis) {
        this(delegate, sourceFormat, bufferMillis, new LinearResampler());
    }

    public ResamplingAudioOutputStream(final AudioOutputStream delegate, final AudioFormat sourceFormat, final float bufferMillis, final Resampler resampler) {
        super(sourceFormat);
        if (!Float.isFinite(bufferMillis) || bufferMillis <= 0F) {
            throw new IllegalArgumentException("Buffer millis must be finite and > 0: " + bufferMillis);
        }
        this.delegate = delegate;
        this.resampler = resampler;

        final int outputFrameCount = this.delegate.getFormat().millisToFrameCount(bufferMillis);
        final int requiredInputFrameCount = Resampler.computeMaxRequiredInputFrameCount(this.getFormat(), this.delegate.getFormat(), Math.addExact(outputFrameCount, 1));
        final int inputFrameCount = Math.addExact(Math.addExact(this.resampler.getLookBehindFrameCount(), requiredInputFrameCount), this.resampler.getLookAheadFrameCount());
        this.inputSamples = new float[Math.multiplyExact(inputFrameCount, this.getFormat().channelCount())];
        this.outputSamples = new float[Math.multiplyExact(outputFrameCount, this.delegate.getFormat().channelCount())];
    }

    @Override
    public void write(final float sample) throws IOException {
        while (this.inputSampleCount == this.inputSamples.length) {
            this.resampleNextBuffer();
        }
        this.inputSamples[this.inputSampleCount++] = sample;
    }

    private void resampleNextBuffer() throws IOException {
        final float[] inputSamples = this.inputSampleCount == this.inputSamples.length ? this.inputSamples : Arrays.copyOf(this.inputSamples, this.inputSampleCount);
        final double inputFramePosition = this.resampler.resample(inputSamples, this.getFormat(), this.outputSamples, this.delegate.getFormat(), this.inputFramePosition);
        this.delegate.write(this.outputSamples, 0, Math.multiplyExact(this.resampler.getLastOutputFrameCount(), this.delegate.getFormat().channelCount()));
        this.inputFramePosition = inputFramePosition;
        this.discardConsumedInput();
    }

    private void discardConsumedInput() {
        final int discardFrameCount = Math.min(Math.max((int) this.inputFramePosition - this.resampler.getLookBehindFrameCount(), 0), this.getFormat().sampleCountToFrameCount(this.inputSampleCount));
        final int discardSampleCount = Math.multiplyExact(discardFrameCount, this.getFormat().channelCount());
        this.inputSampleCount -= discardSampleCount;
        System.arraycopy(this.inputSamples, discardSampleCount, this.inputSamples, 0, this.inputSampleCount);
        this.inputFramePosition -= discardFrameCount;
    }

    @Override
    public void flush() throws IOException {
        this.delegate.flush();
    }

    @Override
    public void close() throws IOException {
        try (this.delegate) {
            while (this.inputFramePosition < this.getFormat().sampleCountToFrameCount(this.inputSampleCount)) {
                this.resampleNextBuffer();
            }
        }
    }

    public AudioOutputStream getDelegate() {
        return this.delegate;
    }

}
