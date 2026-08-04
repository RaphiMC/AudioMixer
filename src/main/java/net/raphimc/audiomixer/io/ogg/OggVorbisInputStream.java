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
package net.raphimc.audiomixer.io.ogg;

import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.SyncState;
import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.Comment;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.Info;
import net.raphimc.audiomixer.util.buffer.RingByteBuffer;
import net.raphimc.audiomixer.util.math.MathUtil;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class OggVorbisInputStream extends InputStream {

    private static final int BUFFER_SIZE = 8192; // Don't change. This is hardcoded in jorbis.
    private static final int PAGEOUT_NEED_MORE_DATA = 0;
    private static final int PAGEOUT_SUCCESS = 1;
    private static final int PAGEOUT_ERROR = -1;
    private static final int PACKETOUT_NEED_MORE_DATA = 0;
    private static final int PACKETOUT_SUCCESS = 1;
    private static final int PACKETOUT_ERROR = -1;

    private final SyncState syncState = new SyncState();
    private final Page page = new Page();
    private final StreamState streamState = new StreamState();
    private final Packet packet = new Packet();
    private final Info info = new Info();
    private final DspState dspState = new DspState();
    private final Block block = new Block(this.dspState);
    private final InputStream oggStream;
    private final RingByteBuffer samplesBuffer;

    public static AudioInputStream createAudioInputStream(final InputStream oggStream) throws IOException {
        final OggVorbisInputStream oggVorbisInputStream = new OggVorbisInputStream(oggStream);
        return new AudioInputStream(oggVorbisInputStream, oggVorbisInputStream.getAudioFormat(), AudioSystem.NOT_SPECIFIED);
    }

    public OggVorbisInputStream(final InputStream oggStream) throws IOException {
        this.oggStream = oggStream;

        final Comment comment = new Comment();
        for (int i = 0; i < 3; i++) {
            if (!this.readPacket()) {
                throw new EOFException("Unexpected end of ogg stream");
            }
            if (this.info.synthesis_headerin(comment, this.packet) < 0) {
                throw new IOException("Invalid ogg header packet " + i);
            }
        }

        if (this.dspState.synthesis_init(this.info) < 0) {
            throw new IOException("Failed to initialize dsp state");
        }
        this.samplesBuffer = new RingByteBuffer(BUFFER_SIZE * Short.BYTES * this.info.channels);
    }

    @Override
    public int read() throws IOException {
        while (this.samplesBuffer.isEmpty()) {
            if (!this.fillSamplesBuffer()) {
                return -1;
            }
        }
        return this.samplesBuffer.read() & 0xFF;
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        while (this.samplesBuffer.isEmpty()) {
            if (!this.fillSamplesBuffer()) {
                return -1;
            }
        }
        return this.samplesBuffer.read(b, off, len);
    }

    @Override
    public void close() throws IOException {
        this.oggStream.close();
    }

    public AudioFormat getAudioFormat() {
        return new AudioFormat(this.info.rate, Short.SIZE, this.info.channels, true, false);
    }

    private boolean fillSamplesBuffer() throws IOException {
        if (!this.readPacket()) {
            return false;
        }
        if (this.block.synthesis(this.packet) < 0) {
            throw new IOException("Failed to decode audio packet");
        }
        if (this.dspState.synthesis_blockin(this.block) < 0) {
            throw new IOException("Failed to submit audio block to dsp state");
        }

        final float[][][] allSamples = new float[1][][];
        final int[] offsets = new int[this.info.channels];

        int frameCount;
        while ((frameCount = this.dspState.synthesis_pcmout(allSamples, offsets)) > 0) {
            for (int i = 0; i < frameCount; i++) {
                for (int channel = 0; channel < this.info.channels; channel++) {
                    final int offset = offsets[channel];
                    final float[] samples = allSamples[0][channel];
                    final float floatSample = MathUtil.clamp(samples[offset + i], -1F, 1F); // jorbis seems to return out of range samples sometimes
                    final short sample = (short) Math.round(floatSample > 0 ? (floatSample * Short.MAX_VALUE) : (floatSample * (-Short.MIN_VALUE)));
                    this.samplesBuffer.write((byte) (sample & 0xFF));
                    this.samplesBuffer.write((byte) ((sample >> 8) & 0xFF));
                }
            }
            if (this.dspState.synthesis_read(frameCount) < 0) {
                throw new IOException("Failed to update dsp state");
            }
        }
        return true;
    }

    private boolean readPacket() throws IOException {
        while (true) {
            final int result = this.streamState.packetout(this.packet);
            switch (result) {
                case PACKETOUT_NEED_MORE_DATA -> {
                    if (this.streamState.eof() != 0) {
                        return false;
                    }
                    if (!this.readPage()) {
                        return false;
                    }
                    if (this.page.bos() != 0) { // Begin of stream -> Initialize stream state
                        this.streamState.init(this.page.serialno());
                    }
                    if (this.streamState.pagein(this.page) < 0) {
                        throw new IOException("Failed to handle page");
                    }
                }
                case PACKETOUT_SUCCESS -> {
                    return true;
                }
                case PACKETOUT_ERROR -> throw new IOException("Corrupted ogg stream");
                default -> throw new IllegalStateException("Unknown packet decode result: " + result);
            }
        }
    }

    private boolean readPage() throws IOException {
        while (true) {
            final int result = this.syncState.pageout(this.page);
            switch (result) {
                case PAGEOUT_NEED_MORE_DATA -> {
                    final int offset = this.syncState.buffer(BUFFER_SIZE);
                    final int size = this.oggStream.read(this.syncState.data, offset, BUFFER_SIZE);
                    if (size == -1) {
                        return false;
                    }
                    if (this.syncState.wrote(size) < 0) {
                        throw new IOException("Failed to update sync state");
                    }
                }
                case PAGEOUT_SUCCESS -> {
                    return true;
                }
                case PAGEOUT_ERROR -> throw new IOException("Corrupted ogg stream");
                default -> throw new IllegalStateException("Unknown page decode result: " + result);
            }
        }
    }

}
