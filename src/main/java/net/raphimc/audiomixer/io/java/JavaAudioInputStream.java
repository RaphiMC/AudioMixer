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
package net.raphimc.audiomixer.io.java;

import net.raphimc.audiomixer.io.pcm.PcmAudioInputStream;
import net.raphimc.audiomixer.util.JavaAudioFormatUtil;
import net.raphimc.audiomixer.util.PcmSampleEncoding;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;
import java.io.InputStream;

public class JavaAudioInputStream extends PcmAudioInputStream {

    public JavaAudioInputStream(final AudioInputStream inputStream) {
        this(new CodeBeforeSuper(inputStream));
    }

    private JavaAudioInputStream(final CodeBeforeSuper codeBeforeSuper) {
        super(new BlockInputStream(codeBeforeSuper.inputStream, codeBeforeSuper.inputStream.getFormat().getFrameSize()), JavaAudioFormatUtil.getAudioFormat(codeBeforeSuper.inputStream.getFormat()), codeBeforeSuper.encoding);
    }

    private static final class CodeBeforeSuper {

        private final AudioInputStream inputStream;
        private final PcmSampleEncoding encoding;

        private CodeBeforeSuper(AudioInputStream inputStream) {
            try {
                JavaAudioFormatUtil.getPcmSampleEncoding(inputStream.getFormat());
            } catch (final IllegalArgumentException ignored) {
                final AudioFormat targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_FLOAT, inputStream.getFormat().getSampleRate(), Float.SIZE, inputStream.getFormat().getChannels(), inputStream.getFormat().getChannels() * Float.BYTES, inputStream.getFormat().getFrameRate(), true);
                inputStream = AudioSystem.getAudioInputStream(targetFormat, inputStream);
            }
            this.inputStream = inputStream;
            this.encoding = JavaAudioFormatUtil.getPcmSampleEncoding(inputStream.getFormat());
        }

    }

    private static final class BlockInputStream extends InputStream {

        private final InputStream delegate;
        private final byte[] buffer;
        private int index;
        private int limit;

        private BlockInputStream(final InputStream delegate, final int blockSize) {
            this.delegate = delegate;
            this.buffer = new byte[blockSize];
        }

        @Override
        public int read() throws IOException {
            if (this.index >= this.limit) {
                this.index = 0;
                this.limit = this.delegate.readNBytes(this.buffer, 0, this.buffer.length);
                if (this.limit == 0) {
                    return -1;
                }
            }
            return this.buffer[this.index++] & 0xFF;
        }

        @Override
        public void close() throws IOException {
            this.delegate.close();
        }

    }

}
