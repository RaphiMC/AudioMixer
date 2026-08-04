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
package net.raphimc.audiomixer.processor;

import net.raphimc.audiomixer.automation.Automations;
import net.raphimc.audiomixer.util.buffer.AudioBuffer;

public abstract class Processor {

    private final Automations automations = new Automations();
    private boolean enabled = true;

    public void process(final AudioBuffer buffer) {
        if (this.enabled) {
            this.automations.process(buffer);
            this.processInternal(buffer);
        }
    }

    protected abstract void processInternal(final AudioBuffer buffer);

    public Automations automations() {
        return this.automations;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

}
