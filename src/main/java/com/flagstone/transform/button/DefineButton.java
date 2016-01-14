/*
 * DefineButton.java
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

package com.flagstone.transform.button;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.flagstone.transform.Constants;
import com.flagstone.transform.DefineTag;
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
 * DefineButton defines the appearance of a button and the actions performed
 * when the button is clicked.
 *
 * <p>
 * DefineButton must contain at least one ButtonShape object. If more than one
 * button shape is defined for a given button state then each shape will be
 * displayed by the button. The order in which the shapes are displayed is
 * determined by the layer assigned to each ButtonShape object.
 * </P>
 *
 * @see ButtonShape
 */
public class DefineButton implements DefineTag {

    /** Format string used in toString() method. */
    private static final String FORMAT = "DefineButton: { identifier=%d;"
    		+ " buttonRecords=%s; actions=%s}";

    /** The unique identifier for this object. */
    private int identifier;
    /** The list of shapes used to draw the button. */
    private List<ButtonShape> shapes;
    /** The actions executed when the button is clicked. */
    private List<Action> actions;

    /** The length of the object, minus the header, when it is encoded. */
    private transient int length;

    /**
     * Creates and initialises a DefineButton object using values encoded
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

    public DefineButton(final SWFDecoder coder, final Context context)
            throws IOException {
        length = coder.readUnsignedShort() & Coder.LENGTH_FIELD;
        if (length == Coder.IS_EXTENDED) {
            length = coder.readInt();
        }
        coder.mark();
        identifier = coder.readUnsignedShort();
        shapes = new ArrayList<ButtonShape>();

        while (coder.scanByte() != 0) {
            shapes.add(new ButtonShape(coder, context));
        }

        coder.readByte();

        actions = new ArrayList<Action>();

        final SWFFactory<Action> decoder = context.getRegistry()
                .getActionDecoder();

        if (decoder == null) {
            actions.add(new ActionData(coder
                            .readBytes(new byte[length - coder.bytesRead()])));
        } else {
            while (coder.bytesRead() < length) {
                decoder.getObject(actions, coder, context);
            }
        }
        coder.check(length);
        coder.unmark();
    }

    /**
     * Creates a DefineButton object with the identifier, button shapes and
     * actions.
     *
     * @param uid
     *            the unique identifier for this button.
     * @param buttons
     *            a list of ButtonShapes that are used to draw the button.
     * @param script
     *            a list of actions that are executed when the button is
     *            clicked.
     */
    public DefineButton(final int uid, final List<ButtonShape> buttons,
            final List<Action> script) {
        setIdentifier(uid);
        setShapes(buttons);
        setActions(script);
    }

    /**
     * Creates and initialises a DefineButton object using the values copied
     * from another DefineButton object.
     *
     * @param object
     *            a DefineButton object from which the values will be
     *            copied.
     */
    public DefineButton(final DefineButton object) {
        identifier = object.identifier;
        shapes = new ArrayList<ButtonShape>(object.shapes.size());
        for (final ButtonShape shape : object.shapes) {
            shapes.add(shape.copy());
        }
        actions = new ArrayList<Action>(object.actions);
    }

    /** {@inheritDoc} */
    @Override
	public int getIdentifier() {
        return identifier;
    }

    /** {@inheritDoc} */
    @Override
	public void setIdentifier(final int uid) {
        if ((uid < 1) || (uid > Coder.USHORT_MAX)) {
            throw new IllegalArgumentRangeException(
                    1, Coder.USHORT_MAX, uid);
        }
        identifier = uid;
    }

    /**
     * Adds the button shape to the list of button shapes.
     *
     * @param obj
     *            an ButtonShape object. Must not be null.
     * @return this object.
     */
    public DefineButton add(final ButtonShape obj) {
        if (obj == null) {
            throw new IllegalArgumentException();
        }
        shapes.add(obj);
        return this;
    }

    /**
     * Adds the action to the list of actions.
     *
     * @param obj
     *            an action object. Must not be null.
     * @return this object.
     */
    public DefineButton add(final Action obj) {
        if (obj == null) {
            throw new IllegalArgumentException();
        }
        actions.add(obj);
        return this;
    }

    /**
     * Get the list of button shapes.
     *
     * @return the list of shapes used to represent the button.
     */
    public List<ButtonShape> getShapes() {
        return shapes;
    }

    /**
     * Get the list of actions that will be executed when the button is
     * clicked and released.
     *
     * @return the actions executed when the button is clicked.
     */
    public List<Action> getActions() {
        return actions;
    }

    /**
     * Sets the list of button shapes defined for this button.
     *
     * @param list
     *            a list of Button objects. Must not be null.
     */
    public void setShapes(final List<ButtonShape> list) {
        if (list == null) {
            throw new IllegalArgumentException();
        }
        shapes = list;
    }

    /**
     * Sets the list of actions that will be executed when the button is
     * clicked and released.
     *
     * @param list
     *            a list of action objects. Must not be null.
     */
    public void setActions(final List<Action> list) {
        if (list == null) {
            throw new IllegalArgumentException();
        }
        actions = list;
    }

    /** {@inheritDoc} */
    @Override
	public DefineButton copy() {
        return new DefineButton(this);
    }

    @Override
    public String toString() {
        return String.format(FORMAT, identifier, shapes, actions);
    }

    /** {@inheritDoc} */
    @Override
	public int prepareToEncode(final Context context) {
        length = 2;

        for (final ButtonShape shape : shapes) {
            length += shape.prepareToEncode(context);
        }

        length += 1;

        for (final Action action : actions) {
            length += action.prepareToEncode(context);
        }

        return (length > Coder.HEADER_LIMIT ? Coder.LONG_HEADER
                : Coder.SHORT_HEADER) + length;
    }

    /** {@inheritDoc} */
    @Override
	public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        if (length > Coder.HEADER_LIMIT) {
            coder.writeShort((MovieTypes.DEFINE_BUTTON
                    << Coder.LENGTH_FIELD_SIZE) | Coder.IS_EXTENDED);
            coder.writeInt(length);
        } else {
            coder.writeShort((MovieTypes.DEFINE_BUTTON
                    << Coder.LENGTH_FIELD_SIZE) | length);
        }
        if (Constants.DEBUG) {
            coder.mark();
        }
        coder.writeShort(identifier);

        for (final ButtonShape shape : shapes) {
            shape.encode(coder, context);
        }

        coder.writeByte(0);

        for (final Action action : actions) {
            action.encode(coder, context);
        }
        if (Constants.DEBUG) {
            coder.check(length);
            coder.unmark();
        }
    }
}
