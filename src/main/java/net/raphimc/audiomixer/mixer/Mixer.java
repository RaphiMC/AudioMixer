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
package net.raphimc.audiomixer.mixer;

import net.raphimc.audiomixer.source.Source;
import net.raphimc.audiomixer.util.buffer.AudioBuffer;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Mixer extends Source implements Collection<Source> {

    private final List<Source> sources = Collections.synchronizedList(new ArrayList<>());
    private int mixedSourceCount;
    private float processingLoad;

    public void limitSourceCount(final int maxSourceCount) {
        synchronized (this.sources) {
            if (this.size() > maxSourceCount) {
                this.sources.subList(0, this.size() - maxSourceCount).clear();
            }
        }
    }

    @Override
    public void render(final AudioBuffer buffer) {
        final long startTime = System.nanoTime();
        super.render(buffer);
        final float neededMillis = (System.nanoTime() - startTime) / 1_000_000F;
        final float availableMillis = buffer.getMillisecondLength();
        this.processingLoad = (neededMillis / availableMillis) * 100F;
    }

    @Override
    protected void renderInternal(final AudioBuffer buffer) {
        this.mixedSourceCount = this.size();
        this.mix(buffer);
        this.removeIf(Source::isFinished);
    }

    protected void mix(final AudioBuffer buffer) {
        final AudioBuffer workBuffer = buffer.createWorkBuffer();
        this.forEach(source -> {
            workBuffer.clear();
            source.render(workBuffer);
            buffer.add(workBuffer);
        });
    }

    public int getMixedSourceCount() {
        return this.mixedSourceCount;
    }

    public float getProcessingLoad() {
        return this.processingLoad;
    }

    @Override
    public int size() {
        return this.sources.size();
    }

    @Override
    public boolean isEmpty() {
        return this.sources.isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
        return this.sources.contains(o);
    }

    @Override
    public Iterator<Source> iterator() {
        return this.sources.iterator();
    }

    @Override
    public Object[] toArray() {
        return this.sources.toArray();
    }

    @Override
    public <T> T[] toArray(final T[] a) {
        return this.sources.toArray(a);
    }

    @Override
    public <T> T[] toArray(final IntFunction<T[]> generator) {
        return this.sources.toArray(generator);
    }

    @Override
    public void forEach(final Consumer<? super Source> action) {
        this.sources.forEach(action);
    }

    @Override
    public boolean add(final Source e) {
        return this.sources.add(e);
    }

    @Override
    public boolean remove(final Object o) {
        return this.sources.remove(o);
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return this.sources.containsAll(c);
    }

    @Override
    public boolean addAll(final Collection<? extends Source> c) {
        return this.sources.addAll(c);
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        return this.sources.removeAll(c);
    }

    @Override
    public boolean removeIf(final Predicate<? super Source> filter) {
        return this.sources.removeIf(filter);
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        return this.sources.retainAll(c);
    }

    @Override
    public void clear() {
        this.sources.clear();
    }

    @Override
    public Spliterator<Source> spliterator() {
        return this.sources.spliterator();
    }

    @Override
    public Stream<Source> stream() {
        return this.sources.stream();
    }

    @Override
    public Stream<Source> parallelStream() {
        return this.sources.parallelStream();
    }

}
