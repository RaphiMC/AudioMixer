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
package net.raphimc.audiomixer.util;

import net.raphimc.audiomixer.io.raw.SampleOutputStream;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;

public class SourceDataLineWriter implements Closeable {

    private final SourceDataLine sourceDataLine;
    private final int minBufferMillis;
    private final CircularBuffer buffer;
    private Thread writerThread;

    public SourceDataLineWriter(final SourceDataLine sourceDataLine, final int minBufferMillis, final int maxBufferMillis) {
        if (minBufferMillis <= 0) {
            throw new IllegalArgumentException("Min buffer millis must be greater than 0");
        }
        if (maxBufferMillis < minBufferMillis * 2) {
            throw new IllegalArgumentException("Max buffer millis must be at least double the min buffer millis");
        }

        this.sourceDataLine = sourceDataLine;
        this.minBufferMillis = minBufferMillis;
        this.buffer = new CircularBuffer(MathUtil.millisToByteCount(sourceDataLine.getFormat(), maxBufferMillis));
    }

    public void start() throws LineUnavailableException {
        if (this.isRunning()) {
            this.close();
        }

        if (!this.sourceDataLine.isOpen()) {
            this.sourceDataLine.open(this.sourceDataLine.getFormat(), MathUtil.millisToByteCount(this.sourceDataLine.getFormat(), this.minBufferMillis));
        }

        TimerHack.ensureRunning();
        this.writerThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    if (this.sourceDataLine.isActive()) {
                        if (this.sourceDataLine.available() >= this.sourceDataLine.getBufferSize()) { // Stop the line if it has completely drained, will be started again when enough data is available
                            this.sourceDataLine.stop();
                        } else { // Line is active and has space available
                            if (this.sourceDataLine.available() > 0 && !this.buffer.isEmpty()) { // Write data if there's space available and data to write
                                final byte[] data = this.buffer.readAllSafe(Math.min(this.buffer.getSize(), this.sourceDataLine.available()));
                                this.sourceDataLine.write(data, 0, data.length);
                            } else { // Nothing to write or no space available, wait a bit
                                Thread.sleep(1);
                            }
                        }
                    } else { // Line is not active
                        if (this.buffer.getSize() >= this.sourceDataLine.getBufferSize()) { // Start the line when at least one buffer worth of data is available
                            this.sourceDataLine.start();
                            final byte[] data = this.buffer.readAllSafe(Math.min(this.buffer.getSize(), this.sourceDataLine.available()));
                            this.sourceDataLine.write(data, 0, data.length);
                        } else { // Not enough data yet, wait a bit
                            Thread.sleep(1);
                        }
                    }
                }
            } catch (InterruptedException ignored) {
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }, "AudioMixer SourceDataLine Writer");
        this.writerThread.setPriority(Thread.NORM_PRIORITY + 1);
        this.writerThread.setDaemon(true);
        this.writerThread.start();
    }

    public void write(final float[] samples) {
        if (!this.isRunning()) {
            return;
        }

        final ByteArrayOutputStream baos = new ByteArrayOutputStream(MathUtil.sampleCountToByteCount(this.sourceDataLine.getFormat(), samples.length));
        final SampleOutputStream sos = new SampleOutputStream(baos, this.sourceDataLine.getFormat());
        try {
            for (float sample : samples) {
                sos.writeSample(sample);
            }
        } catch (IOException ignored) {
        }
        final byte[] sampleData = baos.toByteArray();
        if (sampleData.length > this.buffer.getCapacity()) {
            throw new IllegalArgumentException("Sample data is larger than buffer capacity (" + sampleData.length + " > " + this.buffer.getCapacity() + ")");
        }

        while (this.isRunning() && !this.buffer.hasSpaceFor(sampleData.length)) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                return;
            }
        }
        this.buffer.writeAll(sampleData);
    }

    public void flush() {
        this.buffer.clear();
        this.sourceDataLine.stop(); // Will be started again in the writer thread when data is written
        this.sourceDataLine.flush();
    }

    @Override
    public void close() {
        if (this.isRunning()) {
            this.writerThread.interrupt();
            try {
                this.writerThread.join(1000);
            } catch (InterruptedException ignored) {
            }
            this.writerThread = null;
        }
        this.sourceDataLine.close();
    }

    public boolean canWriteSamplesWithoutBlocking(final int sampleCount) {
        return this.buffer.hasSpaceFor(MathUtil.sampleCountToByteCount(this.sourceDataLine.getFormat(), sampleCount));
    }

    public boolean canWriteMillisWithoutBlocking(final float millis) {
        return this.canWriteSamplesWithoutBlocking(MathUtil.millisToSampleCount(this.sourceDataLine.getFormat(), millis));
    }

    public boolean isRunning() {
        return this.writerThread != null && this.writerThread.isAlive();
    }

    public float getBufferFillMillis() {
        return MathUtil.byteCountToMillis(this.sourceDataLine.getFormat(), this.buffer.getSize());
    }

    public float getSourceDataLineFillMillis() {
        return MathUtil.byteCountToMillis(this.sourceDataLine.getFormat(), this.sourceDataLine.getBufferSize() - this.sourceDataLine.available());
    }

    public float getTotalFillMillis() {
        return this.getBufferFillMillis() + this.getSourceDataLineFillMillis();
    }

    public float getBufferFillPercentage() {
        return (this.buffer.getSize() / (float) this.buffer.getCapacity()) * 100F;
    }

    public float getSourceDataLineFillPercentage() {
        return ((this.sourceDataLine.getBufferSize() - this.sourceDataLine.available()) / (float) this.sourceDataLine.getBufferSize()) * 100F;
    }

    public SourceDataLine getSourceDataLine() {
        return this.sourceDataLine;
    }

    public CircularBuffer getBuffer() {
        return this.buffer;
    }

}
