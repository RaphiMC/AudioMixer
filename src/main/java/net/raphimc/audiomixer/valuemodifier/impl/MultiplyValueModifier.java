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
package net.raphimc.audiomixer.valuemodifier.impl;

import net.raphimc.audiomixer.valuemodifier.ValueModifier;

public class MultiplyValueModifier implements ValueModifier {

    private float modifier;

    public MultiplyValueModifier() {
        this.setModifier(1F);
    }

    public MultiplyValueModifier(final float modifier) {
        this.setModifier(modifier);
    }

    @Override
    public float modify(final float value, final float referenceFrequency) {
        return value * this.modifier;
    }

    public float getModifier() {
        return this.modifier;
    }

    public void setModifier(final float modifier) {
        this.modifier = modifier;
    }

}
