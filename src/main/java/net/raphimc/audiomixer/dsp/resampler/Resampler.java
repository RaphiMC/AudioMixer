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
package net.raphimc.audiomixer.dsp.resampler;

import net.raphimc.audiomixer.util.FloatAudioFormat;
import net.raphimc.audiomixer.util.buffer.AudioBuffer;

public interface Resampler {

    default AudioBuffer resample(final AudioBuffer src, final FloatAudioFormat dstFormat) {
        if (!src.format().equals(dstFormat)) {
            final float pitch = src.format().sampleRate() / dstFormat.sampleRate();
            final int dstSampleCount = (int) Math.ceil((double) src.getFrameCount() * pitch) * dstFormat.channels();
            final AudioBuffer dst = new AudioBuffer(dstFormat, dstSampleCount);
            this.resample(src, dst, 0);
            return dst;
        } else {
            return src;
        }
    }

    default double resample(final AudioBuffer src, final AudioBuffer dst, final double srcPosition) {
        return this.resample(src.samples(), src.format(), dst.samples(), dst.format(), srcPosition);
    }

    default double resample(final float[] src, final FloatAudioFormat srcFormat, final float[] dst, final FloatAudioFormat dstFormat, final double srcPosition) {
        final float pitch = srcFormat.sampleRate() / dstFormat.sampleRate();
        if (srcFormat.channels() == 1 && dstFormat.channels() == 1) {
            return this.resampleMonoToMono(src, dst, pitch, srcPosition);
        } else if (srcFormat.channels() == 2 && dstFormat.channels() == 2) {
            return this.resampleStereoToStereo(src, dst, pitch, srcPosition);
        } else if (srcFormat.channels() == 1 && dstFormat.channels() == 2) {
            return this.resampleMonoToStereo(src, dst, pitch, srcPosition);
        } else if (srcFormat.channels() == 2 && dstFormat.channels() == 1) {
            return this.resampleStereoToMono(src, dst, pitch, srcPosition);
        } else {
            throw new IllegalArgumentException("Unsupported channel configuration: " + srcFormat.channels() + " -> " + dstFormat.channels());
        }
    }

    double resampleMonoToMono(final float[] src, final float[] dst, final float pitch, final double srcPosition);

    double resampleStereoToStereo(final float[] src, final float[] dst, final float pitch, final double srcPosition);

    double resampleMonoToStereo(final float[] src, final float[] dst, final float pitch, final double srcPosition);

    double resampleStereoToMono(final float[] src, final float[] dst, final float pitch, final double srcPosition);

}
