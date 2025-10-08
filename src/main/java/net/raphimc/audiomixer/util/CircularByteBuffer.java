/*
 * This file is part of AudioMixer - https://github.com/RaphiMC/AudioMixer
 * Copyright (C) 2024-2025 RK_01/RaphiMC and contributors
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
package net.raphimc.audiomixer.util;

import java.util.Arrays;

public class CircularByteBuffer {

    private final byte[] buffer;
    private int head;
    private int tail;
    private int size;

    public CircularByteBuffer(final int capacity) {
        this.buffer = new byte[capacity];
    }

    public synchronized void write(final byte value) {
        this.ensureHasEnoughSpace(1);
        this.buffer[this.tail] = value;
        this.tail = (this.tail + 1) % this.buffer.length;
        this.size++;
    }

    public void writeAll(final byte[] values) {
        this.writeAll(values, values.length);
    }

    public synchronized void writeAll(final byte[] values, final int valuesLength) {
        this.ensureHasEnoughSpace(valuesLength);

        final int firstPartLength = Math.min(valuesLength, this.buffer.length - this.tail);
        System.arraycopy(values, 0, this.buffer, this.tail, firstPartLength);
        if (firstPartLength < valuesLength) {
            final int secondPartLength = valuesLength - firstPartLength;
            System.arraycopy(values, firstPartLength, this.buffer, 0, secondPartLength);
        }

        this.tail = (this.tail + valuesLength) % this.buffer.length;
        this.size += valuesLength;
    }

    public byte read() {
        if (this.isEmpty()) {
            throw new IllegalStateException("Buffer is empty");
        }
        return this.readSafe();
    }

    public byte readSafe() {
        return this.readSafe((byte) 0);
    }

    public synchronized byte readSafe(final byte defaultValue) {
        if (this.isEmpty()) {
            return defaultValue;
        }
        final byte value = this.buffer[this.head];
        this.head = (head + 1) % this.buffer.length;
        this.size--;
        return value;
    }

    public byte[] readAllSafe(final int size) {
        return this.readAllSafe(size, (byte) 0);
    }

    public byte[] readAllSafe(final int size, final byte defaultValue) {
        final byte[] values = new byte[size];
        this.readAllSafe(values, defaultValue);
        return values;
    }

    public void readAllSafe(final byte[] values) {
        this.readAllSafe(values, (byte) 0);
    }

    public void readAllSafe(final byte[] values, final byte defaultValue) {
        this.readAllSafe(values, values.length, defaultValue);
    }

    public void readAllSafe(final byte[] values, final int valuesLength) {
        this.readAllSafe(values, valuesLength, (byte) 0);
    }

    public synchronized void readAllSafe(final byte[] values, final int valuesLength, final byte defaultValue) {
        final int elementsToRemove = Math.min(valuesLength, this.size);
        final int firstPartLength = Math.min(elementsToRemove, this.buffer.length - this.head);
        System.arraycopy(this.buffer, this.head, values, 0, firstPartLength);
        if (firstPartLength < elementsToRemove) {
            final int secondPartLength = elementsToRemove - firstPartLength;
            System.arraycopy(this.buffer, 0, values, firstPartLength, secondPartLength);
        }
        Arrays.fill(values, elementsToRemove, valuesLength, defaultValue);

        this.head = (this.head + elementsToRemove) % this.buffer.length;
        this.size -= elementsToRemove;
    }

    public synchronized void clear() {
        this.head = 0;
        this.tail = 0;
        this.size = 0;
    }

    public synchronized boolean hasSpaceFor(final int length) {
        return this.size + length <= this.buffer.length;
    }

    public synchronized boolean isEmpty() {
        return this.size == 0;
    }

    public synchronized boolean isFull() {
        return this.size == this.buffer.length;
    }

    public synchronized int getSize() {
        return this.size;
    }

    public int getCapacity() {
        return this.buffer.length;
    }

    private void ensureHasEnoughSpace(final int length) {
        if (!this.hasSpaceFor(length)) {
            throw new IllegalStateException("Not enough space in the buffer");
        }
    }

}
