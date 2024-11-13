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
package net.raphimc.audiomixer;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BackgroundSourceDataLineAudioMixer extends SourceDataLineAudioMixer {

    private final ScheduledExecutorService mixingScheduler;

    public BackgroundSourceDataLineAudioMixer(final SourceDataLine sourceDataLine) throws LineUnavailableException {
        this(sourceDataLine, 20);
    }

    public BackgroundSourceDataLineAudioMixer(final SourceDataLine sourceDataLine, final int updatePeriodMillis) throws LineUnavailableException {
        super(sourceDataLine, (int) Math.ceil(sourceDataLine.getFormat().getSampleRate() / 1000F * updatePeriodMillis) * sourceDataLine.getFormat().getChannels());

        this.mixingScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            final Thread thread = new Thread(r, "AudioMixer-MixingThread");
            thread.setDaemon(true);
            return thread;
        });
        this.mixingScheduler.scheduleAtFixedRate(this::mixSlice, updatePeriodMillis, updatePeriodMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void setMixSliceSampleCount(final int mixSliceSampleCount) {
        throw new UnsupportedOperationException("Cannot change mix slice sample count for auto-mixing audio mixer");
    }

    public boolean isRunning() {
        return !this.mixingScheduler.isTerminated();
    }

    public void close() {
        this.mixingScheduler.shutdownNow();
        try {
            this.mixingScheduler.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
        }
        super.close();
    }

}
