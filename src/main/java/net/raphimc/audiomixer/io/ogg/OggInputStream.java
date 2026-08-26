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

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class OggInputStream implements Closeable {

    private static final int BUFFER_SIZE = 64 * 1024;
    private static final int PAGEOUT_SUCCESS = 1;
    private static final int PAGEOUT_NEED_MORE_DATA = 0;
    private static final int PAGEOUT_LOST_SYNC = -1;
    private static final int PACKETOUT_SUCCESS = 1;
    private static final int PACKETOUT_NEED_MORE_DATA = 0;
    private static final int PACKETOUT_STREAM_HOLE = -1;

    private final InputStream inputStream;
    private final SyncState syncState = new SyncState();
    private final Map<Integer, StreamState> streamStates = new HashMap<>();

    public OggInputStream(final InputStream inputStream) {
        this.inputStream = inputStream;
        this.syncState.init();
    }

    public OggPacket readUntilPacket(final int streamId) throws IOException {
        while (true) {
            final OggPacket packet = this.readPacket();
            if (packet.streamId() == streamId) {
                return packet;
            }
        }
    }

    public OggPacket readPacket() throws IOException {
        while (true) {
            final OggPacket packet = this.pollPacket();
            if (packet != null) {
                return packet;
            }
            final Page page = this.readPage();
            if (page.bos() != 0) {
                if (!this.streamStates.containsKey(page.serialno())) {
                    final StreamState streamState = new StreamState();
                    streamState.init(page.serialno());
                    this.streamStates.put(page.serialno(), streamState);
                } else {
                    throw new IOException("Duplicate BOS for logical stream: " + page.serialno());
                }
            }
            final StreamState streamState = this.streamStates.get(page.serialno());
            if (streamState != null) {
                checkResult(streamState.pagein(page), "Failed to process page");
            } else {
                throw new IOException("Got page for unknown logical stream: " + page.serialno());
            }
        }
    }

    private OggPacket pollPacket() throws IOException {
        final Packet packet = new Packet();
        final Iterator<Map.Entry<Integer, StreamState>> streamStatesIterator = this.streamStates.entrySet().iterator();
        while (streamStatesIterator.hasNext()) {
            final Map.Entry<Integer, StreamState> entry = streamStatesIterator.next();
            final StreamState streamState = entry.getValue();
            final int result = streamState.packetout(packet);
            switch (result) {
                case PACKETOUT_SUCCESS -> {
                    return new OggPacket(
                        entry.getKey(), Arrays.copyOfRange(packet.packet_base, packet.packet, packet.packet + packet.bytes),
                        packet.b_o_s != 0, packet.e_o_s != 0, packet.granulepos, packet.packetno
                    );
                }
                case PACKETOUT_NEED_MORE_DATA -> {
                    if (streamState.eof() != 0) {
                        streamState.clear();
                        streamStatesIterator.remove();
                    }
                }
                case PACKETOUT_STREAM_HOLE -> throw new IOException("Malformed Ogg stream");
                default -> throw new IllegalStateException("Unknown packet decode result: " + result);
            }
        }
        return null;
    }

    private Page readPage() throws IOException {
        final Page page = new Page();
        while (true) {
            final int result = this.syncState.pageout(page);
            switch (result) {
                case PAGEOUT_SUCCESS -> {
                    return page;
                }
                case PAGEOUT_NEED_MORE_DATA -> {
                    final int offset = this.syncState.buffer(BUFFER_SIZE);
                    final int read = this.inputStream.read(this.syncState.data, offset, BUFFER_SIZE);
                    if (read == -1) {
                        if (this.streamStates.isEmpty()) {
                            throw new EOFException();
                        } else {
                            throw new EOFException("Unexpected end of stream");
                        }
                    }
                    checkResult(this.syncState.wrote(read), "Failed to update sync state");
                }
                case PAGEOUT_LOST_SYNC -> throw new IOException("Malformed Ogg stream");
                default -> throw new IllegalStateException("Unknown page decode result: " + result);
            }
        }
    }

    @Override
    public void close() throws IOException {
        try (this.inputStream) {
            for (StreamState streamState : this.streamStates.values()) {
                streamState.clear();
            }
            this.streamStates.clear();
            checkResult(this.syncState.clear(), "Failed to clear sync state");
        }
    }

    private static void checkResult(final int result, final String message) throws IOException {
        if (result < 0) {
            throw new IOException(message + " (error code: " + result + ")");
        }
    }

    public record OggPacket(int streamId, byte[] data, boolean bos, boolean eos, long granulePosition, long packetNumber) {
    }

}
