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

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RunnableTaskList {

    private final Queue<Runnable> tasks = new ConcurrentLinkedQueue<>();

    public void run() {
        Runnable task;
        while ((task = this.tasks.poll()) != null) {
            task.run();
        }
    }

    public boolean add(final Runnable task) {
        return this.tasks.add(task);
    }

    public boolean remove(final Runnable task) {
        return this.tasks.remove(task);
    }

    public boolean contains(final Runnable task) {
        return this.tasks.contains(task);
    }

    public boolean isEmpty() {
        return this.tasks.isEmpty();
    }

}
