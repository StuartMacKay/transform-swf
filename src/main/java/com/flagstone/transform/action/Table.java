/*
 * Table.java
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

/**
 * Table is used to create a table of string literals that can be referenced by
 * an index rather than using the literal value when executing a sequence of
 * actions.
 *
 * <p>
 * Variables and built-in functions are specified by their name and the Table
 * class contains a table of the respective strings. References to a variable or
 * function can then use its index in the table rather than the name resulting
 * in a more compact representation when the actions are encoded into binary
 * form.
 * </p>
 *
 * @see TableIndex
 * @see Push
 */
public class Table implements Action {

    /** Format string used in toString() method. */
    private static final String FORMAT = "Table: { values=%s}";

    /** The list of strings stored in the table. */
    private List<String> values;

    /** The length of the action, minus the header, when it is encoded. */
    private transient int length;
    /** The number of entries in the table. */
    private transient int tableSize;

    /**
     * Creates and initialises a Table action using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public Table(final SWFDecoder coder) throws IOException {
        length = coder.readUnsignedShort();
        tableSize = coder.readUnsignedShort();
        values = new ArrayList<String>(tableSize);

        if (tableSize > 0) {
            for (int i = 0; i < tableSize; i++) {
                values.add(coder.readString());
            }
        } else {
            /*
             * Reset the length as Macromedia is using the length of a table to
             * hide code following an empty table.
             */
            length = 2;
        }
    }

    /**
     * Creates an empty Table.
     */
    public Table() {
        values = new ArrayList<String>();
    }

    /**
     * Creates a Table using the list of strings.
     *
     * @param list
     *            of Strings that will be added to the table. Must not be null.
     */
    public Table(final List<String> list) {
        values = new ArrayList<String>();
        setValues(list);
    }

    /**
     * Creates and initialises a Table action using the values
     * copied from another Table action.
     *
     * @param object
     *            a Table action from which the values will be
     *            copied.
     */
    public Table(final Table object) {
        values = new ArrayList<String>(object.values);
    }

    /**
     * Adds a String to the variable table.
     *
     * @param aString
     *            a String that will be added to the end of the table. Must not
     *            be null.
     *
     * @return this table.
     */
    public Table add(final String aString) {
        if (aString == null) {
            throw new IllegalArgumentException();
        }
        values.add(aString);
        return this;
    }

    /**
     * Get the table of strings.
     *
     * @return the list of Strings stored in the table.
     */
    public List<String> getValues() {
        return values;
    }

    /**
     * Sets the list of Strings stored in the literal table.
     *
     * @param list
     *            a list of Strings that will replaces the existing literal
     *            table. Must not be null.
     */
    public void setValues(final List<String> list) {
        if (list == null) {
            throw new IllegalArgumentException();
        }
        values = list;
    }

    /** {@inheritDoc} */
    public Table copy() {
        return new Table(this);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, values);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        length = 2;

        for (final String str : values) {
            length += context.strlen(str);
        }

        tableSize = values.size();

        return Coder.ACTION_HEADER + length;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        coder.writeByte(ActionTypes.TABLE);
        coder.writeShort(length);
        coder.writeShort(values.size());

        for (final String str : values) {
            coder.writeString(str);
        }
    }
}
