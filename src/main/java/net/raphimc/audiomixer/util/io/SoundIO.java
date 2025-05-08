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

import net.raphimc.audiomixer.util.AudioFormatModifier;
import net.raphimc.audiomixer.util.GrowableArray;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;

public class SoundIO {

    public static float[] readSamples(final InputStream is, final AudioFormatModifier audioFormatModifier) throws IOException, UnsupportedAudioFileException {
        return readSamples(new BufferedInputStream(is), audioFormatModifier);
    }

    public static float[] readSamples(final BufferedInputStream is, final AudioFormatModifier audioFormatModifier) throws IOException, UnsupportedAudioFileException {
        return readSamples(AudioSystem.getAudioInputStream(is), audioFormatModifier);
    }

    public static float[] readSamples(final AudioInputStream is, final AudioFormatModifier audioFormatModifier) throws IOException {
        final SampleInputStream sis = new SampleInputStream(is, audioFormatModifier);
        final GrowableArray samples = new GrowableArray(1024 * 256);
        float sample;
        while (!Float.isNaN((sample = sis.readSample()))) {
            samples.add(sample);
        }
        sis.close();
        return samples.getArray();
    }

    public static void writeSamples(final float[] samples, final OutputStream os, final AudioFormat targetFormat) throws IOException {
        final SampleOutputStream sos = new SampleOutputStream(os, targetFormat);
        for (float sample : samples) {
            sos.writeSample(sample);
        }
        sos.close();
    }

    public static byte[] writeSamples(final float[] samples, final AudioFormat targetFormat) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeSamples(samples, baos, targetFormat);
        return baos.toByteArray();
    }

    public static AudioInputStream createAudioInputStream(final float[] samples, final AudioFormat targetFormat) throws IOException {
        return new AudioInputStream(new ByteArrayInputStream(writeSamples(samples, targetFormat)), targetFormat, samples.length);
    }

}
