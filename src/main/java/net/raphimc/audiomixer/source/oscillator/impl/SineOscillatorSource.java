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
package net.raphimc.audiomixer.source.oscillator.impl;

import net.raphimc.audiomixer.source.oscillator.OscillatorSource;
import net.raphimc.audiomixer.util.MathUtil;
import net.raphimc.audiomixer.util.buffer.AudioBuffer;

public class SineOscillatorSource extends OscillatorSource {

    private float phase;

    public SineOscillatorSource(final float frequency) {
        super(frequency);
    }

    @Override
    protected void renderDry(final AudioBuffer buffer) {
        final int channels = buffer.format().channels();
        final float phaseIncrement = (this.getFrequency() / buffer.format().sampleRate()) * MathUtil.TWO_PI;
        final float[] samples = buffer.samples();
        for (int i = 0; i < samples.length; i += channels) {
            final float sample = (float) Math.sin(this.phase);
            for (int channel = 0; channel < channels; channel++) {
                samples[i + channel] = sample;
            }
            this.phase += phaseIncrement;
            this.phase %= MathUtil.TWO_PI;
        }
    }

}
