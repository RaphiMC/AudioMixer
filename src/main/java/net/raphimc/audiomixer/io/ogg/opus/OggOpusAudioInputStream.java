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

import net.raphimc.audiomixer.io.AudioInputStream;
import net.raphimc.audiomixer.io.ogg.opus.packet.OpusHeadPacket;
import net.raphimc.audiomixer.io.ogg.opus.packet.OpusTagsPacket;
import net.raphimc.audiomixer.util.ArrayUtil;
import net.raphimc.audiomixer.util.AudioFormat;
import net.raphimc.audiomixer.util.buffer.FloatRingBuffer;
import net.raphimc.audiomixer.util.io.ogg.OggInputStream;
import net.raphimc.audiomixer.util.math.MathUtil;
import org.concentus.OpusDecoder;
import org.concentus.OpusException;

import java.io.IOException;
import java.io.InputStream;

public class OggOpusAudioInputStream extends AudioInputStream {

    private static final byte[] OPUS_MAGIC = new byte[]{(byte) 'O', (byte) 'p', (byte) 'u', (byte) 's', (byte) 'H', (byte) 'e', (byte) 'a', (byte) 'd'};
    private static final int NATIVE_SAMPLE_RATE = 48000; // 48kHz is the native sample rate of Opus
    private static final int MAX_PACKET_MILLIS = 120; // 120ms is the maximum Opus packet size

    private final OggInputStream oggInputStream;
    private final int opusStreamId;
    private final OpusDecoder decoder;
    private final short[] decodeOutputBuffer;
    private final FloatRingBuffer samplesBuffer;
    private int remainingPreSkipFrameCount;
    private long previousGranulePosition;

    public OggOpusAudioInputStream(final InputStream inputStream) throws IOException {
        this(new CodeBeforeSuper(inputStream));
    }

    private OggOpusAudioInputStream(final CodeBeforeSuper codeBeforeSuper) throws IOException {
        super(new AudioFormat(NATIVE_SAMPLE_RATE, codeBeforeSuper.opusHead.outputChannelCount()));
        this.oggInputStream = codeBeforeSuper.oggInputStream;
        this.opusStreamId = codeBeforeSuper.opusStreamId;
        try {
            this.decoder = new OpusDecoder(Math.round(this.getFormat().sampleRate()), this.getFormat().channelCount());
            this.decoder.setGain(codeBeforeSuper.opusHead.outputGain());
        } catch (final OpusException e) {
            throw new IOException("Failed to initialize decoder", e);
        }
        this.decodeOutputBuffer = new short[Math.multiplyExact(Math.multiplyExact(this.decoder.getSampleRate() / 1000, MAX_PACKET_MILLIS), this.getFormat().channelCount())];
        this.samplesBuffer = new FloatRingBuffer(this.decodeOutputBuffer.length);
        this.remainingPreSkipFrameCount = codeBeforeSuper.opusHead.preSkip();
    }

    @Override
    public float read() throws IOException {
        while (this.samplesBuffer.isEmpty()) {
            this.decodeNextPacket();
        }
        return this.samplesBuffer.read();
    }

    private void decodeNextPacket() throws IOException {
        final OggInputStream.OggPacket packet = this.oggInputStream.readUntilPacket(this.opusStreamId);
        if (packet.length() == 0) {
            throw new IOException("Malformed Ogg Opus stream: Zero-length audio packet");
        }
        try {
            final int frameCount = this.decoder.decode(packet.data(), packet.offset(), packet.length(), this.decodeOutputBuffer, 0, this.getFormat().sampleCountToFrameCount(this.decodeOutputBuffer.length), false);
            int firstFrameIndex = 0;
            if (this.remainingPreSkipFrameCount > 0) {
                final int skipFrameCount = Math.min(this.remainingPreSkipFrameCount, frameCount);
                this.remainingPreSkipFrameCount -= skipFrameCount;
                firstFrameIndex = skipFrameCount;
            }
            int lastFrameIndex = frameCount;
            if (packet.eos() && packet.granulePosition() >= 0) {
                final long retainedFrameCount = packet.granulePosition() - this.previousGranulePosition;
                lastFrameIndex = Math.toIntExact(MathUtil.clamp(retainedFrameCount, 0, lastFrameIndex));
            }
            final int firstSampleIndex = Math.multiplyExact(firstFrameIndex, this.getFormat().channelCount());
            final int lastSampleIndex = Math.multiplyExact(lastFrameIndex, this.getFormat().channelCount());
            for (int sampleIndex = firstSampleIndex; sampleIndex < lastSampleIndex; sampleIndex++) {
                final short sample = this.decodeOutputBuffer[sampleIndex];
                if (sample < 0) {
                    this.samplesBuffer.write(-(float) sample / Short.MIN_VALUE);
                } else if (sample > 0) {
                    this.samplesBuffer.write((float) sample / Short.MAX_VALUE);
                } else {
                    this.samplesBuffer.write(0F);
                }
            }
            if (packet.granulePosition() >= 0) {
                this.previousGranulePosition = packet.granulePosition();
            } else {
                this.previousGranulePosition = Math.addExact(this.previousGranulePosition, frameCount);
            }
        } catch (final OpusException e) {
            throw new IOException("Failed to decode audio packet", e);
        }
    }

    @Override
    public void close() throws IOException {
        this.oggInputStream.close();
    }

    private static final class CodeBeforeSuper {

        private final OggInputStream oggInputStream;
        private final int opusStreamId;
        private final OpusHeadPacket opusHead;
        private final OpusTagsPacket opusTags;

        private CodeBeforeSuper(final InputStream inputStream) throws IOException {
            this.oggInputStream = new OggInputStream(inputStream);
            while (true) {
                final OggInputStream.OggPacket packet = this.oggInputStream.readPacket();
                if (packet.bos() && ArrayUtil.startsWith(packet.data(), OPUS_MAGIC)) {
                    this.opusStreamId = packet.streamId();
                    this.opusHead = new OpusHeadPacket(packet.data());
                    break;
                }
            }
            this.opusTags = new OpusTagsPacket(this.oggInputStream.readUntilPacket(this.opusStreamId).data());
        }

    }

}
