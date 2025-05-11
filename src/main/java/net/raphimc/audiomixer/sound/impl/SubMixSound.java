/*
 * This file is part of AudioMixer - https://github.com/RaphiMC/AudioMixer
 * Copyright (C) 2024-2025 RK_01/RaphiMC and contributors
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

import net.raphimc.audiomixer.sound.Sound;
import net.raphimc.audiomixer.util.PcmFloatAudioFormat;

import java.util.ArrayList;
import java.util.List;

public class SubMixSound extends Sound {

    protected final List<Sound> sounds = new ArrayList<>();
    private int maxSounds;
    private int mixedSounds;
    private long mixRenderTime;

    public SubMixSound() {
        this(512);
    }

    public SubMixSound(final int maxSounds) {
        this.setMaxSounds(maxSounds);
    }

    @Override
    public synchronized void render(final PcmFloatAudioFormat audioFormat, final float[] finalMixBuffer) {
        final long start = System.nanoTime();
        this.mixedSounds = this.sounds.size();

        final float[] renderedSamples = new float[finalMixBuffer.length];
        for (Sound sound : this.sounds) {
            sound.render(audioFormat, renderedSamples);
            for (int i = 0; i < finalMixBuffer.length; i++) {
                finalMixBuffer[i] += renderedSamples[i];
            }
        }
        this.soundModifiers.modify(audioFormat, finalMixBuffer);

        this.sounds.removeIf(Sound::isFinished);
        this.mixRenderTime = System.nanoTime() - start;
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    public synchronized void playSound(final Sound sound) {
        if (this.sounds.size() >= this.maxSounds) {
            this.sounds.remove(0);
        }
        this.sounds.add(sound);
    }

    public synchronized void stopSound(final Sound sound) {
        this.sounds.remove(sound);
    }

    public synchronized void stopAllSounds() {
        this.sounds.clear();
    }

    public int getMaxSounds() {
        return this.maxSounds;
    }

    public SubMixSound setMaxSounds(final int maxSounds) {
        if (maxSounds < 1) {
            throw new IllegalArgumentException("Max sounds must be at least 1");
        }

        this.maxSounds = maxSounds;
        return this;
    }

    public int getMixedSounds() {
        return this.mixedSounds;
    }

    public long getMixRenderTime() {
        return this.mixRenderTime;
    }

    public synchronized int getActiveSounds() {
        return this.sounds.size();
    }

}
