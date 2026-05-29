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

import net.raphimc.audiomixer.dsp.parameter.FloatParameter;
import net.raphimc.audiomixer.dsp.parameter.GenericParameter;
import net.raphimc.audiomixer.dsp.processor.Processor;
import net.raphimc.audiomixer.util.buffer.AudioBuffer;
import net.raphimc.audiomixer.util.math.Vector3f;

public class PositionalAudioProcessor extends Processor {

    private final GainPanProcessor internalProcessor = new GainPanProcessor();
    private final FloatParameter maxDistance = FloatParameter.of(0F).withConstraint(FloatParameter.Constraint.GREATER_THAN_ZERO).withChangeListener(this::applyParameters);
    private final GenericParameter<Vector3f> sourcePosition = GenericParameter.of(Vector3f.ZERO).withChangeListener(this::applyParameters);
    private final GenericParameter<Vector3f> listenerPosition = GenericParameter.of(Vector3f.ZERO).withChangeListener(this::applyParameters);
    private final FloatParameter listenerYaw = FloatParameter.of(0F).withConstraint(FloatParameter.Constraint.DEGREES).withChangeListener(this::applyParameters);

    public PositionalAudioProcessor(final float maxDistance) {
        this.maxDistance.set(maxDistance);
    }

    @Override
    protected void processInternal(final AudioBuffer buffer) {
        this.internalProcessor.process(buffer);
    }

    public FloatParameter maxDistance() {
        return this.maxDistance;
    }

    public GenericParameter<Vector3f> sourcePosition() {
        return this.sourcePosition;
    }

    public GenericParameter<Vector3f> listenerPosition() {
        return this.listenerPosition;
    }

    public FloatParameter listenerYaw() {
        return this.listenerYaw;
    }

    private void applyParameters() {
        final float maxDistance = this.maxDistance.get();
        final Vector3f worldDelta = this.sourcePosition.get().subtract(this.listenerPosition.get());
        final float yaw = (float) Math.toRadians(this.listenerYaw.get());
        final float worldDistance = worldDelta.length();
        if (worldDistance == 0F) {
            this.internalProcessor.gain().set(1F);
            this.internalProcessor.pan().set(0F);
        } else if (worldDistance < maxDistance) {
            final float gain = 1F - (worldDistance / maxDistance);
            final float sin = (float) Math.sin(yaw);
            final float cos = (float) Math.cos(yaw);
            final float localX = worldDelta.x() * cos - worldDelta.z() * sin;
            final float localZ = worldDelta.x() * sin + worldDelta.z() * cos;
            final float azimuth = (float) Math.atan2(localX, localZ);
            this.internalProcessor.gain().set(gain);
            this.internalProcessor.pan().set((float) Math.sin(azimuth));
        } else {
            this.internalProcessor.gain().set(0F);
            this.internalProcessor.pan().set(0F);
        }
    }

}
