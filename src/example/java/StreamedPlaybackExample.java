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

import net.raphimc.audiomixer.BackgroundSourceDataLineAudioMixer;
import net.raphimc.audiomixer.pcmsource.impl.StereoPullPcmSource;
import net.raphimc.audiomixer.sound.impl.pcm.StereoSound;
import net.raphimc.audiomixer.util.PcmFloatAudioFormat;
import net.raphimc.audiomixer.util.io.SampleInputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.net.URL;

public class StreamedPlaybackExample {

    public static void main(String[] args) throws Throwable {
        AudioFormat format = new AudioFormat(48000, 16, 2, true, false);
        BackgroundSourceDataLineAudioMixer audioMixer = new BackgroundSourceDataLineAudioMixer(AudioSystem.getSourceDataLine(format));

        final AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new URL("https://example.com/sound.wav"));
        audioMixer.playSound(new StereoSound(new StereoPullPcmSource(new SampleInputStream(audioInputStream, new PcmFloatAudioFormat(format.getSampleRate(), 2)), 48000 * 4)));

        Thread.sleep(Integer.MAX_VALUE);
    }

}
