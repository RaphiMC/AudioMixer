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
package net.raphimc.audiomixer.automation.finite;

import net.raphimc.audiomixer.automation.Automation;
import net.raphimc.audiomixer.parameter.FloatParameter;
import net.raphimc.audiomixer.util.ListenerList;
import net.raphimc.audiomixer.util.math.MathUtil;

public abstract class FiniteAutomation extends Automation {

    private final ListenerList<Automation> finishListeners = new ListenerList<>();
    private final float duration;

    public FiniteAutomation(final FloatParameter parameter, final float duration) {
        super(parameter);
        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be > 0");
        }
        this.duration = duration;
    }

    @Override
    public void advance(final float deltaTime) {
        super.advance(deltaTime);
        if (this.isFinished()) {
            this.finishListeners.invoke(this);
        }
    }

    public boolean isFinished() {
        return this.getTime() >= this.duration;
    }

    public ListenerList<Automation> finishListeners() {
        return this.finishListeners;
    }

    public float getProgress() {
        return MathUtil.clamp(this.getTime() / this.duration, 0F, 1F);
    }

    public void setProgress(final float progress) {
        this.setTime(progress * this.duration);
    }

    public float getDuration() {
        return this.duration;
    }

}
