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
package net.raphimc.audiomixer.dsp.automation.ramp;

import net.raphimc.audiomixer.dsp.automation.Automation;
import net.raphimc.audiomixer.dsp.automation.parameter.Parameter;

public abstract class RampAutomation extends Automation {

    private final float startValue;
    private final float endValue;
    private final float duration;

    public RampAutomation(final Parameter parameter, final float startValue, final float endValue, final float duration) {
        super(parameter);
        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be > 0");
        }
        this.startValue = startValue;
        this.endValue = endValue;
        this.duration = duration;
    }

    @Override
    protected void apply() {
        if (this.time <= 0) {
            this.getParameter().set(this.startValue);
        } else if (this.time >= this.duration) {
            this.getParameter().set(this.endValue);
        } else {
            this.apply((float) (this.time / this.duration));
        }
    }

    protected abstract void apply(final float progress);

    @Override
    protected boolean isFinished() {
        return this.time >= this.duration;
    }

    public float getStartValue() {
        return this.startValue;
    }

    public float getEndValue() {
        return this.endValue;
    }

    public float getDuration() {
        return this.duration;
    }

}
