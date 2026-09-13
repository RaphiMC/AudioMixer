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
package net.raphimc.audiomixer.io.ogg.opus.packet;

import net.raphimc.audiomixer.util.io.BinaryInputStream;
import net.raphimc.audiomixer.util.io.BinaryOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class OpusTagsPacket {

    private static final byte[] MAGIC = new byte[]{(byte) 'O', (byte) 'p', (byte) 'u', (byte) 's', (byte) 'T', (byte) 'a', (byte) 'g', (byte) 's'};

    private final String vendor;
    private final List<String> userComments;

    public OpusTagsPacket(final byte[] data) throws IOException {
        this(new BinaryInputStream(new ByteArrayInputStream(data), ByteOrder.LITTLE_ENDIAN));
    }

    public OpusTagsPacket(final BinaryInputStream inputStream) throws IOException {
        final byte[] magic = inputStream.readBytes(MAGIC.length);
        if (!Arrays.equals(magic, MAGIC)) {
            throw new IOException("Invalid OpusTags packet: Incorrect magic bytes");
        }
        this.vendor = new String(inputStream.readBytes(Math.toIntExact(inputStream.readUnsignedInt())), StandardCharsets.UTF_8);
        final String[] userComments = new String[Math.toIntExact(inputStream.readUnsignedInt())];
        for (int i = 0; i < userComments.length; i++) {
            userComments[i] = new String(inputStream.readBytes(Math.toIntExact(inputStream.readUnsignedInt())), StandardCharsets.UTF_8);
        }
        this.userComments = List.of(userComments);
    }

    public OpusTagsPacket(final String vendor, final List<String> userComments) {
        this.vendor = vendor;
        this.userComments = List.copyOf(userComments);
    }

    public byte[] write() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final BinaryOutputStream outputStream = new BinaryOutputStream(baos, ByteOrder.LITTLE_ENDIAN);
        this.write(outputStream);
        return baos.toByteArray();
    }

    public void write(final BinaryOutputStream outputStream) throws IOException {
        outputStream.write(MAGIC);
        final byte[] vendorBytes = this.vendor.getBytes(StandardCharsets.UTF_8);
        outputStream.writeUnsignedInt(vendorBytes.length);
        outputStream.write(vendorBytes);
        outputStream.writeUnsignedInt(this.userComments.size());
        for (String userComment : this.userComments) {
            final byte[] userCommentBytes = userComment.getBytes(StandardCharsets.UTF_8);
            outputStream.writeUnsignedInt(userCommentBytes.length);
            outputStream.write(userCommentBytes);
        }
    }

    public String vendor() {
        return this.vendor;
    }

    public List<String> userComments() {
        return this.userComments;
    }

}
