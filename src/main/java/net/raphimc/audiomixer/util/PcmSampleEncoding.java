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

    S8(true, Byte.BYTES, ByteOrder.BIG_ENDIAN),
    U8(false, Byte.BYTES, ByteOrder.BIG_ENDIAN),

    S16_BE(true, Short.BYTES, ByteOrder.BIG_ENDIAN),
    S16_LE(true, Short.BYTES, ByteOrder.LITTLE_ENDIAN),
    U16_BE(false, Short.BYTES, ByteOrder.BIG_ENDIAN),
    U16_LE(false, Short.BYTES, ByteOrder.LITTLE_ENDIAN),

    S24_BE(true, MathUtil.MEDIUM_BYTES, ByteOrder.BIG_ENDIAN),
    S24_LE(true, MathUtil.MEDIUM_BYTES, ByteOrder.LITTLE_ENDIAN),
    U24_BE(false, MathUtil.MEDIUM_BYTES, ByteOrder.BIG_ENDIAN),
    U24_LE(false, MathUtil.MEDIUM_BYTES, ByteOrder.LITTLE_ENDIAN),

    S32_BE(true, Integer.BYTES, ByteOrder.BIG_ENDIAN),
    S32_LE(true, Integer.BYTES, ByteOrder.LITTLE_ENDIAN),
    U32_BE(false, Integer.BYTES, ByteOrder.BIG_ENDIAN),
    U32_LE(false, Integer.BYTES, ByteOrder.LITTLE_ENDIAN),

    F32_BE(true, Float.BYTES, ByteOrder.BIG_ENDIAN),
    F32_LE(true, Float.BYTES, ByteOrder.LITTLE_ENDIAN),

    F64_BE(true, Double.BYTES, ByteOrder.BIG_ENDIAN),
    F64_LE(true, Double.BYTES, ByteOrder.LITTLE_ENDIAN);

    private final boolean signed;
    private final int bytesPerSample;
    private final ByteOrder byteOrder;

    PcmSampleEncoding(final boolean signed, final int bytesPerSample, final ByteOrder byteOrder) {
        this.signed = signed;
        this.bytesPerSample = bytesPerSample;
        this.byteOrder = byteOrder;
    }

    public boolean signed() {
        return this.signed;
    }

    public int bytesPerSample() {
        return this.bytesPerSample;
    }

    public ByteOrder byteOrder() {
        return this.byteOrder;
    }

}
