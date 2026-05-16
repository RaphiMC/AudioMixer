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
import net.raphimc.audiomixer.source.audio.impl.BufferedAudioSource;
import net.raphimc.audiomixer.util.FloatAudioFormat;
import net.raphimc.audiomixer.util.buffer.AudioBuffer;

import java.util.Random;

public class Benchmark {

    public static void main(String[] args) {
        FloatAudioFormat outputFormat = new FloatAudioFormat(48000, 2);
        AudioBuffer buffer = new AudioBuffer(outputFormat.withChannels(1), 48000 * 10);
        float[] samples = buffer.samples();
        Random random = new Random();
        for (int i = 0; i < samples.length; i++) {
            samples[i] = random.nextFloat(-1, 1);
        }

        AudioMixer mixer = new AudioMixer(outputFormat);
        mixer.setMaxSources(65535);

        // Add 5000 sources
        for (int i = 0; i < 5000; i++) {
            final BufferedAudioSource source = new BufferedAudioSource(buffer);
            source.setPitch(1.33F);
            mixer.add(source);
        }

        // Warmup (Render 1 second of audio)
        for (int i = 0; i < 100; i++) {
            mixer.renderMillis(10);
        }

        // Benchmark (Render 2 seconds of audio)
        long start = System.currentTimeMillis();
        for (int i = 0; i < 200; i++) {
            mixer.renderMillis(10);
        }
        long end = System.currentTimeMillis();
        System.out.println("Time: " + (end - start) + "ms");
    }

}
