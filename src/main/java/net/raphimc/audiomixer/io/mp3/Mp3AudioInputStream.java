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
package net.raphimc.audiomixer.io.mp3;

import de.sciss.jump3r.mp3.Encoder;
import de.sciss.jump3r.mp3.VBRTag;
import de.sciss.jump3r.mp3.VBRTagData;
import de.sciss.jump3r.mpg.Common;
import de.sciss.jump3r.mpg.Interface;
import de.sciss.jump3r.mpg.MPGLib;
import net.raphimc.audiomixer.io.AudioInputStream;
import net.raphimc.audiomixer.util.AudioFormat;
import net.raphimc.audiomixer.util.buffer.FloatRingBuffer;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class Mp3AudioInputStream extends AudioInputStream {

    private static final int MAX_FRAME_COUNT = 1152;
    private static final Mp3InputStream.Frame FLUSH_FRAME = new Mp3InputStream.Frame(null, 0, 0, null, null, 0, 0, null);

    private final Mp3InputStream mp3InputStream;
    private final MPGLib mpgLib;
    private final MPGLib.mpstr_tag instance;
    private final float[][] decodeOutputBuffer;
    private final FloatRingBuffer samplesBuffer;
    private final int paddingSampleCount;
    private int remainingDelaySampleCount;

    public Mp3AudioInputStream(final InputStream inputStream) throws IOException {
        this(new CodeBeforeSuper(inputStream));
    }

    private Mp3AudioInputStream(final CodeBeforeSuper codeBeforeSuper) throws IOException {
        super(new AudioFormat(codeBeforeSuper.firstFrame.sampleRate(), codeBeforeSuper.firstFrame.channelMode().channelCount()));
        this.mp3InputStream = codeBeforeSuper.mp3InputStream;

        this.mpgLib = new MPGLib();
        final Interface mpgInterface = new Interface();
        final VBRTag vbrTag = new VBRTag();
        final Common common = new Common();
        mpgInterface.setModules(vbrTag, common);
        this.mpgLib.setModules(mpgInterface, common);

        this.instance = this.mpgLib.hip_decode_init();
        if (this.instance == null) {
            throw new IOException("Failed to create HIP instance");
        }
        this.decodeOutputBuffer = new float[2][MAX_FRAME_COUNT];
        final VBRTagData vbrTagData = vbrTag.getVbrTag(codeBeforeSuper.firstFrame.data());
        this.paddingSampleCount = vbrTagData != null && vbrTagData.encPadding >= 0 ? Math.max(Math.multiplyExact(Math.subtractExact(vbrTagData.encPadding, Encoder.DECDELAY + 1), this.getFormat().channelCount()), 0) : 0;
        this.remainingDelaySampleCount = vbrTagData != null && vbrTagData.encDelay >= 0 ? Math.multiplyExact(Math.addExact(vbrTagData.encDelay, Encoder.DECDELAY + 1), this.getFormat().channelCount()) : 0;
        this.samplesBuffer = new FloatRingBuffer(Math.addExact(MAX_FRAME_COUNT * this.getFormat().channelCount(), this.paddingSampleCount));

        this.decodeFrame(codeBeforeSuper.firstFrame);
    }

    @Override
    public float read() throws IOException {
        while (this.samplesBuffer.size() <= this.paddingSampleCount) {
            this.decodeNextFrame();
            if (this.remainingDelaySampleCount > 0) {
                this.remainingDelaySampleCount -= this.samplesBuffer.skip(this.remainingDelaySampleCount);
            }
        }
        return this.samplesBuffer.read();
    }

    private void decodeNextFrame() throws IOException {
        final Mp3InputStream.Frame frame = this.mp3InputStream.readFrame();
        if (frame != null) {
            if (frame.sampleRate() != this.getFormat().sampleRate() || frame.channelMode().channelCount() != this.getFormat().channelCount()) {
                throw new IOException("Malformed MP3 stream: Audio format changed mid-stream (expected: " + this.getFormat().sampleRate() + " Hz, " + this.getFormat().channelCount() + " channels; got: " + frame.sampleRate() + " Hz, " + frame.channelMode().channelCount() + " channels)");
            }
            this.decodeFrame(frame);
        } else {
            this.decodeFrame(FLUSH_FRAME);
            if (this.samplesBuffer.size() <= this.paddingSampleCount) {
                throw new EOFException();
            }
        }
    }

    private void decodeFrame(final Mp3InputStream.Frame frame) throws IOException {
        final int frameCount = checkResult(this.mpgLib.hip_decode1_unclipped(this.instance, frame.data(), frame.offset(), frame.length(), this.decodeOutputBuffer[0], this.decodeOutputBuffer[1]), "Failed to decode MP3 frame");
        final int channelCount = this.getFormat().channelCount();
        for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
            for (int channelIndex = 0; channelIndex < channelCount; channelIndex++) {
                this.samplesBuffer.write(this.decodeOutputBuffer[channelIndex][frameIndex] / Short.MAX_VALUE);
            }
        }
    }

    @Override
    public void close() throws IOException {
        try (this.mp3InputStream) {
            checkResult(this.mpgLib.hip_decode_exit(this.instance), "Failed to close HIP instance");
        }
    }

    private static int checkResult(final int result, final String message) throws IOException {
        if (result < 0) {
            throw new IOException(message + " (error code: " + result + ")");
        } else {
            return result;
        }
    }

    private static final class CodeBeforeSuper {

        private final Mp3InputStream mp3InputStream;
        private final Mp3InputStream.Frame firstFrame;

        private CodeBeforeSuper(final InputStream inputStream) throws IOException {
            this.mp3InputStream = new Mp3InputStream(inputStream);
            this.firstFrame = this.mp3InputStream.readFrameOrThrow();
        }

    }

}
