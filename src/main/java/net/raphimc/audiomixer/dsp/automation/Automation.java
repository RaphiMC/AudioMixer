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
package net.raphimc.audiomixer.dsp.automation;

import net.raphimc.audiomixer.dsp.parameter.FloatParameter;

public abstract class Automation {

    private final FloatParameter parameter;
    protected float time;

    public Automation(final FloatParameter parameter) {
        this.parameter = parameter;
    }

    public void advance(final float deltaTime) {
        this.time += deltaTime / 2F;
        this.apply();
        this.time += deltaTime / 2F;
    }

    protected abstract void apply();

    protected boolean isFinished() {
        return false;
    }

    public FloatParameter getParameter() {
        return this.parameter;
    }

}
