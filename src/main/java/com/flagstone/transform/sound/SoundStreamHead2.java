/*
 * SoundStreamHead2.java
 * Transform
 *
 * Copyright (c) 2001-2010 Flagstone Software Ltd. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  * Neither the name of Flagstone Software Ltd. nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.flagstone.transform.sound;


import java.io.IOException;

import com.flagstone.transform.Constants;
import com.flagstone.transform.MovieTag;
import com.flagstone.transform.MovieTypes;
import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.exception.IllegalArgumentRangeException;
import com.flagstone.transform.exception.IllegalArgumentValueException;

/**
 * SoundStreamHead2 defines the format of a streaming sound, identifying the
 * encoding scheme, the rate at which the sound will be played and the size of
 * the decoded samples.
 *
 * <p>
 * Sounds may be either mono or stereo and encoded using either NATIVE_PCM,
 * ADPCM, MP3 or NELLYMOSER or SPEEX formats and have sampling rates of 5512,
 * 11025, 22050 or 44100 Hertz.
 * </p>
 *
 * <p>
 * The actual sound is streamed used the SoundStreamBlock class which contains
 * the data for each frame in a movie.
 * </p>
 *
 * <p>
 * When a stream sound is played if the Flash Player cannot render the frames
 * fast enough to maintain synchronisation with the sound being played then
 * frames will be skipped. Normally the player will reduce the frame rate so
 * every frame of a movie is played. The different sets of attributes that
 * identify how the sound will be played compared to the way it was encoded
 * allows the Player more control over how the animation is rendered. Reducing
 * the resolution or playback rate can improve synchronisation with the frames
 * displayed.
 * </p>
 *
 * <p>
 * SoundStreamHead2 allows way the sound is played to differ from the way it is
 * encoded and streamed to the player. This allows the Player more control over
 * how the animation is rendered. Reducing the resolution or playback rate can
 * improve synchronisation with the frames displayed.
 * </p>
 *
 * @see SoundStreamBlock
 * @see SoundStreamHead
 */
public class SoundStreamHead2 implements MovieTag {

    /** Format string used in toString() method. */
    private static final String FORMAT = "SoundStreamHead2: { format=%s;"
            + " playbackRate=%d; playbackChannels=%d; playbackSampleSize=%d;"
            + " streamRate=%d; streamChannels=%d; streamSampleSize=%d;"
            + " streamSampleCount=%d; latency=%d}";

    /** The code representing the sound format. */
    private int format;
    /** The playback rate in KHz. */
    private int playRate;
    /** The number of playback channels: 1 = mono, 2 = stereo. */
    private int playChannels;
    /** The number of bits in each sample. */
    private int playSampleSize;
    /** The sound rate in KHz of the stream. */
    private int streamRate;
    /** The number of channels in the stream: 1 = mono, 2 = stereo. */
    private int streamChannels;
    /** The number of bits in each stream sample. */
    private int streamSampleSize;
    /** The number of samples in the stream. */
    private int streamSampleCount;
    /** The latency for MP3 sounds. */
    private int latency;

    /** The length of the object, minus the header, when it is encoded. */
    private transient int length;

    /**
     * The following variable is used to preserve the value of a reserved field
     * when decoding then encoding an existing Flash file. Macromedia's file
     * file format specification states that this field is always zero - it is
     * not, so this is used to preserve the value in case it is implementing an
     * undocumented feature.
     */
    private transient int reserved = 0;

