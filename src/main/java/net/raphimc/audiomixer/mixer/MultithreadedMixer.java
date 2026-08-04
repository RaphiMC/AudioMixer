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

public class MultithreadedMixer extends Mixer {

    @Override
    protected void mix(final AudioBuffer buffer) {
        this.parallelStream().collect(() -> new Accumulator(buffer), Accumulator::accept, Accumulator::combine).finish();
    }

    private static class Accumulator {

        private final AudioBuffer finalBuffer;
        private final AudioBuffer mix;
        private final AudioBuffer scratch;

        private Accumulator(final AudioBuffer finalBuffer) {
            this.finalBuffer = finalBuffer;
            this.mix = finalBuffer.createWorkBuffer();
            this.scratch = finalBuffer.createWorkBuffer();
        }

        private void accept(final Source source) {
            this.scratch.clear();
            source.render(this.scratch);
            this.mix.add(this.scratch);
        }

        private void combine(final Accumulator other) {
            this.mix.add(other.mix);
        }

        private void finish() {
            this.finalBuffer.add(this.mix);
        }

    }

}
