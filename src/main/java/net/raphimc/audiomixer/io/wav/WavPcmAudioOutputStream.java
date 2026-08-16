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
package net.raphimc.audiomixer.io.wav;

import net.raphimc.audiomixer.io.pcm.PcmAudioOutputStream;
import net.raphimc.audiomixer.io.wav.riff.RiffChunk;
import net.raphimc.audiomixer.io.wav.riff.RiffOutputStream;
import net.raphimc.audiomixer.util.PcmAudioFormat;

import java.io.IOException;
import java.io.OutputStream;

public class WavPcmAudioOutputStream extends PcmAudioOutputStream {

    private static final int FMT_CHUNK_LENGTH = Short.BYTES + Short.BYTES + Integer.BYTES + Integer.BYTES + Short.BYTES + Short.BYTES;

    private static final int WAVE_FORMAT_PCM = 0x0001;
    private static final int WAVE_FORMAT_IEEE_FLOAT = 0x0003;

    private final RiffChunk dataChunk;

    public WavPcmAudioOutputStream(final OutputStream outputStream, final PcmAudioFormat format, final long sampleCount) throws IOException {
        this(new CodeBeforeSuper(outputStream, format, sampleCount));
    }

    private WavPcmAudioOutputStream(final CodeBeforeSuper codeBeforeSuper) {
        super(codeBeforeSuper.riffOutputStream, codeBeforeSuper.format);
        this.dataChunk = codeBeforeSuper.dataChunk;
    }

    @Override
    public void close() throws IOException {
        this.dataChunk.close();
        super.close();
    }

    private static final class CodeBeforeSuper {

        private final RiffOutputStream riffOutputStream;
        private final PcmAudioFormat format;
        private final RiffChunk dataChunk;

        private CodeBeforeSuper(final OutputStream outputStream, final PcmAudioFormat format, final long sampleCount) throws IOException {
            final long dataChunkLength = Math.multiplyExact(sampleCount, format.encoding().bytesPerSample());
            this.riffOutputStream = new RiffOutputStream(outputStream, "WAVE", Math.addExact(Math.addExact(RiffChunk.BYTES + FMT_CHUNK_LENGTH + RiffChunk.BYTES, dataChunkLength), dataChunkLength % 2));
            this.format = format;
            try (RiffChunk fmtChunk = this.riffOutputStream.writeChunk("fmt ", FMT_CHUNK_LENGTH)) {
                this.riffOutputStream.writeUnsignedShort(switch (format.encoding()) {
                    case U8, S16_LE, S24_LE, S32_LE -> WAVE_FORMAT_PCM;
                    case F32_LE, F64_LE -> WAVE_FORMAT_IEEE_FLOAT;
                    default -> throw new IllegalArgumentException("Unsupported encoding: " + format.encoding());
                });
                this.riffOutputStream.writeUnsignedShort(format.format().channels());
                this.riffOutputStream.writeUnsignedInt(Math.round(format.format().sampleRate()));
                this.riffOutputStream.writeUnsignedInt(format.bytesPerSecond());
                this.riffOutputStream.writeUnsignedShort(format.bytesPerFrame());
                this.riffOutputStream.writeUnsignedShort(format.encoding().bytesPerSample() * Byte.SIZE);
            }
            this.dataChunk = this.riffOutputStream.writeChunk("data", dataChunkLength);
        }

    }

}
