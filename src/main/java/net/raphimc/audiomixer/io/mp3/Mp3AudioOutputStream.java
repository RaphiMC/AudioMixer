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

import de.sciss.jump3r.mp3.BitStream;
import de.sciss.jump3r.mp3.ID3Tag;
import de.sciss.jump3r.mp3.Lame;
import de.sciss.jump3r.mp3.LameGlobalFlags;
import de.sciss.jump3r.mp3.Presets;
import de.sciss.jump3r.mp3.Quantize;
import de.sciss.jump3r.mp3.QuantizePVT;
import de.sciss.jump3r.mp3.Reservoir;
import de.sciss.jump3r.mp3.Takehiro;
import de.sciss.jump3r.mp3.VBRTag;
import de.sciss.jump3r.mp3.VbrMode;
import de.sciss.jump3r.mp3.Version;
import net.raphimc.audiomixer.io.AudioOutputStream;
import net.raphimc.audiomixer.util.AudioFormat;
import net.raphimc.audiomixer.util.buffer.FloatRingBuffer;
import net.raphimc.audiomixer.util.io.seekable.SeekableOutputStream;
import net.raphimc.audiomixer.util.math.MathUtil;

import java.io.IOException;
import java.io.OutputStream;

public class Mp3AudioOutputStream extends AudioOutputStream {

    private final OutputStream outputStream;
    private final Lame lame;
    private final VBRTag vbrTag;
    private final ID3Tag id3Tag;
    private final LameGlobalFlags instance;
    private final FloatRingBuffer samplesBuffer;
    private final int[][] encodeInputBuffer;
    private final byte[] encodeOutputBuffer;
    private final long dataStartPosition;

    public Mp3AudioOutputStream(final OutputStream outputStream, final AudioFormat format) throws IOException {
        this(outputStream, format, 0.6F);
    }

    public Mp3AudioOutputStream(final OutputStream outputStream, final AudioFormat format, final float quality) throws IOException {
        this(outputStream, format, quality, VbrMode.vbr_off, null);
    }

