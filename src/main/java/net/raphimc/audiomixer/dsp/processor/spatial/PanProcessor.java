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
package net.raphimc.audiomixer.dsp.processor.spatial;

import net.raphimc.audiomixer.dsp.processor.Processor;
import net.raphimc.audiomixer.util.MathUtil;
import net.raphimc.audiomixer.util.buffer.AudioBuffer;

public class PanProcessor implements Processor {

    private float pan;

    public PanProcessor(final float pan) {
        this.setPan(pan);
    }

    @Override
    public void process(final AudioBuffer buffer) {
        if (buffer.format().channels() != 2) {
            throw new IllegalArgumentException("Target audio format must have 2 channels");
        }
        final float normalizedPan = (this.pan + 1F) / 2F;
        final float leftGain = (float) Math.cos(normalizedPan * MathUtil.HALF_PI);
        final float rightGain = (float) Math.sin(normalizedPan * MathUtil.HALF_PI);
        final float[] samples = buffer.samples();
        for (int i = 0; i < samples.length; i += 2) {
            samples[i] *= leftGain;
            samples[i + 1] *= rightGain;
        }
    }

    public float getPan() {
        return this.pan;
    }

    public void setPan(final float pan) {
        if (pan < -1F || pan > 1F) {
            throw new IllegalArgumentException("Pan must be >= -1 and <= 1");
        }
        this.pan = pan;
    }

}
