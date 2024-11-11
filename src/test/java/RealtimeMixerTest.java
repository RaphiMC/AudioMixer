/*
 * This file is part of AudioMixer - https://github.com/RaphiMC/AudioMixer
 * Copyright (C) 2024-2024 RK_01/RaphiMC and contributors
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
import net.raphimc.audiomixer.sound.pcmsource.impl.IntPcmSource;
import net.raphimc.audiomixer.sound.source.MonoSound;
import net.raphimc.audiomixer.util.AudioFormats;
import net.raphimc.audiomixer.util.io.SoundIO;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class RealtimeMixerTest {

    public static void main(String[] args) throws Throwable {
        AudioFormat format = new AudioFormat(48000, 16, 2, true, false);
        BackgroundSourceDataLineAudioMixer audioMixer = new BackgroundSourceDataLineAudioMixer(AudioSystem.getSourceDataLine(format));
        int[] pianoSamples = SoundIO.readSamples(RealtimeMixerTest.class.getResourceAsStream("/piano.wav"), AudioFormats.withChannels(format, 1));

        JFrame frame = new JFrame("AudioMixer Test");
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        frame.pack();
        frame.setSize(480, 360);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() >= KeyEvent.VK_1 && e.getKeyCode() <= KeyEvent.VK_9) {
                    audioMixer.playSound(new MonoSound(new IntPcmSource(pianoSamples), (float) Math.pow(2, ((e.getKeyCode() - KeyEvent.VK_1) - 4) / 4F), 1, 0));
                } else if (e.getKeyCode() == KeyEvent.VK_Q) {
                    audioMixer.playSound(new MonoSound(new IntPcmSource(pianoSamples), 1, 1, -1));
                } else if (e.getKeyCode() == KeyEvent.VK_P) {
                    audioMixer.playSound(new MonoSound(new IntPcmSource(pianoSamples), 1, 1, 1));
                } else if (e.getKeyCode() == KeyEvent.VK_W) {
                    audioMixer.playSound(new MonoSound(new IntPcmSource(pianoSamples), 1, 0.25F, 0));
                } else if (e.getKeyCode() == KeyEvent.VK_E) {
                    audioMixer.playSound(new MonoSound(new IntPcmSource(pianoSamples), 1, 0.75F, 0));
                } else if (e.getKeyCode() == KeyEvent.VK_R) {
                    audioMixer.playSound(new MonoSound(new IntPcmSource(pianoSamples), 1, 1.25F, 0));
                } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    audioMixer.stopAllSounds();
                }
            }
        });

        frame.add(new JLabel("Press 1-9 to play differently pitched piano notes"));
        frame.add(new JLabel("Press q or p to play differently panned piano notes"));
        frame.add(new JLabel("Press w, e, r to play differently loud piano notes"));
        frame.add(new JLabel("Press space to stop all sounds"));

        frame.setVisible(true);
    }

}
