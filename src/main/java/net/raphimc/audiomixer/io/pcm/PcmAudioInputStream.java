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
package net.raphimc.audiomixer.io.pcm;

import net.raphimc.audiomixer.io.AudioInputStream;
import net.raphimc.audiomixer.util.AudioFormat;
import net.raphimc.audiomixer.util.PcmAudioFormat;
import net.raphimc.audiomixer.util.PcmSampleEncoding;
import net.raphimc.audiomixer.util.io.BinaryInputStream;
import net.raphimc.audiomixer.util.math.MathUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

public class PcmAudioInputStream extends AudioInputStream {

    private final BinaryInputStream inputStream;
    private final PcmSampleEncoding encoding;

    public PcmAudioInputStream(final InputStream inputStream, final PcmAudioFormat format) {
        this(inputStream, format.format(), format.encoding());
    }

    public PcmAudioInputStream(final InputStream inputStream, final AudioFormat format, final PcmSampleEncoding encoding) {
        super(format);
        this.inputStream = new BinaryInputStream(inputStream);
        this.encoding = encoding;
    }

    @Override
    public float read() throws IOException {
        return switch (this.encoding) {
            case S8 -> this.readSignedByte();
            case U8 -> this.readUnsignedByte();
            case S16_BE, S16_LE -> this.readSignedShort(this.encoding.byteOrder());
            case U16_BE, U16_LE -> this.readUnsignedShort(this.encoding.byteOrder());
            case S24_BE, S24_LE -> this.readSignedMedium(this.encoding.byteOrder());
            case U24_BE, U24_LE -> this.readUnsignedMedium(this.encoding.byteOrder());
            case S32_BE, S32_LE -> this.readSignedInt(this.encoding.byteOrder());
            case U32_BE, U32_LE -> this.readUnsignedInt(this.encoding.byteOrder());
            case F32_BE, F32_LE -> this.inputStream.readFloat(this.encoding.byteOrder());
            case F64_BE, F64_LE -> (float) this.inputStream.readDouble(this.encoding.byteOrder());
        };
    }

    private float readSignedByte() throws IOException {
        final byte value = this.inputStream.readByte();
        if (value < 0) {
            return -(float) value / Byte.MIN_VALUE;
        } else if (value > 0) {
            return (float) value / Byte.MAX_VALUE;
        } else {
            return 0F;
        }
    }

    private float readUnsignedByte() throws IOException {
        return this.inputStream.readUnsignedByte() / (Byte.MAX_VALUE + 0.5F) - 1F;
    }

    private float readSignedShort(final ByteOrder byteOrder) throws IOException {
        final short value = this.inputStream.readShort(byteOrder);
        if (value < 0) {
            return -(float) value / Short.MIN_VALUE;
        } else if (value > 0) {
            return (float) value / Short.MAX_VALUE;
        } else {
            return 0F;
        }
    }

    private float readUnsignedShort(final ByteOrder byteOrder) throws IOException {
        return this.inputStream.readUnsignedShort(byteOrder) / (Short.MAX_VALUE + 0.5F) - 1F;
    }

    private float readSignedMedium(final ByteOrder byteOrder) throws IOException {
        final int value = this.inputStream.readMedium(byteOrder);
        if (value < 0) {
            return -(float) value / MathUtil.MEDIUM_MIN_VALUE;
        } else if (value > 0) {
            return (float) value / MathUtil.MEDIUM_MAX_VALUE;
        } else {
            return 0F;
        }
    }

    private float readUnsignedMedium(final ByteOrder byteOrder) throws IOException {
        return this.inputStream.readUnsignedMedium(byteOrder) / (MathUtil.MEDIUM_MAX_VALUE + 0.5F) - 1F;
    }

    private float readSignedInt(final ByteOrder byteOrder) throws IOException {
        final int value = this.inputStream.readInt(byteOrder);
        if (value < 0) {
            return (float) (-(double) value / Integer.MIN_VALUE);
        } else if (value > 0) {
            return (float) ((double) value / Integer.MAX_VALUE);
        } else {
            return 0F;
        }
    }

    private float readUnsignedInt(final ByteOrder byteOrder) throws IOException {
        return (float) (this.inputStream.readUnsignedInt(byteOrder) / (Integer.MAX_VALUE + 0.5D)) - 1F;
    }

    @Override
    public void close() throws IOException {
        this.inputStream.close();
    }

    public PcmSampleEncoding getEncoding() {
        return this.encoding;
    }

}
