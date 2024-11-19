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
package net.raphimc.audiomixer.soundmodifier;

import javax.sound.sampled.AudioFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class SoundModifiers {

    protected final List<SoundModifier> soundModifiers = new ArrayList<>();

    public SoundModifiers() {
    }

    public SoundModifiers(final SoundModifier... modifiers) {
        this.soundModifiers.addAll(Arrays.asList(modifiers));
    }

    public synchronized void modify(final AudioFormat audioFormat, final int[] renderedSamples) {
        if (this.soundModifiers.isEmpty()) {
            return;
        }

        for (SoundModifier modifier : this.soundModifiers) {
            modifier.modify(audioFormat, renderedSamples);
        }
    }

    public synchronized List<SoundModifier> get(final Predicate<SoundModifier> predicate) {
        return this.soundModifiers.stream().filter(predicate).toList();
    }

    public synchronized SoundModifier getFirst(final Predicate<SoundModifier> predicate) {
        return this.soundModifiers.stream().filter(predicate).findFirst().orElse(null);
    }

    public synchronized <T extends SoundModifier> T getFirst(final Class<T> clazz) {
        return this.soundModifiers.stream().filter(m -> m.getClass().equals(clazz)).map(clazz::cast).findFirst().orElse(null);
    }

    public synchronized void append(final SoundModifier soundModifier) {
        this.soundModifiers.add(soundModifier);
    }

    public synchronized void prepend(final SoundModifier soundModifier) {
        this.soundModifiers.add(0, soundModifier);
    }

    public synchronized boolean insertBefore(final SoundModifier soundModifier, final SoundModifier other) {
        final int index = this.soundModifiers.indexOf(other);
        if (index != -1) {
            this.soundModifiers.add(index, soundModifier);
            return true;
        } else {
            return false;
        }
    }

    public synchronized boolean insertAfter(final SoundModifier soundModifier, final SoundModifier other) {
        final int index = this.soundModifiers.indexOf(other);
        if (index != -1) {
            this.soundModifiers.add(index + 1, soundModifier);
            return true;
        } else {
            return false;
        }
    }

    public synchronized void remove(final SoundModifier soundModifier) {
        this.soundModifiers.remove(soundModifier);
    }

    public synchronized void clear() {
        this.soundModifiers.clear();
    }

    public synchronized boolean isEmpty() {
        return this.soundModifiers.isEmpty();
    }

}
