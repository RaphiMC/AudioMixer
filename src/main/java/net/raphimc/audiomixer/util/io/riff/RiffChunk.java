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
package net.raphimc.audiomixer.util.io.riff;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.LongSupplier;

public sealed class RiffChunk implements Closeable permits RiffListChunk {

    static final int FOURCC_LENGTH = 4;

    public static final int BYTES = FOURCC_LENGTH + Integer.BYTES;

    private final String identifier;
    private final long length;
    private final Closeable closeable;
    private final LongSupplier remainingSupplier;

    RiffChunk(final String identifier, final long length, final Closeable closeAction, final LongSupplier remainingSupplier) {
        this.identifier = identifier;
        this.length = length;
        this.closeable = closeAction;
        this.remainingSupplier = remainingSupplier;
    }

    @Override
    public void close() throws IOException {
        this.closeable.close();
    }

    public String identifier() {
        return this.identifier;
    }

    public long length() {
        return this.length;
    }

    public long remaining() {
        return this.remainingSupplier.getAsLong();
    }

}
