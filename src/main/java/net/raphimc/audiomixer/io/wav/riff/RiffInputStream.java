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

import net.raphimc.audiomixer.util.io.BinaryInputStream;
import net.raphimc.audiomixer.util.io.BoundedInputStream;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class RiffInputStream extends BinaryInputStream {

    private final RiffListChunk rootChunk;

    public RiffInputStream(final InputStream in) throws IOException {
        super(in, ByteOrder.LITTLE_ENDIAN);
        final RiffChunk chunk = this.readRawChunk();
        if (!chunk.identifier().equals("RIFF")) {
            throw new IOException("Invalid RIFF stream: Expected 'RIFF' but got '" + chunk.identifier() + "'");
        }
        this.rootChunk = new RiffListChunk(this.readFourCharCode(), chunk);
    }

    public RiffChunk findNextChunk(final String identifier) throws IOException {
        while (true) {
            final RiffChunk chunk = this.readChunk();
            if (chunk.identifier().equals(identifier)) {
                return chunk;
            } else {
                chunk.close();
            }
        }
    }

    public RiffChunk readChunk() throws IOException {
        final RiffChunk chunk = this.readRawChunk();
        if (chunk.identifier().equals(RiffListChunk.FOURCC)) {
            return new RiffListChunk(this.readFourCharCode(), chunk);
        } else {
            return chunk;
        }
    }

    public RiffChunk readRawChunk() throws IOException {
        final String identifier = this.readFourCharCode();
        final long length = this.readUnsignedInt();
        final InputStream previousInputStream = this.in;
        final BoundedInputStream chunkInputStream = new BoundedInputStream(previousInputStream, length);
        final Closeable closeAction = () -> {
            chunkInputStream.skipNBytes(chunkInputStream.getRemaining());
            previousInputStream.skipNBytes(length % 2);
            this.in = previousInputStream;
        };
        this.in = chunkInputStream;
        return new RiffChunk(identifier, length, closeAction, chunkInputStream::getRemaining);
    }

    public RiffListChunk getRootChunk() {
        return this.rootChunk;
    }

    private String readFourCharCode() throws IOException {
        return new String(this.readBytes(RiffChunk.FOURCC_LENGTH), StandardCharsets.US_ASCII);
    }

}
