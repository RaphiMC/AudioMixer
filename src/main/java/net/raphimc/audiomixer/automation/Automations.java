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
package net.raphimc.audiomixer.automation;

import net.raphimc.audiomixer.automation.finite.FiniteAutomation;
import net.raphimc.audiomixer.util.buffer.AudioBuffer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Automations implements Collection<Automation> {

    private final Collection<Automation> automations = Collections.synchronizedList(new ArrayList<>(0));

    public void process(final AudioBuffer buffer) {
        if (!this.isEmpty()) {
            this.forEach(automation -> automation.advance(buffer.millisecondLength()));
            this.removeIf(automation -> automation instanceof FiniteAutomation finiteAutomation && finiteAutomation.isFinished());
        }
    }

    @Override
    public int size() {
        return this.automations.size();
    }

    @Override
    public boolean isEmpty() {
        return this.automations.isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
        return this.automations.contains(o);
    }

    @Override
    public Iterator<Automation> iterator() {
        return this.automations.iterator();
    }

    @Override
    public Object[] toArray() {
        return this.automations.toArray();
    }

    @Override
    public <T> T[] toArray(final T[] a) {
        return this.automations.toArray(a);
    }

    @Override
    public <T> T[] toArray(final IntFunction<T[]> generator) {
        return this.automations.toArray(generator);
    }

    @Override
    public void forEach(final Consumer<? super Automation> action) {
        this.automations.forEach(action);
    }

    @Override
    public boolean add(final Automation e) {
        return this.automations.add(e);
    }

    @Override
    public boolean remove(final Object o) {
        return this.automations.remove(o);
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return this.automations.containsAll(c);
    }

    @Override
    public boolean addAll(final Collection<? extends Automation> c) {
        return this.automations.addAll(c);
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        return this.automations.removeAll(c);
    }

    @Override
    public boolean removeIf(final Predicate<? super Automation> filter) {
        return this.automations.removeIf(filter);
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        return this.automations.retainAll(c);
    }

    @Override
    public void clear() {
        this.automations.clear();
    }

    @Override
    public Spliterator<Automation> spliterator() {
        return this.automations.spliterator();
    }

    @Override
    public Stream<Automation> stream() {
        return this.automations.stream();
    }

    @Override
    public Stream<Automation> parallelStream() {
        return this.automations.parallelStream();
    }

}
