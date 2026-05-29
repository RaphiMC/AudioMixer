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
package net.raphimc.audiomixer.dsp.processor.effect;

import net.raphimc.audiomixer.dsp.parameter.IntParameter;
import net.raphimc.audiomixer.dsp.processor.Processor;
import net.raphimc.audiomixer.util.buffer.AudioBuffer;

public class BitDepthReductionProcessor implements Processor {

    private final IntParameter bitDepth = IntParameter.of(8).withConstraint(value -> value > 0 && value <= 16);

    public BitDepthReductionProcessor() {
    }

    public BitDepthReductionProcessor(final int bitDepth) {
        this.bitDepth.set(bitDepth);
    }

    @Override
    public void process(final AudioBuffer buffer) {
        final float step = 2F / (1 << this.bitDepth.get());
        final float[] samples = buffer.samples();
        for (int sampleIndex = 0; sampleIndex < samples.length; sampleIndex++) {
            samples[sampleIndex] = Math.round(samples[sampleIndex] / step) * step;
        }
    }

    public IntParameter bitDepth() {
        return this.bitDepth;
    }

}
