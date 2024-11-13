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

import net.raphimc.audiomixer.soundmodifier.SoundModifier;

import javax.sound.sampled.AudioFormat;

public class PanningModifier implements SoundModifier {

    private float panning;

    public PanningModifier(final float panning) {
        this.setPanning(panning);
    }

    @Override
    public void modify(final AudioFormat audioFormat, final int[] renderedSamples) {
        if (audioFormat.getChannels() != 2) {
            throw new UnsupportedOperationException("Target audio format must have 2 channels");
        }

        for (int i = 0; i < renderedSamples.length; i += 2) {
            renderedSamples[i] = (int) (renderedSamples[i] * (1F - this.panning));
            renderedSamples[i + 1] = (int) (renderedSamples[i + 1] * this.panning);
        }
    }

    public float getPanning() {
        return this.panning * 2F - 1F;
    }

    public void setPanning(final float panning) {
        this.panning = (Math.max(-1F, Math.min(1F, panning)) + 1) / 2F;
    }

}
