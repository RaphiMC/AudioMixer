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
import net.raphimc.audiomixer.sound.modifier.SpatialSound;
import net.raphimc.audiomixer.sound.source.SineWaveSound;

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

        SpatialSound spatialSound = new SpatialSound(new SineWaveSound(400, 1F), 250);
        audioMixer.playSound(spatialSound);

        JFrame frame = new JFrame("AudioMixer Test");
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        frame.pack();
        frame.setSize(480, 360);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        spatialSound.setListenerX(frame.getWidth() / 2F);
        spatialSound.setListenerZ(frame.getHeight() / 2F);
        spatialSound.setSoundX(frame.getWidth() / 2F);
        spatialSound.setSoundZ(frame.getHeight() / 2F);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.RED);
                g.fillOval((int) spatialSound.getSoundX() - 5, (int) spatialSound.getSoundZ() - 5, 10, 10);
                g.setColor(Color.BLUE);
                g.fillOval((int) spatialSound.getListenerX() - 5, (int) spatialSound.getListenerZ() - 5, 10, 10);
            }
        };

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                spatialSound.setSoundX(e.getX());
                spatialSound.setSoundZ(e.getY());
                e.getComponent().repaint();
            }
        });

        frame.add(panel);
        frame.setVisible(true);
    }

}
