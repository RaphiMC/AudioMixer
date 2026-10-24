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

public class PcmAudioInputStream extends AudioInputStream {

    private final BinaryInputStream inputStream;
    private final PcmSampleEncoding encoding;
    private final float integerScale;

    public PcmAudioInputStream(final InputStream inputStream, final PcmAudioFormat format) {
        this(inputStream, format.format(), format.encoding());
    }

    public PcmAudioInputStream(final InputStream inputStream, final AudioFormat format, final PcmSampleEncoding encoding) {
        super(format);
        this.inputStream = new BinaryInputStream(inputStream);
        this.encoding = encoding;
        this.integerScale = Math.scalb(1F, encoding.bytesPerSample() * Byte.SIZE - 1);
    }

    @Override
    public float read() throws IOException {
        return switch (this.encoding) {
            case S8 -> this.inputStream.readByte() / this.integerScale;
            case U8 -> (this.inputStream.readUnsignedByte() + Byte.MIN_VALUE) / this.integerScale;
            case S16_BE, S16_LE -> this.inputStream.readShort(this.encoding.byteOrder()) / this.integerScale;
            case U16_BE, U16_LE -> (this.inputStream.readUnsignedShort(this.encoding.byteOrder()) + Short.MIN_VALUE) / this.integerScale;
            case S24_BE, S24_LE -> this.inputStream.readMedium(this.encoding.byteOrder()) / this.integerScale;
            case U24_BE, U24_LE -> (this.inputStream.readUnsignedMedium(this.encoding.byteOrder()) + MathUtil.MEDIUM_MIN_VALUE) / this.integerScale;
            case S32_BE, S32_LE -> this.inputStream.readInt(this.encoding.byteOrder()) / this.integerScale;
            case U32_BE, U32_LE -> (this.inputStream.readUnsignedInt(this.encoding.byteOrder()) + Integer.MIN_VALUE) / this.integerScale;
            case F32_BE, F32_LE -> this.inputStream.readFloat(this.encoding.byteOrder());
            case F64_BE, F64_LE -> (float) this.inputStream.readDouble(this.encoding.byteOrder());
        };
    }

    @Override
    public void close() throws IOException {
        this.inputStream.close();
    }

    public PcmSampleEncoding getEncoding() {
        return this.encoding;
    }

}
