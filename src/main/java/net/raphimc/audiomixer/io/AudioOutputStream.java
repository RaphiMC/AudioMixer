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
package net.raphimc.audiomixer.io;

import net.raphimc.audiomixer.util.AudioFormat;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

public abstract class AudioOutputStream implements Flushable, Closeable {

    private final AudioFormat format;

    public AudioOutputStream(final AudioFormat format) {
        this.format = format;
    }

    public abstract void write(final float sample) throws IOException;

    public void write(final float[] samples) throws IOException {
        this.write(samples, 0, samples.length);
    }

    public void write(final float[] samples, final int offset, final int length) throws IOException {
        for (int i = 0; i < length; i++) {
            this.write(samples[offset + i]);
        }
    }

    public AudioFormat getFormat() {
        return this.format;
    }

}
