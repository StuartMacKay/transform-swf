/*
 * FrameLabel.java
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
package com.flagstone.transform;

import java.io.IOException;

import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;

/**
 * FrameLabel defines a name for the current frame in a movie or movie clip.
 *
 * <p>
 * The name can be referenced from other objects such as GotoFrame2 to simplify
 * the creation of scripts to control movies by using a predefined name rather
 * than the frame number. The label assigned to a particular frame should be
 * unique. A frame cannot be referenced within a movie before the Player has
 * loaded and displayed the frame that contains the corresponding FrameLabel
 * object.
 * </p>
 *
 * <p>
 * If a frame is defined as an anchor it may also be referenced externally when
 * specifying the movie to play using a URL - similar to the way names links are
 * used in HTML. When the Flash Player loads a movie it will begin playing at
 * the frame specified in the URL.
 * </p>
 */
public class FrameLabel implements MovieTag {

    /** Format string used in toString() method. */
    private static final String FORMAT = "FrameLabel: { label=%s; anchor=%s}";

    /** The label for the frame. */
    private String label;
    /** Whether the frame can be referenced by a URL. */
    private boolean anchor;

    /** The length of the object, minus the header, when it is encoded. */
    private transient int length;

    /**
     * Creates and initialises a FrameLabel object using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public FrameLabel(final SWFDecoder coder) throws IOException {
        length = coder.readUnsignedShort() & Coder.LENGTH_FIELD;
        if (length == Coder.IS_EXTENDED) {
            length = coder.readInt();
        }
        coder.mark();
        label = coder.readString();
        if (coder.bytesRead() < length) {
            anchor = coder.readByte() != 0;
        }
        coder.check(length);
        coder.unmark();
    }

    /**
     * Creates a FrameLabel object with the specified name.
     *
     * @param aString
     *            the string that defines the label that will be assigned to the
     *            current frame. Must not be null or an empty string.
     */
    public FrameLabel(final String aString) {
        setLabel(aString);
    }

    /**
     * Creates a FrameLabel object with the specified name. If the isAnchor flag
     * is true then the frame can be directly addressed by a URL and the Flash
     * Player will begin playing the movie at the specified frame.
     *
     * @param aString
     *            the string that defines the label that will be assigned to the
     *            current frame. Must not be null or an empty string.
     * @param isAnchor
     *            if true the name will be used as an anchor when referencing
     *            the frame in a URL.
     */
    public FrameLabel(final String aString, final boolean isAnchor) {
        setLabel(aString);
        anchor = isAnchor;
    }

    /**
     * Creates a FrameLabel object with a copy of the label and anchor from
     * another FrameLabel object.
     *
     * @param object
     *            a FrameLabel object to copy.
     */
    public FrameLabel(final FrameLabel object) {
        label = object.label;
        anchor = object.anchor;
    }

    /**
     * Get the label for the frame.
     *
     * @return the string used to label the frame.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the label.
     *
     * @param aString
     *            the string that defines the label that will be assigned to the
     *            current frame. Must not be null or an empty string.
     */
    public void setLabel(final String aString) {
        if (aString == null || aString.length() == 0) {
            throw new IllegalArgumentException();
        }
        label = aString;
    }

    /**
     * Is the frame name is also used as an anchor so the frame can be
     * referenced from outside of the movie.
     *
     * @return true is the name can be used as an external reference to the
     * frame.
     */
    public boolean isAnchor() {
        return anchor;
    }

    /**
     * Sets the flag indicating whether the frame name is also used as an anchor
     * so the frame can be referenced from outside of the movie.
     *
     * @param anchored
     *            true if the frame is an anchor frame, false otherwise.
     */
    public void setAnchor(final boolean anchored) {
        anchor = anchored;
    }

    /** {@inheritDoc} */
    public FrameLabel copy() {
        return new FrameLabel(this);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, label, String.valueOf(anchor));
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {

        length = context.strlen(label);
        length += anchor ? 1 : 0;

        return (length > Coder.HEADER_LIMIT ? Coder.LONG_HEADER
                : Coder.SHORT_HEADER) + length;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {

        if (length > Coder.HEADER_LIMIT) {
            coder.writeShort((MovieTypes.FRAME_LABEL
                    << Coder.LENGTH_FIELD_SIZE) | Coder.IS_EXTENDED);
            coder.writeInt(length);
        } else {
            coder.writeShort((MovieTypes.FRAME_LABEL
                    << Coder.LENGTH_FIELD_SIZE) | length);
        }
        coder.writeString(label);

        if (anchor) {
            coder.writeByte(1);
        }
    }
}
