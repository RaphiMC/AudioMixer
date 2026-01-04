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
package net.raphimc.audiomixer;

import net.raphimc.audiomixer.util.PcmFloatAudioFormat;
import net.raphimc.audiomixer.util.SourceDataLineWriter;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class SourceDataLineAudioMixer extends NormalizedAudioMixer implements AutoCloseable {

    private final SourceDataLineWriter sourceDataLineWriter;

    public SourceDataLineAudioMixer(final SourceDataLine sourceDataLine) throws LineUnavailableException {
        this(sourceDataLine, 50);
    }

    public SourceDataLineAudioMixer(final SourceDataLine sourceDataLine, final int bufferMillis) throws LineUnavailableException {
        this(sourceDataLine, bufferMillis, 10);
    }

    public SourceDataLineAudioMixer(final SourceDataLine sourceDataLine, final int bufferMillis, final int mixSliceMillis) throws LineUnavailableException {
        super(new PcmFloatAudioFormat(sourceDataLine.getFormat()));
        this.sourceDataLineWriter = new SourceDataLineWriter(sourceDataLine, bufferMillis, () -> this.renderMillis(mixSliceMillis));
        this.sourceDataLineWriter.start();
    }

    @Override
    public void close() {
        this.sourceDataLineWriter.close();
    }

    public SourceDataLineWriter getSourceDataLineWriter() {
        return this.sourceDataLineWriter;
    }

}
