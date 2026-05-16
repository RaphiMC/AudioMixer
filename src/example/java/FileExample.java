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
import net.raphimc.audiomixer.io.AudioIO;
import net.raphimc.audiomixer.source.audio.impl.BufferedAudioSource;
import net.raphimc.audiomixer.util.buffer.AudioBuffer;
import net.raphimc.audiomixer.util.buffer.AudioBufferBuilder;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.FileInputStream;

public class FileExample {

    public static void main(String[] args) throws Throwable {
        File input = new File("input.wav");
        File output = new File("output.wav");

        // Load the input audio buffer
        AudioBuffer inputAudioBuffer = AudioIO.read(new FileInputStream(input));
        // Create an audio mixer
        AudioMixer mixer = new AudioMixer(inputAudioBuffer.format());
        // Play the audio buffer with half the original pitch
        BufferedAudioSource source = new BufferedAudioSource(inputAudioBuffer);
        source.setPitch(0.5F);
        mixer.add(source);
        // Create the output buffer
        AudioBufferBuilder outputBufferBuilder = new AudioBufferBuilder(mixer.getAudioFormat());
        // Render 1 second of audio until there are no more active sources (The mixer will automatically remove finished sources)
        while (mixer.getActiveSources() > 0) {
            outputBufferBuilder.put(mixer.renderMillis(1000));
        }
        AudioBuffer outputAudioBuffer = outputBufferBuilder.build();
        // Limit the audio samples to [-1, 1]
        outputAudioBuffer.limitToUnitRange();
        // Trim trailing silence
        outputAudioBuffer = outputAudioBuffer.trimTrailingSilence();
        // Write the audio buffer to a file
        AudioSystem.write(AudioIO.createAudioInputStream(outputAudioBuffer.samples(), outputAudioBuffer.format().toJavaPcmAudioFormat(Short.SIZE)), AudioFileFormat.Type.WAVE, output);
    }

}