    public Mp3AudioOutputStream(final OutputStream outputStream, final AudioFormat format, final float quality, final VbrMode vbrMode, final Id3Tags id3Tags) throws IOException {
        super(format);
        if (this.getFormat().channelCount() < 1 || this.getFormat().channelCount() > 2) {
            throw new IllegalArgumentException("Channel count must be 1 or 2: " + this.getFormat().channelCount());
        }
        if (!Float.isFinite(quality) || quality < 0F || quality > 1F) {
            throw new IllegalArgumentException("Quality must be finite and in [0, 1]: " + quality);
        }
        this.outputStream = outputStream;

        this.lame = new Lame();
        final Reservoir reservoir = new Reservoir();
        final Takehiro takehiro = new Takehiro();
        final BitStream bitStream = new BitStream();
        final Presets presets = new Presets();
        final QuantizePVT quantizePvt = new QuantizePVT();
        final Quantize quantize = new Quantize();
        this.vbrTag = new VBRTag();
        final Version version = new Version();
        this.id3Tag = id3Tags != null ? new ID3Tag() : null;
        reservoir.setModules(bitStream);
        takehiro.setModules(quantizePvt);
        bitStream.setModules(null, null, version, this.vbrTag);
        presets.setModules(this.lame);
        quantize.setModules(bitStream, reservoir, quantizePvt, takehiro);
        this.vbrTag.setModules(this.lame, bitStream, version);
        if (this.id3Tag != null) {
            this.id3Tag.setModules(bitStream, version);
        }
        this.lame.setModules(null, bitStream, presets, quantizePvt, quantize, this.vbrTag, version, this.id3Tag, null);
        quantizePvt.setModules(takehiro, reservoir, this.lame.enc.psy);

        this.instance = this.lame.lame_init();
        if (this.instance == null) {
            throw new IOException("Failed to create LAME instance");
        }
        this.instance.quality = 2;
        this.instance.num_channels = this.getFormat().channelCount();
        this.instance.in_samplerate = Math.round(this.getFormat().sampleRate());
        this.instance.write_id3tag_automatic = false;
        this.instance.bWriteVbrTag = this.outputStream instanceof SeekableOutputStream;
        this.instance.VBR = vbrMode;
        switch (this.instance.VBR) {
            case vbr_off -> this.instance.brate = Math.round(MathUtil.interpolateExponential(32F, 320F, quality));
            case vbr_abr -> this.instance.VBR_mean_bitrate_kbps = Math.round(MathUtil.interpolateExponential(32F, 320F, quality));
            case vbr_mt, vbr_rh, vbr_mtrh -> {
                final float vbrQuality = Math.min(((1F - quality) * 10F), 9.999F);
                this.instance.VBR_q = (int) vbrQuality;
                this.instance.VBR_q_frac = vbrQuality % 1;
            }
            default -> throw new IllegalArgumentException("Unsupported VBR mode: " + this.instance.VBR);
        }
        if (this.id3Tag != null) {
            this.id3Tag.id3tag_init(this.instance);
            this.id3Tag.id3tag_add_v2(this.instance);
        }
        if (id3Tags != null) {
            this.id3Tag.id3tag_set_title(this.instance, id3Tags.title());
            this.id3Tag.id3tag_set_artist(this.instance, id3Tags.artist());
            this.id3Tag.id3tag_set_album(this.instance, id3Tags.album());
            this.id3Tag.id3tag_set_year(this.instance, id3Tags.year());
            this.id3Tag.id3tag_set_comment(this.instance, id3Tags.comment());
            checkResult(this.id3Tag.id3tag_set_track(this.instance, id3Tags.track()), "Failed to set ID3 track");
            checkResult(this.id3Tag.id3tag_set_genre(this.instance, id3Tags.genre()), "Failed to set ID3 genre");
            if (id3Tags.albumArt() != null && !this.id3Tag.id3tag_set_albumart(this.instance, id3Tags.albumArt(), id3Tags.albumArt().length)) {
                throw new IOException("Failed to set ID3 album art");
            }
        }
        checkResult(this.lame.lame_init_params(this.instance), "Failed to set parameters");

        this.samplesBuffer = new FloatRingBuffer(this.instance.framesize * this.getFormat().channelCount());
        this.encodeInputBuffer = new int[2][this.instance.framesize];
        this.encodeOutputBuffer = new byte[MathUtil.ceil(1.25F * this.instance.framesize + 7200)];

        if (this.id3Tag != null) {
            final byte[] id3v2Tag = new byte[this.id3Tag.lame_get_id3v2_tag(this.instance, null, 0)];
            final int id3v2TagLength = checkResult(this.id3Tag.lame_get_id3v2_tag(this.instance, id3v2Tag, id3v2Tag.length), "Failed to get ID3v2 tag");
            this.outputStream.write(id3v2Tag, 0, id3v2TagLength);
        }
        this.dataStartPosition = this.outputStream instanceof SeekableOutputStream seekableOutputStream ? seekableOutputStream.position() : 0;
    }

    @Override
    public void write(final float sample) throws IOException {
        if (!Float.isFinite(sample) || sample < -1F || sample > 1F) {
            throw new IOException("Sample must be finite and in [-1, 1]");
        }
        this.samplesBuffer.write(sample);
        if (this.samplesBuffer.isFull()) {
            this.flushSamplesBuffer();
        }
    }

