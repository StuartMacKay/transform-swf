/*
 * DefineText2.java
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

package com.flagstone.transform.text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.flagstone.transform.Constants;
import com.flagstone.transform.MovieTypes;
import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.datatype.Bounds;
import com.flagstone.transform.datatype.CoordTransform;
import com.flagstone.transform.exception.IllegalArgumentRangeException;

/**
 * DefineText2 defines one or more lines of transparent text.
 *
 * <p>
 * It extends the functionality provided by the {@link DefineText} class by
 * encoding the alpha channel of the colour objects used to set the text color.
 * </p>
 *
 * <p>
 * DefineText2 class acts as a container for the text. The bounding rectangle
 * and transform controls how the text is laid out. Each TextSpan object
 * specifies an offset from the left and bottom edges of the bounding rectangle,
 * allowing successive lines of text to be arranged as a block or paragraph. The
 * coordinate transform can be used to control the size and orientation of the
 * text when it is displayed.
 * </p>
 *
 * @see TextSpan
 * @see DefineText
 */
public class DefineText2 implements StaticTextTag {

    /** Format string used in toString() method. */
    private static final String FORMAT = "DefineText2: { identifier=%d;"
    		+ " bounds=%s; transform=%s; objects=%s}";

    /** The unique identifier for this object. */
    private int identifier;
    /** The bounding box that encloses the text. */
    private Bounds bounds;
    /** The position and orientation of the text. */
    private CoordTransform transform;
    /** The segments of text. */
    private List<TextSpan> spans;

    /** The length of the object, minus the header, when it is encoded. */
    private transient int length;
    /** Number of bits used to encode each glyph index. */
    private transient int glyphBits;
    /** Number of bits used to encode each advance. */
    private transient int advanceBits;

    /**
     * Creates and initialises a DefineText2 object using values encoded
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

    public DefineText2(final SWFDecoder coder, final Context context)
            throws IOException {
        length = coder.readUnsignedShort() & Coder.LENGTH_FIELD;
        if (length == Coder.IS_EXTENDED) {
            length = coder.readInt();
        }
        coder.mark();
        identifier = coder.readUnsignedShort();
        bounds = new Bounds(coder);

        /*
         * This code is used to get round a bug in Flash - sometimes 16, 8-bit
         * zeroes are written out before the transform. The root cause in Flash
         * is unknown but seems to be related to the bounds not being set - all
         * values are zero.
         */

        coder.fill();
        coder.mark();
        int sum = 0;
        // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 1 LINES
        for (int i = 0; i < 16; i++) {
            sum += coder.readByte();
        }
        if (sum != 0) {
            coder.reset();
        }
        coder.unmark();

        transform = new CoordTransform(coder);

        glyphBits = coder.readByte();
        advanceBits = coder.readByte();

        context.put(Context.TRANSPARENT, 1);
        context.put(Context.GLYPH_SIZE, glyphBits);
        context.put(Context.ADVANCE_SIZE, advanceBits);

        spans = new ArrayList<TextSpan>();

        while (coder.scanByte() != 0) {
            spans.add(new TextSpan(coder, context));
        }

        coder.readByte();

