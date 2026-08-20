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
package net.raphimc.audiomixer.util.io.seekable;

import java.io.IOException;
import java.util.Arrays;

public class SeekableByteArrayOutputStream extends SeekableOutputStream {

    private byte[] array;
    private int length;
    private int position;

    public SeekableByteArrayOutputStream() {
        this(32);
    }

    public SeekableByteArrayOutputStream(final int initialCapacity) {
        this.array = new byte[initialCapacity];
    }

    @Override
    public void write(final int b) throws IOException {
        this.ensureHasEnoughSpace(1);
        this.array[this.position] = (byte) b;
        this.position = Math.addExact(this.position, 1);
        this.length = Math.max(this.length, this.position);
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        this.ensureHasEnoughSpace(len);
        System.arraycopy(b, off, this.array, this.position, len);
        this.position = Math.addExact(this.position, len);
        this.length = Math.max(this.length, this.position);
    }

    @Override
    public void seek(final long position) {
        this.position = Math.toIntExact(position);
    }

    @Override
    public long position() {
        return this.position;
    }

    public byte[] toByteArray() {
        return Arrays.copyOf(this.array, this.length);
    }

    private void ensureHasEnoughSpace(final int length) {
        final int requiredLength = Math.addExact(this.position, length);
        if (requiredLength > this.array.length) {
            this.array = Arrays.copyOf(this.array, requiredLength + Math.max(length, requiredLength));
        }
    }

}
