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
import java.util.Arrays;
import java.util.List;

public class ModifiableSound implements Sound {

    private final Sound sound;
    private final List<SoundModifier> soundModifiers = new ArrayList<>();

    public ModifiableSound(final Sound sound, final SoundModifier... modifiers) {
        this.sound = sound;
        this.soundModifiers.addAll(Arrays.asList(modifiers));
    }

    @Override
    public void render(final AudioFormat audioFormat, final int[] renderedSamples) {
        this.sound.render(audioFormat, renderedSamples);
        for (final SoundModifier modifier : this.soundModifiers) {
            modifier.modify(audioFormat, renderedSamples);
        }
    }

    @Override
    public boolean isFinished() {
        return this.sound.isFinished();
    }

    public Sound getSound() {
        return this.sound;
    }

    public void addSoundModifier(final SoundModifier soundModifier) {
        this.soundModifiers.add(soundModifier);
    }

    public void removeSoundModifier(final SoundModifier soundModifier) {
        this.soundModifiers.remove(soundModifier);
    }

    protected List<SoundModifier> getSoundModifiers() {
        return this.soundModifiers;
    }

}
