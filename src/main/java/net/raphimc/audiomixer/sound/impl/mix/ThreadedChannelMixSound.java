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
package net.raphimc.audiomixer.sound.impl.mix;

import net.raphimc.audiomixer.util.MathUtil;
import net.raphimc.audiomixer.util.PcmFloatAudioFormat;

import java.util.Arrays;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ThreadedChannelMixSound extends ChannelMixSound implements AutoCloseable {

    private final ThreadPoolExecutor threadPool;
    private final CyclicBarrier startBarrier;
    private final CyclicBarrier stopBarrier;
    private final float[][] threadSamples;

    private PcmFloatAudioFormat currentAudioFormat;
    private int currentRenderSampleCount;

    public ThreadedChannelMixSound(final int threadCount) {
        this(threadCount, 512);
    }

    public ThreadedChannelMixSound(final int threadCount, final int maxSounds) {
        super(threadCount);
        this.threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadCount);
        this.startBarrier = new CyclicBarrier(this.threadPool.getCorePoolSize() + 1);
        this.stopBarrier = new CyclicBarrier(this.threadPool.getCorePoolSize() + 1);
        this.threadSamples = new float[this.threadPool.getCorePoolSize()][];
        this.setMaxSounds(maxSounds);

        for (int i = 0; i < this.threadPool.getCorePoolSize(); i++) {
            final int mixerIndex = i;
            this.threadPool.submit(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        this.startBarrier.await();
                        this.threadSamples[mixerIndex] = new float[this.currentRenderSampleCount];
                        this.getChannel(mixerIndex).render(this.currentAudioFormat, this.threadSamples[mixerIndex]);
                        this.stopBarrier.await();
                    }
                } catch (InterruptedException | BrokenBarrierException ignored) {
                }
            });
        }
    }

    @Override
    public void render(final PcmFloatAudioFormat audioFormat, final float[] finalMixBuffer) {
        final long startTime = System.nanoTime();
        this.currentAudioFormat = audioFormat;
        this.currentRenderSampleCount = finalMixBuffer.length;

        try {
            this.startBarrier.await();
            this.stopBarrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            throw new RuntimeException(e);
        }
        Arrays.fill(finalMixBuffer, 0F);
        for (float[] threadSamples : this.threadSamples) {
            for (int i = 0; i < finalMixBuffer.length; i++) {
                finalMixBuffer[i] += threadSamples[i];
            }
        }
        this.getSoundModifiers().modify(audioFormat, finalMixBuffer);

        final float neededMillis = (System.nanoTime() - startTime) / 1_000_000F;
        final float availableMillis = MathUtil.sampleCountToMillis(audioFormat, finalMixBuffer.length);
        this.cpuLoad = (neededMillis / availableMillis) * 100F;
    }

    @Override
    public void close() {
        this.threadPool.shutdownNow();
    }

}
