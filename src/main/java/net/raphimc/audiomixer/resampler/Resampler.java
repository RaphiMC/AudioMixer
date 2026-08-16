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
package net.raphimc.audiomixer.resampler;

import net.raphimc.audiomixer.util.AudioFormat;
import net.raphimc.audiomixer.util.buffer.AudioBuffer;

public interface Resampler {

    default AudioBuffer resample(final AudioBuffer src, final AudioFormat dstFormat) {
        if (!src.format().equals(dstFormat)) {
            final float pitch = src.format().sampleRate() / dstFormat.sampleRate();
            final AudioBuffer dst = new AudioBuffer(dstFormat, (int) Math.ceil((double) src.frameCount() / pitch));
            this.resample(src, dst, 0);
            return dst;
        } else {
            return src;
        }
    }

    default double resample(final AudioBuffer src, final AudioBuffer dst, final double srcPosition) {
        return this.resample(src.samples(), src.format(), dst.samples(), dst.format(), srcPosition);
    }

    default double resample(final float[] src, final AudioFormat srcFormat, final float[] dst, final AudioFormat dstFormat, final double srcPosition) {
        if (!srcFormat.equals(dstFormat) || srcPosition % 1 != 0) {
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
        } else {
            final int offset = (int) srcPosition * srcFormat.channels();
            final int count = Math.min(dst.length, Math.max(src.length - offset, 0));
            System.arraycopy(src, offset, dst, 0, count);
            return srcPosition + srcFormat.sampleCountToFrameCount(count);
        }
    }

    double resampleMonoToMono(final float[] src, final float[] dst, final float pitch, final double srcPosition);

    double resampleStereoToStereo(final float[] src, final float[] dst, final float pitch, final double srcPosition);

    double resampleMonoToStereo(final float[] src, final float[] dst, final float pitch, final double srcPosition);

    double resampleStereoToMono(final float[] src, final float[] dst, final float pitch, final double srcPosition);

}
