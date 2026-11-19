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
package net.raphimc.audiomixer.util;

import net.raphimc.audiomixer.io.pcm.PcmAudioOutputStream;
import net.raphimc.audiomixer.util.buffer.AudioBuffer;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.ByteArrayOutputStream;
import java.util.function.Supplier;

public class SourceDataLineWriter implements AutoCloseable {

    private final SourceDataLine sourceDataLine;
    private final PcmAudioFormat format;
    private final Thread writerThread;
    private volatile boolean started; // SourceDataLine#isRunning() can remain false until the first write after SourceDataLine#start()
    private volatile boolean closed; // Java clears the interrupt flag in SourceDataLine#write() and doesn't rethrow InterruptedException
    private volatile float processingLoad;

    public SourceDataLineWriter(final SourceDataLine sourceDataLine, final int bufferMillis, final Supplier<AudioBuffer> audioSupplier) throws LineUnavailableException {
        if (bufferMillis <= 0) {
            throw new IllegalArgumentException("Buffer millis must be > 0: " + bufferMillis);
        }
        this.sourceDataLine = sourceDataLine;
        this.format = JavaAudioFormatUtil.getPcmAudioFormat(this.sourceDataLine.getFormat());
        this.sourceDataLine.open(this.sourceDataLine.getFormat(), this.format.millisToByteCount(bufferMillis));
        this.writerThread = new Thread(() -> {
            try (
                ByteArrayOutputStream encodedOutput = new ByteArrayOutputStream();
                PcmAudioOutputStream encoder = new PcmAudioOutputStream(encodedOutput, this.format)
            ) {
                while (this.awaitRunningOrClosed()) {
                    final long startTime = System.nanoTime();
                    final AudioBuffer buffer = audioSupplier.get();
                    if (buffer.sampleCount() <= 0) {
                        continue;
                    }
                    encodedOutput.reset();
                    encoder.write(buffer);
                    final byte[] encodedSamples = encodedOutput.toByteArray();
                    final float neededMillis = (System.nanoTime() - startTime) / 1_000_000F;
                    this.processingLoad = (neededMillis / buffer.millisecondLength()) * 100F;

                    int offset = 0;
                    while (offset < encodedSamples.length && this.awaitRunningOrClosed()) {
                        offset += this.sourceDataLine.write(encodedSamples, offset, encodedSamples.length - offset);
                    }
                }
            } catch (final InterruptedException ignored) {
            } catch (final Throwable e) {
                e.printStackTrace();
                this.close();
            }
        }, "AudioMixer SourceDataLine Writer");
        this.writerThread.setPriority(Thread.NORM_PRIORITY + 1);
        this.writerThread.setDaemon(true);
        this.writerThread.start();
    }

    public synchronized void start() {
        if (this.closed) {
            throw new IllegalStateException("SourceDataLineWriter is closed");
        }
        this.sourceDataLine.start();
        this.started = true;
        this.notifyAll();
    }

    public synchronized void stop() {
        if (this.closed) {
            throw new IllegalStateException("SourceDataLineWriter is closed");
        }
        this.started = false;
        this.sourceDataLine.stop();
    }

    @Override
    public void close() {
        synchronized (this) {
            this.started = false;
            this.closed = true;
            this.writerThread.interrupt();
            this.sourceDataLine.close();
        }
        if (Thread.currentThread() != this.writerThread) {
            try {
                this.writerThread.join();
            } catch (final InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public SourceDataLine getSourceDataLine() {
        return this.sourceDataLine;
    }

    public boolean isRunning() {
        return this.started;
    }

    public float getProcessingLoad() {
        return this.processingLoad;
    }

    private synchronized boolean awaitRunningOrClosed() throws InterruptedException {
        while (!this.started && !this.closed) {
            this.wait();
        }
        return !Thread.currentThread().isInterrupted() && !this.closed;
    }

}
