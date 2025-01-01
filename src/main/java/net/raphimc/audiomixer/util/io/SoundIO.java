/*
 * This file is part of AudioMixer - https://github.com/RaphiMC/AudioMixer
 * Copyright (C) 2024-2025 RK_01/RaphiMC and contributors
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
package net.raphimc.audiomixer.util.io;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;

public class SoundIO {

    public static int[] readSamples(final InputStream is, final AudioFormat targetFormat) throws IOException, UnsupportedAudioFileException {
        return readSamples(new BufferedInputStream(is), targetFormat);
    }

    public static int[] readSamples(final BufferedInputStream is, final AudioFormat targetFormat) throws IOException, UnsupportedAudioFileException {
        return readSamples(AudioSystem.getAudioInputStream(is), targetFormat);
    }

    public static int[] readSamples(AudioInputStream is, final AudioFormat targetFormat) throws IOException {
        if (!is.getFormat().matches(targetFormat)) is = AudioSystem.getAudioInputStream(targetFormat, is);
        final byte[] audioBytes = is.readAllBytes();
        final SampleInputStream sis = new SampleInputStream(new ByteArrayInputStream(audioBytes), targetFormat);

        final int sampleSize = targetFormat.getSampleSizeInBits() / 8;
        final int[] samples = new int[audioBytes.length / sampleSize];
        for (int i = 0; i < samples.length; i++) {
            samples[i] = sis.readSample();
        }

        sis.close();
        return samples;
    }

    public static void writeSamples(final int[] samples, final OutputStream os, final AudioFormat targetFormat) throws IOException {
        final SampleOutputStream sos = new SampleOutputStream(os, targetFormat);
        for (int sample : samples) {
            sos.writeSample(sample);
        }
        sos.close();
    }

    public static byte[] writeSamples(final int[] samples, final AudioFormat targetFormat) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeSamples(samples, baos, targetFormat);
        return baos.toByteArray();
    }

    public static AudioInputStream createAudioInputStream(final int[] samples, final AudioFormat targetFormat) throws IOException {
        return new AudioInputStream(new ByteArrayInputStream(writeSamples(samples, targetFormat)), targetFormat, samples.length);
    }

}
