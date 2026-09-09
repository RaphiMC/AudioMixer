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

import net.raphimc.audiomixer.io.AudioInputStream;
import net.raphimc.audiomixer.resampler.Resampler;
import net.raphimc.audiomixer.resampler.impl.LinearResampler;
import net.raphimc.audiomixer.util.AudioFormat;

import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;

public class ResamplingAudioInputStream extends AudioInputStream {

    private static final float DEFAULT_BUFFER_MILLIS = 10F;

    private final AudioInputStream delegate;
    private final Resampler resampler;
    private final float[] inputSamples;
    private double inputFramePosition;
    private int inputSampleCount;
    private final float[] outputSamples;
    private int outputSampleIndex;
    private int outputSampleCount;

    public ResamplingAudioInputStream(final AudioInputStream delegate, final AudioFormat targetFormat) {
        this(delegate, targetFormat, DEFAULT_BUFFER_MILLIS);
    }

    public ResamplingAudioInputStream(final AudioInputStream delegate, final AudioFormat targetFormat, final Resampler resampler) {
        this(delegate, targetFormat, DEFAULT_BUFFER_MILLIS, resampler);
    }

    public ResamplingAudioInputStream(final AudioInputStream delegate, final AudioFormat targetFormat, final float bufferMillis) {
        this(delegate, targetFormat, bufferMillis, new LinearResampler());
    }

    public ResamplingAudioInputStream(final AudioInputStream delegate, final AudioFormat targetFormat, final float bufferMillis, final Resampler resampler) {
        super(targetFormat);
        if (!Float.isFinite(bufferMillis) || bufferMillis <= 0F) {
            throw new IllegalArgumentException("Buffer millis must be finite and > 0: " + bufferMillis);
        }
        this.delegate = delegate;
        this.resampler = resampler;

        final int outputFrameCount = this.getFormat().millisToFrameCount(bufferMillis);
        final int requiredInputFrameCount = Resampler.computeMaxRequiredInputFrameCount(this.delegate.getFormat(), this.getFormat(), Math.addExact(outputFrameCount, 1));
        final int inputFrameCount = Math.addExact(Math.addExact(this.resampler.getLookBehindFrameCount(), requiredInputFrameCount), this.resampler.getLookAheadFrameCount());
        this.inputSamples = new float[Math.multiplyExact(inputFrameCount, this.delegate.getFormat().channelCount())];
        this.outputSamples = new float[Math.multiplyExact(outputFrameCount, this.getFormat().channelCount())];
    }

    @Override
    public float read() throws IOException {
        if (this.outputSampleIndex >= this.outputSampleCount) {
            this.resampleNextBuffer();
            this.outputSampleIndex = 0;
            if (this.outputSampleCount == 0) {
                throw new EOFException();
            }
        }
        return this.outputSamples[this.outputSampleIndex++];
    }

    private void resampleNextBuffer() throws IOException {
        this.discardConsumedInput();
        this.inputSampleCount += this.delegate.read(this.inputSamples, this.inputSampleCount, this.inputSamples.length - this.inputSampleCount);
        final float[] inputSamples = this.inputSampleCount == this.inputSamples.length ? this.inputSamples : Arrays.copyOf(this.inputSamples, this.inputSampleCount);
        this.inputFramePosition = this.resampler.resample(inputSamples, this.delegate.getFormat(), this.outputSamples, this.getFormat(), this.inputFramePosition);
        this.outputSampleCount = Math.multiplyExact(this.resampler.getLastOutputFrameCount(), this.getFormat().channelCount());
    }

    private void discardConsumedInput() {
        final int discardFrameCount = Math.min(Math.max((int) this.inputFramePosition - this.resampler.getLookBehindFrameCount(), 0), this.delegate.getFormat().sampleCountToFrameCount(this.inputSampleCount));
        final int discardSampleCount = Math.multiplyExact(discardFrameCount, this.delegate.getFormat().channelCount());
        this.inputSampleCount -= discardSampleCount;
        System.arraycopy(this.inputSamples, discardSampleCount, this.inputSamples, 0, this.inputSampleCount);
        this.inputFramePosition -= discardFrameCount;
    }

    @Override
    public void close() throws IOException {
        this.delegate.close();
    }

    public AudioInputStream getDelegate() {
        return this.delegate;
    }

}
