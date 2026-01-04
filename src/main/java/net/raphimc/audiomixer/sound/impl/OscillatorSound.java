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
package net.raphimc.audiomixer.sound.impl;

import net.raphimc.audiomixer.oscillator.Oscillator;
import net.raphimc.audiomixer.sound.Sound;
import net.raphimc.audiomixer.util.PcmFloatAudioFormat;

public class OscillatorSound extends Sound {

    private final Oscillator oscillator;

    public OscillatorSound(final Oscillator oscillator) {
        this.oscillator = oscillator;
    }

    @Override
    public void render(final PcmFloatAudioFormat audioFormat, final float[] renderedSamples) {
        final int numChannels = audioFormat.getChannels();
        final float sampleRate = audioFormat.getSampleRate();

        for (int i = 0; i < renderedSamples.length; i += numChannels) {
            final float sample = this.oscillator.getNextValue(sampleRate);
            for (int channel = 0; channel < numChannels; channel++) {
                renderedSamples[i + channel] = sample;
            }
        }

        this.getSoundModifiers().modify(audioFormat, renderedSamples);
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    public Oscillator getOscillator() {
        return this.oscillator;
    }

}