    /**
     * Creates and initialises a SoundStreamHead2 object using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public SoundStreamHead2(final SWFDecoder coder) throws IOException {
        length = coder.readUnsignedShort() & Coder.LENGTH_FIELD;
        if (length == Coder.IS_EXTENDED) {
            length = coder.readInt();
        }
        coder.mark();
        int info = coder.readByte();
        reserved = (info & Coder.NIB1) >> Coder.TO_LOWER_NIB;
        playRate = readRate(info & Coder.PAIR1);
        playSampleSize = ((info & Coder.BIT1) >> 1) + 1;
        playChannels = (info & Coder.BIT0) + 1;

        info = coder.readByte();
        format = (info & Coder.NIB1) >> Coder.TO_LOWER_NIB;
        streamRate = readRate(info & Coder.PAIR1);
        streamSampleSize = ((info & Coder.BIT1) >> 1) + 1;
        streamChannels = (info & Coder.BIT0) + 1;
        streamSampleCount = coder.readUnsignedShort();

        // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 1 LINES
        if ((length == 6) && (format == 2)) {
            latency = coder.readSignedShort();
        }
        coder.check(length);
        coder.unmark();
    }

    /**
     * Creates a SoundStreamHead2 object specifying all the parameters required
     * to define the sound.
     *
     * @param encoding
     *            the compression format for the sound data, either
     *            DefineSound.NATIVE_PCM, DefineSound.ADPCM, DefineSound.MP3,
     *            DefineSound.PCM or DefineSound.NELLYMOSER (Flash 6+ only).
     * @param playbackRate
     *            the recommended rate for playing the sound, either 5512,
     *            11025, 22050 or 44100 Hz.
     * @param playbackChannels
     *            The recommended number of playback channels: 1 = mono or 2 =
     *            stereo.
     * @param playSize
     *            the recommended uncompressed sample size for playing the
     *            sound, either 1 or 2 bytes.
     * @param streamingRate
     *            the rate at which the sound was sampled, either 5512, 11025,
     *            22050 or 44100 Hz.
     * @param streamingChannels
     *            the number of channels: 1 = mono or 2 = stereo.
     * @param streamingSize
     *            the sample size for the sound, either 1 or 2 bytes.
     * @param streamingCount
     *            the number of samples in each subsequent SoundStreamBlock
     *            object.
     */
    public SoundStreamHead2(final SoundFormat encoding, final int playbackRate,
            final int playbackChannels, final int playSize,
            final int streamingRate, final int streamingChannels,
            final int streamingSize, final int streamingCount) {
        setFormat(encoding);
        setPlayRate(playbackRate);
        setPlayChannels(playbackChannels);
        setPlaySampleSize(playSize);
        setStreamRate(streamingRate);
        setStreamChannels(streamingChannels);
        setStreamSampleSize(streamingSize);
        setStreamSampleCount(streamingCount);
    }

    /**
     * Creates and initialises a SoundStreamHead2 object using the values copied
     * from another SoundStreamHead2 object.
     *
     * @param object
     *            a SoundStreamHead2 object from which the values will be
     *            copied.
     */
    public SoundStreamHead2(final SoundStreamHead2 object) {
        format = object.format;
        playRate = object.playRate;
        playChannels = object.playChannels;
        playSampleSize = object.playSampleSize;
        streamRate = object.streamRate;
        streamChannels = object.streamChannels;
        streamSampleSize = object.streamSampleSize;
        streamSampleCount = object.streamSampleCount;
        latency = object.latency;
    }

    /**
     * Get the compression format used.
     *
     * @return the format for the sound data.
     */
    public SoundFormat getFormat() {
        return SoundFormat.fromInt(format);
    }

    /**
     * Sets the format for the streaming sound.
     *
     * @param encoding
     *            the compression format for the sound data, must be either
     *            NATIVE_PCM, ADPCM, MP3, PCM or NELLYMOSER from SoundFormat.
     */
    public void setFormat(final SoundFormat encoding) {
        format = encoding.getValue();
    }

    /**
     * Returns the recommended playback rate: 5512, 11025, 22050 or 44100 Hz.
     *
     * @return the playback rate in Hertz.
     */
    public int getPlayRate() {
        return playRate;
    }

    /**
     * Get the recommended number of playback channels = 1 = mono 2 =
     * stereo.
     *
     * @return the number of channels.
     */
    public int getPlayChannels() {
        return playChannels;
    }

    /**
     * Get the recommended playback sample range in bytes: 1 or 2.
     *
     * @return the number of bytes in each sample.
     */
    public int getPlaySampleSize() {
        return playSampleSize;
    }

