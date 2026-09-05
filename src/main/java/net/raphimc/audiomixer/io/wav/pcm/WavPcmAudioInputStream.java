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
package net.raphimc.audiomixer.io.wav.pcm;

import net.raphimc.audiomixer.io.pcm.PcmAudioInputStream;
import net.raphimc.audiomixer.io.wav.WavInputStream;
import net.raphimc.audiomixer.util.AudioFormat;
import net.raphimc.audiomixer.util.PcmSampleEncoding;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class WavPcmAudioInputStream extends PcmAudioInputStream {

    private static final UUID FORMAT_PCM = UUID.fromString("00000001-0000-0010-8000-00aa00389b71");
    private static final UUID FORMAT_IEEE_FLOAT = UUID.fromString("00000003-0000-0010-8000-00aa00389b71");

    public WavPcmAudioInputStream(final InputStream inputStream) throws IOException {
        this(new CodeBeforeSuper(inputStream));
    }

    private WavPcmAudioInputStream(final CodeBeforeSuper codeBeforeSuper) {
        super(codeBeforeSuper.wavInputStream, new AudioFormat(codeBeforeSuper.wavInputStream.getSampleRate(), codeBeforeSuper.wavInputStream.getChannelCount()), codeBeforeSuper.encoding);
    }

    private static final class CodeBeforeSuper {

        private final WavInputStream wavInputStream;
        private final PcmSampleEncoding encoding;

        private CodeBeforeSuper(final InputStream inputStream) throws IOException {
            this.wavInputStream = new WavInputStream(inputStream);
            if (this.wavInputStream.getFormat().equals(FORMAT_PCM)) {
                this.encoding = switch (this.wavInputStream.getBitsPerSample()) {
                    case 8 -> PcmSampleEncoding.U8;
                    case 16 -> PcmSampleEncoding.S16_LE;
                    case 24 -> PcmSampleEncoding.S24_LE;
                    case 32 -> PcmSampleEncoding.S32_LE;
                    default -> throw new IOException("Unsupported PCM bit depth: " + this.wavInputStream.getBitsPerSample());
                };
            } else if (this.wavInputStream.getFormat().equals(FORMAT_IEEE_FLOAT)) {
                this.encoding = switch (this.wavInputStream.getBitsPerSample()) {
                    case 32 -> PcmSampleEncoding.F32_LE;
                    case 64 -> PcmSampleEncoding.F64_LE;
                    default -> throw new IOException("Unsupported IEEE_FLOAT bit depth: " + this.wavInputStream.getBitsPerSample());
                };
            } else {
                throw new IOException("Unsupported format: " + this.wavInputStream.getFormat());
            }
            if (this.wavInputStream.getExtensibleSampleInfo() != -1 && this.wavInputStream.getBitsPerSample() != this.wavInputStream.getExtensibleSampleInfo()) {
                throw new IOException("Unsupported valid bits per sample: " + this.wavInputStream.getExtensibleSampleInfo());
            }
        }

    }

}
