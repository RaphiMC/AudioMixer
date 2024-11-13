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
import net.raphimc.audiomixer.soundmodifier.SoundModifier;
import net.raphimc.audiomixer.soundmodifier.SoundModifiers;

import javax.sound.sampled.AudioFormat;

public class ModifiableSound implements Sound {

    private final Sound sound;
    private final SoundModifiers soundModifiers;

    public ModifiableSound(final Sound sound, final SoundModifier... modifiers) {
        this(sound, new SoundModifiers(modifiers));
    }

    public ModifiableSound(final Sound sound, final SoundModifiers soundModifiers) {
        this.sound = sound;
        this.soundModifiers = soundModifiers;
    }

    @Override
    public void render(final AudioFormat audioFormat, final int[] renderedSamples) {
        this.sound.render(audioFormat, renderedSamples);
        this.soundModifiers.modify(audioFormat, renderedSamples);
    }

    @Override
    public boolean isFinished() {
        return this.sound.isFinished();
    }

    public Sound getSound() {
        return this.sound;
    }

    public SoundModifiers getSoundModifiers() {
        return this.soundModifiers;
    }

}