    /**
     * Get the sample rate: 5512, 11025, 22050 or 44100 Hz in the streaming
     * sound.
     *
     * @return the stream rate in Hertz.
     */
    public float getStreamRate() {
        return streamRate;
    }

    /**
     * Get the number of channels, 1 = mono 2 = stereo, in the streaming
     * sound.
     *
     * @return the number of channels defined in the streaming sound.
     */
    public int getStreamChannels() {
        return streamChannels;
    }

    /**
     * Get the sample size in bytes: 1 or 2 in the streaming sound.
     *
     * @return the number of bytes per sample in the streaming sound.
     */
    public int getStreamSampleSize() {
        return streamSampleSize;
    }

    /**
     * Get the average number of samples in each stream block following.
     *
     * @return the number of sample in each stream block.
     */
    public int getStreamSampleCount() {
        return streamSampleCount;
    }

    /**
     * Sets the recommended playback rate in Hz. Must be either: 5512, 11025,
     * 22050 or 44100.
     *
     * @param rate
     *            the recommended rate for playing the sound.
     */
    public void setPlayRate(final int rate) {
        if ((rate != SoundRate.KHZ_5K) && (rate != SoundRate.KHZ_11K)
                && (rate != SoundRate.KHZ_22K) && (rate != SoundRate.KHZ_44K)) {
            throw new IllegalArgumentValueException(
                    new int[] {SoundRate.KHZ_5K, SoundRate.KHZ_11K,
                            SoundRate.KHZ_22K, SoundRate.KHZ_44K}, rate);
        }
        playRate = rate;
    }

    /**
     * Sets the recommended number of playback channels = 1 = mono 2 = stereo.
     *
     * @param channels
     *            the recommended number of playback channels.
     */
    public void setPlayChannels(final int channels) {
        if ((channels < 1) || (channels > 2)) {
            throw new IllegalArgumentRangeException(1, 2, channels);
        }
        playChannels = channels;
    }

    /**
     * Sets the recommended playback sample size in bytes. Must be wither 1 or
     * 2.
     *
     * @param playSize
     *            the recommended sample size for playing the sound.
     */
    public void setPlaySampleSize(final int playSize) {
        if ((playSize < 1) || (playSize > 2)) {
            throw new IllegalArgumentRangeException(1, 2, playSize);
        }
        playSampleSize = playSize;
    }

    /**
     * Sets the sample rate in Hz for the streaming sound. Must be either: 5512,
     * 11025, 22050 or 44100.
     *
     * @param rate
     *            the rate at which the streaming sound was sampled.
     */
    public void setStreamRate(final int rate) {
        if ((rate != SoundRate.KHZ_5K) && (rate != SoundRate.KHZ_11K)
                && (rate != SoundRate.KHZ_22K) && (rate != SoundRate.KHZ_44K)) {
            throw new IllegalArgumentValueException(
                    new int[] {SoundRate.KHZ_5K, SoundRate.KHZ_11K,
                            SoundRate.KHZ_22K, SoundRate.KHZ_44K}, rate);
        }
        streamRate = rate;
    }

    /**
     * Sets the number of channels in the streaming sound: 1 = mono 2 = stereo.
     *
     * @param channels
     *            the number of channels in the streaming sound.
     */
    public void setStreamChannels(final int channels) {
        if ((channels < 1) || (channels > 2)) {
            throw new IllegalArgumentRangeException(1, 2, channels);
        }
        streamChannels = channels;
    }

    /**
     * Sets the sample size in bytes for the streaming sound. Must be 1 or 2.
     *
     * @param size
     *            the sample size for the sound.
     */
    public void setStreamSampleSize(final int size) {
        if ((size < 1) || (size > 2)) {
            throw new IllegalArgumentRangeException(1, 2, size);
        }
        streamSampleSize = size;
    }

