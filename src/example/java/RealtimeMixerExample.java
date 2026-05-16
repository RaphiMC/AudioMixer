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

import net.raphimc.audiomixer.SourceDataLineAudioMixer;
import net.raphimc.audiomixer.dsp.processor.dynamics.GainProcessor;
import net.raphimc.audiomixer.dsp.processor.spatial.PanProcessor;
import net.raphimc.audiomixer.io.AudioIO;
import net.raphimc.audiomixer.source.audio.AudioSource;
import net.raphimc.audiomixer.source.audio.impl.BufferedAudioSource;
import net.raphimc.audiomixer.util.buffer.AudioBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class RealtimeMixerExample {

    public static void main(String[] args) throws Throwable {
        AudioFormat format = new AudioFormat(48000, 16, 2, true, false);
        SourceDataLineAudioMixer audioMixer = new SourceDataLineAudioMixer(AudioSystem.getSourceDataLine(format));
        AudioBuffer audioBuffer = AudioIO.read(RealtimeMixerExample.class.getResourceAsStream("/piano.wav"), audioMixer.getAudioFormat().withChannels(1));

        JFrame frame = new JFrame("AudioMixer Test");
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        frame.pack();
        frame.setSize(480, 360);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() >= KeyEvent.VK_1 && e.getKeyCode() <= KeyEvent.VK_9) {
                    final AudioSource source = new BufferedAudioSource(audioBuffer);
                    source.setPitch((float) Math.pow(2, ((e.getKeyCode() - KeyEvent.VK_1) - 4) / 4F));
                    audioMixer.add(source);
                } else if (e.getKeyCode() == KeyEvent.VK_Q) {
                    final AudioSource source = new BufferedAudioSource(audioBuffer);
                    source.getProcessors().add(new PanProcessor(-1F));
                    audioMixer.add(source);
                } else if (e.getKeyCode() == KeyEvent.VK_P) {
                    final AudioSource source = new BufferedAudioSource(audioBuffer);
                    source.getProcessors().add(new PanProcessor(1F));
                    audioMixer.add(source);
                } else if (e.getKeyCode() == KeyEvent.VK_W) {
                    final AudioSource source = new BufferedAudioSource(audioBuffer);
                    source.getProcessors().add(new GainProcessor(0.25F));
                    audioMixer.add(source);
                } else if (e.getKeyCode() == KeyEvent.VK_E) {
                    final AudioSource source = new BufferedAudioSource(audioBuffer);
                    source.getProcessors().add(new GainProcessor(0.5F));
                    audioMixer.add(source);
                } else if (e.getKeyCode() == KeyEvent.VK_R) {
                    final AudioSource source = new BufferedAudioSource(audioBuffer);
                    source.getProcessors().add(new GainProcessor(1F));
                    audioMixer.add(source);
                } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    audioMixer.clear();
                }
            }
        });

        frame.add(new JLabel("Press 1-9 to play differently pitched piano notes"));
        frame.add(new JLabel("Press q or p to play differently panned piano notes"));
        frame.add(new JLabel("Press w, e, r to play differently loud piano notes"));
        frame.add(new JLabel("Press space to stop all audio sources"));

        frame.setVisible(true);
    }

}
