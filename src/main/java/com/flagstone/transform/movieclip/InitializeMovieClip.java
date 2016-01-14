/*
 * Initialize.java
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

package com.flagstone.transform.movieclip;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.flagstone.transform.Constants;
import com.flagstone.transform.MovieTag;
import com.flagstone.transform.MovieTypes;
import com.flagstone.transform.action.Action;
import com.flagstone.transform.action.ActionData;
import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.coder.SWFFactory;
import com.flagstone.transform.exception.IllegalArgumentRangeException;

/**
 * Initialize is used to specify a sequence of actions that are executed to
 * initialise a movie clip before it is displayed.
 *
 * <p>
 * Initialize implements the #initclip pragma defined in the ActionScript
 * language.
 * </p>
 *
 * <p>
 * Unlike the DoAction class which specifies the actions that are executed when
 * a particular frame is displayed the actions contained in an Initialize object
 * are executed only once, regardless of where the object is included in a
 * movie. If a frame containing the Initialize object is played again the
 * actions are skipped. Also there can only be one Initialize object for each
 * movie clip defined in the movie.
 * </p>
 */
public class InitializeMovieClip implements MovieTag {

    /** Format string used in toString() method. */
    private static final String FORMAT = "Initialize: { identifier=%d;"
                + " actions=%s}";

    /** The unique identifier of the movie clip that will be initialized. */
    private int identifier;
    /** The actions used to initialize the movie clip. */
    private List<Action> actions;

    /** The length of the object, minus the header, when it is encoded. */
    private transient int length;

    /**
     * Creates and initialises an InitializeMovieClip object using values
     * encoded in the Flash binary format.
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
    public InitializeMovieClip(final SWFDecoder coder, final Context context)
            throws IOException {
        length = coder.readUnsignedShort() & Coder.LENGTH_FIELD;
        if (length == Coder.IS_EXTENDED) {
            length = coder.readInt();
        }
        coder.mark();
        identifier = coder.readUnsignedShort();
        actions = new ArrayList<Action>();

        final SWFFactory<Action> decoder = context.getRegistry()
                .getActionDecoder();

        if (decoder == null) {
            actions.add(new ActionData(coder.readBytes(new byte[length - 2])));
        } else {
            while (coder.bytesRead() < length) {
                decoder.getObject(actions, coder, context);
            }
        }
        coder.unmark();
    }

    /**
     * Creates a Initialize object that will initialise the movie clip with the
     * specified identifier with the actions in the list.
     *
     * @param uid
     *            the identifier of the movie clip to initialise. Must be in the
     *            range 1..65535.
     * @param list
     *            the list of action objects. Must not be null.
     */
    public InitializeMovieClip(final int uid, final List<Action> list) {
        setIdentifier(uid);
        setActions(list);
    }

    /**
     * Creates and initialises an InitializeMovieClip object using the values
     * copied from another InitializeMovieClip object.
     *
     * @param object
     *            an InitializeMovieClip object from which the values will be
     *            copied.
     */
    public InitializeMovieClip(final InitializeMovieClip object) {
        identifier = object.identifier;
        actions = new ArrayList<Action>(object.actions);
    }

    /**
     * Get the identifier of the movie clip that will be initialised.
     *
     * @return the movie clip identifier.
     */
    public int getIdentifier() {
        return identifier;
    }

    /**
     * Sets the identifier of the movie clip that will be initialised.
     *
     * @param uid
     *            the identifier of the movie clip. The value must be in the
     *            range 1..65535.
     */
    public void setIdentifier(final int uid) {
        if ((uid < 1) || (uid > Coder.USHORT_MAX)) {
            throw new IllegalArgumentRangeException(
                    1, Coder.USHORT_MAX, uid);
        }
        identifier = uid;
    }

    /**
     * Adds the action object to the list of actions.
     *
     * @param anAction
     *            an object belonging to a class derived from Action. Must not
     *            be null.
     * @return this object.
     */
    public InitializeMovieClip add(final Action anAction) {
        if (anAction == null) {
            throw new IllegalArgumentException();
        }
        actions.add(anAction);
        return this;
    }

    /**
     * Get the list of actions that are used to initialise the movie clip.
     *
     * @return the actions to initialize the movie clip.
     */
    public List<Action> getActions() {
        return actions;
    }

    /**
     * Set the list of actions of the movie clip that will be initialised.
     *
     * @param list
     *            the list of action objects. Must not be null.
     */
    public void setActions(final List<Action> list) {
        if (list == null) {
            throw new IllegalArgumentException();
        }
        actions = list;
    }

    /** {@inheritDoc} */
    public InitializeMovieClip copy() {
        return new InitializeMovieClip(this);
    }

    @Override
    public String toString() {
        return String.format(FORMAT, identifier, actions);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        length = 2;

        for (final Action action : actions) {
            length += action.prepareToEncode(context);
        }

        return (length > Coder.HEADER_LIMIT ? Coder.LONG_HEADER
                : Coder.SHORT_HEADER) + length;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {

        if (length > Coder.HEADER_LIMIT) {
            coder.writeShort((MovieTypes.INITIALIZE
                    << Coder.LENGTH_FIELD_SIZE) | Coder.IS_EXTENDED);
            coder.writeInt(length);
        } else {
            coder.writeShort((MovieTypes.INITIALIZE
                    << Coder.LENGTH_FIELD_SIZE) | length);
        }
        if (Constants.DEBUG) {
            coder.mark();
        }
        coder.writeShort(identifier);
        for (final Action action : actions) {
            action.encode(coder, context);
        }
        if (Constants.DEBUG) {
            coder.check(length);
            coder.unmark();
        }
    }
}
