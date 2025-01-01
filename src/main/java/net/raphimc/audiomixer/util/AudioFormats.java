/*
 * This file is part of AudioMixer - https://github.com/RaphiMC/AudioMixer
 * Copyright (C) 2024-2025 RK_01/RaphiMC and contributors
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

public class AudioFormats {

    public static AudioFormat withChannels(final AudioFormat audioFormat, final int channels) {
        if (audioFormat.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
            throw new IllegalArgumentException("Unsupported audio format: " + audioFormat);
        }

        return new AudioFormat(audioFormat.getSampleRate(), audioFormat.getSampleSizeInBits(), channels, true, audioFormat.isBigEndian());
    }

    public static AudioFormat withSampleSizeInBits(final AudioFormat audioFormat, final int sampleSizeInBits) {
        if (audioFormat.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
            throw new IllegalArgumentException("Unsupported audio format: " + audioFormat);
        }

        return new AudioFormat(audioFormat.getSampleRate(), sampleSizeInBits, audioFormat.getChannels(), true, audioFormat.isBigEndian());
    }

}