    private void flushSamplesBuffer() throws IOException {
        final int frameCount = this.getFormat().sampleCountToFrameCount(this.samplesBuffer.size());
        final int channelCount = this.getFormat().channelCount();
        for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
            for (int channelIndex = 0; channelIndex < channelCount; channelIndex++) {
                this.encodeInputBuffer[channelIndex][frameIndex] = Math.round(this.samplesBuffer.read() * Integer.MAX_VALUE);
            }
        }
        final int length = checkResult(this.lame.lame_encode_buffer_int(this.instance, this.encodeInputBuffer[0], this.encodeInputBuffer[1], frameCount, this.encodeOutputBuffer, 0, this.encodeOutputBuffer.length), "Failed to encode buffer");
        this.outputStream.write(this.encodeOutputBuffer, 0, length);
    }

    @Override
    public void flush() throws IOException {
        this.outputStream.flush();
    }

    @Override
    public void close() throws IOException {
        try (this.outputStream) {
            if (!this.samplesBuffer.isEmpty()) {
                this.flushSamplesBuffer();
            }
            final int length = checkResult(this.lame.lame_encode_flush(this.instance, this.encodeOutputBuffer, 0, this.encodeOutputBuffer.length), "Failed to flush encoder");
            this.outputStream.write(this.encodeOutputBuffer, 0, length);

            if (this.id3Tag != null) {
                final byte[] id3v1Tag = new byte[this.id3Tag.lame_get_id3v1_tag(this.instance, null, 0)];
                final int id3v1TagLength = checkResult(this.id3Tag.lame_get_id3v1_tag(this.instance, id3v1Tag, id3v1Tag.length), "Failed to get ID3v1 tag");
                this.outputStream.write(id3v1Tag, 0, id3v1TagLength);
            }
            if (this.outputStream instanceof SeekableOutputStream seekableOutputStream) {
                final byte[] lameTagFrame = new byte[this.vbrTag.getLameTagFrame(this.instance, new byte[0])];
                final int lameTagFrameLength = checkResult(this.vbrTag.getLameTagFrame(this.instance, lameTagFrame), "Failed to get LAME tag frame");
                final long previousPosition = seekableOutputStream.position();
                seekableOutputStream.seek(this.dataStartPosition);
                seekableOutputStream.write(lameTagFrame, 0, lameTagFrameLength);
                seekableOutputStream.seek(previousPosition);
            }
            checkResult(this.lame.lame_close(this.instance), "Failed to close LAME instance");
        }
    }

    private static int checkResult(final int result, final String message) throws IOException {
        if (result < 0) {
            throw new IOException(message + " (error code: " + result + ")");
        } else {
            return result;
        }
    }

    public record Id3Tags(String title, String artist, String album, String year, String comment, String track, String genre, byte[] albumArt) {

        public Id3Tags() {
            this(null, null, null, null, null, null, null, null);
        }

        public Id3Tags withTitle(final String title) {
            return new Id3Tags(title, this.artist, this.album, this.year, this.comment, this.track, this.genre, this.albumArt);
        }

        public Id3Tags withArtist(final String artist) {
            return new Id3Tags(this.title, artist, this.album, this.year, this.comment, this.track, this.genre, this.albumArt);
        }

        public Id3Tags withAlbum(final String album) {
            return new Id3Tags(this.title, this.artist, album, this.year, this.comment, this.track, this.genre, this.albumArt);
        }

        public Id3Tags withYear(final String year) {
            return new Id3Tags(this.title, this.artist, this.album, year, this.comment, this.track, this.genre, this.albumArt);
        }

        public Id3Tags withComment(final String comment) {
            return new Id3Tags(this.title, this.artist, this.album, this.year, comment, this.track, this.genre, this.albumArt);
        }

        public Id3Tags withTrack(final String track) {
            return new Id3Tags(this.title, this.artist, this.album, this.year, this.comment, track, this.genre, this.albumArt);
        }

        public Id3Tags withGenre(final String genre) {
            return new Id3Tags(this.title, this.artist, this.album, this.year, this.comment, this.track, genre, this.albumArt);
        }

        public Id3Tags withAlbumArt(final byte[] albumArt) {
            return new Id3Tags(this.title, this.artist, this.album, this.year, this.comment, this.track, this.genre, albumArt);
        }

    }

}
