/*
 * WaitForFrame2.java
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
import com.flagstone.transform.exception.IllegalArgumentRangeException;

/**
 * The WaitForFrame2 action instructs the player to wait until the specified
 * frame number or named frame has been loaded.
 *
 * <p>
 * If the frame has been loaded then the following <i>n</i> actions are
 * executed. The WaitForFrame2 action extends the WaitForFrame action by
 * allowing the name of a frame to be specified.
 * </p>
 *
 * <p>
 * WaitForFrame2 is a stack-based action. The frame number or frame name which
 * should be loaded to trigger execution of the following actions is popped from
 * the Flash Player's stack. Note however that this method of waiting until a
 * frame has been loaded is considered obsolete. Determining the number of
 * frames loaded using the FramesLoaded property of the Flash player in
 * combination with an If action is now preferred.
 * </p>
 *
 * @see Push
 * @see If
 */
public class WaitForFrame2 implements Action {

    /** Format string used in toString() method. */
    private static final String FORMAT = "WaitForFrame2: { actionCount=%d}";

    /** The highest number of actions that can be executed. */
    private static final int MAX_COUNT = 255;

    /** The number of actions to be executed. */
    private final transient int actionCount;

    /**
     * Creates and initialises a WaitForFrame2 action using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public WaitForFrame2(final SWFDecoder coder) throws IOException {
        coder.readUnsignedShort();
        actionCount = coder.readByte();
    }

    /**
     * Creates a WaitForFrame2 object with the number of actions to execute if
     * the frame has been loaded.
     *
     * @param count
     *            the number of actions to execute. Must be in the range 0..255.
     */
    public WaitForFrame2(final int count) {
        if ((count < 0) || (count > MAX_COUNT)) {
            throw new IllegalArgumentRangeException(0, MAX_COUNT, count);
        }
        actionCount = count;
    }

    /**
     * Creates and initialises a WaitForFrame2 action using the values
     * copied from another WaitForFrame2 action.
     *
     * @param object
     *            a WaitForFrame2 action from which the values will be
     *            copied.
     */
    public WaitForFrame2(final WaitForFrame2 object) {
        actionCount = object.actionCount;
    }

    /**
     * Returns the number of actions to execute.
     *
     * @return the number of actions, (not encoded bytes).
     */
    public int getActionCount() {
        return actionCount;
    }

    /** {@inheritDoc} */
    public WaitForFrame2 copy() {
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, actionCount);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        return Coder.ACTION_HEADER + 1;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        coder.writeByte(ActionTypes.WAIT_FOR_FRAME_2);
        coder.writeShort(1);
        coder.writeByte(actionCount);
    }
}
