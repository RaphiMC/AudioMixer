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

import net.raphimc.audiomixer.io.AudioOutputStream;
import net.raphimc.audiomixer.util.AudioFormat;
import net.raphimc.audiomixer.util.PcmAudioFormat;
import net.raphimc.audiomixer.util.PcmSampleEncoding;
import net.raphimc.audiomixer.util.io.BinaryOutputStream;
import net.raphimc.audiomixer.util.math.MathUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;

public class PcmAudioOutputStream extends AudioOutputStream {

    private final BinaryOutputStream outputStream;
    private final PcmSampleEncoding encoding;

    public PcmAudioOutputStream(final OutputStream outputStream, final PcmAudioFormat format) {
        this(outputStream, format.format(), format.encoding());
    }

    public PcmAudioOutputStream(final OutputStream outputStream, final AudioFormat format, final PcmSampleEncoding encoding) {
        super(format);
        this.outputStream = new BinaryOutputStream(outputStream);
        this.encoding = encoding;
    }

    @Override
    public void write(final float sample) throws IOException {
        switch (this.encoding) {
            case S8 -> this.writeSignedByte(sample);
            case U8 -> this.writeUnsignedByte(sample);
            case S16_BE, S16_LE -> this.writeSignedShort(sample, this.encoding.byteOrder());
            case U16_BE, U16_LE -> this.writeUnsignedShort(sample, this.encoding.byteOrder());
            case S24_BE, S24_LE -> this.writeSignedMedium(sample, this.encoding.byteOrder());
            case U24_BE, U24_LE -> this.writeUnsignedMedium(sample, this.encoding.byteOrder());
            case S32_BE, S32_LE -> this.writeSignedInt(sample, this.encoding.byteOrder());
            case U32_BE, U32_LE -> this.writeUnsignedInt(sample, this.encoding.byteOrder());
            case F32_BE, F32_LE -> this.outputStream.writeFloat(sample, this.encoding.byteOrder());
            case F64_BE, F64_LE -> this.outputStream.writeDouble(sample, this.encoding.byteOrder());
            default -> throw new UnsupportedOperationException("Unsupported encoding: " + this.encoding);
        }
    }

    private void writeSignedByte(final float sample) throws IOException {
        this.checkSampleRange(sample);
        final byte value;
        if (sample < 0F) {
            value = (byte) Math.round(-sample * Byte.MIN_VALUE);
        } else if (sample > 0F) {
            value = (byte) Math.round(sample * Byte.MAX_VALUE);
        } else {
            value = 0;
        }
        this.outputStream.writeByte(value);
    }

    private void writeUnsignedByte(final float sample) throws IOException {
        this.checkSampleRange(sample);
        this.outputStream.writeUnsignedByte(Math.round((sample + 1F) * (Byte.MAX_VALUE + 0.5F)));
    }

    private void writeSignedShort(final float sample, final ByteOrder byteOrder) throws IOException {
        this.checkSampleRange(sample);
        final short value;
        if (sample < 0F) {
            value = (short) Math.round(-sample * Short.MIN_VALUE);
        } else if (sample > 0F) {
            value = (short) Math.round(sample * Short.MAX_VALUE);
        } else {
            value = 0;
        }
        this.outputStream.writeShort(value, byteOrder);
    }

    private void writeUnsignedShort(final float sample, final ByteOrder byteOrder) throws IOException {
        this.checkSampleRange(sample);
        this.outputStream.writeUnsignedShort(Math.round((sample + 1F) * (Short.MAX_VALUE + 0.5F)), byteOrder);
    }

    private void writeSignedMedium(final float sample, final ByteOrder byteOrder) throws IOException {
        this.checkSampleRange(sample);
        final int value;
        if (sample < 0F) {
            value = Math.round(-sample * MathUtil.MEDIUM_MIN_VALUE);
        } else if (sample > 0F) {
            value = Math.round(sample * MathUtil.MEDIUM_MAX_VALUE);
        } else {
            value = 0;
        }
        this.outputStream.writeMedium(value, byteOrder);
    }

    private void writeUnsignedMedium(final float sample, final ByteOrder byteOrder) throws IOException {
        this.checkSampleRange(sample);
        this.outputStream.writeUnsignedMedium(Math.round((sample + 1F) * (MathUtil.MEDIUM_MAX_VALUE + 0.5F)), byteOrder);
    }

    private void writeSignedInt(final float sample, final ByteOrder byteOrder) throws IOException {
        this.checkSampleRange(sample);
        final int value;
        if (sample < 0F) {
            value = (int) Math.round((double) -sample * Integer.MIN_VALUE);
        } else if (sample > 0F) {
            value = (int) Math.round((double) sample * Integer.MAX_VALUE);
        } else {
            value = 0;
        }
        this.outputStream.writeInt(value, byteOrder);
    }

    private void writeUnsignedInt(final float sample, final ByteOrder byteOrder) throws IOException {
        this.checkSampleRange(sample);
        this.outputStream.writeUnsignedInt(Math.round((sample + 1F) * (Integer.MAX_VALUE + 0.5D)), byteOrder);
    }

    @Override
    public void flush() throws IOException {
        this.outputStream.flush();
    }

    @Override
    public void close() throws IOException {
        this.outputStream.close();
    }

    public PcmSampleEncoding getEncoding() {
        return this.encoding;
    }

    private void checkSampleRange(final float sample) throws IOException {
        if (!Float.isFinite(sample) || sample < -1F || sample > 1F) {
            throw new IOException("Sample must be finite and in [-1, 1]");
        }
    }

}
