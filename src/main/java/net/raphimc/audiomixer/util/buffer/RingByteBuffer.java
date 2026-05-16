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
package net.raphimc.audiomixer.util.buffer;

public class RingByteBuffer {

    private final byte[] buffer;
    private int head;
    private int tail;
    private int size;

    public RingByteBuffer(final int capacity) {
        this.buffer = new byte[capacity];
    }

    public byte read() {
        this.checkNotEmpty();
        final byte value = this.buffer[this.head];
        this.head = (this.head + 1) % this.buffer.length;
        this.size--;
        return value;
    }

    public int read(final byte[] dst, final int offset, final int length) {
        this.checkNotEmpty();
        final int readLength = Math.min(length, this.size);
        final int firstPartLength = Math.min(readLength, this.buffer.length - this.head);
        System.arraycopy(this.buffer, this.head, dst, offset, firstPartLength);
        if (firstPartLength < readLength) {
            final int secondPartLength = readLength - firstPartLength;
            System.arraycopy(this.buffer, 0, dst, offset + firstPartLength, secondPartLength);
        }
        this.head = (this.head + readLength) % this.buffer.length;
        this.size -= readLength;
        return readLength;
    }

    public void write(final byte value) {
        this.checkHasEnoughSpace(1);
        this.buffer[this.tail] = value;
        this.tail = (this.tail + 1) % this.buffer.length;
        this.size++;
    }

    public void write(final byte[] src, final int offset, final int length) {
        this.checkHasEnoughSpace(length);
        final int firstPartLength = Math.min(length, this.buffer.length - this.tail);
        System.arraycopy(src, offset, this.buffer, this.tail, firstPartLength);
        if (firstPartLength < length) {
            final int secondPartLength = length - firstPartLength;
            System.arraycopy(src, offset + firstPartLength, this.buffer, 0, secondPartLength);
        }
        this.tail = (this.tail + length) % this.buffer.length;
        this.size += length;
    }

    public void clear() {
        this.head = 0;
        this.tail = 0;
        this.size = 0;
    }

    public boolean isEmpty() {
        return this.size == 0;
    }

    public boolean isFull() {
        return this.size == this.buffer.length;
    }

    public int remaining() {
        return this.buffer.length - this.size;
    }

    public int size() {
        return this.size;
    }

    private void checkNotEmpty() {
        if (this.isEmpty()) {
            throw new IllegalStateException("Buffer is empty");
        }
    }

    private void checkHasEnoughSpace(final int length) {
        if (this.size + length > this.buffer.length) {
            throw new IllegalStateException("Not enough space in buffer");
        }
    }

}
