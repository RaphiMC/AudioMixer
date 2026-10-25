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
package net.raphimc.audiomixer.io.flac;

import io.github.raphimc.libflac.Format;
import io.github.raphimc.libflac.Frame;
import io.github.raphimc.libflac.StreamDecoder;
import io.github.raphimc.libflac.StreamDecoderErrorStatus;
import io.github.raphimc.libflac.StreamDecoderInitStatus;
import io.github.raphimc.libflac.StreamDecoderState;
import io.github.raphimc.libflac.StreamInfo;
import io.github.raphimc.libflac.StreamMetadata;
import net.raphimc.audiomixer.io.AudioInputStream;
import net.raphimc.audiomixer.util.AudioFormat;
import net.raphimc.audiomixer.util.buffer.FloatRingBuffer;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class FlacAudioInputStream extends AudioInputStream {

    private final InputStream inputStream;
    private final StreamDecoder decoder;
    private final FloatRingBuffer samplesBuffer;

    public FlacAudioInputStream(final InputStream inputStream) throws IOException {
        this(new CodeBeforeSuper(inputStream));
    }

    private FlacAudioInputStream(final CodeBeforeSuper codeBeforeSuper) {
        super(new AudioFormat(codeBeforeSuper.streamInfo.getSampleRate(), codeBeforeSuper.streamInfo.getChannels()));
        this.inputStream = codeBeforeSuper.inputStream;
        this.decoder = codeBeforeSuper.decoder;
        this.samplesBuffer = new FloatRingBuffer(Format.MAX_BLOCK_SIZE * this.getFormat().channelCount());
        codeBeforeSuper.writeCallback = this::writeFrame;
    }

    @Override
    public float read() throws IOException {
        while (this.samplesBuffer.isEmpty()) {
            this.decodeNextFrame();
        }
        return this.samplesBuffer.read();
    }

    private void decodeNextFrame() throws IOException {
        switch (this.decoder.getState()) {
            case END_OF_STREAM -> throw new EOFException();
            case ABORTED -> throw new IOException("Decoder has been aborted");
            default -> {
                if (!this.decoder.processSingle()) {
                    throw new IOException("Failed to process frame: " + this.decoder.getState());
                }
            }
        }
    }

    private boolean writeFrame(final StreamDecoder decoder, final Frame frame, final int[][] buffer) throws IOException {
        if (frame.getHeader().getSampleRate() != this.getFormat().sampleRate() || frame.getHeader().getChannels() != this.getFormat().channelCount()) {
            throw new IOException("Malformed FLAC stream: Audio format changed mid-stream (expected: " + this.getFormat().sampleRate() + " Hz, " + this.getFormat().channelCount() + " channels; got: " + frame.getHeader().getSampleRate() + " Hz, " + frame.getHeader().getChannels() + " channels)");
        }
        final float integerScale = Math.scalb(1F, frame.getHeader().getBitsPerSample() - 1);
        final int frameCount = frame.getHeader().getBlocksize();
        final int channelCount = frame.getHeader().getChannels();
        for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
            for (int channelIndex = 0; channelIndex < channelCount; channelIndex++) {
                this.samplesBuffer.write(buffer[channelIndex][frameIndex] / integerScale);
            }
        }
        return true;
    }

    @Override
    public void close() throws IOException {
        try (this.inputStream; this.decoder) {
            if (this.decoder.getState() == StreamDecoderState.END_OF_STREAM) {
                if (!this.decoder.finish()) {
                    throw new IOException("MD5 checksum mismatch");
                }
            }
        }
    }

    private static final class CodeBeforeSuper implements StreamDecoder.ReadCallback, StreamDecoder.WriteCallback, StreamDecoder.MetadataCallback, StreamDecoder.ErrorCallback {

        private final InputStream inputStream;
        private final StreamDecoder decoder;
        private StreamInfo streamInfo;
        private StreamDecoder.WriteCallback writeCallback;

        private CodeBeforeSuper(final InputStream inputStream) throws IOException {
            this.inputStream = inputStream;
            this.decoder = new StreamDecoder();
            this.decoder.setMd5Checking(true);
            try {
                final StreamDecoderInitStatus initStatus = this.decoder.initStream(this, null, null, null, null, this, this, this);
                if (initStatus != StreamDecoderInitStatus.OK) {
                    throw new IOException("Failed to initialize decoder: " + initStatus);
                }
                if (!this.decoder.processUntilEndOfMetadata()) {
                    throw new IOException("Failed to process metadata: " + this.decoder.getState());
                }
                if (this.streamInfo == null) {
                    throw new IOException("Failed to find stream info");
                }
            } catch (final Throwable e) {
                try (this.decoder) {
                    throw e;
                }
            }
        }

        @Override
        public int read(final StreamDecoder decoder, final byte[] buffer, final int offset, final int bytes) throws IOException {
            return this.inputStream.read(buffer, offset, bytes);
        }

        @Override
        public boolean write(final StreamDecoder decoder, final Frame frame, final int[][] buffer) throws IOException {
            return this.writeCallback.write(decoder, frame, buffer);
        }

        @Override
        public void metadata(final StreamDecoder decoder, final StreamMetadata metadata) {
            if (metadata instanceof StreamInfo info) {
                this.streamInfo = info;
            }
        }

        @Override
        public void error(final StreamDecoder decoder, final StreamDecoderErrorStatus status) {
            throwUnchecked(new IOException("Decoder error: " + status));
        }

        @SuppressWarnings("unchecked")
        private static <T extends Throwable> void throwUnchecked(final Throwable t) throws T {
            throw (T) t;
        }

    }

}
