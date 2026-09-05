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
import net.raphimc.audiomixer.util.AudioFormat;
import net.raphimc.audiomixer.util.buffer.AudioBuffer;

import java.util.Random;

public final class Benchmark {

    private Benchmark() {
    }

    public static void main(final String[] args) {
        final AudioFormat outputFormat = new AudioFormat(48000, 2);
        final AudioBuffer buffer = new AudioBuffer(outputFormat.withChannelCount(1), 48000 * 10);
        final float[] samples = buffer.samples();
        final Random random = new Random();
        for (int i = 0; i < samples.length; i++) {
            samples[i] = random.nextFloat(-1, 1);
        }

        final AudioMixer mixer = new AudioMixer(outputFormat);

        // Add 5000 sources
        for (int i = 0; i < 5000; i++) {
            final BufferedAudioSource source = new BufferedAudioSource(buffer);
            source.pitch().set(1.33F);
            mixer.add(source);
        }

        // Warmup (Render 1 second of audio)
        for (int i = 0; i < 100; i++) {
            mixer.renderMillis(10);
        }

        // Benchmark (Render 2 seconds of audio)
        final long start = System.currentTimeMillis();
        for (int i = 0; i < 200; i++) {
            mixer.renderMillis(10);
        }
        final long end = System.currentTimeMillis();
        System.out.println("Time: " + (end - start) + "ms");
    }

}
