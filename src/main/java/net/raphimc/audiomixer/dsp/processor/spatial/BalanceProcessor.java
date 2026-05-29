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
import net.raphimc.audiomixer.dsp.processor.Processor;
import net.raphimc.audiomixer.dsp.processor.dynamics.StereoGainProcessor;
import net.raphimc.audiomixer.util.buffer.AudioBuffer;

public class BalanceProcessor extends Processor {

    private final StereoGainProcessor internalProcessor = new StereoGainProcessor();
    private final FloatParameter balance = FloatParameter.of(0F).withConstraint(FloatParameter.Constraint.SIGNED_NORMALIZED).withChangeListener(this::applyBalance);

    public BalanceProcessor() {
        this.applyBalance();
    }

    public BalanceProcessor(final float balance) {
        this.applyBalance();
        this.balance.set(balance);
    }

    @Override
    protected void processInternal(final AudioBuffer buffer) {
        this.internalProcessor.process(buffer);
    }

    public FloatParameter balance() {
        return this.balance;
    }

    private void applyBalance() {
        final float balance = this.balance.get();
        this.internalProcessor.leftGain().set((1F - balance) / 2F);
        this.internalProcessor.rightGain().set((1F + balance) / 2F);
    }

}
