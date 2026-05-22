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

import net.raphimc.audiomixer.io.raw.SampleInputStream;
import net.raphimc.audiomixer.io.raw.SampleOutputStream;
import net.raphimc.audiomixer.util.FloatAudioFormat;
import net.raphimc.audiomixer.util.buffer.AudioBuffer;
import net.raphimc.audiomixer.util.buffer.AudioBufferBuilder;
import net.raphimc.audiomixer.util.math.MathUtil;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;

public class AudioIO {

    public static AudioBuffer read(final InputStream is) throws IOException, UnsupportedAudioFileException {
        return read(new SampleInputStream(is));
    }

    public static AudioBuffer read(final InputStream is, final FloatAudioFormat targetFormat) throws IOException, UnsupportedAudioFileException {
        return read(new SampleInputStream(is, targetFormat));
    }

    public static AudioBuffer read(final BufferedInputStream is) throws IOException, UnsupportedAudioFileException {
        return read(new SampleInputStream(is));
    }

    public static AudioBuffer read(final BufferedInputStream is, final FloatAudioFormat targetFormat) throws IOException, UnsupportedAudioFileException {
        return read(new SampleInputStream(is, targetFormat));
    }

    public static AudioBuffer read(final AudioInputStream is) throws IOException {
        return read(new SampleInputStream(is));
    }

    public static AudioBuffer read(final AudioInputStream is, final FloatAudioFormat targetFormat) throws IOException {
        return read(new SampleInputStream(is, targetFormat));
    }

    public static AudioBuffer read(final SampleInputStream is) throws IOException {
        try (is) {
            final AudioBufferBuilder bufferBuilder = new AudioBufferBuilder(is.getFormat());
            try {
                while (true) {
                    bufferBuilder.put(is.readSample());
                }
            } catch (EOFException ignored) {
            }
            return bufferBuilder.build();
        }
    }

    public static void writeSamples(final float[] samples, final OutputStream os, final AudioFormat targetFormat) throws IOException {
        final SampleOutputStream sos = new SampleOutputStream(os, targetFormat);
        for (float sample : samples) {
            sos.writeSample(sample);
        }
        sos.close();
    }

    public static byte[] writeSamples(final float[] samples, final AudioFormat targetFormat) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(MathUtil.sampleCountToByteCount(targetFormat, samples.length));
        writeSamples(samples, baos, targetFormat);
        return baos.toByteArray();
    }

    public static AudioInputStream createAudioInputStream(final float[] samples, final AudioFormat targetFormat) throws IOException {
        return new AudioInputStream(new ByteArrayInputStream(writeSamples(samples, targetFormat)), targetFormat, samples.length);
    }

}
