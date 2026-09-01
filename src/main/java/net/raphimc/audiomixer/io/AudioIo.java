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

import net.raphimc.audiomixer.io.javasound.JavaSoundAudioInputStream;
import net.raphimc.audiomixer.io.mp3.Mp3AudioInputStream;
import net.raphimc.audiomixer.io.ogg.opus.OggOpusAudioInputStream;
import net.raphimc.audiomixer.io.ogg.vorbis.OggVorbisAudioInputStream;
import net.raphimc.audiomixer.io.wav.WavInputStream;
import net.raphimc.audiomixer.io.wav.pcm.WavPcmAudioInputStream;
import net.raphimc.audiomixer.resampler.Resampler;
import net.raphimc.audiomixer.resampler.impl.LinearResampler;
import net.raphimc.audiomixer.util.ArrayUtil;
import net.raphimc.audiomixer.util.AudioFormat;
import net.raphimc.audiomixer.util.buffer.AudioBuffer;
import net.raphimc.audiomixer.util.io.ogg.OggInputStream;
import net.raphimc.audiomixer.util.io.riff.RiffInputStream;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public final class AudioIo {

    private static final int BUFFER_SIZE = 128 * 1024;

    private static final byte[] RIFF_MAGIC = new byte[]{(byte) 'R', (byte) 'I', (byte) 'F', (byte) 'F'};
    private static final String WAV_MAGIC = "WAVE";
    private static final UUID WAV_FORMAT_PCM = UUID.fromString("00000001-0000-0010-8000-00aa00389b71");
    private static final UUID WAV_FORMAT_IEEE_FLOAT = UUID.fromString("00000003-0000-0010-8000-00aa00389b71");

    private static final byte[] OGG_MAGIC = new byte[]{(byte) 'O', (byte) 'g', (byte) 'g', (byte) 'S'};
    private static final byte[] OGG_VORBIS_MAGIC = new byte[]{(byte) 0x01, (byte) 'v', (byte) 'o', (byte) 'r', (byte) 'b', (byte) 'i', (byte) 's'};
    private static final byte[] OGG_OPUS_MAGIC = new byte[]{(byte) 'O', (byte) 'p', (byte) 'u', (byte) 's', (byte) 'H', (byte) 'e', (byte) 'a', (byte) 'd'};

    private static final byte[] TAGGED_MP3_MAGIC = new byte[]{(byte) 'I', (byte) 'D', (byte) '3'};

    private AudioIo() {
    }

    public static AudioBuffer read(final InputStream inputStream) throws IOException {
        try (AudioInputStream audioInputStream = open(inputStream)) {
            return new AudioBuffer(audioInputStream.getFormat(), audioInputStream.readFully());
        }
    }

    public static AudioBuffer read(final InputStream inputStream, final AudioFormat targetFormat) throws IOException {
        return read(inputStream, targetFormat, new LinearResampler());
    }

    public static AudioBuffer read(final InputStream inputStream, final AudioFormat targetFormat, final Resampler resampler) throws IOException {
        return resampler.resample(read(inputStream), targetFormat);
    }

    public static AudioInputStream open(final InputStream inputStream) throws IOException {
        final BufferedInputStream bis = new BufferedInputStream(inputStream, BUFFER_SIZE);
        bis.mark(BUFFER_SIZE);
        final byte[] buffer = bis.readNBytes(BUFFER_SIZE);
        bis.reset();
        if (ArrayUtil.startsWith(buffer, RIFF_MAGIC)) {
            final RiffInputStream riffInputStream = new RiffInputStream(new ByteArrayInputStream(buffer));
            if (riffInputStream.getRootChunk().identifier().equals(WAV_MAGIC)) {
                final WavInputStream wavInputStream = new WavInputStream(new ByteArrayInputStream(buffer));
                if (wavInputStream.getFormat().equals(WAV_FORMAT_PCM) || wavInputStream.getFormat().equals(WAV_FORMAT_IEEE_FLOAT)) {
                    return new WavPcmAudioInputStream(bis);
                }
            }
        } else if (ArrayUtil.startsWith(buffer, OGG_MAGIC)) {
            final OggInputStream oggInputStream = new OggInputStream(new ByteArrayInputStream(buffer));
            while (true) {
                final OggInputStream.OggPacket packet = oggInputStream.readPacket();
                if (packet.bos()) {
                    if (ArrayUtil.startsWith(packet.data(), OGG_VORBIS_MAGIC)) {
                        return new OggVorbisAudioInputStream(bis);
                    } else if (ArrayUtil.startsWith(packet.data(), OGG_OPUS_MAGIC)) {
                        return new OggOpusAudioInputStream(bis);
                    }
                } else {
                    break;
                }
            }
        } else if (ArrayUtil.startsWith(buffer, TAGGED_MP3_MAGIC)) {
            return new Mp3AudioInputStream(bis);
        } else if (buffer.length >= 2 && buffer[0] == (byte) 0xFF && (buffer[1] & 0xE0) == 0xE0 && ((buffer[1] >> 3) & 0x03) == 0x01) { // Untagged MP3
            return new Mp3AudioInputStream(bis);
        }
        try {
            return new JavaSoundAudioInputStream(AudioSystem.getAudioInputStream(bis));
        } catch (final UnsupportedAudioFileException e) {
            throw new IOException("Unsupported audio file format", e);
        }
    }

}
