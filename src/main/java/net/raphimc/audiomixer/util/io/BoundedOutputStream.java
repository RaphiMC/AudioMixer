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
import java.io.OutputStream;

public class BoundedOutputStream extends OutputStream {

    private final OutputStream delegate;
    private long remaining;

    public BoundedOutputStream(final OutputStream delegate, final long maxLength) {
        this.delegate = delegate;
        this.remaining = maxLength;
    }

    @Override
    public void write(final int b) throws IOException {
        if (this.remaining > 0) {
            this.delegate.write(b);
            this.remaining--;
        } else {
            throw new IOException("Maximum output length exceeded");
        }
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        if (this.remaining > 0) {
            final int boundedLength = (int) Math.min(len, this.remaining);
            this.delegate.write(b, off, boundedLength);
            this.remaining -= boundedLength;
            if (boundedLength < len) {
                throw new IOException("Maximum output length exceeded");
            }
        } else if (len != 0) {
            throw new IOException("Maximum output length exceeded");
        }
    }

    @Override
    public void flush() throws IOException {
        this.delegate.flush();
    }

    @Override
    public void close() throws IOException {
        this.delegate.close();
    }

    public long getRemaining() {
        return this.remaining;
    }

}
