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
package net.raphimc.audiomixer.util;

import net.raphimc.audiomixer.util.math.MathUtil;

import java.nio.ByteOrder;

public enum PcmSampleEncoding {

    S8(Type.SIGNED, Byte.BYTES, ByteOrder.BIG_ENDIAN),
    U8(Type.UNSIGNED, Byte.BYTES, ByteOrder.BIG_ENDIAN),

    S16_BE(Type.SIGNED, Short.BYTES, ByteOrder.BIG_ENDIAN),
    S16_LE(Type.SIGNED, Short.BYTES, ByteOrder.LITTLE_ENDIAN),
    U16_BE(Type.UNSIGNED, Short.BYTES, ByteOrder.BIG_ENDIAN),
    U16_LE(Type.UNSIGNED, Short.BYTES, ByteOrder.LITTLE_ENDIAN),

    S24_BE(Type.SIGNED, MathUtil.MEDIUM_BYTES, ByteOrder.BIG_ENDIAN),
    S24_LE(Type.SIGNED, MathUtil.MEDIUM_BYTES, ByteOrder.LITTLE_ENDIAN),
    U24_BE(Type.UNSIGNED, MathUtil.MEDIUM_BYTES, ByteOrder.BIG_ENDIAN),
    U24_LE(Type.UNSIGNED, MathUtil.MEDIUM_BYTES, ByteOrder.LITTLE_ENDIAN),

    S32_BE(Type.SIGNED, Integer.BYTES, ByteOrder.BIG_ENDIAN),
    S32_LE(Type.SIGNED, Integer.BYTES, ByteOrder.LITTLE_ENDIAN),
    U32_BE(Type.UNSIGNED, Integer.BYTES, ByteOrder.BIG_ENDIAN),
    U32_LE(Type.UNSIGNED, Integer.BYTES, ByteOrder.LITTLE_ENDIAN),

    F32_BE(Type.FLOAT, Float.BYTES, ByteOrder.BIG_ENDIAN),
    F32_LE(Type.FLOAT, Float.BYTES, ByteOrder.LITTLE_ENDIAN),

    F64_BE(Type.FLOAT, Double.BYTES, ByteOrder.BIG_ENDIAN),
    F64_LE(Type.FLOAT, Double.BYTES, ByteOrder.LITTLE_ENDIAN);

    private final Type type;
    private final int bytesPerSample;
    private final ByteOrder byteOrder;

    PcmSampleEncoding(final Type type, final int bytesPerSample, final ByteOrder byteOrder) {
        this.type = type;
        this.bytesPerSample = bytesPerSample;
        this.byteOrder = byteOrder;
    }

    public Type type() {
        return this.type;
    }

    public int bytesPerSample() {
        return this.bytesPerSample;
    }

    public ByteOrder byteOrder() {
        return this.byteOrder;
    }

    public enum Type {

        SIGNED,
        UNSIGNED,
        FLOAT

    }

}
