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
package net.raphimc.audiomixer.io.mp3;

import javazoom.jl.decoder.*;
import net.raphimc.audiomixer.util.CircularBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;
import java.io.InputStream;

public class Mp3InputStream extends InputStream {

    private final Bitstream mp3BitStream;
    private final Decoder decoder = new Decoder(null);
    private final SampleBuffer outputBuffer;
    private final InputStream mp3Stream;
    private final CircularBuffer samplesBuffer;

    public static AudioInputStream createAudioInputStream(final InputStream mp3Stream) throws IOException {
        final Mp3InputStream mp3InputStream = new Mp3InputStream(mp3Stream);
        return new AudioInputStream(mp3InputStream, mp3InputStream.getAudioFormat(), AudioSystem.NOT_SPECIFIED);
    }

    public Mp3InputStream(final InputStream mp3Stream) throws IOException {
        this.mp3Stream = mp3Stream;
        this.mp3BitStream = new Bitstream(mp3Stream);

        Header frame;
        try {
            frame = this.mp3BitStream.readFrame();
        } catch (BitstreamException e) {
            throw new IOException("Error reading mp3 frame", e);
        }
        if (frame == null) {
            throw new IOException("Invalid mp3 file: Can't read first frame");
        }

        final int channels = frame.mode() == Header.SINGLE_CHANNEL ? 1 : 2;
        this.outputBuffer = new SampleBuffer(frame.frequency(), channels);
        this.decoder.setOutputBuffer(this.outputBuffer);
        this.samplesBuffer = new CircularBuffer(this.outputBuffer.getBuffer().length * Short.BYTES);
    }

    @Override
    public int read() throws IOException {
        while (this.samplesBuffer.isEmpty()) {
            if (!this.fillSamplesBuffer()) {
                return -1;
            }
        }
        return this.samplesBuffer.readSafe() & 0xFF;
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        while (this.samplesBuffer.isEmpty()) {
            if (!this.fillSamplesBuffer()) {
                return -1;
            }
        }
        final byte[] data = this.samplesBuffer.readAllSafe(Math.min(len, this.samplesBuffer.getSize()));
        System.arraycopy(data, 0, b, off, data.length);
        return data.length;
    }

    @Override
    public void close() throws IOException {
        this.mp3Stream.close();
    }

    public AudioFormat getAudioFormat() {
        return new AudioFormat(this.outputBuffer.getSampleFrequency(), Short.SIZE, this.outputBuffer.getChannelCount(), true, false);
    }

    private boolean fillSamplesBuffer() throws IOException {
        try {
            final Header frame = this.mp3BitStream.readFrame();
            if (frame == null) {
                return false;
            }

            this.decoder.decodeFrame(frame, this.mp3BitStream);
            this.mp3BitStream.closeFrame();

            final short[] buffer = this.outputBuffer.getBuffer();
            for (int i = 0; i < this.outputBuffer.getBufferLength(); i++) {
                final short sample = buffer[i];
                this.samplesBuffer.write((byte) (sample & 0xFF));
                this.samplesBuffer.write((byte) ((sample >> 8) & 0xFF));
            }
            return true;
        } catch (BitstreamException e) {
            throw new IOException("Error reading mp3 frame", e);
        } catch (DecoderException e) {
            throw new IOException("Error decoding mp3 frame", e);
        }
    }

}
