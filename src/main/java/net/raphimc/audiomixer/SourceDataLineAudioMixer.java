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

import net.raphimc.audiomixer.util.io.SampleOutputStream;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SourceDataLineAudioMixer extends NormalizingAudioMixer {

    private final SourceDataLine sourceDataLine;
    private final int mixSliceSampleCount;
    private final int sampleByteSize;

    public SourceDataLineAudioMixer(final SourceDataLine sourceDataLine, final int mixSampleCount) throws LineUnavailableException {
        this(sourceDataLine, 512, mixSampleCount);
    }

    public SourceDataLineAudioMixer(final SourceDataLine sourceDataLine, final int maxSounds, final int mixSampleCount) throws LineUnavailableException {
        this(sourceDataLine, maxSounds, 4000, mixSampleCount);
    }

    public SourceDataLineAudioMixer(final SourceDataLine sourceDataLine, final int maxSounds, final int decayPeriodMillis, final int mixSliceSampleCount) throws LineUnavailableException {
        super(sourceDataLine.getFormat(), maxSounds, decayPeriodMillis);
        this.sourceDataLine = sourceDataLine;
        if (!sourceDataLine.isOpen()) {
            sourceDataLine.open(sourceDataLine.getFormat(), (int) sourceDataLine.getFormat().getSampleRate());
        }
        sourceDataLine.start();

        this.mixSliceSampleCount = mixSliceSampleCount;
        this.sampleByteSize = sourceDataLine.getFormat().getSampleSizeInBits() / 8;
    }

    public void close() {
        this.sourceDataLine.close();
    }

    public void mixSlice() {
        final int[] samples = this.mix(this.mixSliceSampleCount);

        if (this.sourceDataLine.available() < samples.length * this.sampleByteSize) {
            // In case of buffer overrun, flush the queued samples
            this.sourceDataLine.flush();
        }

        final ByteArrayOutputStream baos = new ByteArrayOutputStream(samples.length * this.sampleByteSize);
        final SampleOutputStream sos = new SampleOutputStream(baos, this.sourceDataLine.getFormat());
        try {
            for (int sample : samples) {
                sos.writeSample(sample);
            }
        } catch (IOException ignored) {
        }
        final byte[] sampleData = baos.toByteArray();
        this.sourceDataLine.write(sampleData, 0, sampleData.length);
    }

    public SourceDataLine getSourceDataLine() {
        return this.sourceDataLine;
    }

}
