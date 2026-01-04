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
package net.raphimc.audiomixer.soundmodifier.impl;

import net.raphimc.audiomixer.soundmodifier.SoundModifier;
import net.raphimc.audiomixer.util.MathUtil;
import net.raphimc.audiomixer.util.PcmFloatAudioFormat;
import net.raphimc.audiomixer.util.SoundSampleUtil;

public class NormalizationModifier implements SoundModifier {

    private int decayPeriodMillis;
    private float runningMaxSampleValue = 0F;

    public NormalizationModifier() {
        this(3000);
    }

    public NormalizationModifier(final int decayPeriodMillis) {
        this.setDecayPeriodMillis(decayPeriodMillis);
    }

    @Override
    public void modify(final PcmFloatAudioFormat audioFormat, final float[] renderedSamples) {
        final float timeElapsedMillis = MathUtil.sampleCountToMillis(audioFormat, renderedSamples.length);
        final float decayFactor = (float) Math.exp(-timeElapsedMillis / this.decayPeriodMillis);
        this.runningMaxSampleValue *= decayFactor;
        final float max = SoundSampleUtil.getMax(renderedSamples);
        if (max > this.runningMaxSampleValue) {
            this.runningMaxSampleValue = max;
        }
        SoundSampleUtil.normalize(renderedSamples, this.runningMaxSampleValue);
    }

    public void reset() {
        this.runningMaxSampleValue = 0F;
    }

    public int getDecayPeriodMillis() {
        return decayPeriodMillis;
    }

    public NormalizationModifier setDecayPeriodMillis(final int decayPeriodMillis) {
        if (decayPeriodMillis <= 0) {
            throw new IllegalArgumentException("Decay period must be greater than 0");
        }
        this.decayPeriodMillis = decayPeriodMillis;
        return this;
    }

}
