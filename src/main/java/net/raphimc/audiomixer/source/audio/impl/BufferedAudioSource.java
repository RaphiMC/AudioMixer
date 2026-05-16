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
package net.raphimc.audiomixer.source.audio.impl;

import net.raphimc.audiomixer.dsp.resampler.Resampler;
import net.raphimc.audiomixer.source.audio.AudioSource;
import net.raphimc.audiomixer.util.buffer.AudioBuffer;

public class BufferedAudioSource extends AudioSource {

    public BufferedAudioSource(final AudioBuffer audioBuffer) {
        super(audioBuffer);
    }

    public BufferedAudioSource(final AudioBuffer audioBuffer, final Resampler resampler) {
        super(audioBuffer, resampler);
    }

    @Override
    public boolean isFinished() {
        return this.position >= this.getFrameCount();
    }

    public int getFrameCount() {
        return this.buffer.getFrameCount();
    }

    public double getPosition() {
        return this.position;
    }

    public void setPosition(final double position) {
        if (position < 0 || position > this.getFrameCount()) {
            throw new IllegalArgumentException("Position must be >= 0 and <= " + this.getFrameCount());
        }
        this.position = position;
    }

    public float getProgress() {
        return (float) (this.getPosition() / this.getFrameCount());
    }

    public void setProgress(final float progress) {
        this.setPosition((int) ((double) progress * this.getFrameCount()));
    }

}
