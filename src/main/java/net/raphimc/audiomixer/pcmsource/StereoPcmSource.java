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
package net.raphimc.audiomixer.pcmsource;

public interface StereoPcmSource extends PcmSource {

    float[] consumeSample(final float increment);

    default int consumeSamples(final float[] buffer) {
        return this.consumeSamples(buffer, 0, buffer.length);
    }

    default int consumeSamples(final float[] buffer, final int offset, final int length) {
        int i;
        for (i = 0; i < length && !this.hasReachedEnd(); i += 2) {
            final int index = offset * 2 + i;
            final float[] sample = this.consumeSample(1);
            buffer[index] = sample[0];
            buffer[index + 1] = sample[1];
        }
        return i;
    }

}
