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
package net.raphimc.audiomixer.util.io;

import java.io.IOException;
import java.io.InputStream;

public class BoundedInputStream extends InputStream {

    private final InputStream delegate;
    private long remaining;

    public BoundedInputStream(final InputStream delegate, final long maxLength) {
        this.delegate = delegate;
        this.remaining = maxLength;
    }

    @Override
    public int read() throws IOException {
        if (this.remaining > 0) {
            final int value = this.delegate.read();
            if (value != -1) {
                this.remaining--;
            } else {
                this.remaining = 0;
            }
            return value;
        } else {
            return -1;
        }
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        if (this.remaining > 0) {
            final int boundedLength = (int) Math.min(len, this.remaining);
            final int read = this.delegate.read(b, off, boundedLength);
            if (read > 0) {
                this.remaining -= read;
            } else if (read == -1) {
                this.remaining = 0;
            }
            return read;
        } else if (len == 0) {
            return 0;
        } else {
            return -1;
        }
    }

    @Override
    public long skip(final long n) throws IOException {
        if (this.remaining > 0) {
            final long boundedLength = Math.min(n, this.remaining);
            final long skipped = this.delegate.skip(boundedLength);
            this.remaining -= skipped;
            return skipped;
        } else {
            return 0;
        }
    }

    @Override
    public int available() throws IOException {
        return (int) Math.min(this.delegate.available(), this.remaining);
    }

    @Override
    public void close() throws IOException {
        this.delegate.close();
    }

    public long getRemaining() {
        return this.remaining;
    }

}
