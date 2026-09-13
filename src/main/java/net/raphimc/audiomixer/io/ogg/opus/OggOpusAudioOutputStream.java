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
package net.raphimc.audiomixer.io.ogg.opus;

import net.raphimc.audiomixer.io.AudioOutputStream;
import net.raphimc.audiomixer.io.ogg.opus.packet.OpusHeadPacket;
import net.raphimc.audiomixer.io.ogg.opus.packet.OpusTagsPacket;
import net.raphimc.audiomixer.util.AudioFormat;
import net.raphimc.audiomixer.util.buffer.FloatRingBuffer;
import net.raphimc.audiomixer.util.io.ogg.OggOutputStream;
import net.raphimc.audiomixer.util.math.MathUtil;
import org.concentus.OpusApplication;
import org.concentus.OpusEncoder;
import org.concentus.OpusException;
import org.concentus.OpusSignal;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class OggOpusAudioOutputStream extends AudioOutputStream {

    private static final int GRANULE_SAMPLE_RATE = 48000; // Opus granule position and pre-skip always use 48kHz units
    private static final int MAX_FRAME_MILLIS = 60; // 60ms is the maximum Opus frame size

    private final OggOutputStream oggOutputStream;
    private final int opusStreamId;
    private final OpusEncoder encoder;
    private final int granuleScale;
    private final FloatRingBuffer samplesBuffer;
    private final short[] encodeInputBuffer;
    private final byte[] encodeOutputBuffer = new byte[4000]; // 4000 bytes is the recommended output buffer size
    private long granulePosition;

    public OggOpusAudioOutputStream(final OutputStream outputStream, final AudioFormat format) throws IOException {
        this(outputStream, format, 0.6F);
    }

    public OggOpusAudioOutputStream(final OutputStream outputStream, final AudioFormat format, final float quality) throws IOException {
        this(outputStream, format, quality, OpusSignal.OPUS_SIGNAL_AUTO, Collections.emptyMap());
    }

    public OggOpusAudioOutputStream(final OutputStream outputStream, final AudioFormat format, final float quality, final OpusSignal signalType, final Map<String, List<String>> tags) throws IOException {
        super(format);
        if (!Float.isFinite(quality) || quality < 0F || quality > 1F) {
            throw new IllegalArgumentException("Quality must be finite and in [0, 1]: " + quality);
        }
        this.oggOutputStream = new OggOutputStream(outputStream);
        this.opusStreamId = ThreadLocalRandom.current().nextInt();
        try {
            this.encoder = new OpusEncoder(Math.round(this.getFormat().sampleRate()), this.getFormat().channelCount(), OpusApplication.OPUS_APPLICATION_AUDIO);
        } catch (final OpusException e) {
            throw new IOException("Failed to initialize encoder", e);
        }
        this.encoder.setBitrate(Math.round(MathUtil.interpolateExponential(32F, 256F, quality)) * 1000);
        this.encoder.setComplexity(10);
        this.encoder.setUseConstrainedVBR(false);
        this.encoder.setSignalType(signalType);
        this.encoder.setEnableAnalysis(true);
        this.granuleScale = GRANULE_SAMPLE_RATE / this.encoder.getSampleRate();
        this.samplesBuffer = new FloatRingBuffer(Math.multiplyExact(Math.multiplyExact(this.encoder.getSampleRate() / 1000, MAX_FRAME_MILLIS), this.getFormat().channelCount()));
        this.encodeInputBuffer = new short[this.samplesBuffer.capacity()];

        final OpusHeadPacket opusHead = new OpusHeadPacket(this.getFormat().channelCount(), Math.multiplyExact(this.encoder.getLookahead(), this.granuleScale), this.encoder.getSampleRate());
        this.oggOutputStream.writePacket(this.opusStreamId, opusHead.write(), false, this.granulePosition);
        final OpusTagsPacket opusTags = new OpusTagsPacket("Concentus (libopus 1.1.2)", tags.entrySet().stream().flatMap(entry -> entry.getValue().stream().map(value -> entry.getKey() + "=" + value)).toList());
        this.oggOutputStream.writePacket(this.opusStreamId, opusTags.write(), false, this.granulePosition);
        this.oggOutputStream.flushStream(this.opusStreamId);
    }

    @Override
    public void write(final float sample) throws IOException {
        if (!Float.isFinite(sample) || sample < -1F || sample > 1F) {
            throw new IOException("Sample must be finite and in [-1, 1]");
        }
        this.samplesBuffer.write(sample);
        if (this.samplesBuffer.isFull()) {
            this.flushSamplesBuffer(false);
        }
    }

    private void flushSamplesBuffer(final boolean endOfStream) throws IOException {
        final int sampleCount = this.samplesBuffer.size();
        for (int sampleIndex = 0; sampleIndex < sampleCount; sampleIndex++) {
            final float sample = this.samplesBuffer.read();
            if (sample < 0F) {
                this.encodeInputBuffer[sampleIndex] = (short) Math.round(-sample * Short.MIN_VALUE);
            } else if (sample > 0F) {
                this.encodeInputBuffer[sampleIndex] = (short) Math.round(sample * Short.MAX_VALUE);
            } else {
                this.encodeInputBuffer[sampleIndex] = 0;
            }
        }
        if (endOfStream) {
            Arrays.fill(this.encodeInputBuffer, sampleCount, this.encodeInputBuffer.length, (short) 0);
        }
        this.granulePosition = Math.addExact(this.granulePosition, Math.multiplyExact(this.getFormat().sampleCountToFrameCount(sampleCount), this.granuleScale));
        try {
            final int length = this.encoder.encode(this.encodeInputBuffer, 0, this.getFormat().sampleCountToFrameCount(this.encodeInputBuffer.length), this.encodeOutputBuffer, 0, this.encodeOutputBuffer.length);
            this.oggOutputStream.writePacket(this.opusStreamId, this.encodeOutputBuffer, 0, length, endOfStream, this.granulePosition);
        } catch (final OpusException e) {
            throw new IOException("Failed to encode buffer", e);
        }
    }

    @Override
    public void flush() throws IOException {
        this.oggOutputStream.flush();
    }

    @Override
    public void close() throws IOException {
        try (this.oggOutputStream) {
            this.write(new float[Math.multiplyExact(this.encoder.getLookahead(), this.getFormat().channelCount())]); // Flush the encoder
            this.flushSamplesBuffer(true);
        }
    }

}
