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

import net.raphimc.audiomixer.sound.Sound;
import net.raphimc.audiomixer.util.AudioFormats;

import javax.sound.sampled.AudioFormat;

public class SpatialSound implements Sound {

    private final Sound sound;
    private float maxDistance;
    private float soundX;
    private float soundY;
    private float soundZ;
    private float listenerX;
    private float listenerY;
    private float listenerZ;
    private float listenerYaw;

    private float panning;
    private float attenuation;

    public SpatialSound(final Sound sound, final float maxDistance) {
        if (maxDistance <= 0) {
            throw new IllegalArgumentException("Max distance must be greater than zero");
        }

        this.sound = sound;
        this.setMaxDistance(maxDistance);
    }

    @Override
    public void render(final AudioFormat audioFormat, final int[] renderedSamples, final int renderedSamplesLength) {
        if (audioFormat.getChannels() != 2) {
            throw new UnsupportedOperationException("Target audio format must have 2 channels");
        }
        this.sound.render(AudioFormats.withChannels(audioFormat, 1), renderedSamples, renderedSamplesLength / 2);
        this.calculatePanningAndAttenuation();

        for (int i = renderedSamplesLength / 2 - 1; i >= 0; i--) {
            final int sample = renderedSamples[i];
            renderedSamples[i * 2] = (int) (sample * (1F - this.panning) * this.attenuation);
            renderedSamples[i * 2 + 1] = (int) (sample * this.panning * this.attenuation);
        }
    }

    @Override
    public boolean isFinished() {
        return this.sound.isFinished();
    }

    public Sound getSound() {
        return this.sound;
    }

    public float getMaxDistance() {
        return this.maxDistance;
    }

    public void setMaxDistance(final float maxDistance) {
        if (maxDistance <= 0) {
            throw new IllegalArgumentException("Max distance must be greater than zero");
        }

        this.maxDistance = maxDistance;
    }

    public float getSoundX() {
        return this.soundX;
    }

    public void setSoundX(final float soundX) {
        this.soundX = soundX;
    }

    public float getSoundY() {
        return this.soundY;
    }

    public void setSoundY(final float soundY) {
        this.soundY = soundY;
    }

    public float getSoundZ() {
        return this.soundZ;
    }

    public void setSoundZ(final float soundZ) {
        this.soundZ = soundZ;
    }

    public float getListenerX() {
        return this.listenerX;
    }

    public void setListenerX(final float listenerX) {
        this.listenerX = listenerX;
    }

    public float getListenerY() {
        return this.listenerY;
    }

    public void setListenerY(final float listenerY) {
        this.listenerY = listenerY;
    }

    public float getListenerZ() {
        return this.listenerZ;
    }

    public void setListenerZ(final float listenerZ) {
        this.listenerZ = listenerZ;
    }

    public float getListenerYaw() {
        return this.listenerYaw;
    }

    public void setListenerYaw(final float listenerYaw) {
        this.listenerYaw = listenerYaw;
    }

    public float getListenerYawDegrees() {
        return (float) Math.toDegrees(this.listenerYaw);
    }

    public void setListenerYawDegrees(final int listenerYaw) {
        this.setListenerYaw((float) Math.toRadians(listenerYaw % 360));
    }

    private void calculatePanningAndAttenuation() {
        final float deltaX = this.soundX - this.listenerX;
        final float deltaY = this.soundY - this.listenerY;
        final float deltaZ = this.soundZ - this.listenerZ;
        final float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        if (distance >= this.maxDistance) {
            this.panning = 0.5F;
            this.attenuation = 0F;
            return;
        }
        this.attenuation = 1F - (distance / this.maxDistance);

        final float angleToSound = (float) Math.atan2(deltaX, -deltaZ);
        final float relativeAngle = (float) ((angleToSound - this.listenerYaw + Math.PI) % (2 * Math.PI) - Math.PI);
        this.panning = ((float) Math.sin(relativeAngle) + 1) / 2F;
    }

}
