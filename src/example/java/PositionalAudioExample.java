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
import net.raphimc.audiomixer.dsp.processor.spatial.PositionalAudioProcessor;
import net.raphimc.audiomixer.source.oscillator.OscillatorSource;
import net.raphimc.audiomixer.source.oscillator.impl.SineOscillatorSource;
import net.raphimc.audiomixer.util.math.Vector3f;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class PositionalAudioExample {

    public static void main(String[] args) throws Throwable {
        AudioFormat format = new AudioFormat(48000, 16, 2, true, false);
        SourceDataLineAudioMixer audioMixer = new SourceDataLineAudioMixer(AudioSystem.getSourceDataLine(format));

        final OscillatorSource oscillator = new SineOscillatorSource(440);
        final PositionalAudioProcessor positionalAudioProcessor = new PositionalAudioProcessor(250);
        oscillator.getProcessors().add(positionalAudioProcessor);
        audioMixer.add(oscillator);

        JFrame frame = new JFrame("AudioMixer Test");
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        frame.pack();
        frame.setSize(480, 360);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        positionalAudioProcessor.listenerPosition().set(new Vector3f(frame.getWidth() / 2F, 0F, frame.getHeight() / 2F));
        positionalAudioProcessor.sourcePosition().set(positionalAudioProcessor.listenerPosition().get());

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.RED);
                g.fillOval((int) positionalAudioProcessor.sourcePosition().get().x() - 5, (int) positionalAudioProcessor.sourcePosition().get().z() - 5, 10, 10);
                g.setColor(Color.BLUE);
                g.fillOval((int) positionalAudioProcessor.listenerPosition().get().x() - 5, (int) positionalAudioProcessor.listenerPosition().get().z() - 5, 10, 10);
            }
        };

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                positionalAudioProcessor.sourcePosition().set(new Vector3f(e.getX(), 0F, e.getY()));
                e.getComponent().repaint();
            }
        });

        frame.add(panel);
        frame.setVisible(true);
    }

}
