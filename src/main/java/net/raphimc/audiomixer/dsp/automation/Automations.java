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

import net.raphimc.audiomixer.util.buffer.AudioBuffer;

import java.util.ArrayList;
import java.util.List;

public class Automations {

    private final List<Automation> automations = new ArrayList<>(0);

    public synchronized void update(final AudioBuffer buffer) {
        if (!this.automations.isEmpty()) {
            for (Automation automation : this.automations) {
                automation.advance(buffer.getMillisecondLength());
            }
            this.automations.removeIf(Automation::isFinished);
        }
    }

    public synchronized void add(final Automation automation) {
        this.automations.add(automation);
    }

    public synchronized void remove(final Automation automation) {
        this.automations.remove(automation);
    }

    public synchronized void clear() {
        this.automations.clear();
    }

}
