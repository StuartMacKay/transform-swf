/*
 * ShowFrame.java
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
 * ShowFrame is used to instruct the Flash Player to display a single frame in a
 * movie or movie clip.
 *
 * <p>
 * When a frame is displayed the Flash Player performs the following:
 * <ul>
 * <li>Any actions defined using a DoAction object are executed.</li>
 * <li>
 * The contents of the Flash Player's display list are drawn on the screen.
 * </li>
 * </ul>
 * </p>
 *
 * <p>
 * The scope of a frame is delineated by successive ShowFrame objects. All the
 * commands that affect change the state of the display list or define actions
 * to be executed take effect when the Flash Player displays the frame. All the
 * objects displayed in a frame must be defined before they can be displayed.
 * </p>
 */
public class ShowFrame implements MovieTag {

    /** Format string used in toString() method. */
    private static final String FORMAT = "ShowFrame";
    /** Singleton. */
    private static final ShowFrame INSTANCE = new ShowFrame();

    /**
     * Returns a shared ShowFrame object.
     *
     * @return an object that can safely be shared among objects.
     */
    public static ShowFrame getInstance() {
        return INSTANCE;
    }

    /**
     * Returns a shared ShowFrame object.
     *
     * @param coder
     *            an SWFEncoder object.

     * @param context a Context object used to obtain the total number of
     * frames in a movie.
     *
     * @return an object that can safely be shared among objects.
     *
     * @throws IOException if an error occurs while encoding the object.
     */
    public static ShowFrame getInstance(final SWFDecoder coder,
            final Context context) throws IOException {
        coder.readUnsignedShort();
        return INSTANCE;
    }

    /** Private constructor for the singleton. */
    private ShowFrame() {
    }

    /** {@inheritDoc} */
    public ShowFrame copy() {
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return FORMAT;
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        return 2;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        coder.writeShort(MovieTypes.SHOW_FRAME << Coder.LENGTH_FIELD_SIZE);
    }
}
