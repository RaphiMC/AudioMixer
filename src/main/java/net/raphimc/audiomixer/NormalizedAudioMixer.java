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

import net.raphimc.audiomixer.soundmodifier.impl.NormalizationModifier;
import net.raphimc.audiomixer.soundmodifier.impl.VolumeModifier;
import net.raphimc.audiomixer.util.PcmFloatAudioFormat;

public class NormalizedAudioMixer extends AudioMixer {

    private final NormalizationModifier normalizationModifier = new NormalizationModifier();
    private final VolumeModifier volumeModifier = new VolumeModifier(1F);

    public NormalizedAudioMixer(final PcmFloatAudioFormat audioFormat) {
        super(audioFormat);
        this.getSoundModifiers().append(this.normalizationModifier);
        this.getSoundModifiers().append(this.volumeModifier);
    }

    @Override
    public void stopAllSounds() {
        super.stopAllSounds();
        this.normalizationModifier.reset();
    }

    public VolumeModifier getVolumeModifier() {
        return this.volumeModifier;
    }

    public NormalizationModifier getNormalizationModifier() {
        return this.normalizationModifier;
    }

    public NormalizedAudioMixer setMasterVolume(final int masterVolume) {
        return this.setMasterVolume(masterVolume / 100F);
    }

    public NormalizedAudioMixer setMasterVolume(final float masterVolume) {
        this.volumeModifier.setVolume(masterVolume);
        return this;
    }

    public float getMasterVolume() {
        return this.volumeModifier.getVolume();
    }

}
