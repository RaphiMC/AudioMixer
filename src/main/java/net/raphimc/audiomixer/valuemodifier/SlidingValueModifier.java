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
package net.raphimc.audiomixer.valuemodifier;

public abstract class SlidingValueModifier implements ValueModifier {

    private final float durationMillis;
    private float elapsedMillis;
    private Runnable finishCallback;

    public SlidingValueModifier(final float durationMillis) {
        if (durationMillis <= 0) {
            throw new IllegalArgumentException("Duration must be greater than 0");
        }
        this.durationMillis = durationMillis;
    }

    @Override
    public float modify(final float value, final float referenceFrequency) {
        final float progress = this.elapsedMillis / this.durationMillis;
        final float modifiedValue = this.modify(value, referenceFrequency, progress);
        this.elapsedMillis += 1000F / referenceFrequency;
        if (this.elapsedMillis > this.durationMillis) {
            this.elapsedMillis = this.durationMillis;
        }
        if (this.finishCallback != null && progress == 1F) {
            this.finishCallback.run();
        }
        return modifiedValue;
    }

    protected abstract float modify(final float value, final float referenceFrequency, final float progress);

    public void resetProgress() {
        this.elapsedMillis = 0;
    }

    public float getDurationMillis() {
        return this.durationMillis;
    }

    public float getProgress() {
        return this.elapsedMillis / this.durationMillis;
    }

    public Runnable getFinishCallback() {
        return this.finishCallback;
    }

    public SlidingValueModifier setFinishCallback(final Runnable finishCallback) {
        this.finishCallback = finishCallback;
        return this;
    }

}
