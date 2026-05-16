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
package net.raphimc.audiomixer.source.mixer;

import net.raphimc.audiomixer.source.Source;
import net.raphimc.audiomixer.util.buffer.AudioBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class MixerSource extends Source {

    private final List<Source> sources = new ArrayList<>();
    private int maxSources;

    private float processingLoad;

    public MixerSource() {
        this(512);
    }

    public MixerSource(final int maxSources) {
        this.setMaxSources(maxSources);
    }

    @Override
    public void render(final AudioBuffer audioBuffer) {
        final long startTime = System.nanoTime();
        super.render(audioBuffer);
        final float neededMillis = (System.nanoTime() - startTime) / 1_000_000F;
        final float availableMillis = audioBuffer.getMillisecondLength();
        this.processingLoad = (neededMillis / availableMillis) * 100F;
    }

    @Override
    protected synchronized void renderDry(final AudioBuffer buffer) {
        final AudioBuffer sourceBuffer = new AudioBuffer(buffer.format(), buffer.samples().length);
        for (Source source : this.sources) {
            sourceBuffer.clear();
            source.render(sourceBuffer);
            buffer.mix(sourceBuffer);
        }
        this.sources.removeIf(Source::isFinished);
    }

    public synchronized void add(final Source source) {
        if (this.sources.size() >= this.maxSources) {
            this.sources.remove(0);
        }
        this.sources.add(source);
    }

    public synchronized void remove(final Source source) {
        this.sources.remove(source);
    }

    public synchronized void clear() {
        this.sources.clear();
    }

    public synchronized void forEach(final Consumer<Source> action) {
        this.sources.forEach(action);
    }

    public synchronized void removeIf(final Predicate<Source> predicate) {
        this.sources.removeIf(predicate);
    }

    public int getMaxSources() {
        return this.maxSources;
    }

    public void setMaxSources(final int maxSources) {
        if (maxSources < 1) {
            throw new IllegalArgumentException("Max sources must be >= 1");
        }
        this.maxSources = maxSources;
    }

    public int getActiveSources() {
        return this.sources.size();
    }

    public float getProcessingLoad() {
        return this.processingLoad;
    }

}
