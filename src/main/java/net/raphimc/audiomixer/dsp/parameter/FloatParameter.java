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
package net.raphimc.audiomixer.dsp.parameter;

import net.raphimc.audiomixer.util.functional.Float2FloatFunction;
import net.raphimc.audiomixer.util.functional.FloatPredicate;

public interface FloatParameter {

    static FloatParameter of(final float initialValue) {
        return new FloatParameter() {
            private float value = initialValue;

            @Override
            public float get() {
                return this.value;
            }

            @Override
            public void set(final float value) {
                if (!Float.isFinite(value)) {
                    throw new IllegalArgumentException("Value must be finite");
                }
                this.value = value;
            }
        };
    }

    float get();

    void set(final float value);

    default FloatParameter withConstraint(final Constraint constraint) {
        return new FloatParameter() {
            @Override
            public float get() {
                return FloatParameter.this.get();
            }

            @Override
            public void set(final float value) {
                if (!constraint.test(value)) {
                    throw new IllegalArgumentException("Failed to set value: " + constraint.errorMessage());
                }
                FloatParameter.this.set(value);
            }
        };
    }

    default FloatParameter withChangeListener(final Runnable onChange) {
        return new FloatParameter() {
            @Override
            public float get() {
                return FloatParameter.this.get();
            }

            @Override
            public void set(final float value) {
                if (FloatParameter.this.get() != value) {
                    FloatParameter.this.set(value);
                    onChange.run();
                }
            }
        };
    }

    default FloatParameter withMapping(final Float2FloatFunction getMapper, final Float2FloatFunction setMapper) {
        return new FloatParameter() {
            @Override
            public float get() {
                return getMapper.apply(FloatParameter.this.get());
            }

            @Override
            public void set(final float value) {
                FloatParameter.this.set(setMapper.apply(value));
            }
        };
    }

    @FunctionalInterface
    interface Constraint extends FloatPredicate {

        Constraint POSITIVE = new Constraint() {
            @Override
            public boolean test(final float value) {
                return value >= 0F;
            }

            @Override
            public String errorMessage() {
                return "Value must be >= 0";
            }
        };

        Constraint GREATER_THAN_ZERO = new Constraint() {
            @Override
            public boolean test(final float value) {
                return value > 0F;
            }

            @Override
            public String errorMessage() {
                return "Value must be > 0";
            }
        };

        Constraint AT_LEAST_ONE = new Constraint() {
            @Override
            public boolean test(final float value) {
                return value >= 1F;
            }

            @Override
            public String errorMessage() {
                return "Value must be >= 1";
            }
        };

        Constraint NORMALIZED = new Constraint() {
            @Override
            public boolean test(final float value) {
                return value >= 0F && value <= 1F;
            }

            @Override
            public String errorMessage() {
                return "Value must be in [0, 1]";
            }
        };

        Constraint SIGNED_NORMALIZED = new Constraint() {
            @Override
            public boolean test(final float value) {
                return value >= -1F && value <= 1F;
            }

            @Override
            public String errorMessage() {
                return "Value must be in [-1, 1]";
            }
        };

        Constraint DEGREES = new Constraint() {
            @Override
            public boolean test(final float value) {
                return value >= 0F && value < 360F;
            }

            @Override
            public String errorMessage() {
                return "Value must be in [0, 360)";
            }
        };

        default String errorMessage() {
            return "Value does not satisfy constraint";
        }

    }

}
