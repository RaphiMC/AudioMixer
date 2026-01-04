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
package net.raphimc.audiomixer.util;

import java.util.Arrays;

public class CircularFloatBuffer {

    private final float[] buffer;
    private int head;
    private int tail;
    private int size;

    public CircularFloatBuffer(final int capacity) {
        this.buffer = new float[capacity];
    }

    public synchronized void write(final float value) {
        this.ensureHasEnoughSpace(1);
        this.buffer[this.tail] = value;
        this.tail = (this.tail + 1) % this.buffer.length;
        this.size++;
    }

    public void writeAll(final float[] values) {
        this.writeAll(values, values.length);
    }

    public synchronized void writeAll(final float[] values, final int valuesLength) {
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

    public float read() {
        if (this.isEmpty()) {
            throw new IllegalStateException("Buffer is empty");
        }
        return this.readSafe();
    }

    public float readSafe() {
        return this.readSafe(0F);
    }

    public synchronized float readSafe(final float defaultValue) {
        if (this.isEmpty()) {
            return defaultValue;
        }
        final float value = this.buffer[this.head];
        this.head = (head + 1) % this.buffer.length;
        this.size--;
        return value;
    }

    public float[] readAllSafe(final int size) {
        return this.readAllSafe(size, 0F);
    }

    public float[] readAllSafe(final int size, final float defaultValue) {
        final float[] values = new float[size];
        this.readAllSafe(values, defaultValue);
        return values;
    }

    public void readAllSafe(final float[] values) {
        this.readAllSafe(values, 0F);
    }

    public void readAllSafe(final float[] values, final float defaultValue) {
        this.readAllSafe(values, values.length, defaultValue);
    }

    public void readAllSafe(final float[] values, final int valuesLength) {
        this.readAllSafe(values, valuesLength, 0F);
    }

    public synchronized void readAllSafe(final float[] values, final int valuesLength, final float defaultValue) {
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
