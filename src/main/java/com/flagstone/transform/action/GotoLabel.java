/*
 * GotoLabel.java
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
package com.flagstone.transform.action;


import java.io.IOException;

import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;

/**
 * The GotoLabel action instructs the player to move to the frame in the current
 * movie with the specified label - previously assigned using a FrameLabel
 * object.
 */
public class GotoLabel implements Action {

    /** Format string used in toString() method. */
    private static final String FORMAT = "GotoLabel: { label=%s}";

    /** The frame label. */
    private final transient String label;

    /** The length of the action, minus the header, when it is encoded. */
    private transient int length;

    /**
     * Creates and initialises a GotoLabel action using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public GotoLabel(final SWFDecoder coder) throws IOException {
        length = coder.readUnsignedShort();
        label = coder.readString();
    }

    /**
     * Creates a GotoLabel action with the specified frame label.
     *
     * @param aString
     *            the label assigned a particular frame in the movie. Must not
     *            be null or an empty string.
     */
    public GotoLabel(final String aString) {
        if (aString == null || aString.length() == 0) {
            throw new IllegalArgumentException();
        }
        label = aString;
    }

    /**
     * Creates and initialises a GotoLabel action using the values
     * copied from another GotoLabel action.
     *
     * @param object
     *            a GotoLabel action from which the values will be
     *            copied.
     */
    public GotoLabel(final GotoLabel object) {
        label = object.label;
    }

    /**
     * Get the frame label.
     *
     * @return the label assigned a particular frame in the movie.
     */
    public String getLabel() {
        return label;
    }

    /** {@inheritDoc} */
    public GotoLabel copy() {
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, label);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        length = context.strlen(label);

        return Coder.ACTION_HEADER + length;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        coder.writeByte(ActionTypes.GOTO_LABEL);
        coder.writeShort(length);
        coder.writeString(label);
    }
}
