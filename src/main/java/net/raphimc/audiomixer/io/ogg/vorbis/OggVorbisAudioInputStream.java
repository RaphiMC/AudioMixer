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
import net.raphimc.audiomixer.util.ArrayUtil;
import net.raphimc.audiomixer.util.AudioFormat;
import net.raphimc.audiomixer.util.buffer.FloatRingBuffer;
import net.raphimc.audiomixer.util.io.ogg.OggInputStream;

import java.io.IOException;
import java.io.InputStream;

public class OggVorbisAudioInputStream extends AudioInputStream {

    private static final byte[] VORBIS_MAGIC = new byte[]{(byte) 0x01, (byte) 'v', (byte) 'o', (byte) 'r', (byte) 'b', (byte) 'i', (byte) 's'};
    private static final int MAX_FRAME_COUNT = 8192; // Hardcoded in JOrbis

    private final OggInputStream oggInputStream;
    private final int vorbisStreamId;
    private final DspState dspState = new DspState();
    private final Block block = new Block(this.dspState);
    private final FloatRingBuffer samplesBuffer;

    public OggVorbisAudioInputStream(final InputStream inputStream) throws IOException {
        this(new CodeBeforeSuper(inputStream));
    }

    private OggVorbisAudioInputStream(final CodeBeforeSuper codeBeforeSuper) throws IOException {
        super(new AudioFormat(codeBeforeSuper.info.rate, codeBeforeSuper.info.channels));
        this.oggInputStream = codeBeforeSuper.oggInputStream;
        this.vorbisStreamId = codeBeforeSuper.vorbisStreamId;
        checkResult(this.dspState.synthesis_init(codeBeforeSuper.info), "Failed to initialize dsp state");
        this.samplesBuffer = new FloatRingBuffer(MAX_FRAME_COUNT * this.getFormat().channelCount());
    }

    @Override
    public float read() throws IOException {
        while (this.samplesBuffer.isEmpty()) {
            this.decodeNextPacket();
        }
        return this.samplesBuffer.read();
    }

    private void decodeNextPacket() throws IOException {
        final Packet packet = convertPacket(this.oggInputStream.readUntilPacket(this.vorbisStreamId));
        checkResult(this.block.synthesis(packet), "Failed to decode audio packet");
        checkResult(this.dspState.synthesis_blockin(this.block), "Failed to process audio block");

        final int channelCount = this.getFormat().channelCount();
        final float[][][] samplesRef = new float[1][][];
        final int[] offsets = new int[channelCount];
        int frameCount;
        while ((frameCount = this.dspState.synthesis_pcmout(samplesRef, offsets)) > 0) {
            final float[][] channelSamples = samplesRef[0];
            for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
                for (int channelIndex = 0; channelIndex < channelCount; channelIndex++) {
                    this.samplesBuffer.write(channelSamples[channelIndex][offsets[channelIndex] + frameIndex]);
                }
            }
            checkResult(this.dspState.synthesis_read(frameCount), "Failed to update dsp state");
        }
    }

    @Override
    public void close() throws IOException {
        this.oggInputStream.close();
    }

    private static Packet convertPacket(final OggInputStream.OggPacket inPacket) {
        final Packet outPacket = new Packet();
        outPacket.packet_base = inPacket.data();
        outPacket.bytes = inPacket.data().length;
        outPacket.b_o_s = inPacket.bos() ? 1 : 0;
        outPacket.e_o_s = inPacket.eos() ? 1 : 0;
        outPacket.granulepos = inPacket.granulePosition();
        outPacket.packetno = inPacket.packetNumber();
        return outPacket;
    }

    private static void checkResult(final int result, final String message) throws IOException {
        if (result < 0) {
            throw new IOException(message + " (error code: " + result + ")");
        }
    }

    private static final class CodeBeforeSuper {

        private final OggInputStream oggInputStream;
        private final int vorbisStreamId;
        private final Info info = new Info();
        private final Comment comment = new Comment();

        private CodeBeforeSuper(final InputStream inputStream) throws IOException {
            this.oggInputStream = new OggInputStream(inputStream);
            while (true) {
                final OggInputStream.OggPacket packet = this.oggInputStream.readPacket();
                if (packet.bos() && ArrayUtil.startsWith(packet.data(), VORBIS_MAGIC)) {
                    this.vorbisStreamId = packet.streamId();
                    checkResult(this.info.synthesis_headerin(this.comment, convertPacket(packet)), "Failed to process info packet");
                    break;
                }
            }
            checkResult(this.info.synthesis_headerin(this.comment, convertPacket(this.oggInputStream.readUntilPacket(this.vorbisStreamId))), "Failed to process comment packet");
            checkResult(this.info.synthesis_headerin(this.comment, convertPacket(this.oggInputStream.readUntilPacket(this.vorbisStreamId))), "Failed to process setup packet");
        }

    }

}
