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
package net.raphimc.audiomixer;

import net.raphimc.audiomixer.sound.Sound;
import net.raphimc.audiomixer.sound.impl.mix.MixSound;
import net.raphimc.audiomixer.soundmodifier.SoundModifiers;
import net.raphimc.audiomixer.util.MathUtil;
import net.raphimc.audiomixer.util.PcmFloatAudioFormat;

import javax.sound.sampled.AudioFormat;

public class AudioMixer {

    private final PcmFloatAudioFormat audioFormat;
    private final MixSound masterMixSound = new MixSound();

    public AudioMixer(final PcmFloatAudioFormat audioFormat) {
        this.audioFormat = audioFormat;
    }

    public void playSound(final Sound sound) {
        this.masterMixSound.playSound(sound);
    }

    public void stopSound(final Sound sound) {
        this.masterMixSound.stopSound(sound);
    }

    public void stopAllSounds() {
        this.masterMixSound.stopAllSounds();
    }

    public SoundModifiers getSoundModifiers() {
        return this.masterMixSound.getSoundModifiers();
    }

    public float[] renderMillis(final float millis) {
        return this.render(MathUtil.millisToFrameCount(this.audioFormat, millis));
    }

    public float[] render(final int frameCount) {
        final float[] renderedSamples = new float[frameCount * this.audioFormat.getChannels()];
        this.masterMixSound.render(this.audioFormat, renderedSamples);
        return renderedSamples;
    }

    public AudioFormat getAudioFormat() {
        return this.audioFormat;
    }

    public MixSound getMasterMixSound() {
        return this.masterMixSound;
    }

}
