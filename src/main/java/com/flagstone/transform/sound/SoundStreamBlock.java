/*
 * SoundStreamBlock.java
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
import java.util.Arrays;

import com.flagstone.transform.Constants;
import com.flagstone.transform.MovieTag;
import com.flagstone.transform.MovieTypes;
import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;

/**
 * SoundStreamBlock contains the sound data being streamed to the Flash Player.
 *
 * <p>
 * Streaming sounds are played in tight synchronisation with one
 * SoundStreamBlock object defining the sound for each frame displayed in a
 * movie. When a streaming sound is played if the Flash Player cannot render the
 * frames fast enough to maintain synchronisation with the sound being played
 * then frames will be skipped. Normally the player will reduce the frame rate
 * so every frame of a movie is played.
 * </p>
 *
 * @see SoundStreamHead
 * @see SoundStreamHead2
 */
public class SoundStreamBlock implements MovieTag {

    /** Format string used in toString() method. */
    private static final String FORMAT = "SoundStreamBlock: {"
                + "sound=byte<%d> ...}";

    /** Encoded sound data. */
    private byte[] sound;

    /** The length of the object, minus the header, when it is encoded. */
    private transient int length;

    /**
     * Creates and initialises a SoundStreamBlock object using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public SoundStreamBlock(final SWFDecoder coder) throws IOException {
        length = coder.readUnsignedShort() & Coder.LENGTH_FIELD;
        if (length == Coder.IS_EXTENDED) {
            length = coder.readInt();
        }
        sound = coder.readBytes(new byte[length]);
    }

    /**
     * Creates a SoundStreamBlock specifying the sound data in the format
     * defined by a preceding SoundStreamHead or SoundStreamHead2 object.
     *
     * @param bytes
     *            a list of bytes containing the sound data. Must not be null.
     */
    public SoundStreamBlock(final byte[] bytes) {
        setSound(bytes);
    }

    /**
     * Creates and initialises a SoundStreamBlock object using the values copied
     * from another SoundStreamBlock object.
     *
     * @param object
     *            a SoundStreamBlock object from which the values will be
     *            copied.
     */
    public SoundStreamBlock(final SoundStreamBlock object) {
        sound = object.sound;
    }

    /**
     * Get a copy of the sound data in the format defined by a preceding
     * SoundStreamHead or SoundStreamHead2 object.
     *
     * @return a copy of the sound.
     */
    public byte[] getSound() {
        return Arrays.copyOf(sound, sound.length);
    }

    /**
     * Sets the sound data.
     *
     * @param bytes
     *            a list of bytes containing the sound data. Must not be null.
     */
    public void setSound(final byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException();
        }
        sound = Arrays.copyOf(bytes, bytes.length);
    }

    /** {@inheritDoc} */
    public SoundStreamBlock copy() {
        return new SoundStreamBlock(this);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, sound.length);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        length = sound.length;
        return (length > Coder.HEADER_LIMIT ? Coder.LONG_HEADER
                : Coder.SHORT_HEADER) + length;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {

        if (length > Coder.HEADER_LIMIT) {
            coder.writeShort((MovieTypes.SOUND_STREAM_BLOCK
                    << Coder.LENGTH_FIELD_SIZE) | Coder.IS_EXTENDED);
            coder.writeInt(length);
        } else {
            coder.writeShort((MovieTypes.SOUND_STREAM_BLOCK
                    << Coder.LENGTH_FIELD_SIZE) | length);
        }
        if (Constants.DEBUG) {
            coder.mark();
        }
        coder.writeBytes(sound);
        if (Constants.DEBUG) {
            coder.check(length);
            coder.unmark();
        }
    }
}
