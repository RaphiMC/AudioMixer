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
import net.raphimc.audiomixer.util.buffer.AudioBuffer;

public class BalanceProcessor implements Processor {

    private float balance;

    public BalanceProcessor(final float balance) {
        this.setBalance(balance);
    }

    @Override
    public void process(final AudioBuffer buffer) {
        if (buffer.format().channels() != 2) {
            throw new IllegalArgumentException("Target audio format must have 2 channels");
        }
        final float leftGain = (1F - this.balance) / 2F;
        final float rightGain = (1F + this.balance) / 2F;
        final float[] samples = buffer.samples();
        for (int i = 0; i < samples.length; i += 2) {
            samples[i] *= leftGain;
            samples[i + 1] *= rightGain;
        }
    }

    public float getBalance() {
        return this.balance;
    }

    public void setBalance(final float balance) {
        if (balance < -1F || balance > 1F) {
            throw new IllegalArgumentException("Balance must be >= -1 and <= 1");
        }
        this.balance = balance;
    }

}
