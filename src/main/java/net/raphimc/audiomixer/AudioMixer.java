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
package net.raphimc.audiomixer;

import net.raphimc.audiomixer.mixer.Mixer;
import net.raphimc.audiomixer.util.AudioFormat;
import net.raphimc.audiomixer.util.buffer.AudioBuffer;
import net.raphimc.audiomixer.util.collection.RunnableTaskList;

public class AudioMixer extends Mixer {

    private final AudioFormat format;
    private final RunnableTaskList preRenderTasks = new RunnableTaskList();

    public AudioMixer(final AudioFormat format) {
        this.format = format;
    }

    public AudioBuffer renderMillis(final float millis) {
        return this.render(this.format.millisToFrameCount(millis));
    }

    public AudioBuffer render(final int frameCount) {
        final AudioBuffer buffer = new AudioBuffer(this.format, frameCount);
        this.render(buffer);
        return buffer;
    }

    @Override
    public void render(final AudioBuffer buffer) {
        this.preRenderTasks.run();
        super.render(buffer);
    }

    public AudioFormat getFormat() {
        return this.format;
    }

    public RunnableTaskList preRenderTasks() {
        return this.preRenderTasks;
    }

}
