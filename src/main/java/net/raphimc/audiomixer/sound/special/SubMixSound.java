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
import net.raphimc.audiomixer.sound.SoundModifier;

import javax.sound.sampled.AudioFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class SubMixSound implements Sound {

    private final int maxSounds;
    private final List<Sound> sounds = new ArrayList<>();
    private final List<SoundModifier> soundModifiers = new ArrayList<>();
    private int mixedSounds;

    public SubMixSound() {
        this(512);
    }

    public SubMixSound(final int maxSounds) {
        if (maxSounds < 1) {
            throw new IllegalArgumentException("Max sounds must be at least 1");
        }
        if (maxSounds > 65535) {
            throw new IllegalArgumentException("Max sounds must be at most 65535");
        }

        this.maxSounds = maxSounds;
    }

    @Override
    public void render(final AudioFormat audioFormat, final int[] finalMixBuffer) {
        this.mixedSounds = this.sounds.size();

        final int[] renderedSamples = new int[finalMixBuffer.length];
        for (Sound sound : this.sounds) {
            sound.render(audioFormat, renderedSamples);
            for (int i = 0; i < finalMixBuffer.length; i++) {
                finalMixBuffer[i] += renderedSamples[i];
            }
        }
        for (SoundModifier modifier : this.soundModifiers) {
            modifier.modify(audioFormat, finalMixBuffer);
        }

        this.sounds.removeIf(Sound::isFinished);
    }

    @Override
    public boolean isFinished() {
        return false;
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

    public List<SoundModifier> getSoundModifiers(final Predicate<SoundModifier> predicate) {
        return this.soundModifiers.stream().filter(predicate).toList();
    }

    public void appendSoundModifier(final SoundModifier soundModifier) {
        this.soundModifiers.add(soundModifier);
    }

    public void prependSoundModifier(final SoundModifier soundModifier) {
        this.soundModifiers.add(0, soundModifier);
    }

    public boolean insertSoundModifierBefore(final SoundModifier soundModifier, final SoundModifier other) {
        final int index = this.soundModifiers.indexOf(other);
        if (index != -1) {
            this.soundModifiers.add(index, soundModifier);
            return true;
        } else {
            return false;
        }
    }

    public boolean insertSoundModifierAfter(final SoundModifier soundModifier, final SoundModifier other) {
        final int index = this.soundModifiers.indexOf(other);
        if (index != -1) {
            this.soundModifiers.add(index + 1, soundModifier);
            return true;
        } else {
            return false;
        }
    }

    public void removeSoundModifier(final SoundModifier soundModifier) {
        this.soundModifiers.remove(soundModifier);
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

    protected List<Sound> getSounds() {
        return this.sounds;
    }

    protected List<SoundModifier> getSoundModifiers() {
        return this.soundModifiers;
    }

    @Deprecated(forRemoval = true)
    public void addSoundModifier(final SoundModifier soundModifier) {
        this.appendSoundModifier(soundModifier);
    }

}
