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
package net.raphimc.audiomixer.io.ogg.vorbis;

import com.jcraft.jogg.Packet;
import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.Comment;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.Info;
import net.raphimc.audiomixer.io.AudioInputStream;
import net.raphimc.audiomixer.io.ogg.OggInputStream;
import net.raphimc.audiomixer.util.AudioFormat;
import net.raphimc.audiomixer.util.buffer.FloatRingBuffer;
import net.raphimc.audiomixer.util.math.MathUtil;

import java.io.IOException;
import java.io.InputStream;

public class OggVorbisAudioInputStream extends AudioInputStream {

    private final OggInputStream oggInputStream;
    private final FloatRingBuffer samplesBuffer;
    private final DspState dspState;
    private final Block block;

    public OggVorbisAudioInputStream(final InputStream inputStream) throws IOException {
        this(new CodeBeforeSuper(inputStream));
    }

    private OggVorbisAudioInputStream(final CodeBeforeSuper codeBeforeSuper) {
        super(new AudioFormat(codeBeforeSuper.info.rate, codeBeforeSuper.info.channels));
        this.oggInputStream = codeBeforeSuper.oggInputStream;
        this.samplesBuffer = new FloatRingBuffer(OggInputStream.BUFFER_SIZE * this.getFormat().channels());
        this.dspState = codeBeforeSuper.dspState;
        this.block = new Block(this.dspState);
    }

    @Override
    public float read() throws IOException {
        while (this.samplesBuffer.isEmpty()) {
            this.decodeNextPacket();
        }
        return this.samplesBuffer.read();
    }

    @Override
    public void close() throws IOException {
        this.oggInputStream.close();
    }

    private void decodeNextPacket() throws IOException {
        final Packet packet = this.oggInputStream.readPacket();
        if (this.block.synthesis(packet) < 0) {
            throw new IOException("Failed to decode audio packet");
        }
        if (this.dspState.synthesis_blockin(this.block) < 0) {
            throw new IOException("Failed to submit audio block to dsp state");
        }

        final int channels = this.getFormat().channels();
        final float[][][] samplesRef = new float[1][][];
        final int[] offsets = new int[channels];
        int frameCount;
        while ((frameCount = this.dspState.synthesis_pcmout(samplesRef, offsets)) > 0) {
            final float[][] allSamples = samplesRef[0];
            for (int i = 0; i < frameCount; i++) {
                for (int channel = 0; channel < channels; channel++) {
                    this.samplesBuffer.write(MathUtil.clamp(allSamples[channel][offsets[channel] + i], -1F, 1F)); // jorbis seems to return out of range samples sometimes
                }
            }
            if (this.dspState.synthesis_read(frameCount) < 0) {
                throw new IOException("Failed to update dsp state");
            }
        }
    }

    private static final class CodeBeforeSuper {

        private final OggInputStream oggInputStream;
        private final Info info = new Info();
        private final Comment comment = new Comment();
        private final DspState dspState = new DspState();

        private CodeBeforeSuper(final InputStream inputStream) throws IOException {
            this.oggInputStream = new OggInputStream(inputStream);
            for (int i = 0; i < 3; i++) {
                final Packet packet = this.oggInputStream.readPacket();
                if (this.info.synthesis_headerin(this.comment, packet) < 0) {
                    throw new IOException("Invalid Ogg header packet " + i);
                }
            }
            if (this.dspState.synthesis_init(this.info) < 0) {
                throw new IOException("Failed to initialize dsp state");
            }
        }

    }

}
