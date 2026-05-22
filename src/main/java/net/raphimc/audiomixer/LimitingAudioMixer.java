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

import net.raphimc.audiomixer.dsp.parameter.FloatParameter;
import net.raphimc.audiomixer.dsp.processor.dynamics.GainProcessor;
import net.raphimc.audiomixer.dsp.processor.dynamics.LimiterProcessor;
import net.raphimc.audiomixer.dsp.processor.effect.HardClipProcessor;
import net.raphimc.audiomixer.util.FloatAudioFormat;

public class LimitingAudioMixer extends AudioMixer {

    private final LimiterProcessor limiterProcessor = new LimiterProcessor();
    private final GainProcessor gainProcessor = new GainProcessor(1F);

    public LimitingAudioMixer(final FloatAudioFormat audioFormat) {
        super(audioFormat);
        this.getProcessors().add(this.limiterProcessor);
        this.getProcessors().add(this.gainProcessor);
        this.getProcessors().add(new HardClipProcessor());
    }

    public FloatParameter gain() {
        return this.gainProcessor.gain();
    }

    public FloatParameter gainDb() {
        return this.gainProcessor.gainDb();
    }

    public LimiterProcessor getLimiterProcessor() {
        return this.limiterProcessor;
    }

    public GainProcessor getGainProcessor() {
        return this.gainProcessor;
    }

}
