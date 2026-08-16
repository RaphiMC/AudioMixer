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
package net.raphimc.audiomixer.util;

public record PcmAudioFormat(AudioFormat format, PcmSampleEncoding encoding) {

    public int millisToByteCount(final float millis) {
        return this.frameCountToByteCount(this.format.millisToFrameCount(millis));
    }

    public float byteCountToMillis(final int byteCount) {
        return this.format.frameCountToMillis(this.byteCountToFrameCount(byteCount));
    }

    public int frameCountToByteCount(final int frameCount) {
        return frameCount * this.bytesPerFrame();
    }

    public int byteCountToFrameCount(final int byteCount) {
        if (byteCount % this.bytesPerFrame() != 0) {
            throw new IllegalArgumentException("Byte count must be a multiple of the frame size");
        }
        return byteCount / this.bytesPerFrame();
    }

    public int bytesPerFrame() {
        return this.encoding.bytesPerSample() * this.format.channels();
    }

    public int bytesPerSecond() {
        return Math.round(this.format.sampleRate()) * this.bytesPerFrame();
    }

}
