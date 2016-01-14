/*
 * With.java
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
import java.util.ArrayList;
import java.util.List;

import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.coder.SWFFactory;

/**
 * With is a stack-based action and supports the <em>with</em> statement from
 * the ActionScript language.
 *
 * <pre>
 * with(_root.movieClip) {
 *     gotoAndPlay(&quot;frame&quot;);
 * }
 * </pre>
 *
 * <p>
 * The action temporarily selects the movie clip allowing the following stream
 * of actions to control the movie clip's time-line.
 * </p>
 */
public class With implements Action {

    /** Format string used in toString() method. */
    private static final String FORMAT = "With: { actions=%s}";

    /** The list of actions that will be executed. */
    private final transient List<Action> actions;

    /** The length of the action, minus the header, when it is encoded. */
    private transient int length;

    /**
     * Creates and initialises a With action using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @param context
     *            a Context object used to manage the decoders for different
     *            type of object and to pass information on how objects are
     *            decoded.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public With(final SWFDecoder coder, final Context context)
            throws IOException {
        final SWFFactory<Action> decoder = context.getRegistry()
                .getActionDecoder();

        coder.readUnsignedShort();
        length = coder.readUnsignedShort();
        actions = new ArrayList<Action>();
        coder.mark();
        while (coder.bytesRead() < length) {
            decoder.getObject(actions, coder, context);
        }
        coder.unmark();
    }

    /**
     * Creates a With object with a list of actions.
     *
     * @param list
     *            the list of action objects. Must not be null.
     */
    public With(final List<Action> list) {
        if (list == null) {
            throw new IllegalArgumentException();
        }
        actions = list;
    }

    /**
     * Creates and initialises a With action using the values
     * copied from another With action.
     *
     * @param object
     *            a With action from which the values will be
     *            copied.
     */
    public With(final With object) {
        actions = new ArrayList<Action>(object.actions);
    }

    /**
     * Get the list of actions that are executed for the movie clip target.
     *
     * @return a copy of the list of actions that will be executed.
     */
    public List<Action> getActions() {
        return new ArrayList<Action>(actions);
    }

    /** {@inheritDoc} */
    public With copy() {
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, actions);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        length = 2;

        for (final Action action : actions) {
            length += action.prepareToEncode(context);
        }

        return Coder.ACTION_HEADER + length;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        coder.writeByte(ActionTypes.WITH);
        coder.writeShort(2);
        coder.writeShort(length - 2);

        for (final Action action : actions) {
            action.encode(coder, context);
        }
    }
}
