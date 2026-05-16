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
package net.raphimc.audiomixer.source.audio.impl;

import net.raphimc.audiomixer.dsp.resampler.Resampler;
import net.raphimc.audiomixer.dsp.resampler.impl.LinearResampler;
import net.raphimc.audiomixer.io.raw.SampleInputStream;
import net.raphimc.audiomixer.source.audio.StreamingAudioSource;
import net.raphimc.audiomixer.util.buffer.AudioBufferBuilder;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;

public class PullAudioSource extends StreamingAudioSource implements Closeable {

    private final SampleInputStream sampleInputStream;
    private final Thread readThread;

    public PullAudioSource(final SampleInputStream sampleInputStream) {
        this(sampleInputStream, 1000);
    }

    public PullAudioSource(final SampleInputStream sampleInputStream, final int bufferMillis) {
        this(sampleInputStream, bufferMillis, LinearResampler.INSTANCE);
    }

    public PullAudioSource(final SampleInputStream sampleInputStream, final int bufferMillis, final Resampler resampler) {
        super(sampleInputStream.getFormat(), resampler);
        if (bufferMillis <= 0) {
            throw new IllegalArgumentException("Buffer millis must be greater than 0");
        }

        this.sampleInputStream = sampleInputStream;
        this.readThread = new Thread(() -> {
            final int bufferFrameCount = this.sampleInputStream.getFormat().millisToFrameCount(bufferMillis);
            final int bufferSampleCount = this.sampleInputStream.getFormat().millisToSampleCount(bufferMillis);
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    while (!Thread.currentThread().isInterrupted() && this.getRemainingFrameCount() < bufferFrameCount) {
                        final AudioBufferBuilder bufferBuilder = new AudioBufferBuilder(this.sampleInputStream.getFormat(), bufferSampleCount);
                        try {
                            for (int i = 0; i < bufferSampleCount; i++) {
                                bufferBuilder.put(this.sampleInputStream.readSample());
                            }
                        } catch (EOFException e) {
                            Thread.currentThread().interrupt();
                        }
                        this.enqueueBuffer(bufferBuilder.build());
                        Thread.sleep(10);
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
        }, "AudioMixer PullAudioSource Reader");
        this.readThread.setDaemon(true);
        this.readThread.start();
    }

    @Override
    public boolean isFinished() {
        return !this.readThread.isAlive() && this.getRemainingFrameCount() <= 0;
    }

    @Override
    public void close() throws IOException {
        this.readThread.interrupt();
        this.sampleInputStream.close();
    }

}
