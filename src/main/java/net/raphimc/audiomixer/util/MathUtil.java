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

import javax.sound.sampled.AudioFormat;

public class MathUtil {

    public static final float PI = (float) Math.PI;
    public static final float HALF_PI = (float) (Math.PI / 2D);
    public static final float TWO_PI = (float) (Math.PI * 2D);

    public static float clamp(final float value, final float min, final float max) {
        return Math.max(min, Math.min(value, max));
    }

    public static int roundDownToMultiple(final int value, final int multiple) {
        return (value / multiple) * multiple;
    }

    public static int roundUpToMultiple(final int value, final int multiple) {
        return (int) (Math.ceil((double) value / multiple) * multiple);
    }

    public static int millisToFrameCount(final AudioFormat audioFormat, final float millis) {
        return (int) Math.ceil(audioFormat.getSampleRate() / 1000F * millis);
    }

    public static int millisToByteCount(final AudioFormat audioFormat, final float millis) {
        return millisToFrameCount(audioFormat, millis) * audioFormat.getFrameSize();
    }

    public static float sampleCountToMillis(final AudioFormat audioFormat, final int sampleCount) {
        return (sampleCount / (audioFormat.getSampleRate() * audioFormat.getChannels())) * 1000F;
    }

    public static int sampleCountToByteCount(final AudioFormat audioFormat, final int sampleCount) {
        return sampleCount * (audioFormat.getSampleSizeInBits() / Byte.SIZE);
    }

    public static int byteCountToFrameCount(final AudioFormat audioFormat, final int byteCount) {
        return byteCount / audioFormat.getFrameSize();
    }

}
