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

public class SourceDataLineWriter implements AutoCloseable {

    private final SourceDataLine sourceDataLine;
    private final PcmAudioFormat format;
    private final Thread writerThread;
    private volatile boolean interrupted; // Java clears the interrupt flag in SourceDataLine#write() and doesn't rethrow InterruptedException
    private volatile float processingLoad;

    public SourceDataLineWriter(final SourceDataLine sourceDataLine, final int bufferMillis, final Callback callback) throws LineUnavailableException {
        if (bufferMillis <= 0) {
            throw new IllegalArgumentException("Buffer millis must be > 0: " + bufferMillis);
        }
        this.sourceDataLine = sourceDataLine;
        this.format = JavaAudioFormatUtil.getPcmAudioFormat(this.sourceDataLine.getFormat());
        this.sourceDataLine.open(this.sourceDataLine.getFormat(), this.format.millisToByteCount(bufferMillis));
        this.sourceDataLine.start();
        this.writerThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted() && !this.interrupted) {
                    while (this.sourceDataLine.available() > 0 && !Thread.currentThread().isInterrupted() && !this.interrupted) {
                        final long startTime = System.nanoTime();
                        final AudioBuffer buffer = callback.renderAudio(this.format.byteCountToFrameCount(this.sourceDataLine.available()));
                        if (buffer.sampleCount() <= 0) {
                            break;
                        }
                        final ByteArrayOutputStream baos = new ByteArrayOutputStream(this.format.frameCountToByteCount(buffer.frameCount()));
                        try (PcmAudioOutputStream aos = new PcmAudioOutputStream(baos, this.format)) {
                            aos.write(buffer);
                        }
                        final byte[] pcmData = baos.toByteArray();
                        final float neededMillis = (System.nanoTime() - startTime) / 1_000_000F;
                        this.processingLoad = (neededMillis / buffer.millisecondLength()) * 100F;
                        this.sourceDataLine.write(pcmData, 0, pcmData.length);
                    }
                    Thread.sleep(1);
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

    @Override
    public void close() {
        this.interrupted = true;
        this.writerThread.interrupt();
        this.sourceDataLine.close();
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

    public float getProcessingLoad() {
        return this.processingLoad;
    }

    @FunctionalInterface
    public interface Callback {

        default AudioBuffer renderAudio(final int requestedFrameCount) {
            return this.renderAudio();
        }

        AudioBuffer renderAudio();

    }

}
