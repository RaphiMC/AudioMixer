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

import net.raphimc.audiomixer.io.raw.SampleOutputStream;
import net.raphimc.audiomixer.soundmodifier.impl.NormalizationModifier;
import net.raphimc.audiomixer.soundmodifier.impl.VolumeModifier;
import net.raphimc.audiomixer.util.PcmFloatAudioFormat;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SourceDataLineAudioMixer extends AudioMixer {

    private final SourceDataLine sourceDataLine;
    private final int sampleByteSize;
    private int mixSliceSampleCount;
    private BufferOverrunStrategy bufferOverrunStrategy = BufferOverrunStrategy.DO_NOTHING;
    private final NormalizationModifier normalizationModifier = new NormalizationModifier();
    private final VolumeModifier volumeModifier = new VolumeModifier(1F);

    public SourceDataLineAudioMixer(final SourceDataLine sourceDataLine, final int mixSliceMillis) throws LineUnavailableException {
        this(sourceDataLine, mixSliceMillis, Math.max(mixSliceMillis * 3, 50));
    }

    public SourceDataLineAudioMixer(final SourceDataLine sourceDataLine, final int mixSliceMillis, final int bufferMillis) throws LineUnavailableException {
        super(new PcmFloatAudioFormat(sourceDataLine.getFormat()));
        this.sourceDataLine = sourceDataLine;
        this.sampleByteSize = sourceDataLine.getFormat().getSampleSizeInBits() / 8;
        this.mixSliceSampleCount = (int) Math.ceil(sourceDataLine.getFormat().getSampleRate() / 1000F * mixSliceMillis) * sourceDataLine.getFormat().getChannels();

        if (!sourceDataLine.isOpen()) {
            final int bufferSampleCount = (int) Math.ceil(sourceDataLine.getFormat().getSampleRate() / 1000F * bufferMillis) * sourceDataLine.getFormat().getChannels();
            sourceDataLine.open(sourceDataLine.getFormat(), bufferSampleCount * this.sampleByteSize);
        }
        if (sourceDataLine.getBufferSize() < this.mixSliceSampleCount * 2 * this.sampleByteSize) {
            throw new IllegalArgumentException("SourceDataLine buffer has to be at least twice the size of the mix slice size");
        }
        sourceDataLine.start();

        this.getSoundModifiers().append(this.normalizationModifier);
        this.getSoundModifiers().append(this.volumeModifier);
    }

    @Override
    public void stopAllSounds() {
        super.stopAllSounds();
        this.normalizationModifier.reset();
    }

    @Override
    public AudioFormat getAudioFormat() {
        return this.sourceDataLine.getFormat();
    }

    public void close() {
        this.sourceDataLine.close();
    }

    public void mixSlice() {
        final int samplesSize = this.mixSliceSampleCount * this.sampleByteSize;
        if (this.bufferOverrunStrategy == BufferOverrunStrategy.DO_NOTHING && this.sourceDataLine.available() < samplesSize) {
            return;
        }
        final float[] samples = this.mix(this.mixSliceSampleCount);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream(samplesSize);
        final SampleOutputStream sos = new SampleOutputStream(baos, this.sourceDataLine.getFormat());
        try {
            for (float sample : samples) {
                sos.writeSample(sample);
            }
        } catch (IOException ignored) {
        }
        final byte[] sampleData = baos.toByteArray();

        if (this.bufferOverrunStrategy == BufferOverrunStrategy.FLUSH && this.sourceDataLine.available() < sampleData.length) {
            this.sourceDataLine.flush();
        }
        this.sourceDataLine.write(sampleData, 0, sampleData.length);
    }

    public SourceDataLine getSourceDataLine() {
        return this.sourceDataLine;
    }

    public int getMixSliceSampleCount() {
        return this.mixSliceSampleCount;
    }

    public SourceDataLineAudioMixer setMixSliceSampleCount(final int mixSliceSampleCount) {
        if (this.sourceDataLine.getBufferSize() < mixSliceSampleCount * 2 * this.sampleByteSize) {
            throw new IllegalArgumentException("SourceDataLine buffer is too small for the new mix slice size. Increase the SourceDataLine buffer size or reduce the mix slice size");
        }
        this.mixSliceSampleCount = mixSliceSampleCount;
        return this;
    }

    public SourceDataLineAudioMixer setMixSliceMillis(final int mixSliceMillis) {
        return this.setMixSliceSampleCount((int) Math.ceil(this.getAudioFormat().getSampleRate() / 1000F * mixSliceMillis) * this.getAudioFormat().getChannels());
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

    public int getBufferedSampleCount() {
        return (this.sourceDataLine.getBufferSize() - this.sourceDataLine.available()) / this.sampleByteSize;
    }

    public int getBufferedMillis() {
        return (int) ((float) this.getBufferedSampleCount() / this.getAudioFormat().getChannels() / this.getAudioFormat().getSampleRate() * 1000F);
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
