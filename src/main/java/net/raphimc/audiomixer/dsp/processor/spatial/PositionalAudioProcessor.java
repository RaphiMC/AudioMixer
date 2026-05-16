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

public class PositionalAudioProcessor implements Processor {

    private final GainPanProcessor processor = new GainPanProcessor(1F, 0F);

    private float maxDistance;
    private float sourceX;
    private float sourceY;
    private float sourceZ;
    private float listenerX;
    private float listenerY;
    private float listenerZ;
    private float listenerYaw;

    private boolean recalculate = true;

    public PositionalAudioProcessor(final float maxDistance) {
        this.setMaxDistance(maxDistance);
    }

    @Override
    public void process(final AudioBuffer buffer) {
        if (this.recalculate) {
            this.recalculate = false;
            this.calculatePanAndAttenuation();
        }
        this.processor.process(buffer);
    }

    public float getMaxDistance() {
        return this.maxDistance;
    }

    public void setMaxDistance(final float maxDistance) {
        if (maxDistance <= 0) {
            throw new IllegalArgumentException("Max distance must be > 0");
        }
        this.maxDistance = maxDistance;
        this.recalculate = true;
    }

    public float getSourceX() {
        return this.sourceX;
    }

    public void setSourceX(final float sourceX) {
        this.sourceX = sourceX;
        this.recalculate = true;
    }

    public float getSourceY() {
        return this.sourceY;
    }

    public void setSourceY(final float sourceY) {
        this.sourceY = sourceY;
        this.recalculate = true;
    }

    public float getSourceZ() {
        return this.sourceZ;
    }

    public void setSourceZ(final float sourceZ) {
        this.sourceZ = sourceZ;
        this.recalculate = true;
    }

    public void setSourcePosition(final float x, final float y, final float z) {
        this.setSourceX(x);
        this.setSourceY(y);
        this.setSourceZ(z);
    }

    public float getListenerX() {
        return this.listenerX;
    }

    public void setListenerX(final float listenerX) {
        this.listenerX = listenerX;
        this.recalculate = true;
    }

    public float getListenerY() {
        return this.listenerY;
    }

    public void setListenerY(final float listenerY) {
        this.listenerY = listenerY;
        this.recalculate = true;
    }

    public float getListenerZ() {
        return this.listenerZ;
    }

    public void setListenerZ(final float listenerZ) {
        this.listenerZ = listenerZ;
        this.recalculate = true;
    }

    public void setListenerPosition(final float x, final float y, final float z) {
        this.setListenerX(x);
        this.setListenerY(y);
        this.setListenerZ(z);
    }

    public float getListenerYaw() {
        return this.listenerYaw;
    }

    public void setListenerYaw(final float listenerYaw) {
        this.listenerYaw = listenerYaw;
        this.recalculate = true;
    }

    public float getListenerYawDegrees() {
        return (float) Math.toDegrees(this.listenerYaw);
    }

    public void setListenerYawDegrees(final int listenerYaw) {
        this.setListenerYaw((float) Math.toRadians(listenerYaw % 360));
    }

    private void calculatePanAndAttenuation() {
        final float deltaX = this.sourceX - this.listenerX;
        final float deltaY = this.sourceY - this.listenerY;
        final float deltaZ = this.sourceZ - this.listenerZ;
        final float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        if (distance < this.maxDistance) {
            this.processor.setGain(1F - (distance / this.maxDistance));
            final float angleToSource = (float) Math.atan2(deltaX, -deltaZ);
            final float relativeAngle = (angleToSource - this.listenerYaw + MathUtil.PI) % MathUtil.TWO_PI - MathUtil.PI;
            this.processor.setPan(((float) Math.sin(relativeAngle) + 1) / 2F);
        } else {
            this.processor.setGain(0F);
            this.processor.setPan(0F);
        }
    }

}
