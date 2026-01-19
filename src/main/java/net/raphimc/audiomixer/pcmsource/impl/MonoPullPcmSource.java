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
package net.raphimc.audiomixer.pcmsource.impl;

import net.raphimc.audiomixer.interpolator.Interpolator;
import net.raphimc.audiomixer.interpolator.impl.LinearInterpolator;
import net.raphimc.audiomixer.io.raw.SampleInputStream;
import net.raphimc.audiomixer.util.MathUtil;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;

public class MonoPullPcmSource extends MonoPushPcmSource implements Closeable {

    private final SampleInputStream sampleInputStream;
    private final Thread readThread;

    public MonoPullPcmSource(final SampleInputStream sampleInputStream) {
        this(sampleInputStream, 1000);
    }

    public MonoPullPcmSource(final SampleInputStream sampleInputStream, final int bufferMillis) {
        this(sampleInputStream, bufferMillis, LinearInterpolator.INSTANCE);
    }

    public MonoPullPcmSource(final SampleInputStream sampleInputStream, final int bufferMillis, final Interpolator interpolator) {
        super(interpolator);
        if (bufferMillis <= 0) {
            throw new IllegalArgumentException("Buffer millis must be greater than 0");
        }

        final int bufferFrameCount = MathUtil.millisToFrameCount(sampleInputStream.getFormat(), bufferMillis);
        this.sampleInputStream = sampleInputStream;
        this.readThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    while (!Thread.currentThread().isInterrupted() && this.getQueuedFrameCount() < bufferFrameCount) {
                        float[] buffer = new float[0];
                        int bufferLength = 0;
                        try {
                            buffer = new float[bufferFrameCount];
                            for (bufferLength = 0; bufferLength < buffer.length; bufferLength++) {
                                buffer[bufferLength] = this.sampleInputStream.readSample();
                            }
                        } catch (IOException e) {
                            Thread.currentThread().interrupt();
                        }
                        if (bufferLength > 0) {
                            if (buffer.length != bufferLength) {
                                this.enqueueSamples(Arrays.copyOf(buffer, bufferLength));
                            } else {
                                this.enqueueSamples(buffer);
                            }
                        }
                    }
                    Thread.sleep(100);
                }
            } catch (InterruptedException ignored) {
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                try {
                    this.close();
                } catch (IOException ignored) {
                }
            }
        }, "AudioMixer MonoPullPcmSource Reader");
        this.readThread.setDaemon(true);
        this.readThread.start();
    }

    @Override
    public boolean hasReachedEnd() {
        return !this.readThread.isAlive() && this.getQueuedFrameCount() == 0;
    }

    @Override
    public void close() throws IOException {
        this.readThread.interrupt();
        this.sampleInputStream.close();
    }

}
