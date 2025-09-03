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
package net.raphimc.audiomixer.sound.impl.mix;

import net.raphimc.audiomixer.sound.Sound;

import java.util.function.Consumer;

public class ChannelMixSound extends MixSound {

    private final MixSound[] channels;

    private int currentChannel;

    public ChannelMixSound(final int channels) {
        this(channels, 1);
    }

    public ChannelMixSound(final int channels, final int maxSoundsPerChannel) {
        super(channels);
        if (channels < 1) {
            throw new IllegalArgumentException("Channels must be at least 1");
        }

        this.channels = new MixSound[channels];
        for (int i = 0; i < this.channels.length; i++) {
            this.channels[i] = new MixSound();
            super.playSound(this.channels[i]);
        }
        this.setMaxSounds(channels * maxSoundsPerChannel);
    }

    @Override
    public synchronized void playSound(final Sound sound) {
        this.channels[this.currentChannel].playSound(sound);
        this.currentChannel = (this.currentChannel + 1) % this.channels.length;
    }

    @Override
    public void stopSound(final Sound sound) {
        for (MixSound mixSound : this.channels) {
            mixSound.stopSound(sound);
        }
    }

    @Override
    public void stopAllSounds() {
        for (MixSound mixSound : this.channels) {
            mixSound.stopAllSounds();
        }
    }

    @Override
    public void forEachSound(final Consumer<Sound> action) {
        for (MixSound mixSound : this.channels) {
            mixSound.forEachSound(action);
        }
    }

    public MixSound getChannel(final int channel) {
        if (channel < 0 || channel >= this.channels.length) {
            throw new IllegalArgumentException("Channel must be between 0 and " + (this.channels.length - 1));
        }
        return this.channels[channel];
    }

    @Override
    public int getMaxSounds() {
        int maxSounds = 0;
        for (MixSound mixSound : this.channels) {
            maxSounds += mixSound.getMaxSounds();
        }
        return maxSounds;
    }

    @Override
    public ChannelMixSound setMaxSounds(final int maxSounds) {
        if (this.channels != null) {
            for (MixSound mixSound : this.channels) {
                mixSound.setMaxSounds((int) Math.ceil((double) maxSounds / this.channels.length));
            }
        } else { // Called from super constructor
            super.setMaxSounds(maxSounds);
        }
        return this;
    }

    @Override
    public int getMixedSounds() {
        int mixedSounds = 0;
        for (MixSound mixSound : this.channels) {
            mixedSounds += mixSound.getMixedSounds();
        }
        return mixedSounds;
    }

    @Override
    public int getActiveSounds() {
        int activeSounds = 0;
        for (MixSound mixSound : this.channels) {
            activeSounds += mixSound.getActiveSounds();
        }
        return activeSounds;
    }

    public int getChannelCount() {
        return this.channels.length;
    }

}
