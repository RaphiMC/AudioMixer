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
import net.raphimc.audiomixer.sound.modifier.SpatialModifier;
import net.raphimc.audiomixer.sound.source.SineWaveSound;
import net.raphimc.audiomixer.sound.special.ModifiableSound;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class SpatialAudioTest {

    public static void main(String[] args) throws Throwable {
        AudioFormat format = new AudioFormat(48000, 16, 2, true, false);
        BackgroundSourceDataLineAudioMixer audioMixer = new BackgroundSourceDataLineAudioMixer(AudioSystem.getSourceDataLine(format));

        final SpatialModifier spatialModifier = new SpatialModifier(250);
        audioMixer.playSound(new ModifiableSound(new SineWaveSound(400, 1F), spatialModifier));

        JFrame frame = new JFrame("AudioMixer Test");
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        frame.pack();
        frame.setSize(480, 360);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        spatialModifier.setListenerX(frame.getWidth() / 2F);
        spatialModifier.setListenerZ(frame.getHeight() / 2F);
        spatialModifier.setSoundX(frame.getWidth() / 2F);
        spatialModifier.setSoundZ(frame.getHeight() / 2F);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.RED);
                g.fillOval((int) spatialModifier.getSoundX() - 5, (int) spatialModifier.getSoundZ() - 5, 10, 10);
                g.setColor(Color.BLUE);
                g.fillOval((int) spatialModifier.getListenerX() - 5, (int) spatialModifier.getListenerZ() - 5, 10, 10);
            }
        };

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                spatialModifier.setSoundX(e.getX());
                spatialModifier.setSoundZ(e.getY());
                e.getComponent().repaint();
            }
        });

        frame.add(panel);
        frame.setVisible(true);
    }

}
