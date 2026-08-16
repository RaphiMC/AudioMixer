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
package net.raphimc.audiomixer.io.wav.riff;

import net.raphimc.audiomixer.util.io.BinaryOutputStream;
import net.raphimc.audiomixer.util.io.BoundedOutputStream;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class RiffOutputStream extends BinaryOutputStream {

    private final RiffListChunk rootChunk;

    public RiffOutputStream(final OutputStream out, final String identifier, final long length) throws IOException {
        super(out, ByteOrder.LITTLE_ENDIAN);
        final RiffChunk chunk = this.writeChunk("RIFF", Math.addExact(length, RiffChunk.FOURCC_LENGTH));
        this.writeFourCharCode(identifier);
        this.rootChunk = new RiffListChunk(identifier, chunk);
    }

    public RiffListChunk writeListChunk(final String identifier, final long length) throws IOException {
        final RiffChunk chunk = this.writeChunk(RiffListChunk.FOURCC, Math.addExact(length, RiffChunk.FOURCC_LENGTH));
        this.writeFourCharCode(identifier);
        return new RiffListChunk(identifier, chunk);
    }

    public RiffChunk writeChunk(final String identifier, final long length) throws IOException {
        this.writeFourCharCode(identifier);
        this.writeUnsignedInt(length);
        final OutputStream previousOutputStream = this.out;
        final BoundedOutputStream chunkOutputStream = new BoundedOutputStream(previousOutputStream, length);
        final Closeable closeAction = () -> {
            if (chunkOutputStream.getRemaining() != 0) {
                chunkOutputStream.write(new byte[Math.toIntExact(chunkOutputStream.getRemaining())]);
            }
            if (length % 2 != 0) {
                previousOutputStream.write(0);
            }
            this.out = previousOutputStream;
        };
        this.out = chunkOutputStream;
        return new RiffChunk(identifier, length, closeAction, chunkOutputStream::getRemaining);
    }

    public RiffListChunk getRootChunk() {
        return this.rootChunk;
    }

    private void writeFourCharCode(final String code) throws IOException {
        final byte[] bytes = code.getBytes(StandardCharsets.US_ASCII);
        if (bytes.length != RiffChunk.FOURCC_LENGTH) {
            throw new IllegalArgumentException("FourCC must be " + RiffChunk.FOURCC_LENGTH + " characters long");
        }
        this.write(bytes);
    }

}
