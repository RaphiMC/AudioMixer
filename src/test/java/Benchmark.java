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

import javax.sound.sampled.AudioFormat;
import java.util.Random;

public class Benchmark {

    public static void main(String[] args) {
        AudioFormat format = new AudioFormat(48000, 16, 2, true, false);
        int[] samples = new int[48000 * 10];
        Random random = new Random();
        for (int i = 0; i < samples.length; i++) {
            samples[i] = random.nextInt(65536) - 32768;
        }
        AudioMixer mixer = new AudioMixer(format);
        mixer.getMasterMixSound().setMaxSounds(65535);

        // Play 5000 concurrent sounds
        for (int i = 0; i < 5000; i++) {
            mixer.playSound(new OptimizedMonoSound(new MonoIntPcmSource(samples), 1.33F, 1, 0));
        }

        // Warmup (Mix 1 second of audio)
        for (int i = 0; i < 100; i++) {
            mixer.mixMillis(10);
        }

        // Benchmark (Mix 2 seconds of audio)
        long start = System.currentTimeMillis();
        for (int i = 0; i < 200; i++) {
            mixer.mixMillis(10);
        }
        long end = System.currentTimeMillis();
        System.out.println("Time: " + (end - start) + "ms");
    }

}
