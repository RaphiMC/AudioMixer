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

public class SeekableBufferedOutputStream extends SeekableOutputStream {

    private final SeekableOutputStream delegate;
    private final byte[] buffer;
    private int position;

    public SeekableBufferedOutputStream(final SeekableOutputStream delegate) {
        this(delegate, 8192);
    }

    public SeekableBufferedOutputStream(final SeekableOutputStream delegate, final int size) {
        this.delegate = delegate;
        this.buffer = new byte[size];
    }

    @Override
    public void write(final int b) throws IOException {
        if (this.position >= this.buffer.length) {
            this.flushBuffer();
        }
        this.buffer[this.position++] = (byte) b;
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        if (len >= this.buffer.length) {
            this.flushBuffer();
            this.delegate.write(b, off, len);
            return;
        }
        if (len > this.buffer.length - this.position) {
            this.flushBuffer();
        }
        System.arraycopy(b, off, this.buffer, this.position, len);
        this.position += len;
    }

    @Override
    public void flush() throws IOException {
        this.flushBuffer();
        this.delegate.flush();
    }

    @Override
    public void close() throws IOException {
        try (this.delegate) {
            this.flushBuffer();
        }
    }

    @Override
    public void seek(final long position) throws IOException {
        this.flushBuffer();
        this.delegate.seek(position);
    }

    @Override
    public long position() throws IOException {
        return Math.addExact(this.delegate.position(), this.position);
    }

    private void flushBuffer() throws IOException {
        if (this.position > 0) {
            this.delegate.write(this.buffer, 0, this.position);
            this.position = 0;
        }
    }

}
