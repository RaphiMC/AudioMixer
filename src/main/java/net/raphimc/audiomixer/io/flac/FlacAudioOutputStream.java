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
import io.github.raphimc.libflac.StreamEncoder;
import io.github.raphimc.libflac.StreamEncoderInitStatus;
import io.github.raphimc.libflac.StreamEncoderSeekStatus;
import io.github.raphimc.libflac.StreamEncoderState;
import io.github.raphimc.libflac.StreamMetadata;
import io.github.raphimc.libflac.VorbisComment;
import net.raphimc.audiomixer.io.AudioOutputStream;
import net.raphimc.audiomixer.util.AudioFormat;
import net.raphimc.audiomixer.util.buffer.FloatRingBuffer;
import net.raphimc.audiomixer.util.io.seekable.SeekableOutputStream;
import net.raphimc.audiomixer.util.math.MathUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FlacAudioOutputStream extends AudioOutputStream {

    private static final Encoding DEFAULT_ENCODING = new Encoding(16, 6);

    private final OutputStream outputStream;
    private final StreamEncoder encoder;
    private final float integerScale;
    private final int minIntegerValue;
    private final int maxIntegerValue;
    private final FloatRingBuffer samplesBuffer;
    private final int[] encodeInputBuffer;

    public FlacAudioOutputStream(final OutputStream outputStream, final AudioFormat format) throws IOException {
        this(outputStream, format, DEFAULT_ENCODING);
    }

    public FlacAudioOutputStream(final OutputStream outputStream, final AudioFormat format, final Map<String, List<String>> tags) throws IOException {
        this(outputStream, format, DEFAULT_ENCODING, tags);
    }

    public FlacAudioOutputStream(final OutputStream outputStream, final AudioFormat format, final Encoding encoding) throws IOException {
        this(outputStream, format, encoding, Collections.emptyMap());
    }

    public FlacAudioOutputStream(final OutputStream outputStream, final AudioFormat format, final Encoding encoding, final Map<String, List<String>> tags) throws IOException {
        super(format);
        this.outputStream = outputStream;

        this.encoder = new StreamEncoder();
        this.encoder.setStreamableSubset(false);
        this.encoder.setChannels(format.channelCount());
        this.encoder.setBitsPerSample(encoding.bitsPerSample());
        this.encoder.setSampleRate(Math.round(format.sampleRate()));
        this.encoder.setCompressionLevel(encoding.compressionLevel());
        final VorbisComment vorbisComment = new VorbisComment();
        for (Map.Entry<String, List<String>> entries : tags.entrySet()) {
            for (String value : entries.getValue()) {
                final String comment = VorbisComment.entryFromNameValuePair(entries.getKey(), value);
                if (comment == null || !vorbisComment.appendComment(comment)) {
                    throw new IllegalArgumentException("Invalid comment: " + entries.getKey() + '=' + value);
                }
            }
        }
        if (!this.encoder.setMetadata(new StreamMetadata[]{vorbisComment})) {
            throw new IOException("Failed to set metadata");
        }
        final StreamEncoderInitStatus initStatus = this.encoder.initStream(this::writeOutput, this::seekOutput, this::tellOutput, null);
        if (initStatus != StreamEncoderInitStatus.OK) {
            throw new IOException("Failed to initialize encoder: " + initStatus);
        }
        this.integerScale = Math.scalb(1F, this.encoder.getBitsPerSample() - 1);
        this.minIntegerValue = -1 << (this.encoder.getBitsPerSample() - 1);
        this.maxIntegerValue = (1 << (this.encoder.getBitsPerSample() - 1)) - 1;
        this.samplesBuffer = new FloatRingBuffer(this.encoder.getBlocksize() * this.getFormat().channelCount());
        this.encodeInputBuffer = new int[this.samplesBuffer.capacity()];
    }

    @Override
    public void write(final float sample) throws IOException {
        if (!Float.isFinite(sample)) {
            throw new IOException("Sample must be finite: " + sample);
        }
        this.samplesBuffer.write(sample);
        if (this.samplesBuffer.isFull()) {
            this.flushSamplesBuffer();
        }
    }

    private void flushSamplesBuffer() throws IOException {
        final int sampleCount = this.samplesBuffer.size();
        for (int sampleIndex = 0; sampleIndex < sampleCount; sampleIndex++) {
            this.encodeInputBuffer[sampleIndex] = MathUtil.clamp(Math.round(this.samplesBuffer.read() * this.integerScale), this.minIntegerValue, this.maxIntegerValue);
        }
        if (!this.encoder.processInterleaved(this.encodeInputBuffer, this.getFormat().sampleCountToFrameCount(sampleCount))) {
            throw new IOException("Failed to process buffer: " + this.encoder.getState());
        }
    }

    private boolean writeOutput(final StreamEncoder encoder, final byte[] buffer, final int offset, final int bytes, final int samples, final int currentFrame) throws IOException {
        this.outputStream.write(buffer, offset, bytes);
        return true;
    }

    private StreamEncoderSeekStatus seekOutput(final StreamEncoder encoder, final long absoluteByteOffset) throws IOException {
        if (this.outputStream instanceof SeekableOutputStream seekableOutputStream) {
            seekableOutputStream.seek(absoluteByteOffset);
            return StreamEncoderSeekStatus.OK;
        } else {
            return StreamEncoderSeekStatus.UNSUPPORTED;
        }
    }

    private long tellOutput(final StreamEncoder encoder) throws IOException {
        return this.outputStream instanceof SeekableOutputStream seekableOutputStream ? seekableOutputStream.position() : -1;
    }

    @Override
    public void flush() throws IOException {
        this.outputStream.flush();
    }

    @Override
    public void close() throws IOException {
        try (this.outputStream; this.encoder) {
            this.flushSamplesBuffer();
            this.encoder.finish();
            if (this.encoder.getState() != StreamEncoderState.UNINITIALIZED) {
                throw new IOException("Failed to finish encoder: " + this.encoder.getState());
            }
        }
    }

    public record Encoding(int bitsPerSample, int compressionLevel) {

        public Encoding {
            if (bitsPerSample < Format.MIN_BITS_PER_SAMPLE || bitsPerSample > Format.MAX_BITS_PER_SAMPLE) {
                throw new IllegalArgumentException("Bits per sample must be in [" + Format.MIN_BITS_PER_SAMPLE + ", " + Format.MAX_BITS_PER_SAMPLE + "]: " + bitsPerSample);
            }
            if (compressionLevel < 0 || compressionLevel > 8) {
                throw new IllegalArgumentException("Compression level must be in [0, 8]: " + compressionLevel);
            }
        }

    }

}
