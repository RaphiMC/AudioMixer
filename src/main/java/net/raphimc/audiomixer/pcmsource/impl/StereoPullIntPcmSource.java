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
package net.raphimc.audiomixer.pcmsource.impl;

import net.raphimc.audiomixer.util.io.SampleInputStream;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;

public class StereoPullIntPcmSource extends StereoPushIntPcmSource implements Closeable {

    private final SampleInputStream sampleInputStream;
    private final int bufferSize;
    private final Thread readThread;
    private volatile boolean reachedEnd;

    public StereoPullIntPcmSource(final SampleInputStream sampleInputStream) {
        this(sampleInputStream, 1024 * 1024);
    }

    public StereoPullIntPcmSource(final SampleInputStream sampleInputStream, final int bufferSize) {
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("Buffer size must be greater than 0");
        }
        if (bufferSize % 2 != 0) {
            throw new IllegalArgumentException("Buffer size must be a multiple of 2");
        }

        this.sampleInputStream = sampleInputStream;
        this.bufferSize = bufferSize;
        this.readThread = new Thread(() -> {
            try {
                while (!this.reachedEnd) {
                    while (!this.reachedEnd && this.getQueuedSampleCount() < this.bufferSize) {
                        int[] buffer = new int[0];
                        int bufferLen = 0;
                        try {
                            buffer = new int[this.bufferSize];
                            for (bufferLen = 0; bufferLen < buffer.length; bufferLen++) {
                                buffer[bufferLen] = this.sampleInputStream.readSample();
                            }
                        } catch (IOException ignored) {
                            this.reachedEnd = true;
                        }
                        if (bufferLen > 0) {
                            if (buffer.length != bufferLen) {
                                this.enqueueSamples(Arrays.copyOf(buffer, bufferLen));
                            } else {
                                this.enqueueSamples(buffer);
                            }
                        }
                    }

                    Thread.sleep(100);
                }
            } catch (Throwable e) {
                if (e.getCause() instanceof InterruptedException) return;

                e.printStackTrace();
                this.reachedEnd = true;
            }
        }, "StereoPullIntPcmSource-ReadThread");
        this.readThread.setDaemon(true);
        this.readThread.start();
    }

    @Override
    public boolean hasReachedEnd() {
        return this.reachedEnd && this.getQueuedSampleCount() == 0;
    }

    @Override
    public void close() throws IOException {
        this.reachedEnd = true;
        this.readThread.interrupt();
        this.sampleInputStream.close();
    }

}