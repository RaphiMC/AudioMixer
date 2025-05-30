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

import net.raphimc.audiomixer.interpolator.Interpolator;
import net.raphimc.audiomixer.interpolator.impl.LinearInterpolator;
import net.raphimc.audiomixer.pcmsource.StereoPcmSource;

import java.util.ArrayList;
import java.util.List;

public class StereoPushPcmSource implements StereoPcmSource {

    private final List<float[]> samplesQueue = new ArrayList<>();
    private final float[] buffer = new float[2];
    private final Interpolator interpolator;
    private double position;

    public StereoPushPcmSource() {
        this(LinearInterpolator.INSTANCE);
    }

    public StereoPushPcmSource(final Interpolator interpolator) {
        if (interpolator == null) {
            throw new IllegalArgumentException("Interpolator must not be null");
        }
        this.interpolator = interpolator;
    }

    @Override
    public synchronized float[] consumeSample(final float increment) {
        if (this.samplesQueue.isEmpty()) {
            this.buffer[0] = 0F;
            this.buffer[1] = 0F;
            return this.buffer;
        }
        final float[] samples = this.samplesQueue.get(0);
        if ((int) this.position * 2 >= samples.length) {
            this.samplesQueue.remove(0);
            this.position = 0;
            return this.consumeSample(increment);
        }

        this.buffer[0] = this.interpolator.interpolate(samples, this.position, 0, 2);
        this.buffer[1] = this.interpolator.interpolate(samples, this.position, 1, 2);
        this.position += increment;
        return this.buffer;
    }

    @Override
    public boolean hasReachedEnd() {
        return false;
    }

    public synchronized void enqueueSamples(final float[] samples) {
        if (samples == null || samples.length == 0) {
            throw new IllegalArgumentException("Samples must not be null or empty");
        }
        if (samples.length % 2 != 0) {
            throw new IllegalArgumentException("Sample count must be a multiple of 2");
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
        int total = -(int) this.position * 2;
        for (float[] samples : this.samplesQueue) {
            total += samples.length;
        }
        return total / 2;
    }

}
