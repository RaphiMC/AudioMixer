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

public class PcmAudioOutputStream extends AudioOutputStream {

    private final BinaryOutputStream outputStream;
    private final PcmSampleEncoding encoding;
    private final float integerScale;

    public PcmAudioOutputStream(final OutputStream outputStream, final PcmAudioFormat format) {
        this(outputStream, format.format(), format.encoding());
    }

    public PcmAudioOutputStream(final OutputStream outputStream, final AudioFormat format, final PcmSampleEncoding encoding) {
        super(format);
        this.outputStream = new BinaryOutputStream(outputStream);
        this.encoding = encoding;
        this.integerScale = Math.scalb(1F, encoding.bytesPerSample() * Byte.SIZE - 1);
    }

    @Override
    public void write(final float sample) throws IOException {
        if (!Float.isFinite(sample)) {
            throw new IOException("Sample must be finite: " + sample);
        }
        switch (this.encoding) {
            case S8 -> this.outputStream.writeByte((byte) MathUtil.clamp(Math.round(sample * this.integerScale), Byte.MIN_VALUE, Byte.MAX_VALUE));
            case U8 -> this.outputStream.writeUnsignedByte(MathUtil.clamp(Math.round(sample * this.integerScale), Byte.MIN_VALUE, Byte.MAX_VALUE) - Byte.MIN_VALUE);
            case S16_BE, S16_LE -> this.outputStream.writeShort((short) MathUtil.clamp(Math.round(sample * this.integerScale), Short.MIN_VALUE, Short.MAX_VALUE), this.encoding.byteOrder());
            case U16_BE, U16_LE -> this.outputStream.writeUnsignedShort(MathUtil.clamp(Math.round(sample * this.integerScale), Short.MIN_VALUE, Short.MAX_VALUE) - Short.MIN_VALUE, this.encoding.byteOrder());
            case S24_BE, S24_LE -> this.outputStream.writeMedium(MathUtil.clamp(Math.round(sample * this.integerScale), MathUtil.MEDIUM_MIN_VALUE, MathUtil.MEDIUM_MAX_VALUE), this.encoding.byteOrder());
            case U24_BE, U24_LE -> this.outputStream.writeUnsignedMedium(MathUtil.clamp(Math.round(sample * this.integerScale), MathUtil.MEDIUM_MIN_VALUE, MathUtil.MEDIUM_MAX_VALUE) - MathUtil.MEDIUM_MIN_VALUE, this.encoding.byteOrder());
            case S32_BE, S32_LE -> this.outputStream.writeInt(Math.round(sample * this.integerScale), this.encoding.byteOrder());
            case U32_BE, U32_LE -> this.outputStream.writeUnsignedInt((long) Math.round(sample * this.integerScale) - Integer.MIN_VALUE, this.encoding.byteOrder());
            case F32_BE, F32_LE -> this.outputStream.writeFloat(sample, this.encoding.byteOrder());
            case F64_BE, F64_LE -> this.outputStream.writeDouble(sample, this.encoding.byteOrder());
            default -> throw new UnsupportedOperationException("Unsupported encoding: " + this.encoding);
        }
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

}
