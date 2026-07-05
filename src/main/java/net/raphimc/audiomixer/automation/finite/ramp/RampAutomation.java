/*
 * This file is part of AudioMixer - https://github.com/RaphiMC/AudioMixer
 * Copyright (C) 2024-2026 RK_01/RaphiMC and contributors
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
package net.raphimc.audiomixer.automation.finite.ramp;

import net.raphimc.audiomixer.automation.finite.FiniteAutomation;
import net.raphimc.audiomixer.parameter.FloatParameter;

public abstract class RampAutomation extends FiniteAutomation {

    private final float startValue;
    private final float endValue;

    public RampAutomation(final FloatParameter parameter, final float startValue, final float endValue, final float duration) {
        super(parameter, duration);
        this.startValue = startValue;
        this.endValue = endValue;
    }

    public float getStartValue() {
        return this.startValue;
    }

    public float getEndValue() {
        return this.endValue;
    }

}
