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
package net.raphimc.audiomixer;

import net.raphimc.audiomixer.soundmodifier.impl.NormalizationModifier;
import net.raphimc.audiomixer.soundmodifier.impl.VolumeModifier;
import net.raphimc.audiomixer.util.PcmFloatAudioFormat;
import net.raphimc.audiomixer.util.SourceDataLineWriter;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.Closeable;

public class SourceDataLineAudioMixer extends AudioMixer implements Closeable {

    private final SourceDataLineWriter sourceDataLineWriter;
    private BufferOverrunStrategy bufferOverrunStrategy = BufferOverrunStrategy.DO_NOTHING;
    private final NormalizationModifier normalizationModifier = new NormalizationModifier();
    private final VolumeModifier volumeModifier = new VolumeModifier(1F);

    public SourceDataLineAudioMixer(final SourceDataLine sourceDataLine) throws LineUnavailableException {
        this(sourceDataLine, 20, 100);
    }

    public SourceDataLineAudioMixer(final SourceDataLine sourceDataLine, final int minBufferMillis, final int maxBufferMillis) throws LineUnavailableException {
        super(new PcmFloatAudioFormat(sourceDataLine.getFormat()));
        this.sourceDataLineWriter = new SourceDataLineWriter(sourceDataLine, minBufferMillis, maxBufferMillis);
        this.sourceDataLineWriter.start();

        this.getSoundModifiers().append(this.normalizationModifier);
        this.getSoundModifiers().append(this.volumeModifier);
    }

    @Override
    public void stopAllSounds() {
        super.stopAllSounds();
        this.sourceDataLineWriter.flush();
        this.normalizationModifier.reset();
    }

    @Override
    public AudioFormat getAudioFormat() {
        return this.sourceDataLineWriter.getSourceDataLine().getFormat();
    }

    public void mixAndWriteMillis(final float millis) {
        if (this.bufferOverrunStrategy == BufferOverrunStrategy.DO_NOTHING && !this.sourceDataLineWriter.canWriteMillisWithoutBlocking(millis)) {
            return;
        }
        final float[] samples = this.mixMillis(millis);
        if (this.bufferOverrunStrategy == BufferOverrunStrategy.FLUSH && !this.sourceDataLineWriter.canWriteSamplesWithoutBlocking(samples.length)) {
            this.sourceDataLineWriter.flush();
        }
        this.sourceDataLineWriter.write(samples);
    }

    public void mixAndWrite(final int sampleCount) {
        if (this.bufferOverrunStrategy == BufferOverrunStrategy.DO_NOTHING && !this.sourceDataLineWriter.canWriteSamplesWithoutBlocking(sampleCount)) {
            return;
        }
        final float[] samples = this.mix(sampleCount);
        if (this.bufferOverrunStrategy == BufferOverrunStrategy.FLUSH && !this.sourceDataLineWriter.canWriteSamplesWithoutBlocking(samples.length)) {
            this.sourceDataLineWriter.flush();
        }
        this.sourceDataLineWriter.write(samples);
    }

    @Override
    public void close() {
        this.sourceDataLineWriter.close();
    }

    public SourceDataLineWriter getSourceDataLineWriter() {
        return this.sourceDataLineWriter;
    }

    public BufferOverrunStrategy getBufferOverrunStrategy() {
        return this.bufferOverrunStrategy;
    }

    public SourceDataLineAudioMixer setBufferOverrunStrategy(final BufferOverrunStrategy bufferOverrunStrategy) {
        this.bufferOverrunStrategy = bufferOverrunStrategy;
        return this;
    }

    public VolumeModifier getVolumeModifier() {
        return this.volumeModifier;
    }

    public NormalizationModifier getNormalizationModifier() {
        return this.normalizationModifier;
    }

    public SourceDataLineAudioMixer setMasterVolume(final int masterVolume) {
        return this.setMasterVolume(masterVolume / 100F);
    }

    public SourceDataLineAudioMixer setMasterVolume(final float masterVolume) {
        this.volumeModifier.setVolume(masterVolume);
        return this;
    }

    public float getMasterVolume() {
        return this.volumeModifier.getVolume();
    }

    public enum BufferOverrunStrategy {
        /**
         * Don't mix if the buffer would overrun. Causes new sounds to be started at the next mixSlice() call.
         */
        DO_NOTHING,
        /**
         * Flushes the buffer if it would overrun. Causes new sounds to be started immediately, but may cause audio pops.
         */
        FLUSH,
        /**
         * Blocks until the buffer has enough space.
         */
        BLOCK
    }

}
