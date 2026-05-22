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

import java.util.function.IntPredicate;

public interface IntParameter {

    static IntParameter of(final int initialValue) {
        return new IntParameter() {
            private int value = initialValue;

            @Override
            public int get() {
                return this.value;
            }

            @Override
            public void set(final int value) {
                this.value = value;
            }
        };
    }

    int get();

    void set(final int value);

    default IntParameter withConstraint(final Constraint constraint) {
        return new IntParameter() {
            @Override
            public int get() {
                return IntParameter.this.get();
            }

            @Override
            public void set(final int value) {
                if (!constraint.test(value)) {
                    throw new IllegalArgumentException("Failed to set value: " + constraint.errorMessage());
                }
                IntParameter.this.set(value);
            }
        };
    }

    default IntParameter withChangeListener(final Runnable onChange) {
        return new IntParameter() {
            @Override
            public int get() {
                return IntParameter.this.get();
            }

            @Override
            public void set(final int value) {
                if (IntParameter.this.get() != value) {
                    IntParameter.this.set(value);
                    onChange.run();
                }
            }
        };
    }

    default FloatParameter asFloatParameter() {
        return new FloatParameter() {
            @Override
            public float get() {
                return IntParameter.this.get();
            }

            @Override
            public void set(final float value) {
                IntParameter.this.set((int) value);
            }
        };
    }

    @FunctionalInterface
    interface Constraint extends IntPredicate {

        Constraint POSITIVE = new Constraint() {
            @Override
            public boolean test(final int value) {
                return value >= 0;
            }

            @Override
            public String errorMessage() {
                return "Value must be >= 0";
            }
        };

        Constraint GREATER_THAN_ZERO = new Constraint() {
            @Override
            public boolean test(final int value) {
                return value > 0;
            }

            @Override
            public String errorMessage() {
                return "Value must be > 0";
            }
        };

        default String errorMessage() {
            return "Value does not satisfy constraint";
        }

    }

}
