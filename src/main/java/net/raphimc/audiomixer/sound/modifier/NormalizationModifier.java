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
package net.raphimc.audiomixer.sound.modifier;

import net.raphimc.audiomixer.sound.SoundModifier;
import net.raphimc.audiomixer.util.SoundSampleUtil;

import javax.sound.sampled.AudioFormat;

public class NormalizationModifier implements SoundModifier {

    private final int decayPeriodMillis;
    private int runningMaxSampleValue = 1;

    public NormalizationModifier() {
        this(4000);
    }

    public NormalizationModifier(final int decayPeriodMillis) {
        this.decayPeriodMillis = decayPeriodMillis;
    }

    @Override
    public void modify(final AudioFormat audioFormat, final int[] renderedSamples) {
        final int timeElapsedMillis = ((renderedSamples.length / audioFormat.getChannels()) * 1000) / (int) audioFormat.getSampleRate();
        final double decayFactor = Math.exp(-((double) timeElapsedMillis / this.decayPeriodMillis));
        final int newRunningMaxSampleValue = (int) (this.runningMaxSampleValue * decayFactor);
        this.runningMaxSampleValue = Math.max(1, Math.max(newRunningMaxSampleValue, SoundSampleUtil.getMax(renderedSamples)));
        SoundSampleUtil.normalize(renderedSamples, (int) Math.pow(2, audioFormat.getSampleSizeInBits() - 1) - 1, this.runningMaxSampleValue);
    }

    public void reset() {
        this.runningMaxSampleValue = 1;
    }

}
