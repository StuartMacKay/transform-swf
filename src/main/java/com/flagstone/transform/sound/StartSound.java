/*
 * StartSound.java
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

/**
 * StartSound instructs the player to start or stop playing a sound defined
 * using the DefineSound class.
 *
 * <p>
 * StartSound contains a SoundInfo object that defines how the sound fades in
 * and out, whether it is repeated as well as specifying an envelope that
 * provides a finer degree of control over the levels at which the sound is
 * played.
 * </p>
 *
 * @see DefineSound
 * @see SoundInfo
 */
public class StartSound implements MovieTag {

    /** Format string used in toString() method. */
    private static final String FORMAT = "StartSound: { sound=%s}";

    /** Describes how the sound is played. */
    private SoundInfo sound;

    /** The length of the object, minus the header, when it is encoded. */
    private transient int length;

    /**
     * Creates and initialises a StartSound object using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public StartSound(final SWFDecoder coder) throws IOException {
        length = coder.readUnsignedShort() & Coder.LENGTH_FIELD;
        if (length == Coder.IS_EXTENDED) {
            length = coder.readInt();
        }
        coder.mark();
        sound = new SoundInfo(coder.readUnsignedShort(), coder);
        coder.check(length);
        coder.unmark();
    }

    /**
     * Creates a StartSound object with an Sound object that identifies the
     * sound and controls how it is played.
     *
     * @param aSound
     *            the Sound object. Must not be null.
     */
    public StartSound(final SoundInfo aSound) {
        setSound(aSound);
    }

    /**
     * Creates and initialises a StartSound object using the values copied
     * from another StartSound object.
     *
     * @param object
     *            a StartSound object from which the values will be
     *            copied.
     */
    public StartSound(final StartSound object) {
        sound = object.sound.copy();
    }

    /**
     * Get the Sound object describing how the sound will be played.
     *
     * @return the SoundInfo object that controls the playback.
     */
    public SoundInfo getSound() {
        return sound;
    }

    /**
     * Sets the Sound object that describes how the sound will be played.
     *
     * @param aSound
     *            the Sound object that controls how the sound is played. Must
     *            not be null.
     */
    public void setSound(final SoundInfo aSound) {
        if (aSound == null) {
            throw new IllegalArgumentException();
        }
        sound = aSound;
    }

    /** {@inheritDoc} */
    public StartSound copy() {
        return new StartSound(this);
    }

    @Override
    public String toString() {
        return String.format(FORMAT, sound);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        length = sound.prepareToEncode(context);
        return (length > Coder.HEADER_LIMIT ? Coder.LONG_HEADER
                : Coder.SHORT_HEADER) + length;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {

        if (length > Coder.HEADER_LIMIT) {
            coder.writeShort((MovieTypes.START_SOUND
                    << Coder.LENGTH_FIELD_SIZE) | Coder.IS_EXTENDED);
            coder.writeInt(length);
        } else {
            coder.writeShort((MovieTypes.START_SOUND
                    << Coder.LENGTH_FIELD_SIZE) | length);
        }
        if (Constants.DEBUG) {
            coder.mark();
        }
        sound.encode(coder, context);
        if (Constants.DEBUG) {
            coder.check(length);
            coder.unmark();
        }
    }
}
