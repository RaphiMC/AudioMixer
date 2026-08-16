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
package net.raphimc.audiomixer.io;

import net.raphimc.audiomixer.io.mp3.Mp3AudioInputStream;
import net.raphimc.audiomixer.io.ogg.OggVorbisAudioInputStream;
import net.raphimc.audiomixer.io.wav.WavPcmAudioInputStream;
import net.raphimc.audiomixer.resampler.impl.LinearResampler;
import net.raphimc.audiomixer.util.AudioFormat;
import net.raphimc.audiomixer.util.buffer.AudioBuffer;
import net.raphimc.audiomixer.util.io.BinaryInputStream;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public final class AudioIo {

    private static final int BUFFER_SIZE = 1024 * 1024;
    private static final int MAGIC_LENGTH = 4;
    private static final byte[] WAV_MAGIC = new byte[]{(byte) 'R', (byte) 'I', (byte) 'F', (byte) 'F'};
    private static final byte[] OGG_MAGIC = new byte[]{(byte) 'O', (byte) 'g', (byte) 'g', (byte) 'S'};
    private static final byte[] TAGGED_MP3_MAGIC = new byte[]{(byte) 'I', (byte) 'D', (byte) '3'};

    private AudioIo() {
    }

    public static AudioBuffer read(final InputStream inputStream) throws IOException {
        try (AudioInputStream audioInputStream = open(inputStream)) {
            return new AudioBuffer(audioInputStream.getFormat(), audioInputStream.readFully());
        }
    }

    public static AudioBuffer read(final InputStream inputStream, final AudioFormat targetFormat) throws IOException {
        return LinearResampler.INSTANCE.resample(read(inputStream), targetFormat);
    }

    public static AudioInputStream open(final InputStream inputStream) throws IOException {
        final BinaryInputStream bis = new BinaryInputStream(new BufferedInputStream(inputStream, BUFFER_SIZE));
        bis.mark(MAGIC_LENGTH);
        final byte[] magic = bis.readBytes(MAGIC_LENGTH);
        bis.reset();
        if (Arrays.equals(magic, 0, WAV_MAGIC.length, WAV_MAGIC, 0, WAV_MAGIC.length)) {
            return new WavPcmAudioInputStream(bis);
        } else if (Arrays.equals(magic, 0, OGG_MAGIC.length, OGG_MAGIC, 0, OGG_MAGIC.length)) {
            return new OggVorbisAudioInputStream(bis);
        } else if (Arrays.equals(magic, 0, TAGGED_MP3_MAGIC.length, TAGGED_MP3_MAGIC, 0, TAGGED_MP3_MAGIC.length)) {
            return new Mp3AudioInputStream(bis);
        } else if (magic[0] == (byte) 0xFF && (magic[1] & 0xE0) == 0xE0 && ((magic[1] >> 3) & 0x03) == 0x01) { // Untagged MP3
            return new Mp3AudioInputStream(bis);
        } else {
            throw new IOException("Unsupported audio file format");
        }
    }

}
