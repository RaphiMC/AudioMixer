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

import net.raphimc.audiomixer.io.raw.SampleOutputStream;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.ByteArrayOutputStream;

public class SourceDataLineWriter implements AutoCloseable {

    private final SourceDataLine sourceDataLine;
    private final Callback callback;
    private Thread writerThread;
    private boolean interrupted; // Java clears the interrupt flag in SourceDataLine#write() and doesn't rethrow InterruptedException
    private float cpuLoad;

    public SourceDataLineWriter(final SourceDataLine sourceDataLine, final int bufferMillis, final Callback callback) throws LineUnavailableException {
        if (bufferMillis <= 0) {
            throw new IllegalArgumentException("Buffer millis must be greater than 0");
        }

        this.sourceDataLine = sourceDataLine;
        this.sourceDataLine.open(this.sourceDataLine.getFormat(), MathUtil.millisToByteCount(this.sourceDataLine.getFormat(), bufferMillis));
        this.callback = callback;
    }

    public void start() {
        if (this.isRunning()) {
            this.stop();
        }

        TimerHack.ensureRunning();
        this.interrupted = false;
        this.writerThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted() && !this.interrupted) {
                    while (this.sourceDataLine.available() > 0 && !Thread.currentThread().isInterrupted() && !this.interrupted) {
                        final long startTime = System.nanoTime();
                        final float[] samples = this.callback.provideSamples(MathUtil.byteCountToSampleCount(this.sourceDataLine.getFormat(), this.sourceDataLine.available()));
                        final ByteArrayOutputStream baos = new ByteArrayOutputStream(MathUtil.sampleCountToByteCount(this.sourceDataLine.getFormat(), samples.length));
                        final SampleOutputStream sos = new SampleOutputStream(baos, this.sourceDataLine.getFormat());
                        for (float sample : samples) {
                            sos.writeSample(sample);
                        }
                        final byte[] sampleData = baos.toByteArray();
                        if (!this.sourceDataLine.isActive()) {
                            this.sourceDataLine.start();
                        }
                        final float neededMillis = (System.nanoTime() - startTime) / 1_000_000F;
                        final float availableMillis = MathUtil.sampleCountToMillis(this.sourceDataLine.getFormat(), samples.length);
                        this.cpuLoad = (neededMillis / availableMillis) * 100F;
                        this.sourceDataLine.write(sampleData, 0, sampleData.length);
                    }
                    Thread.sleep(1);
                }
            } catch (InterruptedException ignored) {
            } catch (Throwable e) {
                e.printStackTrace();
                this.close();
            }
        }, "AudioMixer SourceDataLine Writer");
        this.writerThread.setPriority(Thread.NORM_PRIORITY + 1);
        this.writerThread.setDaemon(true);
        this.writerThread.start();
    }

    public void flush() {
        this.sourceDataLine.stop(); // Will be started again in the writer thread
        this.sourceDataLine.flush();
    }

    public void stop() {
        if (this.isRunning()) {
            this.writerThread.interrupt();
            this.interrupted = true;
            try {
                this.writerThread.join(1000);
            } catch (InterruptedException ignored) {
            }
            this.writerThread = null;
        }
        this.sourceDataLine.stop();
    }

    @Override
    public void close() {
        this.stop();
        this.sourceDataLine.close();
    }

    public boolean isRunning() {
        return this.writerThread != null && this.writerThread.isAlive();
    }

    public SourceDataLine getSourceDataLine() {
        return this.sourceDataLine;
    }

    public float getCpuLoad() {
        return this.cpuLoad;
    }

    @FunctionalInterface
    public interface Callback {

        default float[] provideSamples(final int availableSampleCount) {
            return this.provideSamples();
        }

        float[] provideSamples();

    }

}
