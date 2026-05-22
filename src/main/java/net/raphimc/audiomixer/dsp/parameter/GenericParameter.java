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

import java.util.function.Predicate;

public interface GenericParameter<T> {

    static <T> GenericParameter<T> of(final T initialValue) {
        return new GenericParameter<>() {
            private T value = initialValue;

            @Override
            public T get() {
                return this.value;
            }

            @Override
            public void set(final T value) {
                if (value == null) {
                    throw new IllegalArgumentException("Value must be non-null");
                }
                this.value = value;
            }
        };
    }

    T get();

    void set(final T value);

    default GenericParameter<T> withConstraint(final Constraint<? super T> constraint) {
        return new GenericParameter<>() {
            @Override
            public T get() {
                return GenericParameter.this.get();
            }

            @Override
            public void set(final T value) {
                if (!constraint.test(value)) {
                    throw new IllegalArgumentException("Failed to set value: " + constraint.errorMessage());
                }
                GenericParameter.this.set(value);
            }
        };
    }

    default GenericParameter<T> withChangeListener(final Runnable onChange) {
        return new GenericParameter<>() {
            @Override
            public T get() {
                return GenericParameter.this.get();
            }

            @Override
            public void set(final T value) {
                if (GenericParameter.this.get() != value) {
                    GenericParameter.this.set(value);
                    onChange.run();
                }
            }
        };
    }

    @FunctionalInterface
    interface Constraint<T> extends Predicate<T> {

        default String errorMessage() {
            return "Value does not satisfy constraint";
        }

    }

}
