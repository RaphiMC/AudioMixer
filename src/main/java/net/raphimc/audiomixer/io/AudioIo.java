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
import net.raphimc.audiomixer.io.ogg.opus.OggOpusAudioInputStream;
import net.raphimc.audiomixer.io.ogg.vorbis.OggVorbisAudioInputStream;
import net.raphimc.audiomixer.io.special.JavaSoundAudioInputStream;
import net.raphimc.audiomixer.io.special.ResamplingAudioInputStream;
import net.raphimc.audiomixer.io.wav.WavInputStream;
import net.raphimc.audiomixer.io.wav.pcm.WavPcmAudioInputStream;
import net.raphimc.audiomixer.resampler.Resampler;
import net.raphimc.audiomixer.resampler.impl.BlackmanSincResampler;
import net.raphimc.audiomixer.util.ArrayUtil;
import net.raphimc.audiomixer.util.AudioFormat;
import net.raphimc.audiomixer.util.buffer.AudioBuffer;
import net.raphimc.audiomixer.util.io.ogg.OggInputStream;
import net.raphimc.audiomixer.util.io.riff.RiffInputStream;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public final class AudioIo {

    private static final int MAGIC_LENGTH = 16;
    private static final int MAX_HEADER_LENGTH = 256 * 1024;

    private static final byte[] RIFF_MAGIC = new byte[]{'R', 'I', 'F', 'F'};
    private static final String WAV_MAGIC = "WAVE";
    private static final UUID WAV_FORMAT_PCM = UUID.fromString("00000001-0000-0010-8000-00aa00389b71");
    private static final UUID WAV_FORMAT_IEEE_FLOAT = UUID.fromString("00000003-0000-0010-8000-00aa00389b71");

    private static final byte[] OGG_MAGIC = new byte[]{'O', 'g', 'g', 'S'};
    private static final byte[] OGG_VORBIS_MAGIC = new byte[]{1, 'v', 'o', 'r', 'b', 'i', 's'};
    private static final byte[] OGG_OPUS_MAGIC = new byte[]{'O', 'p', 'u', 's', 'H', 'e', 'a', 'd'};

    private static final byte[] MP3_ID3V1_MAGIC = new byte[]{'T', 'A', 'G'};
    private static final byte[] MP3_ID3V2_MAGIC = new byte[]{'I', 'D', '3'};
    private static final byte[] MP3_APE_MAGIC = new byte[]{'A', 'P', 'E', 'T', 'A', 'G', 'E', 'X'};

    private AudioIo() {
    }

    public static AudioBuffer read(final InputStream inputStream) throws IOException {
        try (AudioInputStream audioInputStream = open(inputStream)) {
            return new AudioBuffer(audioInputStream.getFormat(), audioInputStream.readFully());
        }
    }

    public static AudioBuffer read(final InputStream inputStream, final AudioFormat targetFormat) throws IOException {
        return read(inputStream, targetFormat, new BlackmanSincResampler());
    }

    public static AudioBuffer read(final InputStream inputStream, final AudioFormat targetFormat, final Resampler resampler) throws IOException {
        return resampler.resample(read(inputStream), targetFormat);
    }

    public static AudioInputStream open(final InputStream inputStream) throws IOException {
        final BufferedInputStream bis = new BufferedInputStream(inputStream);
        bis.mark(MAGIC_LENGTH);
        final byte[] magic = bis.readNBytes(MAGIC_LENGTH);
        bis.reset();
        if (ArrayUtil.startsWith(magic, RIFF_MAGIC)) {
            bis.mark(MAX_HEADER_LENGTH);
            final RiffInputStream riffInputStream = new RiffInputStream(bis);
            bis.reset();
            if (riffInputStream.getRootChunk().identifier().equals(WAV_MAGIC)) {
                bis.mark(MAX_HEADER_LENGTH);
                final WavInputStream wavInputStream = new WavInputStream(bis);
                bis.reset();
                if (wavInputStream.getFormat().equals(WAV_FORMAT_PCM) || wavInputStream.getFormat().equals(WAV_FORMAT_IEEE_FLOAT)) {
                    return new WavPcmAudioInputStream(bis);
                }
            }
        } else if (ArrayUtil.startsWith(magic, OGG_MAGIC)) {
            bis.mark(MAX_HEADER_LENGTH);
            final OggInputStream oggInputStream = new OggInputStream(bis);
            while (true) {
                final OggInputStream.OggPacket packet = oggInputStream.readPacket();
                if (packet.bos()) {
                    if (ArrayUtil.startsWith(packet.data(), OGG_VORBIS_MAGIC)) {
                        bis.reset();
                        return new OggVorbisAudioInputStream(bis);
                    } else if (ArrayUtil.startsWith(packet.data(), OGG_OPUS_MAGIC)) {
                        bis.reset();
                        return new OggOpusAudioInputStream(bis);
                    }
                } else {
                    bis.reset();
                    break;
                }
            }
        } else if (ArrayUtil.startsWith(magic, MP3_ID3V1_MAGIC) || ArrayUtil.startsWith(magic, MP3_ID3V2_MAGIC) || ArrayUtil.startsWith(magic, MP3_APE_MAGIC) || (magic.length >= 2 && (magic[0] & 0xFF) == 0xFF && (magic[1] & 0xE0) == 0xE0)) {
            return new Mp3AudioInputStream(bis);
        }
        try {
            return new JavaSoundAudioInputStream(AudioSystem.getAudioInputStream(bis));
        } catch (final UnsupportedAudioFileException e) {
            throw new IOException("Unsupported audio file format", e);
        }
    }

    public static AudioInputStream open(final InputStream inputStream, final AudioFormat targetFormat) throws IOException {
        return open(inputStream, targetFormat, new BlackmanSincResampler());
    }

    public static AudioInputStream open(final InputStream inputStream, final AudioFormat targetFormat, final Resampler resampler) throws IOException {
        return ResamplingAudioInputStream.wrapIfNeeded(open(inputStream), targetFormat, resampler);
    }

}
