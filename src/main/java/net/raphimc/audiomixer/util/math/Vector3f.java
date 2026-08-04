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
package net.raphimc.audiomixer.util.math;

public record Vector3f(float x, float y, float z) {

    public static final Vector3f ZERO = new Vector3f(0F, 0F, 0F);

    public Vector3f {
        if (!Float.isFinite(x) || !Float.isFinite(y) || !Float.isFinite(z)) {
            throw new IllegalArgumentException("Components must be finite");
        }
    }

    public Vector3f add(final Vector3f other) {
        return this.add(other.x, other.y, other.z);
    }

    public Vector3f add(final float x, final float y, final float z) {
        return new Vector3f(this.x + x, this.y + y, this.z + z);
    }

    public Vector3f subtract(final Vector3f other) {
        return this.subtract(other.x, other.y, other.z);
    }

    public Vector3f subtract(final float x, final float y, final float z) {
        return new Vector3f(this.x - x, this.y - y, this.z - z);
    }

    public Vector3f multiply(final float scalar) {
        return this.multiply(scalar, scalar, scalar);
    }

    public Vector3f multiply(final Vector3f other) {
        return this.multiply(other.x, other.y, other.z);
    }

    public Vector3f multiply(final float x, final float y, final float z) {
        return new Vector3f(this.x * x, this.y * y, this.z * z);
    }

    public Vector3f divide(final float scalar) {
        return this.divide(scalar, scalar, scalar);
    }

    public Vector3f divide(final Vector3f other) {
        return this.divide(other.x, other.y, other.z);
    }

    public Vector3f divide(final float x, final float y, final float z) {
        return new Vector3f(this.x / x, this.y / y, this.z / z);
    }

    public float distanceTo(final Vector3f other) {
        return this.subtract(other).length();
    }

    public float length() {
        return (float) Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

}
