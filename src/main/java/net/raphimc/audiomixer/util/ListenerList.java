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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ListenerList<T> implements Collection<Consumer<T>> {

    private final Collection<Consumer<T>> listeners = Collections.synchronizedList(new ArrayList<>(0));

    public void invoke(final T object) {
        if (!this.isEmpty()) {
            this.forEach(listener -> listener.accept(object));
        }
    }

    @Override
    public int size() {
        return this.listeners.size();
    }

    @Override
    public boolean isEmpty() {
        return this.listeners.isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
        return this.listeners.contains(o);
    }

    @Override
    public Iterator<Consumer<T>> iterator() {
        return this.listeners.iterator();
    }

    @Override
    public Object[] toArray() {
        return this.listeners.toArray();
    }

    @Override
    public <T> T[] toArray(final T[] a) {
        return this.listeners.toArray(a);
    }

    @Override
    public <T> T[] toArray(final IntFunction<T[]> generator) {
        return this.listeners.toArray(generator);
    }

    @Override
    public void forEach(final Consumer<? super Consumer<T>> action) {
        this.listeners.forEach(action);
    }

    @Override
    public boolean add(final Consumer<T> e) {
        return this.listeners.add(e);
    }

    @Override
    public boolean remove(final Object o) {
        return this.listeners.remove(o);
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return this.listeners.containsAll(c);
    }

    @Override
    public boolean addAll(final Collection<? extends Consumer<T>> c) {
        return this.listeners.addAll(c);
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        return this.listeners.removeAll(c);
    }

    @Override
    public boolean removeIf(final Predicate<? super Consumer<T>> filter) {
        return this.listeners.removeIf(filter);
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        return this.listeners.retainAll(c);
    }

    @Override
    public void clear() {
        this.listeners.clear();
    }

    @Override
    public Spliterator<Consumer<T>> spliterator() {
        return this.listeners.spliterator();
    }

    @Override
    public Stream<Consumer<T>> stream() {
        return this.listeners.stream();
    }

    @Override
    public Stream<Consumer<T>> parallelStream() {
        return this.listeners.parallelStream();
    }

}
