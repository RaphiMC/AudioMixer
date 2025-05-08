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
import javax.sound.sampled.AudioSystem;

public class AudioFormatModifier {

    public static final AudioFormatModifier NONE = new AudioFormatModifier(AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED);

    private final float sampleRate;
    private final int channels;

    public static AudioFormatModifier ofSampleRate(final float sampleRate) {
        return new AudioFormatModifier(sampleRate, AudioSystem.NOT_SPECIFIED);
    }

    public static AudioFormatModifier ofChannels(final int channels) {
        return new AudioFormatModifier(AudioSystem.NOT_SPECIFIED, channels);
    }

    public static AudioFormatModifier ofSampleRateAndChannels(final float sampleRate, final int channels) {
        return new AudioFormatModifier(sampleRate, channels);
    }

    private AudioFormatModifier(final float sampleRate, final int channels) {
        this.sampleRate = sampleRate;
        this.channels = channels;
    }

    public AudioFormat getFloatAudioFormat(final AudioFormat audioFormat) {
        return new AudioFormat(
                AudioFormat.Encoding.PCM_FLOAT,
                getSampleRate(audioFormat.getSampleRate()),
                Float.SIZE,
                getChannels(audioFormat.getChannels()),
                getChannels(audioFormat.getChannels()) * Float.BYTES,
                getSampleRate(audioFormat.getSampleRate()),
                true
        );
    }

    public float getSampleRate(final float sampleRate) {
        return this.sampleRate != AudioSystem.NOT_SPECIFIED ? this.sampleRate : sampleRate;
    }

    public int getChannels(final int channels) {
        return this.channels != AudioSystem.NOT_SPECIFIED ? this.channels : channels;
    }

}
