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
package net.raphimc.audiomixer.sound.special;

import net.raphimc.audiomixer.sound.Sound;
import net.raphimc.audiomixer.soundmodifier.SoundModifiers;

import javax.sound.sampled.AudioFormat;
import java.util.ArrayList;
import java.util.List;

public class SubMixSound implements Sound {

    protected final List<Sound> sounds = new ArrayList<>();
    private final SoundModifiers soundModifiers = new SoundModifiers();
    private int maxSounds;
    private int mixedSounds;

    public SubMixSound() {
        this(512);
    }

    public SubMixSound(final int maxSounds) {
        this.setMaxSounds(maxSounds);
    }

    @Override
    public synchronized void render(final AudioFormat audioFormat, final int[] finalMixBuffer) {
        this.mixedSounds = this.sounds.size();

        final int[] renderedSamples = new int[finalMixBuffer.length];
        for (Sound sound : this.sounds) {
            sound.render(audioFormat, renderedSamples);
            for (int i = 0; i < finalMixBuffer.length; i++) {
                finalMixBuffer[i] += renderedSamples[i];
            }
        }
        this.soundModifiers.modify(audioFormat, finalMixBuffer);

        this.sounds.removeIf(Sound::isFinished);
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    public synchronized void playSound(final Sound sound) {
        if (this.sounds.size() >= this.maxSounds) {
            this.sounds.remove(0);
        }
        this.sounds.add(sound);
    }

    public synchronized void stopSound(final Sound sound) {
        this.sounds.remove(sound);
    }

    public synchronized void stopAllSounds() {
        this.sounds.clear();
    }

    public int getMaxSounds() {
        return this.maxSounds;
    }

    public void setMaxSounds(final int maxSounds) {
        if (maxSounds < 1) {
            throw new IllegalArgumentException("Max sounds must be at least 1");
        }
        if (maxSounds > 65535) {
            throw new IllegalArgumentException("Max sounds must be at most 65535");
        }

        this.maxSounds = maxSounds;
    }

    public SoundModifiers getSoundModifiers() {
        return this.soundModifiers;
    }

    public int getMixedSounds() {
        return this.mixedSounds;
    }

    public synchronized int getActiveSounds() {
        return this.sounds.size();
    }

}
