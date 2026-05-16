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
package net.raphimc.audiomixer.dsp.processor;

import net.raphimc.audiomixer.util.buffer.AudioBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Processors implements Processor {

    private final List<Processor> processors = new ArrayList<>(2);

    @Override
    public synchronized void process(final AudioBuffer buffer) {
        for (Processor processor : this.processors) {
            processor.process(buffer);
        }
    }

    public synchronized List<Processor> getAll(final Predicate<Processor> predicate) {
        return this.processors.stream().filter(predicate).toList();
    }

    public synchronized Processor getFirst(final Predicate<Processor> predicate) {
        for (Processor processor : this.processors) {
            if (predicate.test(processor)) {
                return processor;
            }
        }
        return null;
    }

    public synchronized <T extends Processor> T getFirst(final Class<T> clazz) {
        for (Processor processor : this.processors) {
            if (processor.getClass().equals(clazz)) {
                return (T) processor;
            }
        }
        return null;
    }

    public synchronized void add(final Processor processor) {
        this.processors.add(processor);
    }

    public synchronized void addFirst(final Processor processor) {
        this.processors.add(0, processor);
    }

    public synchronized void remove(final Processor processor) {
        this.processors.remove(processor);
    }

    public synchronized <T extends Processor> void removeAll(final Class<T> clazz) {
        this.processors.removeIf(m -> m.getClass().equals(clazz));
    }

    public synchronized void clear() {
        this.processors.clear();
    }

}
