/*
 * GotoFrame.java
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
 * The GotoFrame action instructs the player to move to the specified frame
 * number in the current movie's main time-line.
 *
 * <p>
 * GotoFrame is only used to control the main time-line of a movie. Controlling
 * how an individual movie clip is played is handled by a different mechanism.
 * From Flash 5 onward movie clips are defined as objects and the ExecuteMethod
 * action is used to execute the gotoAndPlay() or gotoAndStop() which start and
 * stop playing a movie clip.
 * </p>
 *
 * @see GotoFrame2
 */
public class GotoFrame implements Action {

    /** Format string used in toString() method. */
    private static final String FORMAT = "GotoFrame: { frameNumber=%d}";
    /** The maximum offset to the next frame. */
    private static final int MAX_FRAME_OFFSET = 65535;

    /** The frame number to be displayed. */
    private final transient int frameNumber;

    /**
     * Creates and initialises an GotoFrame action using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public GotoFrame(final SWFDecoder coder) throws IOException {
        coder.readUnsignedShort();
        frameNumber = coder.readUnsignedShort();
    }

    /**
     * Creates a GotoFrame with the specified frame number.
     *
     * @param number
     *            the number of the frame. Must be in the range 0..65535.
     */
    public GotoFrame(final int number) {
        if ((number < 0) || (number > MAX_FRAME_OFFSET)) {
            throw new IllegalArgumentRangeException(1,
                    MAX_FRAME_OFFSET, number);
        }
        frameNumber = number;
    }

    /**
     * Creates and initialises a GotoFrame action using the values
     * copied from another GotoFrame action.
     *
     * @param object
     *            a GotoFrame action from which the values will be
     *            copied.
     */
    public GotoFrame(final GotoFrame object) {
        frameNumber = object.frameNumber;
    }

    /**
     * Returns the number of the frame to move the main time-line to.
     *
     * @return the offset to the next frame to be displayed.
     */
    public int getFrameNumber() {
        return frameNumber;
    }

    /** {@inheritDoc} */
    public GotoFrame copy() {
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, frameNumber);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        return Coder.ACTION_HEADER + 2;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        coder.writeByte(ActionTypes.GOTO_FRAME);
        coder.writeShort(2);
        coder.writeShort(frameNumber);
    }
}
