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
package net.raphimc.audiomixer.util.collection;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class ConsumerListenerList<T> {

    private final List<Consumer<T>> listeners = new CopyOnWriteArrayList<>();

    public void accept(final T t) {
        if (!this.listeners.isEmpty()) {
            for (Consumer<T> listener : this.listeners) {
                listener.accept(t);
            }
        }
    }

    public boolean add(final Consumer<T> listener) {
        return this.listeners.add(listener);
    }

    public boolean remove(final Consumer<T> listener) {
        return this.listeners.remove(listener);
    }

    public boolean contains(final Consumer<T> listener) {
        return this.listeners.contains(listener);
    }

    public boolean isEmpty() {
        return this.listeners.isEmpty();
    }

}
