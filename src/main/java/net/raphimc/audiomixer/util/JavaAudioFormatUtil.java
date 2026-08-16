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

public final class JavaAudioFormatUtil {

    private JavaAudioFormatUtil() {
    }

    public static AudioFormat getAudioFormat(final javax.sound.sampled.AudioFormat format) {
        return new AudioFormat(format.getSampleRate(), format.getChannels());
    }

    public static PcmAudioFormat getPcmAudioFormat(final javax.sound.sampled.AudioFormat format) {
        return new PcmAudioFormat(getAudioFormat(format), getPcmSampleEncoding(format));
    }

    public static PcmSampleEncoding getPcmSampleEncoding(final javax.sound.sampled.AudioFormat format) {
        if (format.getEncoding() == javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED) {
            return switch (format.getSampleSizeInBits()) {
                case 8 -> PcmSampleEncoding.S8;
                case 16 -> format.isBigEndian() ? PcmSampleEncoding.S16_BE : PcmSampleEncoding.S16_LE;
                case 24 -> format.isBigEndian() ? PcmSampleEncoding.S24_BE : PcmSampleEncoding.S24_LE;
                case 32 -> format.isBigEndian() ? PcmSampleEncoding.S32_BE : PcmSampleEncoding.S32_LE;
                default -> throw new IllegalArgumentException("Unsupported sample size: " + format.getSampleSizeInBits());
            };
        } else if (format.getEncoding() == javax.sound.sampled.AudioFormat.Encoding.PCM_UNSIGNED) {
            return switch (format.getSampleSizeInBits()) {
                case 8 -> PcmSampleEncoding.U8;
                case 16 -> format.isBigEndian() ? PcmSampleEncoding.U16_BE : PcmSampleEncoding.U16_LE;
                case 24 -> format.isBigEndian() ? PcmSampleEncoding.U24_BE : PcmSampleEncoding.U24_LE;
                case 32 -> format.isBigEndian() ? PcmSampleEncoding.U32_BE : PcmSampleEncoding.U32_LE;
                default -> throw new IllegalArgumentException("Unsupported sample size: " + format.getSampleSizeInBits());
            };
        } else if (format.getEncoding() == javax.sound.sampled.AudioFormat.Encoding.PCM_FLOAT) {
            return switch (format.getSampleSizeInBits()) {
                case 32 -> format.isBigEndian() ? PcmSampleEncoding.F32_BE : PcmSampleEncoding.F32_LE;
                case 64 -> format.isBigEndian() ? PcmSampleEncoding.F64_BE : PcmSampleEncoding.F64_LE;
                default -> throw new IllegalArgumentException("Unsupported sample size: " + format.getSampleSizeInBits());
            };
        } else {
            throw new IllegalArgumentException("Unsupported encoding: " + format.getEncoding());
        }
    }

}
