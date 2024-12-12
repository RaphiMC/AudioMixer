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
package net.raphimc.audiomixer.soundmodifier.impl;

import net.raphimc.audiomixer.soundmodifier.SoundModifier;

import javax.sound.sampled.AudioFormat;

public class SpatialModifier implements SoundModifier {

    private float maxDistance;
    private float soundX;
    private float soundY;
    private float soundZ;
    private float listenerX;
    private float listenerY;
    private float listenerZ;
    private float listenerYaw;

    private boolean recalculate = true;
    private float panning;
    private float attenuation;

    public SpatialModifier(final float maxDistance) {
        this.setMaxDistance(maxDistance);
    }

    @Override
    public void modify(final AudioFormat audioFormat, final int[] renderedSamples) {
        if (audioFormat.getChannels() != 2) {
            throw new UnsupportedOperationException("Target audio format must have 2 channels");
        }

        if (this.recalculate) {
            this.recalculate = false;
            this.calculatePanningAndAttenuation();
        }

        for (int i = 0; i < renderedSamples.length; i += 2) {
            renderedSamples[i] = (int) (renderedSamples[i] * (1F - this.panning) * this.attenuation);
            renderedSamples[i + 1] = (int) (renderedSamples[i + 1] * this.panning * this.attenuation);
        }
    }

    public float getMaxDistance() {
        return this.maxDistance;
    }

    public SpatialModifier setMaxDistance(final float maxDistance) {
        if (maxDistance <= 0) {
            throw new IllegalArgumentException("Max distance must be greater than zero");
        }

        this.maxDistance = maxDistance;
        this.recalculate = true;
        return this;
    }

    public float getSoundX() {
        return this.soundX;
    }

    public SpatialModifier setSoundX(final float soundX) {
        this.soundX = soundX;
        this.recalculate = true;
        return this;
    }

    public float getSoundY() {
        return this.soundY;
    }

    public SpatialModifier setSoundY(final float soundY) {
        this.soundY = soundY;
        this.recalculate = true;
        return this;
    }

    public float getSoundZ() {
        return this.soundZ;
    }

    public SpatialModifier setSoundZ(final float soundZ) {
        this.soundZ = soundZ;
        this.recalculate = true;
        return this;
    }

    public float getListenerX() {
        return this.listenerX;
    }

    public SpatialModifier setListenerX(final float listenerX) {
        this.listenerX = listenerX;
        this.recalculate = true;
        return this;
    }

    public float getListenerY() {
        return this.listenerY;
    }

    public SpatialModifier setListenerY(final float listenerY) {
        this.listenerY = listenerY;
        this.recalculate = true;
        return this;
    }

    public float getListenerZ() {
        return this.listenerZ;
    }

    public SpatialModifier setListenerZ(final float listenerZ) {
        this.listenerZ = listenerZ;
        this.recalculate = true;
        return this;
    }

    public float getListenerYaw() {
        return this.listenerYaw;
    }

    public SpatialModifier setListenerYaw(final float listenerYaw) {
        this.listenerYaw = listenerYaw;
        this.recalculate = true;
        return this;
    }

    public float getListenerYawDegrees() {
        return (float) Math.toDegrees(this.listenerYaw);
    }

    public SpatialModifier setListenerYawDegrees(final int listenerYaw) {
        return this.setListenerYaw((float) Math.toRadians(listenerYaw % 360));
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
