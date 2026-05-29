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

import net.raphimc.audiomixer.util.buffer.AudioBuffer;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ParallelMixerSource extends GroupedMixerSource implements AutoCloseable {

    private final ThreadPoolExecutor threadPool;
    private final CyclicBarrier startBarrier;
    private final CyclicBarrier stopBarrier;
    private final AudioBuffer[] threadBuffers;

    public ParallelMixerSource(final int threads) {
        this(threads, 512);
    }

    public ParallelMixerSource(final int threads, final int maxSources) {
        super(threads);
        this.threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(threads);
        this.startBarrier = new CyclicBarrier(this.threadPool.getCorePoolSize() + 1);
        this.stopBarrier = new CyclicBarrier(this.threadPool.getCorePoolSize() + 1);
        this.threadBuffers = new AudioBuffer[this.threadPool.getCorePoolSize()];
        this.setMaxSources(maxSources);

        for (int i = 0; i < this.threadPool.getCorePoolSize(); i++) {
            final int groupIndex = i;
            this.threadPool.submit(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        this.startBarrier.await();
                        this.getGroup(groupIndex).render(this.threadBuffers[groupIndex]);
                        this.stopBarrier.await();
                    }
                } catch (InterruptedException | BrokenBarrierException ignored) {
                }
            });
        }
    }

    @Override
    protected void renderInternal(final AudioBuffer buffer) {
        for (int i = 0; i < this.threadBuffers.length; i++) {
            this.threadBuffers[i] = buffer.createWorkBuffer();
        }
        try {
            this.startBarrier.await();
            this.stopBarrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            throw new RuntimeException(e);
        }
        for (AudioBuffer threadBuffer : this.threadBuffers) {
            buffer.add(threadBuffer);
        }
    }

    @Override
    public void close() {
        this.threadPool.shutdownNow();
    }

}
