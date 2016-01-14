/*
 * ButtonSound.java
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

package com.flagstone.transform.button;

import java.io.IOException;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;

import com.flagstone.transform.Constants;
import com.flagstone.transform.Event;
import com.flagstone.transform.MovieTag;
import com.flagstone.transform.MovieTypes;
import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.exception.IllegalArgumentRangeException;
import com.flagstone.transform.sound.SoundInfo;

/**
 * ButtonSound defines the sounds that are played when an event occurs in a
 * button. Sounds are only played for the RollOver, RollOut, Press and Release
 * events.
 *
 * <p>
 * For each event a {@link SoundInfo} object identifies the sound and controls
 * how it is played. For events where no sound should be played simply specify a
 * null value instead of a SoundInfo object.
 * </p>
 *
 * @see DefineButton
 * @see DefineButton2
 */
public class ButtonSound implements MovieTag {

    /** Format string used in toString() method. */
    private static final String FORMAT = "ButtonSound: { identifier=%d;"
            + " table=%s}";

    /** The set of button events that support sounds. */
    private static final EnumSet<Event>EVENTS = EnumSet.of(
            Event.ROLL_OUT, Event.ROLL_OVER,
            Event.PRESS, Event.RELEASE);

    /** The unique identifier of the button. */
    private int identifier;
    /** Table of sounds played for different button events. */
    private final transient Map<Event, SoundInfo>table;

    /** The length of the object, minus the header, when it is encoded. */
    private transient int length;

    /**
     * Creates and initialises a ButtonSound object using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public ButtonSound(final SWFDecoder coder) throws IOException {
        length = coder.readUnsignedShort() & Coder.LENGTH_FIELD;
        if (length == Coder.IS_EXTENDED) {
            length = coder.readInt();
        }
        coder.mark();
        identifier = coder.readUnsignedShort();
        table = new LinkedHashMap<Event, SoundInfo>();
        decodeInfo(Event.ROLL_OUT, coder);
        decodeInfo(Event.ROLL_OVER, coder);
        decodeInfo(Event.PRESS, coder);
        decodeInfo(Event.RELEASE, coder);
        coder.check(length);
        coder.unmark();
    }

    /**
     * Decoder the optional sound for each button event.
     * @param event the button event.
     * @param coder the SWFDecoder containing the encoded data.
     * @throws IOException if an error occurs decoding the sound.
     */
    private void decodeInfo(final Event event,
            final SWFDecoder coder) throws IOException {
        if (coder.bytesRead() < length) {
            final int uid = coder.readUnsignedShort();
            if (uid != 0) {
                table.put(event, new SoundInfo(uid, coder));
            }
        }
    }

    /**
     * Creates a ButtonSound object that defines the sound played for a single
     * button event.
     *
     * @param uid
     *            the unique identifier of the DefineButton or DefineButton2
     *            object that defines the button. Must be in the range 1..65535.
     * @param eventCode
     *            the event that identifies when the sound id played, must be
     *            either Event.EventType.rollOver,
     *            Event.EventType.rollOut, Event.EventType.press or
     *            Event.EventType.release.
     * @param aSound
     *            an SoundInfo object that identifies a sound and controls how
     *            it is played.
     */
    public ButtonSound(final int uid, final Event eventCode,
            final SoundInfo aSound) {
        table = new LinkedHashMap<Event, SoundInfo>();
        setIdentifier(uid);
        setSoundInfo(eventCode, aSound);
    }

    /**
     * Creates and initialises a ButtonSound object using the values copied
     * from another ButtonSound object.
     *
     * @param object
     *            a ButtonSound object from which the values will be
     *            copied.
     */
    public ButtonSound(final ButtonSound object) {

        identifier = object.identifier;
        table = new LinkedHashMap<Event, SoundInfo>();

        for (Event event : object.table.keySet()) {
            table.put(event, object.table.get(event).copy());
        }
    }

    /**
     * Get the unique identifier of the button that this object applies to.
     *
     * @return the unique identifier of the sound.
     */
    public int getIdentifier() {
        return identifier;
    }

    /**
     * Returns the SoundInfo object for the specified event. Null is returned if
     * there is no SoundInfo object defined for the event code.
     *
     * @param event
     *            The button event, must be one of Event.ROLL_OVER,
     *            Event.ROLL_OUT, Event.PRESS, Event.RELEASE.
     * @return the SoundInfo that identifies and controls the sound that will be
     *            played for the event or null if not SoundInfo is defined for
     *            the event.
     */
    public SoundInfo getSoundInfo(final Event event) {
         return table.get(event);
    }

    /**
     * Sets the identifier of the button that this object applies to.
     *
     * @param uid
     *            the unique identifier of the button which this object applies
     *            to. Must be in the range 1..65535.
     */
    public void setIdentifier(final int uid) {
        if ((uid < 1) || (uid > Coder.USHORT_MAX)) {
            throw new IllegalArgumentRangeException(
                    1, Coder.USHORT_MAX, uid);
        }
        identifier = uid;
    }

    /**
     * Sets the SoundInfo object for the specified button event. The argument
     * may be null allowing the SoundInfo object for a given event to be
     * deleted.
     *
     * @param event
     *            the code representing the button event, must be either
     *            Event.EventType.RollOver, Event.EventType.RollOut,
     *            Event.EventType.Press or Event.EventType.Release.
     * @param info
     *            an SoundInfo object that identifies and controls how the sound
     *            is played.
     */
    public void setSoundInfo(final Event event, final SoundInfo info) {
        table.put(event, info);
    }

    /** {@inheritDoc} */
    @Override
	public ButtonSound copy() {
        return new ButtonSound(this);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, identifier, table.toString());
    }

    /** {@inheritDoc} */
    @Override
	public int prepareToEncode(final Context context) {
        length = 2;

        for (Event event : EVENTS) {
            if (table.containsKey(event)) {
                length += table.get(event).prepareToEncode(context);
            } else {
                length += 2;
            }
        }
        return (length > Coder.HEADER_LIMIT ? Coder.LONG_HEADER
                : Coder.SHORT_HEADER) + length;
    }

    /** {@inheritDoc} */
    @Override
	public void encode(final SWFEncoder coder, final Context context)
            throws IOException {

        if (length > Coder.HEADER_LIMIT) {
            coder.writeShort((MovieTypes.BUTTON_SOUND
                    << Coder.LENGTH_FIELD_SIZE) | Coder.IS_EXTENDED);
            coder.writeInt(length);
        } else {
            coder.writeShort((MovieTypes.BUTTON_SOUND
                    << Coder.LENGTH_FIELD_SIZE) | length);
        }
        if (Constants.DEBUG) {
            coder.mark();
        }
        coder.writeShort(identifier);

        for (Event event : EVENTS) {
            if (table.containsKey(event)) {
                table.get(event).encode(coder, context);
            } else {
                coder.writeShort(0);
            }
        }
        if (Constants.DEBUG) {
            coder.check(length);
            coder.unmark();
        }
    }
}
