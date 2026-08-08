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
package net.raphimc.audiomixer.processor;

import net.raphimc.audiomixer.util.buffer.AudioBuffer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class Processors extends Processor implements List<Processor> {

    private final List<Processor> processors = Collections.synchronizedList(new ArrayList<>(0));

    @Override
    public void processInternal(final AudioBuffer buffer) {
        if (!this.isEmpty()) {
            this.forEach(processor -> processor.process(buffer));
        }
    }

    public <T extends Processor> T getFirst(final Class<T> clazz) {
        synchronized (this.processors) {
            for (Processor processor : this.processors) {
                if (clazz.isInstance(processor)) {
                    return clazz.cast(processor);
                }
            }
        }
        return null;
    }

    @Override
    public int size() {
        return this.processors.size();
    }

    @Override
    public boolean isEmpty() {
        return this.processors.isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
        return this.processors.contains(o);
    }

    @Override
    public Iterator<Processor> iterator() {
        return this.processors.iterator();
    }

    @Override
    public Object[] toArray() {
        return this.processors.toArray();
    }

    @Override
    public <T> T[] toArray(final T[] a) {
        return this.processors.toArray(a);
    }

    @Override
    public <T> T[] toArray(final IntFunction<T[]> generator) {
        return this.processors.toArray(generator);
    }

    @Override
    public void forEach(final Consumer<? super Processor> action) {
        this.processors.forEach(action);
    }

    @Override
    public boolean add(final Processor e) {
        return this.processors.add(e);
    }

    @Override
    public void add(final int index, final Processor element) {
        this.processors.add(index, element);
    }

    @Override
    public boolean remove(final Object o) {
        return this.processors.remove(o);
    }

    @Override
    public Processor remove(final int index) {
        return this.processors.remove(index);
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return this.processors.containsAll(c);
    }

    @Override
    public boolean addAll(final Collection<? extends Processor> c) {
        return this.processors.addAll(c);
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends Processor> c) {
        return this.processors.addAll(index, c);
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        return this.processors.removeAll(c);
    }

    @Override
    public boolean removeIf(final Predicate<? super Processor> filter) {
        return this.processors.removeIf(filter);
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        return this.processors.retainAll(c);
    }

    @Override
    public void replaceAll(final UnaryOperator<Processor> operator) {
        this.processors.replaceAll(operator);
    }

    @Override
    public void sort(final Comparator<? super Processor> c) {
        this.processors.sort(c);
    }

    @Override
    public void clear() {
        this.processors.clear();
    }

    @Override
    public Processor get(final int index) {
        return this.processors.get(index);
    }

    @Override
    public Processor set(final int index, final Processor element) {
        return this.processors.set(index, element);
    }

    @Override
    public int indexOf(final Object o) {
        return this.processors.indexOf(o);
    }

    @Override
    public int lastIndexOf(final Object o) {
        return this.processors.lastIndexOf(o);
    }

    @Override
    public ListIterator<Processor> listIterator() {
        return this.processors.listIterator();
    }

    @Override
    public ListIterator<Processor> listIterator(final int index) {
        return this.processors.listIterator(index);
    }

    @Override
    public List<Processor> subList(final int fromIndex, final int toIndex) {
        return this.processors.subList(fromIndex, toIndex);
    }

    @Override
    public Spliterator<Processor> spliterator() {
        return this.processors.spliterator();
    }

    @Override
    public Stream<Processor> stream() {
        return this.processors.stream();
    }

    @Override
    public Stream<Processor> parallelStream() {
        return this.processors.parallelStream();
    }

}
