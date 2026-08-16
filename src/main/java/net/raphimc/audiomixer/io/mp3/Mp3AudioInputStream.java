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
package net.raphimc.audiomixer.io.mp3;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.DecoderException;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;
import net.raphimc.audiomixer.io.AudioInputStream;
import net.raphimc.audiomixer.io.ogg.OggInputStream;
import net.raphimc.audiomixer.util.AudioFormat;
import net.raphimc.audiomixer.util.buffer.FloatRingBuffer;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class Mp3AudioInputStream extends AudioInputStream {

    private final Bitstream mp3InputStream;
    private final SampleBuffer outputBuffer;
    private final FloatRingBuffer samplesBuffer;
    private final Decoder decoder = new Decoder();

    public Mp3AudioInputStream(final InputStream inputStream) throws IOException {
        this(new CodeBeforeSuper(inputStream));
    }

    private Mp3AudioInputStream(final CodeBeforeSuper codeBeforeSuper) {
        super(new AudioFormat(codeBeforeSuper.firstFrame.frequency(), codeBeforeSuper.firstFrame.mode() == Header.SINGLE_CHANNEL ? 1 : 2));
        this.mp3InputStream = codeBeforeSuper.mp3InputStream;
        this.outputBuffer = new SampleBuffer(codeBeforeSuper.firstFrame.frequency(), this.getFormat().channels());
        this.samplesBuffer = new FloatRingBuffer(OggInputStream.BUFFER_SIZE * this.getFormat().channels());
        this.decoder.setOutputBuffer(this.outputBuffer);
    }

    @Override
    public float read() throws IOException {
        while (this.samplesBuffer.isEmpty()) {
            this.processNextFrame();
        }
        return this.samplesBuffer.read();
    }

    @Override
    public void close() throws IOException {
        try {
            this.mp3InputStream.close();
        } catch (final BitstreamException e) {
            throw new IOException("Failed to close mp3 stream", e);
        }
    }

    private void processNextFrame() throws IOException {
        try {
            final Header frame = this.mp3InputStream.readFrame();
            if (frame == null) {
                throw new EOFException();
            }

            this.decoder.decodeFrame(frame, this.mp3InputStream);
            this.mp3InputStream.closeFrame();

            final short[] buffer = this.outputBuffer.getBuffer();
            for (int i = 0; i < this.outputBuffer.getBufferLength(); i++) {
                final short sample = buffer[i];
                if (sample < 0) {
                    this.samplesBuffer.write(-(float) sample / Short.MIN_VALUE);
                } else if (sample > 0) {
                    this.samplesBuffer.write((float) sample / Short.MAX_VALUE);
                } else {
                    this.samplesBuffer.write(0F);
                }
            }
        } catch (final BitstreamException e) {
            throw new IOException("Failed to read mp3 frame", e);
        } catch (final DecoderException e) {
            throw new IOException("Failed to decode mp3 frame", e);
        }
    }

    private static final class CodeBeforeSuper {

        private final Bitstream mp3InputStream;
        private final Header firstFrame;

        private CodeBeforeSuper(final InputStream inputStream) throws IOException {
            this.mp3InputStream = new Bitstream(inputStream);
            try {
                this.firstFrame = this.mp3InputStream.readFrame();
            } catch (final BitstreamException e) {
                throw new IOException("Failed to read mp3 frame", e);
            }
            if (this.firstFrame == null) {
                throw new EOFException("Unexpected end of mp3 stream");
            }
        }

    }

}
