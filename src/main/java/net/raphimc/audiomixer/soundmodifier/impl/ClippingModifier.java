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
package net.raphimc.audiomixer.soundmodifier.impl;

import net.raphimc.audiomixer.soundmodifier.SoundModifier;
import net.raphimc.audiomixer.util.MathUtil;
import net.raphimc.audiomixer.util.PcmFloatAudioFormat;

public class ClippingModifier implements SoundModifier {

    private float multiplier;

    public ClippingModifier() {
        this(1F);
    }

    public ClippingModifier(final float multiplier) {
        this.setMultiplier(multiplier);
    }

    @Override
    public void modify(final PcmFloatAudioFormat audioFormat, final float[] renderedSamples) {
        for (int i = 0; i < renderedSamples.length; i++) {
            renderedSamples[i] = MathUtil.clamp(renderedSamples[i] * this.multiplier, -1F, 1F);
        }
    }

    public float getMultiplier() {
        return this.multiplier;
    }

    public ClippingModifier setMultiplier(final float multiplier) {
        if (multiplier < 1F) {
            throw new IllegalArgumentException("Multiplier must be greater than or equal to 1");
        }
        this.multiplier = multiplier;
        return this;
    }

}
