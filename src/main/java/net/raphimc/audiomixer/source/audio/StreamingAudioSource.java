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
import net.raphimc.audiomixer.util.FloatAudioFormat;
import net.raphimc.audiomixer.util.buffer.AudioBuffer;

public abstract class StreamingAudioSource extends AudioSource {

    public StreamingAudioSource(final FloatAudioFormat format) {
        super(new AudioBuffer(format, 0));
    }

    public StreamingAudioSource(final FloatAudioFormat format, final Resampler resampler) {
        super(new AudioBuffer(format, 0), resampler);
    }

    @Override
    protected synchronized void renderInternal(final AudioBuffer buffer) {
        super.renderInternal(buffer);
    }

    protected synchronized void enqueueBuffer(final AudioBuffer buffer) {
        final int frameOffset = (int) this.position;
        final int sampleOffset = frameOffset * this.getFormat().channels();
        this.buffer = this.buffer.slice(sampleOffset, this.buffer.getSampleCount()).append(buffer);
        this.position -= frameOffset;
    }

    @Override
    public synchronized void setSampleRate(final float sampleRate) {
        super.setSampleRate(sampleRate);
    }

}
