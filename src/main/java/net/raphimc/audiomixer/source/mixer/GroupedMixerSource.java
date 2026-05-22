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
package net.raphimc.audiomixer.source.mixer;

import net.raphimc.audiomixer.source.Source;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class GroupedMixerSource extends MixerSource {

    private final MixerSource[] groups;

    private int currentGroup;

    public GroupedMixerSource(final int groups) {
        this(groups, 1);
    }

    public GroupedMixerSource(final int groups, final int maxSourcesPerGroup) {
        super(groups);
        if (groups < 1) {
            throw new IllegalArgumentException("Groups must be >= 1");
        }

        this.groups = new MixerSource[groups];
        for (int i = 0; i < this.groups.length; i++) {
            this.groups[i] = new MixerSource();
            super.add(this.groups[i]);
        }
        this.setMaxSources(groups * maxSourcesPerGroup);
    }

    @Override
    public synchronized void add(final Source source) {
        this.groups[this.currentGroup].add(source);
        this.currentGroup = (this.currentGroup + 1) % this.groups.length;
    }

    @Override
    public void remove(final Source source) {
        for (MixerSource mixer : this.groups) {
            mixer.remove(source);
        }
    }

    @Override
    public void clear() {
        for (MixerSource mixer : this.groups) {
            mixer.clear();
        }
    }

    @Override
    public void forEach(final Consumer<Source> action) {
        for (MixerSource mixer : this.groups) {
            mixer.forEach(action);
        }
    }

    @Override
    public void removeIf(final Predicate<Source> predicate) {
        for (MixerSource mixer : this.groups) {
            mixer.removeIf(predicate);
        }
    }

    public MixerSource getGroup(final int group) {
        if (group < 0 || group >= this.groups.length) {
            throw new IllegalArgumentException("Group must be in [0, group count)");
        }
        return this.groups[group];
    }

    @Override
    public int getMaxSources() {
        int maxSources = 0;
        for (MixerSource mixer : this.groups) {
            maxSources += mixer.getMaxSources();
        }
        return maxSources;
    }

    @Override
    public void setMaxSources(final int maxSources) {
        if (this.groups != null) {
            for (MixerSource mixer : this.groups) {
                mixer.setMaxSources((int) Math.ceil((double) maxSources / this.groups.length));
            }
        } else { // Called from super constructor
            super.setMaxSources(maxSources);
        }
    }

    @Override
    public int getActiveSources() {
        int activeSources = 0;
        for (MixerSource mixer : this.groups) {
            activeSources += mixer.getActiveSources();
        }
        return activeSources;
    }

    @Override
    public int getMixedSources() {
        int mixedSources = 0;
        for (MixerSource mixer : this.groups) {
            mixedSources += mixer.getMixedSources();
        }
        return mixedSources;
    }

    public int getGroupCount() {
        return this.groups.length;
    }

}
