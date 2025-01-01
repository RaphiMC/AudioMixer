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

import net.raphimc.audiomixer.AudioMixer;
import net.raphimc.audiomixer.pcmsource.impl.MonoIntPcmSource;
import net.raphimc.audiomixer.sound.impl.pcm.OptimizedMonoSound;
import net.raphimc.audiomixer.util.AudioFormats;
import net.raphimc.audiomixer.util.GrowableArray;
import net.raphimc.audiomixer.util.SoundSampleUtil;
import net.raphimc.audiomixer.util.io.SoundIO;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.FileInputStream;

public class FileTest {

    public static void main(String[] args) throws Throwable {
        File input = new File("input.wav");
        File output = new File("output.wav");
        AudioFormat format = new AudioFormat(48000, 16, 2, true, false);

        // Load the input audio samples
        int[] samples = SoundIO.readSamples(new FileInputStream(input), AudioFormats.withChannels(format, 1));
        // Create an audio mixer
        AudioMixer mixer = new AudioMixer(format);
        // Play the audio samples
        mixer.playSound(new OptimizedMonoSound(new MonoIntPcmSource(samples), 2F, 1, 0));
        // Create the output buffer
        GrowableArray outputSamples = new GrowableArray(0);
        // Mix 1 second of audio until there are no more active sounds (The mixer will automatically stop finished sounds)
        while (mixer.getMasterMixSound().getActiveSounds() > 0) {
            outputSamples.add(mixer.mixMillis(1000));
        }
        // Normalize the audio samples, so that they are in the range of a short (16 bit)
        SoundSampleUtil.normalize(outputSamples.getArrayDirect(), Short.MAX_VALUE);
        // Write the audio samples to a file
        AudioSystem.write(SoundIO.createAudioInputStream(outputSamples.getArray(), format), AudioFileFormat.Type.WAVE, output);
    }

}
