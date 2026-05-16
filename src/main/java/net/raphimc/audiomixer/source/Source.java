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
package net.raphimc.audiomixer.source;

import net.raphimc.audiomixer.dsp.automation.Automations;
import net.raphimc.audiomixer.dsp.processor.Processors;
import net.raphimc.audiomixer.util.FloatAudioFormat;
import net.raphimc.audiomixer.util.buffer.AudioBuffer;

public abstract class Source {

    private final Automations automations = new Automations();
    private final Processors processors = new Processors();

    public AudioBuffer renderMillis(final FloatAudioFormat format, final float millis) {
        return this.render(format, format.millisToFrameCount(millis));
    }

    public AudioBuffer render(final FloatAudioFormat format, final int frameCount) {
        final AudioBuffer buffer = new AudioBuffer(format, frameCount * format.channels());
        this.render(buffer);
        return buffer;
    }

    public void render(final AudioBuffer audioBuffer) {
        this.automations.update(audioBuffer);
        this.renderDry(audioBuffer);
        this.processors.process(audioBuffer);
    }

    protected abstract void renderDry(final AudioBuffer buffer);

    public boolean isFinished() {
        return false;
    }

    public Automations getAutomations() {
        return this.automations;
    }

    public Processors getProcessors() {
        return this.processors;
    }

}
