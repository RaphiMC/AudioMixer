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
package net.raphimc.audiomixer.util.io.ogg;

import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class OggOutputStream implements Flushable, Closeable {

    private final OutputStream outputStream;
    private final Map<Integer, StreamState> streamStates = new HashMap<>();

    public OggOutputStream(final OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void writePacket(final int streamId, final byte[] data, final boolean eos, final long granulePosition) throws IOException {
        this.writePacket(streamId, data, 0, data.length, eos, granulePosition);
    }

    public void writePacket(final int streamId, final byte[] data, final int offset, final int length, final boolean eos, final long granulePosition) throws IOException {
        final Packet packet = new Packet();
        packet.packet_base = data;
        packet.packet = offset;
        packet.bytes = length;
        packet.e_o_s = eos ? 1 : 0;
        packet.granulepos = granulePosition;

        final StreamState streamState = this.streamStates.computeIfAbsent(streamId, serialNumber -> {
            final StreamState newStreamState = new StreamState();
            newStreamState.init(serialNumber);
            return newStreamState;
        });
        checkResult(streamState.packetin(packet), "Failed to process packet");
        final Page page = new Page();
        while (streamState.pageout(page) > 0) {
            this.writePage(page);
        }
        if (streamState.eof() != 0) {
            streamState.clear();
            this.streamStates.remove(streamId);
        }
    }

    private void writePage(final Page page) throws IOException {
        this.outputStream.write(page.header_base, page.header, page.header_len);
        this.outputStream.write(page.body_base, page.body, page.body_len);
    }

    public void flushStreams() throws IOException {
        for (StreamState streamState : this.streamStates.values()) {
            this.flushStream(streamState);
        }
    }

    public void flushStream(final int streamId) throws IOException {
        final StreamState streamState = this.streamStates.get(streamId);
        if (streamState == null) {
            throw new IllegalArgumentException("Logical stream " + streamId + " does not exist");
        }
        this.flushStream(streamState);
    }

    private void flushStream(final StreamState streamState) throws IOException {
        final Page page = new Page();
        while (streamState.flush(page) > 0) {
            this.writePage(page);
        }
    }

    @Override
    public void flush() throws IOException {
        this.outputStream.flush();
    }

    @Override
    public void close() throws IOException {
        try (this.outputStream) {
            if (!this.streamStates.isEmpty()) {
                throw new IOException("Not all logical streams were closed properly");
            }
        }
    }

    private static void checkResult(final int result, final String message) throws IOException {
        if (result < 0) {
            throw new IOException(message + " (error code: " + result + ")");
        }
    }

}
