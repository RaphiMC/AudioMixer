/*
 * This file is part of AudioMixer - https://github.com/RaphiMC/AudioMixer
 * Copyright (C) 2024-2024 RK_01/RaphiMC and contributors
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
package net.raphimc.audiomixer.sound.pcmsource.impl;

import net.raphimc.audiomixer.sound.pcmsource.PcmSource;
import net.raphimc.audiomixer.util.InterpolationUtil;

import java.util.LinkedList;
import java.util.Queue;

public class PushIntPcmSource implements PcmSource {

    private final Queue<int[]> samples = new LinkedList<>();
    private double position;

    @Override
    public synchronized int getCurrentSample() {
        final int[] currentSamples = this.samples.peek();
        if (currentSamples == null) {
            return 0;
        }
        if ((int) this.position >= currentSamples.length) {
            this.samples.poll();
            this.position = 0;
            return this.getCurrentSample();
        }

        return InterpolationUtil.interpolateLinear(currentSamples, this.position);
    }

    @Override
    public synchronized void incrementPosition(final double increment) {
        if (!this.samples.isEmpty()) {
            this.position += increment;
        }
    }

    @Override
    public boolean hasReachedEnd() {
        return false;
    }

    public synchronized void enqueueSamples(final int[] samples) {
        this.samples.add(samples);
    }

    public synchronized int getQueuedSampleCount() {
        int total = -(int) this.position;
        for (int[] sample : this.samples) {
            total += sample.length;
        }
        return total;
    }

}