    /**
     * Sets the number of samples in each stream block.
     *
     * @param count
     *            the number of samples in each subsequent SoundStreamBlock
     *            object.
     */
    public void setStreamSampleCount(final int count) {
        if (count < 0) {
            throw new IllegalArgumentRangeException(0,
                    Integer.MAX_VALUE, count);
        }
        streamSampleCount = count;
    }

    /**
     * For MP3 encoded sounds, returns the number of samples to skip when
     * starting to play a sound.
     *
     * @return the number of samples skipped in an MP3 encoded sound Returns 0
     *         for other sound formats.
     */
    public int getLatency() {
        return latency;
    }

    /**
     * Set the number of samples to skip when starting to play an MP3 encoded
     * sound.
     *
     * @param delay
     *            the number of samples to be skipped in an MP3 encoded sound
     *            should be 0 for other sound formats.
     */
    public void setLatency(final int delay) {
        latency = delay;
    }

    /** {@inheritDoc} */
    @Override
	public SoundStreamHead2 copy() {
        return new SoundStreamHead2(this);
    }

    @Override
    public String toString() {
        return String.format(FORMAT, format, playRate, playChannels,
                playSampleSize, streamRate, streamChannels, streamSampleSize,
                streamSampleCount, latency);
    }

    /** {@inheritDoc} */
    @Override
	public int prepareToEncode(final Context context) {
        // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 1 LINES
        length = 4;

        if ((format == 2) && (latency > 0)) {
            length += 2;
        }
        return (length > Coder.HEADER_LIMIT ? Coder.LONG_HEADER
                : Coder.SHORT_HEADER) + length;
    }

    /** {@inheritDoc} */
    @Override
	public void encode(final SWFEncoder coder, final Context context)
            throws IOException {

        if (length > Coder.HEADER_LIMIT) {
            coder.writeShort((MovieTypes.SOUND_STREAM_HEAD_2
                    << Coder.LENGTH_FIELD_SIZE) | Coder.IS_EXTENDED);
            coder.writeInt(length);
        } else {
            coder.writeShort((MovieTypes.SOUND_STREAM_HEAD_2
                    << Coder.LENGTH_FIELD_SIZE) | length);
        }
        if (Constants.DEBUG) {
            coder.mark();
        }
        int bits = reserved << Coder.TO_UPPER_NIB;
        bits |= writeRate(playRate);
        bits |= (playSampleSize - 1) << 1;
        bits |= playChannels - 1;
        coder.writeByte(bits);

        bits = format << Coder.TO_UPPER_NIB;
        bits |= writeRate(streamRate);
        bits |= (streamSampleSize - 1) << 1;
        bits |= streamChannels - 1;
        coder.writeByte(bits);
        coder.writeShort(streamSampleCount);

        if ((format == 2) && (latency > 0)) {
            coder.writeShort(latency);
        }
        if (Constants.DEBUG) {
            coder.check(length);
            coder.unmark();
        }
    }

    /**
     * Convert the code representing the rate into actual KHz.
     * @param value the code representing the sound rate.
     * @return the actual rate in KHz.
     */
    private int readRate(final int value) {
        int rate;
        switch (value) {
        case 0:
            rate = SoundRate.KHZ_5K;
            break;
        case Coder.BIT2:
            rate = SoundRate.KHZ_11K;
            break;
        case Coder.BIT3:
            rate = SoundRate.KHZ_22K;
            break;
        case Coder.BIT2 | Coder.BIT3:
            rate = SoundRate.KHZ_44K;
            break;
        default:
            rate = 0;
            break;
        }
        return rate;
    }

    /**
     * Convert the rate in KHz to the code that represents the rate.
     * @param rate the rate in KHz.
     * @return the code representing the sound rate.
     */
    private int writeRate(final int rate) {
        int value;
        switch (rate) {
        case SoundRate.KHZ_11K:
            value = Coder.BIT2;
            break;
        case SoundRate.KHZ_22K:
            value = Coder.BIT3;
            break;
        case SoundRate.KHZ_44K:
            value = Coder.BIT2 | Coder.BIT3;
            break;
        default:
            value = 0;
            break;
        }
        return value;
    }
}
