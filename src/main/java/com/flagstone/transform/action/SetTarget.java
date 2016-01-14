/*
 * SetTarget.java
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
 * SetTarget selects a movie clip to allow its time-line to be controlled. The
 * action performs a "context switch". All following actions such as GotoFrame,
 * Play, etc. will be applied to the specified object until another
 * <b>SetTarget</b> action is executed. Setting the target to be the empty
 * string ("") returns the target to the movie's main time-line.
 *
 */
public class SetTarget implements Action {

    /** Format string used in toString() method. */
    private static final String FORMAT = "SetTarget: { target=%s}";

    /** The name of the movie clip. */
    private final transient String target;

    /** The length of the action, minus the header, when it is encoded. */
    private transient int length;

    /**
     * Creates and initialises a SetTarget action using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public SetTarget(final SWFDecoder coder) throws IOException {
        length = coder.readUnsignedShort();
        target = coder.readString(length);
    }

    /**
     * Creates a SetTarget action that changes the context to the specified
     * target.
     *
     * @param aString
     *            the name of a movie clip. Must not be null.
     */
    public SetTarget(final String aString) {
        if (aString == null) {
            throw new IllegalArgumentException();
        }
        target = aString;
    }

    /**
     * Creates and initialises a SetTarget action using the values
     * copied from another SetTarget action.
     *
     * @param object
     *            a SetTarget action from which the values will be
     *            copied.
     */
    public SetTarget(final SetTarget object) {
        target = object.target;
    }

    /**
     * Get the name of the target movie clip.
     *
     * @return the name of the movie clip.
     */
    public String getTarget() {
        return target;
    }

    /** {@inheritDoc} */
    public SetTarget copy() {
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, target);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        length = context.strlen(target);

        return Coder.ACTION_HEADER + length;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        coder.writeByte(ActionTypes.SET_TARGET);
        coder.writeShort(length);
        coder.writeString(target);
    }
}
