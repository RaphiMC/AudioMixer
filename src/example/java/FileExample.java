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

import net.raphimc.audiomixer.AudioMixer;
import net.raphimc.audiomixer.io.AudioIo;
import net.raphimc.audiomixer.io.wav.pcm.WavPcmAudioOutputStream;
import net.raphimc.audiomixer.source.audio.impl.BufferedAudioSource;
import net.raphimc.audiomixer.util.PcmSampleEncoding;
import net.raphimc.audiomixer.util.buffer.AudioBuffer;
import net.raphimc.audiomixer.util.buffer.AudioBufferBuilder;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public final class FileExample {

    private FileExample() {
    }

    public static void main(final String[] args) throws Throwable {
        final File input = new File("input.wav");
        final File output = new File("output.wav");

        // Load the input audio buffer
        final AudioBuffer inputAudioBuffer = AudioIo.read(new FileInputStream(input));
        // Create an audio mixer
        final AudioMixer mixer = new AudioMixer(inputAudioBuffer.format());
        // Play the audio buffer with half the original pitch
        final BufferedAudioSource source = new BufferedAudioSource(inputAudioBuffer);
        source.pitch().set(0.5F);
        mixer.add(source);
        // Create the output buffer
        final AudioBufferBuilder outputBufferBuilder = new AudioBufferBuilder(mixer.getFormat());
        // Render 1 second of audio until there are no more active sources (The mixer will automatically remove finished sources)
        while (!mixer.isEmpty()) {
            outputBufferBuilder.append(mixer.renderMillis(1000));
        }
        AudioBuffer outputAudioBuffer = outputBufferBuilder.build();
        // Limit the audio samples to [-1, 1]
        outputAudioBuffer.limitToUnitRange();
        // Trim trailing silence
        outputAudioBuffer = outputAudioBuffer.trimTrailingSilence();
        // Write the audio buffer to a file
        final WavPcmAudioOutputStream wavOutputStream = new WavPcmAudioOutputStream(new BufferedOutputStream(new FileOutputStream(output)), outputAudioBuffer.format(), PcmSampleEncoding.S16_LE, outputAudioBuffer.sampleCount());
        wavOutputStream.write(outputAudioBuffer);
        wavOutputStream.close();
    }

}
