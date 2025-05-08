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
package net.raphimc.audiomixer.pcmsource.impl;

import net.raphimc.audiomixer.pcmsource.MonoPcmSource;
import net.raphimc.audiomixer.util.InterpolationUtil;

import java.util.ArrayList;
import java.util.List;

public class MonoPushPcmSource implements MonoPcmSource {

    private final List<float[]> samplesQueue = new ArrayList<>();
    private double position;

    @Override
    public synchronized float consumeSample(final float increment) {
        if (this.samplesQueue.isEmpty()) {
            return 0;
        }
        final float[] samples = this.samplesQueue.get(0);
        if ((int) this.position >= samples.length) {
            this.samplesQueue.remove(0);
            this.position = 0;
            return this.consumeSample(increment);
        }

        final float sample = InterpolationUtil.interpolateLinear(samples, this.position);
        this.position += increment;
        return sample;
    }

    @Override
    public boolean hasReachedEnd() {
        return false;
    }

    public synchronized void enqueueSamples(final float[] samples) {
        if (samples == null || samples.length == 0) {
            throw new IllegalArgumentException("Samples must not be null or empty");
        }

        this.samplesQueue.add(samples);
    }

    public synchronized void flushQueue() {
        this.samplesQueue.clear();
        this.position = 0;
    }

    public synchronized int getQueuedBufferCount() {
        return this.samplesQueue.size();
    }

    public synchronized int getQueuedSampleCount() {
        int total = -(int) this.position;
        for (float[] samples : this.samplesQueue) {
            total += samples.length;
        }
        return total;
    }

}
