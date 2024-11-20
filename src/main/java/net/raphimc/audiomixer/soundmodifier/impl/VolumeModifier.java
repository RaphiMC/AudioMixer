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
package net.raphimc.audiomixer.soundmodifier.impl;

import net.raphimc.audiomixer.modulator.Modulator;
import net.raphimc.audiomixer.soundmodifier.SoundModifier;

import javax.sound.sampled.AudioFormat;

public class VolumeModifier implements SoundModifier {

    private float volume;
    private Modulator volumeModulator;

    public VolumeModifier(final float volume) {
        this.setVolume(volume);
    }

    @Override
    public void modify(final AudioFormat audioFormat, final int[] renderedSamples) {
        final boolean hasVolumeModulator = this.volumeModulator != null;
        for (int i = 0; i < renderedSamples.length; i++) {
            final float volume;
            if (!hasVolumeModulator) {
                volume = this.volume;
            } else {
                volume = Math.min(Math.max(this.volumeModulator.modifyValue(this.volume, audioFormat.getSampleRate()), 0F), 1F);
            }

            renderedSamples[i] = (int) (renderedSamples[i] * volume);
        }
    }

    public float getVolume() {
        return this.volume;
    }

    public void setVolume(final float volume) {
        if (volume < 0 || volume > 1) {
            throw new IllegalArgumentException("Volume must be between 0 and 1");
        }

        this.volume = volume;
    }

    public Modulator getVolumeModulator() {
        return this.volumeModulator;
    }

    public void setVolumeModulator(final Modulator volumeModulator) {
        this.volumeModulator = volumeModulator;
    }

}
