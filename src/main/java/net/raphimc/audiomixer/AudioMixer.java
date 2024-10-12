/*
 * This file is part of AudioMixer - https://github.com/RaphiMC/AudioMixer
 * Copyright (C) 2024-2024 RK_01/RaphiMC and contributors
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
package net.raphimc.audiomixer;

import net.raphimc.audiomixer.sound.Sound;

import javax.sound.sampled.AudioFormat;
import java.util.ArrayList;
import java.util.List;

public class AudioMixer {

    private final AudioFormat audioFormat;
    private final int maxSounds;
    private final List<Sound> sounds = new ArrayList<>();
    private int mixedSounds;

    public AudioMixer(final AudioFormat audioFormat) {
        this(audioFormat, 512);
    }

    public AudioMixer(final AudioFormat audioFormat, final int maxSounds) {
        if (audioFormat.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
            throw new IllegalArgumentException("Unsupported audio format: " + audioFormat);
        }
        if (maxSounds < 1) {
            throw new IllegalArgumentException("Max sounds must be at least 1");
        }
        if (maxSounds > 65535) {
            throw new IllegalArgumentException("Max sounds must be at most 65535");
        }

        this.audioFormat = audioFormat;
        this.maxSounds = maxSounds;
    }

    public void playSound(final Sound sound) {
        if (this.sounds.size() >= this.maxSounds) {
            this.sounds.remove(0);
        }
        this.sounds.add(sound);
    }

    public void stopSound(final Sound sound) {
        this.sounds.remove(sound);
    }

    public void stopAllSounds() {
        this.sounds.clear();
    }

    public int[] mix(final float millis) {
        return this.mix((int) Math.ceil(millis * this.audioFormat.getSampleRate() / 1000F / this.audioFormat.getChannels()) * this.audioFormat.getChannels());
    }

    public int[] mix(final int sampleCount) {
        if (sampleCount % this.audioFormat.getChannels() != 0) {
            throw new IllegalArgumentException("Sample count must be a multiple of the channel count");
        }
        this.mixedSounds = this.sounds.size();

        final int[] finalMixBuffer = new int[sampleCount];
        final int[] renderedSamples = new int[sampleCount];
        for (Sound sound : this.sounds) {
            sound.render(this.audioFormat, renderedSamples);
            for (int i = 0; i < sampleCount; i++) {
                finalMixBuffer[i] += renderedSamples[i];
            }
        }
        this.sounds.removeIf(Sound::isFinished);
        return finalMixBuffer;
    }

    public AudioFormat getAudioFormat() {
        return this.audioFormat;
    }

    public int getMaxSounds() {
        return this.maxSounds;
    }

    public int getMixedSounds() {
        return this.mixedSounds;
    }

    public int getActiveSounds() {
        return this.sounds.size();
    }

}
