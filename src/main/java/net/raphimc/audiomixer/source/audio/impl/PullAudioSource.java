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

import net.raphimc.audiomixer.io.AudioInputStream;
import net.raphimc.audiomixer.resampler.Resampler;
import net.raphimc.audiomixer.resampler.impl.LinearResampler;
import net.raphimc.audiomixer.source.audio.StreamingAudioSource;
import net.raphimc.audiomixer.util.buffer.AudioBuffer;

import java.io.Closeable;
import java.io.IOException;

public class PullAudioSource extends StreamingAudioSource implements Closeable {

    private final AudioInputStream inputStream;
    private final Thread readThread;

    public PullAudioSource(final AudioInputStream inputStream) {
        this(inputStream, 1000);
    }

    public PullAudioSource(final AudioInputStream inputStream, final int bufferMillis) {
        this(inputStream, bufferMillis, LinearResampler.INSTANCE);
    }

    public PullAudioSource(final AudioInputStream inputStream, final int bufferMillis, final Resampler resampler) {
        super(inputStream.getFormat(), resampler);
        if (bufferMillis <= 0) {
            throw new IllegalArgumentException("Buffer millis must be greater than 0");
        }

        this.inputStream = inputStream;
        this.readThread = new Thread(() -> {
            final int bufferFrameCount = this.inputStream.getFormat().millisToFrameCount(bufferMillis);
            final int bufferSampleCount = this.inputStream.getFormat().millisToSampleCount(bufferMillis);
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    while (!Thread.currentThread().isInterrupted() && this.getRemainingFrameCount() < bufferFrameCount) {
                        final float[] buffer = this.inputStream.read(bufferSampleCount);
                        this.enqueueBuffer(new AudioBuffer(this.inputStream.getFormat(), buffer));
                        if (buffer.length < bufferSampleCount) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    Thread.sleep(10);
                }
            } catch (final InterruptedException ignored) {
            } catch (final Throwable e) {
                e.printStackTrace();
            } finally {
                try {
                    this.close();
                } catch (final IOException ignored) {
                }
            }
        }, "AudioMixer PullAudioSource Reader");
        this.readThread.setDaemon(true);
        this.readThread.start();
    }

    @Override
    public boolean isFinished() {
        return !this.readThread.isAlive() && super.isFinished();
    }

    @Override
    public void close() throws IOException {
        this.readThread.interrupt();
        this.inputStream.close();
    }

}
