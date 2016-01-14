/*
 * GotoFrame2.java
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
 * The GotoFrame2 action instructs the player to go to the named or numbered
 * frame in the current movie's main time-line. It extends the functionality
 * provided by the GotoFrame action by allowing the name of a frame, previously
 * assigned using the FrameLabel object, to be specified.
 *
 * <p>
 * Up to Flash Version 4, movies contained a single sequence of 65536 frames. In
 * Flash 5 the concept of Scenes was added which allowed movies to contain
 * 'pages' of frames. GotoFrame2 contains a frameOffset attribute which allows
 * the frames in each scene to be referenced by its 'logical' number. The
 * frameOffset for a given scene is added to the frame number to generate the
 * 'physical' page number.
 * </p>
 *
 * <p>
 * GotoFrame2 is a stack-based action. The name or number of the frame is pushed
 * onto the stack before the GotoFrame2 action is executed. If a frameOffset is
 * specified it is added to the number of the frame identified by the stack
 * arguments to give the final frame number. Whether the movie starts playing
 * the frame is controlled by the boolean attribute, <i>play</i>. When set to
 * true the movie starts playing the frame as soon as it has been loaded by the
 * Flash Player.
 * </p>
 *
 * <p>
 * GotoFrame2 is only used to control the main time-line of a movie. Controlling
 * how an individual movie clip is played is handled by a different mechanism.
 * From Flash 5 onward movie clips are defined as objects. The ExecuteMethod
 * action is used to execute the gotoAndPlay() or gotoAndStop() methods that
 * control a movie clip's time-line.
 * </p>
 *
 * @see GotoFrame
 */
public class GotoFrame2 implements Action {

    /** Format string used in toString() method. */
    private static final String FORMAT = "Gotoframe2: { playFrame=%s;"
            + " frameOffset=%d}";

    /** The maximum offset to the next frame. */
    private static final int MAX_FRAME_OFFSET = 65535;
    /** Bit mask for field indication if the encoded action has an offset. */
    private static final int OFFSET_MASK = 0x02;
    /** Bit mask for field containing play attribute. */
    private static final int PLAY_MASK = 0x01;
    /** Encoded length with offset. */
    private static final int LEN_WITH_OFFSET = 3;
    /** Encoded length without offset. */
    private static final int LEN_NO_OFFSET = 1;

    /** Indicates whether the Flash Player should start playing the frame. */
    private final transient boolean play;
    /** The offset to the next frame. */
    private final transient int frameOffset;

    /** The length of the action, minus the header, when it is encoded. */
    private transient int length;
    /** Flag used to indicate the action contains a frame offset. */
    private transient boolean hasOffset;

    /**
     * Creates and initialises an GotoFrame2 action using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public GotoFrame2(final SWFDecoder coder) throws IOException {
        length = coder.readUnsignedShort();

        final int bits = coder.readByte();
        hasOffset = (bits & Coder.BIT1) != 0;
        play = (bits & Coder.BIT0) != 0;

        if (hasOffset) {
            frameOffset = coder.readSignedShort();
        } else {
            frameOffset = 0;
        }
    }

    /**
     * Creates a GotoFrame2 object with the specified play flag setting.
     *
     * @param aBool
     *            true if the player should being playing the movie at the
     *            specified frame. false if the player should stop playing the
     *            movie.
     */
    public GotoFrame2(final boolean aBool) {
        this(0, aBool);
    }

    /**
     * Creates a GotoFrame2 object with the specified play flag setting and
     * frame offset for a given scene.
     *
     * @param offset
     *            a number which will be added to the number of the frame popped
     *            from the stack to give the final frame number. Must be in the
     *            range 1..65535.
     * @param aBool
     *            true if the player should being playing the movie at the
     *            specified frame, false if the player should stop playing the
     *            movie.
     */
    public GotoFrame2(final int offset, final boolean aBool) {
        if ((offset < 0) || (offset > MAX_FRAME_OFFSET)) {
            throw new IllegalArgumentRangeException(0,
                    MAX_FRAME_OFFSET, offset);
        }
        frameOffset = offset;
        play = aBool;
    }

    /**
     * Creates and initialises a GotoFrame2 action using the values
     * copied from another GotoFrame2 action.
     *
     * @param object
     *            a GotoFrame2 action from which the values will be
     *            copied.
     */
    public GotoFrame2(final GotoFrame2 object) {
        play = object.play;
        frameOffset = object.frameOffset;
    }

    /**
     * Returns the offset that will be added to the 'logical' frame number
     * obtained from the stack to generate the 'physical' frame number.
     *
     * @return the offset to the next frame to be displayed.
     */
    public int getFrameOffset() {
        return frameOffset;
    }

    /**
     * Returns the play flag.
     *
     * @return true if the player will being playing the movie at the specified
     *         frame, false otherwise.
     */
    public boolean isPlay() {
        return play;
    }

    /** {@inheritDoc} */
    public GotoFrame2 copy() {
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, String.valueOf(play), frameOffset);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        hasOffset = frameOffset > 0;
        if (hasOffset) {
            length = LEN_WITH_OFFSET;
        } else {
            length = LEN_NO_OFFSET;
        }
        return Coder.ACTION_HEADER + length;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        coder.writeByte(ActionTypes.GOTO_FRAME_2);
        coder.writeShort(length);

        int flags = 0;
        if (hasOffset) {
            flags |= OFFSET_MASK;
        }
        if (play) {
            flags |= PLAY_MASK;
        }
        coder.writeByte(flags);

        if (hasOffset) {
            coder.writeShort(frameOffset);
        }
    }
}
