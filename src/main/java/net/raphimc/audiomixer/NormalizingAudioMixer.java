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
package net.raphimc.audiomixer;

import net.raphimc.audiomixer.util.SoundSampleUtil;

import javax.sound.sampled.AudioFormat;

public class NormalizingAudioMixer extends AudioMixer {

    private final int decayPeriodMillis;
    private int runningMaxSampleValue = 1;
    private float masterVolume = 1F;

    public NormalizingAudioMixer(final AudioFormat audioFormat) {
        this(audioFormat, 4000);
    }

    public NormalizingAudioMixer(final AudioFormat audioFormat, final int decayPeriodMillis) {
        super(audioFormat);
        this.decayPeriodMillis = decayPeriodMillis;
    }

    public NormalizingAudioMixer(final AudioFormat audioFormat, final int maxSounds, final int decayPeriodMillis) {
        super(audioFormat, maxSounds);
        this.decayPeriodMillis = decayPeriodMillis;
    }

    @Override
    public void stopAllSounds() {
        super.stopAllSounds();
        this.runningMaxSampleValue = 1;
    }

    @Override
    public int[] mix(final int sampleCount) {
        final int[] samples = super.mix(sampleCount);

        final int timeElapsedMillis = ((samples.length / this.getAudioFormat().getChannels()) * 1000) / (int) this.getAudioFormat().getSampleRate();
        final double decayFactor = Math.exp(-((double) timeElapsedMillis / this.decayPeriodMillis));
        final int newRunningMaxSampleValue = (int) (this.runningMaxSampleValue * decayFactor);
        this.runningMaxSampleValue = Math.max(1, Math.max(newRunningMaxSampleValue, SoundSampleUtil.getMax(samples)));
        SoundSampleUtil.normalize(samples, (int) Math.pow(2, this.getAudioFormat().getSampleSizeInBits() - 1) - 1, this.runningMaxSampleValue);

        if (this.masterVolume != 1F) {
            for (int i = 0; i < samples.length; i++) {
                samples[i] = (int) (samples[i] * this.masterVolume);
            }
        }
        return samples;
    }

    public void setMasterVolume(final int masterVolume) {
        if (masterVolume < 0 || masterVolume > 100) {
            throw new IllegalArgumentException("Master volume must be between 0 and 100");
        }

        this.setMasterVolume(masterVolume / 100F);
    }

    public void setMasterVolume(final float masterVolume) {
        if (masterVolume < 0F || masterVolume > 1F) {
            throw new IllegalArgumentException("Master volume must be between 0 and 1");
        }

        this.masterVolume = masterVolume;
    }

    public float getMasterVolume() {
        return this.masterVolume;
    }

}
