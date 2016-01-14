/*
 * WaitForFrame.java
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
 * The WaitForFrame action instructs the player to wait until the specified
 * frame number has been loaded.
 *
 * <p>
 * If the frame has been loaded then the actions in the following <i>n</i>
 * actions are executed. This action is most often used to execute a short
 * animation loop that plays until the main part of a movie has been loaded.
 * </p>
 *
 * <p>
 * This method of waiting until a frame has been loaded is considered obsolete.
 * Determining the number of frames loaded using the FramesLoaded property of
 * the Flash player in combination with an If action is now the preferred
 * mechanism.
 * </p>
 *
 * @see Push
 * @see If
 */
public class WaitForFrame implements Action {

    /** Format string used in toString() method. */
    private static final String FORMAT = "WaitForFrame: { frameNumber=%d;"
            + " actionCount=%d}";

    /** The maximum offset to the next frame. */
    private static final int MAX_FRAME_OFFSET = 65535;
    /** The highest number of actions that can be executed. */
    private static final int MAX_COUNT = 255;

    /** The frame number to test. */
    private final transient int frameNumber;
    /** The number of actions to execute if the frame has been loaded. */
    private final transient int actionCount;

    /**
     * Creates and initialises a WaitForFrame action using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public WaitForFrame(final SWFDecoder coder) throws IOException {
        coder.readUnsignedShort();
        frameNumber = coder.readUnsignedShort();
        actionCount = coder.readByte();
    }

    /**
     * Creates a WaitForFrame object with the specified frame number and the
     * number of actions that will be executed when the frame is loaded.
     *
     * @param frame
     *            the number of the frame to wait for. Must be in the range
     *            1..65535.
     * @param count
     *            the number (not bytes) of actions to execute. Must be in the
     *            range 0..255.
     */
    public WaitForFrame(final int frame, final int count) {
        if ((frame < 1) || (frame > MAX_FRAME_OFFSET)) {
            throw new IllegalArgumentRangeException(1, MAX_FRAME_OFFSET, frame);
        }
        frameNumber = frame;
        if ((count < 0) || (count > MAX_COUNT)) {
            throw new IllegalArgumentRangeException(1, MAX_COUNT, count);
        }
        actionCount = count;
    }

    /**
     * Creates and initialises a WaitForFrame action using the values
     * copied from another WaitForFrame action.
     *
     * @param object
     *            a WaitForFrame action from which the values will be
     *            copied.
     */
    public WaitForFrame(final WaitForFrame object) {
        frameNumber = object.frameNumber;
        actionCount = object.actionCount;
    }

    /**
     * Get the frame number to test to see if has been loaded.
     *
     * @return the frame number to test.
     */
    public int getFrameNumber() {
        return frameNumber;
    }

    /**
     * Get the number of actions that will be executed when the specified
     * frame is loaded.
     *
     * @return the number of actions to execute.
     */
    public int getActionCount() {
        return actionCount;
    }

    /** {@inheritDoc} */
    public WaitForFrame copy() {
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, frameNumber, actionCount);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        //CHECKSTYLE:OFF - Fixed length when encoded.
        return Coder.ACTION_HEADER + 3;
        //CHECKSTYLE:ON
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        coder.writeByte(ActionTypes.WAIT_FOR_FRAME);
        //CHECKSTYLE:OFF - Fixed length when encoded.
        coder.writeShort(3);
        //CHECKSTYLE:ON
        coder.writeShort(frameNumber);
        coder.writeByte(actionCount);
    }
}
