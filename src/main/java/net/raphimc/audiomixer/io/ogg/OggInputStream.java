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
package net.raphimc.audiomixer.io.ogg;

import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.SyncState;
import org.jetbrains.annotations.ApiStatus;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

@ApiStatus.Internal
public class OggInputStream implements Closeable {

    public static final int BUFFER_SIZE = 8192; // Don't change. This is hardcoded in jorbis.
    private static final int PAGEOUT_NEED_MORE_DATA = 0;
    private static final int PAGEOUT_SUCCESS = 1;
    private static final int PAGEOUT_ERROR = -1;
    private static final int PACKETOUT_NEED_MORE_DATA = 0;
    private static final int PACKETOUT_SUCCESS = 1;
    private static final int PACKETOUT_ERROR = -1;

    private final InputStream inputStream;
    private final SyncState syncState = new SyncState();
    private final StreamState streamState = new StreamState();

    public OggInputStream(final InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public Packet readPacket() throws IOException {
        final Packet packet = new Packet();
        while (true) {
            final int result = this.streamState.packetout(packet);
            switch (result) {
                case PACKETOUT_NEED_MORE_DATA -> {
                    if (this.streamState.eof() != 0) {
                        throw new EOFException();
                    }
                    final Page page = this.readPage();
                    if (page.bos() != 0) { // Begin of stream -> Initialize stream state
                        this.streamState.init(page.serialno());
                    }
                    if (this.streamState.pagein(page) < 0) {
                        throw new IOException("Failed to handle page");
                    }
                }
                case PACKETOUT_SUCCESS -> {
                    return packet;
                }
                case PACKETOUT_ERROR -> throw new IOException("Corrupted ogg stream");
                default -> throw new IllegalStateException("Unknown packet decode result: " + result);
            }
        }
    }

    public Page readPage() throws IOException {
        final Page page = new Page();
        while (true) {
            final int result = this.syncState.pageout(page);
            switch (result) {
                case PAGEOUT_NEED_MORE_DATA -> {
                    final int offset = this.syncState.buffer(BUFFER_SIZE);
                    final int read = this.inputStream.read(this.syncState.data, offset, BUFFER_SIZE);
                    if (read == -1) {
                        throw new EOFException("Unexpected end of ogg stream");
                    }
                    if (this.syncState.wrote(read) < 0) {
                        throw new IOException("Failed to update sync state");
                    }
                }
                case PAGEOUT_SUCCESS -> {
                    return page;
                }
                case PAGEOUT_ERROR -> throw new IOException("Corrupted ogg stream");
                default -> throw new IllegalStateException("Unknown page decode result: " + result);
            }
        }
    }

    @Override
    public void close() throws IOException {
        this.inputStream.close();
    }

}
