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
package net.raphimc.audiomixer.dsp.automation.parameter;

import net.raphimc.audiomixer.util.functional.FloatConsumer;
import net.raphimc.audiomixer.util.functional.FloatSupplier;

public class Parameter {

    private final FloatSupplier getter;
    private final FloatConsumer setter;

    public Parameter(final FloatSupplier getter, final FloatConsumer setter) {
        this.getter = getter;
        this.setter = setter;
    }

    public float get() {
        return this.getter.get();
    }

    public void set(final float value) {
        this.setter.accept(value);
    }

    public void multiply(final float value) {
        this.set(this.get() * value);
    }

}