        context.remove(Context.TRANSPARENT);
        context.put(Context.GLYPH_SIZE, 0);
        context.put(Context.ADVANCE_SIZE, 0);
        coder.check(length);
        coder.unmark();
    }

    /**
     * Creates a DefineText2 object with the specified bounding rectangle,
     * coordinate transform and text records.
     *
     * @param uid
     *            the unique identifier for this object. Must be in the range
     *            1..65535
     * @param rect
     *            the bounding rectangle enclosing the text. Must not be null.
     * @param matrix
     *            an CoordTransform to change the size and orientation of the
     *            text. Must not be null.
     * @param list
     *            a list of TextSpan objects that define the text to be
     *            displayed. Must not be null.
     */
    public DefineText2(final int uid, final Bounds rect,
            final CoordTransform matrix, final List<TextSpan> list) {
        setIdentifier(uid);
        setBounds(rect);
        setTransform(matrix);
        setSpans(list);
    }

    /**
     * Creates and initialises a DefineText2 object using the values copied
     * from another DefineText2 object.
     *
     * @param object
     *            a DefineText2 object from which the values will be
     *            copied.
     */
    public DefineText2(final DefineText2 object) {
        identifier = object.identifier;
        bounds = object.bounds;
        transform = object.transform;
        spans = new ArrayList<TextSpan>(object.spans.size());
        for (final TextSpan span : object.spans) {
            spans.add(span.copy());
        }
    }

    /** {@inheritDoc} */
    public int getIdentifier() {
        return identifier;
    }

    /** {@inheritDoc} */
    public void setIdentifier(final int uid) {
        if ((uid < 1) || (uid > Coder.USHORT_MAX)) {
            throw new IllegalArgumentRangeException(
                    1, Coder.USHORT_MAX, uid);
        }
        identifier = uid;
    }

    /**
     * Add a TextSpan object to the list of text spans.
     *
     * @param obj
     *            an TextSpan object. Must not be null.
     * @return this object.
     */
    public DefineText2 add(final TextSpan obj) {
        if (obj == null) {
            throw new IllegalArgumentException();
        }
        spans.add(obj);
        return this;
    }

    /**
     * Returns the bounding rectangle that completely encloses the text to be
     * displayed.
     *
     * @return the bounding rectangle of the text.
     */
    public Bounds getBounds() {
        return bounds;
    }

    /**
     * Get the coordinate transform that controls the size, location and
     * orientation of the text when it is displayed.
     *
     * @return the coordinate transform used to position the text.
     */
    public CoordTransform getTransform() {
        return transform;
    }

    /**
     * Get the list of text spans that define the text to be displayed.
     *
     * @return the list of text blocks.
     */
    public List<TextSpan> getSpans() {
        return spans;
    }

    /**
     * Sets the bounding rectangle that encloses the text being displayed.
     *
     * @param rect
     *            the bounding rectangle enclosing the text. Must not be null.
     */
    public void setBounds(final Bounds rect) {
        if (rect == null) {
            throw new IllegalArgumentException();
        }
        bounds = rect;
    }

    /**
     * Sets the coordinate transform that changes the orientation and size of
     * the text displayed.
     *
     * @param matrix
     *            an CoordTransform to change the size and orientation of the
     *            text. Must not be null.
     */
    public void setTransform(final CoordTransform matrix) {
        if (matrix == null) {
            throw new IllegalArgumentException();
        }
        transform = matrix;
    }

    /**
     * Sets the list of text spans that define the text to be displayed.
     *
     * @param list
     *            a list of TextSpan objects that define the text to be
     *            displayed. Must not be null.
     */
    public void setSpans(final List<TextSpan> list) {
        if (list == null) {
            throw new IllegalArgumentException();
        }
        spans = list;
    }

    /** {@inheritDoc} */
    public DefineText2 copy() {
        return new DefineText2(this);
    }

    @Override
    public String toString() {
        return String.format(FORMAT, identifier, bounds, transform, spans);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        glyphBits = calculateSizeForGlyphs();
        advanceBits = calculateSizeForAdvances();

        context.put(Context.TRANSPARENT, 1);
        context.put(Context.GLYPH_SIZE, glyphBits);
        context.put(Context.ADVANCE_SIZE, advanceBits);

        length = 2 + bounds.prepareToEncode(context);
        length += transform.prepareToEncode(context);
        length += 2;

        for (final TextSpan span : spans) {
            length += span.prepareToEncode(context);
        }

        length += 1;

        context.remove(Context.TRANSPARENT);
        context.put(Context.GLYPH_SIZE, 0);
        context.put(Context.ADVANCE_SIZE, 0);

        return (length > Coder.HEADER_LIMIT ? Coder.LONG_HEADER
                : Coder.SHORT_HEADER) + length;
    }


    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {

        if (length > Coder.HEADER_LIMIT) {
            coder.writeShort((MovieTypes.DEFINE_TEXT_2
                    << Coder.LENGTH_FIELD_SIZE) | Coder.IS_EXTENDED);
            coder.writeInt(length);
        } else {
            coder.writeShort((MovieTypes.DEFINE_TEXT_2
                    << Coder.LENGTH_FIELD_SIZE) | length);
        }
        if (Constants.DEBUG) {
            coder.mark();
        }
        coder.writeShort(identifier);
        context.put(Context.TRANSPARENT, 1);
        context.put(Context.GLYPH_SIZE, glyphBits);
        context.put(Context.ADVANCE_SIZE, advanceBits);

        bounds.encode(coder, context);
        transform.encode(coder, context);

        coder.writeByte(glyphBits);
        coder.writeByte(advanceBits);

        for (final TextSpan span : spans) {
            span.encode(coder, context);
        }

        coder.writeByte(0);

        context.remove(Context.TRANSPARENT);
        context.put(Context.GLYPH_SIZE, 0);
        context.put(Context.ADVANCE_SIZE, 0);
        if (Constants.DEBUG) {
            coder.check(length);
            coder.unmark();
        }
    }

    /**
     * The number of bits used to encode the glyph indices.
     * @return the number of bits used to encode each glyph index.
     */
    private int calculateSizeForGlyphs() {
        int total = 0;
        int size;

        for (final TextSpan span : spans) {
            size = span.glyphBits();

            if (size > total) {
                total = size;
            }
        }

        return total;
    }

    /**
     * The number of bits used to encode the advances.
     * @return the number of bits used to encode each advance.
     */
    private int calculateSizeForAdvances() {
        int total = 0;
        int size;

        for (final TextSpan span : spans) {
            size = span.advanceBits();

            if (size > total) {
                total = size;
            }
        }

        return total;
    }
}