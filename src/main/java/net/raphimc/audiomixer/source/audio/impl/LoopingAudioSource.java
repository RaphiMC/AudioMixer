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
package net.raphimc.audiomixer.source.audio.impl;

import net.raphimc.audiomixer.resampler.Resampler;
import net.raphimc.audiomixer.resampler.impl.LinearResampler;
import net.raphimc.audiomixer.util.buffer.AudioBuffer;

import java.util.Arrays;

public class LoopingAudioSource extends BufferedAudioSource {

    private final LoopBuffer loopBuffer = new LoopBuffer();
    private boolean loopEnabled = true;
    private int loopStartPosition;
    private int loopEndPosition;

    public LoopingAudioSource(final AudioBuffer buffer) {
        this(buffer, new LinearResampler());
    }

    public LoopingAudioSource(final AudioBuffer buffer, final Resampler resampler) {
        super(buffer, resampler);
        if (this.getFrameCount() == 0) {
            throw new IllegalArgumentException("Buffer must not be empty");
        }
        if (this.resampler.getLookBehindFrameCount() > 0) {
            throw new IllegalArgumentException("Resampler must not require look behind frames");
        }
        this.loopEndPosition = this.getFrameCount() - 1;
    }

    @Override
    protected void renderInternal(final AudioBuffer buffer) {
        this.loopBuffer.clear();
        int sourcePosition = (int) this.position;
        int neededFrameCount = Resampler.computeMaxRequiredInputFrameCount(this.getFormat(), buffer.format(), buffer.frameCount()) + this.resampler.getLookAheadFrameCount();
        if (sourcePosition < this.loopStartPosition && neededFrameCount > 0) { // Intro
            final int frameCount = Math.min(neededFrameCount, this.loopStartPosition - sourcePosition);
            this.loopBuffer.append(this.buffer, sourcePosition, frameCount);
            sourcePosition += frameCount;
            neededFrameCount -= frameCount;
        }
        if (this.loopEnabled && sourcePosition >= this.loopStartPosition && sourcePosition <= this.loopEndPosition) { // Loop
            while (neededFrameCount > 0) {
                final int frameCount = Math.min(Math.min(neededFrameCount, this.getLoopFrameCount()), this.loopEndPosition - sourcePosition + 1);
                this.loopBuffer.append(this.buffer, sourcePosition, frameCount);
                sourcePosition += frameCount;
                neededFrameCount -= frameCount;
                if (sourcePosition > this.loopEndPosition) {
                    sourcePosition = this.loopStartPosition + (sourcePosition - this.loopStartPosition) % this.getLoopFrameCount();
                }
            }
        } else if (neededFrameCount > 0) { // Outro
            final int frameCount = Math.min(neededFrameCount, this.getFrameCount() - sourcePosition);
            this.loopBuffer.append(this.buffer, sourcePosition, frameCount);
            neededFrameCount -= frameCount;
            if (neededFrameCount > 0) { // Reached end of source buffer
                this.loopBuffer.trimToLength();
            }
        }

        final double fractionalPosition = this.position % 1;
        double consumedFrameCount = this.resampler.resample(this.loopBuffer.array, this.getFormat(), buffer.samples(), buffer.format(), fractionalPosition) - fractionalPosition;
        if (this.position < this.loopStartPosition && consumedFrameCount > 0) { // Intro
            final double frameCount = Math.min(consumedFrameCount, this.loopStartPosition - this.position);
            this.position += frameCount;
            consumedFrameCount -= frameCount;
        }
        if (this.loopEnabled && (int) this.position >= this.loopStartPosition && (int) this.position <= this.loopEndPosition) { // Loop
            while (consumedFrameCount > 0) {
                final double frameCount = Math.min(Math.min(consumedFrameCount, this.getLoopFrameCount()), this.loopEndPosition - this.position + 1);
                this.position += frameCount;
                consumedFrameCount -= frameCount;
                if ((int) this.position > this.loopEndPosition) {
                    this.position = this.loopStartPosition + (this.position - this.loopStartPosition) % this.getLoopFrameCount();
                }
            }
        } else if (consumedFrameCount > 0) { // Outro
            this.position += Math.min(consumedFrameCount, this.getFrameCount() - this.position);
        }
    }

    public boolean isLoopEnabled() {
        return this.loopEnabled;
    }

    public void setLoopEnabled(final boolean loopEnabled) {
        this.loopEnabled = loopEnabled;
    }

    public int getLoopStartPosition() {
        return this.loopStartPosition;
    }

    public void setLoopStartPosition(final int loopStartPosition) {
        if (loopStartPosition < 0 || loopStartPosition >= this.getFrameCount()) {
            throw new IllegalArgumentException("Loop start position must be in [0, frame count)");
        }
        this.loopStartPosition = loopStartPosition;
        if (loopStartPosition > this.loopEndPosition) {
            this.loopEndPosition = loopStartPosition;
        }
    }

    public int getLoopEndPosition() {
        return this.loopEndPosition;
    }

    public void setLoopEndPosition(final int loopEndPosition) {
        if (loopEndPosition < 0 || loopEndPosition >= this.getFrameCount()) {
            throw new IllegalArgumentException("Loop end position must be in [0, frame count)");
        }
        this.loopEndPosition = loopEndPosition;
        if (loopEndPosition < this.loopStartPosition) {
            this.loopStartPosition = loopEndPosition;
        }
    }

    public void setLoopRange(final int loopStartPosition, final int loopEndPosition) {
        if (loopStartPosition > loopEndPosition) {
            throw new IllegalArgumentException("Loop start position must be <= loop end position");
        }
        this.setLoopStartPosition(loopStartPosition);
        this.setLoopEndPosition(loopEndPosition);
    }

    private int getLoopFrameCount() {
        return this.loopEndPosition - this.loopStartPosition + 1;
    }

    private static final class LoopBuffer {

        private float[] array = new float[0];
        private int length;

        private void append(final AudioBuffer buffer, final int offset, final int length) {
            final int sampleCount = length * buffer.format().channels();
            if (this.length + sampleCount > this.array.length) {
                this.array = Arrays.copyOf(this.array, this.length + Math.max(sampleCount, this.length));
            }
            System.arraycopy(buffer.samples(), offset * buffer.format().channels(), this.array, this.length, sampleCount);
            this.length += sampleCount;
        }

        private void trimToLength() {
            if (this.length < this.array.length) {
                this.array = Arrays.copyOf(this.array, this.length);
            }
        }

        private void clear() {
            Arrays.fill(this.array, 0, this.length, 0);
            this.length = 0;
        }

    }

}
