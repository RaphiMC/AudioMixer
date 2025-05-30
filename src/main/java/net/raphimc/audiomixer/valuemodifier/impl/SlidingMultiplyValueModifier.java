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
package net.raphimc.audiomixer.valuemodifier.impl;

import net.raphimc.audiomixer.valuemodifier.SlidingValueModifier;

public class SlidingMultiplyValueModifier extends SlidingValueModifier {

    private final float startModifier;
    private final float endModifier;

    public SlidingMultiplyValueModifier(final float startModifier, final float endModifier, final float durationMillis) {
        super(durationMillis);

        this.startModifier = startModifier;
        this.endModifier = endModifier;
    }

    @Override
    public float modify(final float value, final float referenceFrequency, final float progress) {
        return value * (this.startModifier + (this.endModifier - this.startModifier) * progress);
    }

    public float getStartModifier() {
        return this.startModifier;
    }

    public float getEndModifier() {
        return this.endModifier;
    }

}
