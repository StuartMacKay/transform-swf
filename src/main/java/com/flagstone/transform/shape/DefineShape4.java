/*
 * DefineShape4.java
 * Transform
 *
 * Copyright (c) 2009-2010 Flagstone Software Ltd. All rights reserved.
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

package com.flagstone.transform.shape;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.flagstone.transform.Constants;
import com.flagstone.transform.MovieTypes;
import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.coder.SWFFactory;
import com.flagstone.transform.datatype.Bounds;
import com.flagstone.transform.exception.IllegalArgumentRangeException;
import com.flagstone.transform.fillstyle.FillStyle;
import com.flagstone.transform.linestyle.LineStyle;
import com.flagstone.transform.linestyle.LineStyle1;
import com.flagstone.transform.linestyle.LineStyle2;

/**
 * DefineShape4 extends DefienShape3 by specifying the bounding box for the
 * edges of the shape (the outline without taking the stroke thickness into
 * account) as well as hints for improving the way shapes are drawn.
 */
public class DefineShape4 implements ShapeTag {

    /**
     * Reserved length for style counts indicated that the number of line
     * or fill styles is encoded in the next 16-bit word.
     */
    private static final int EXTENDED = 255;
    /** Number of bytes used to encode the number of styles. */
    private static final int EXTENDED_LENGTH = 3;

    /** Format string used in toString() method. */
    private static final String FORMAT = "DefineShape4: { identifier=%d;"
            + " shapeBounds=%s; edgeBounds=%s; fillStyles=%s; lineStyles=%s;"
            + " winding=%b; shape=%s}";

    /** The unique identifier for this object. */
    private int identifier;
    /** The bounding box for the shape at the start of the morph. */
    private Bounds bounds;
    /** The bounding box for the shape, excluding line widths at the start. */
    private Bounds edgeBounds;
    /** The list of fill styles for the shape. */
    private List<FillStyle> fillStyles;
    /** The list of line styles for the shape. */
    private List<LineStyle> lineStyles;
    /** The shape. */
    private Shape shape;

    /** Indicates whether fill winding is used. */
    private transient int winding;
    /** Indicates whether any of the line styles contain scaling strokes. */
    private transient int scaling;

    /** The length of the object, minus the header, when it is encoded. */
    private transient int length;
    /** The number of bits to encode indices into the fill style list. */
    private transient int fillBits;
    /** The number of bits to encode indices into the line style list. */
    private transient int lineBits;

    /**
     * Creates and initialises a DefineShape4 object using values encoded
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
    public DefineShape4(final SWFDecoder coder, final Context context)
            throws IOException {
        length = coder.readUnsignedShort() & Coder.LENGTH_FIELD;
        if (length == Coder.IS_EXTENDED) {
            length = coder.readInt();
        }
        coder.mark();
        identifier = coder.readUnsignedShort();
        context.put(Context.TRANSPARENT, 1);
        context.put(Context.TYPE, MovieTypes.DEFINE_SHAPE_4);

        bounds = new Bounds(coder);
        edgeBounds = new Bounds(coder);

        // scaling hints are implied by the line styles used
        winding = coder.readByte() & Coder.BIT2;

        int fillStyleCount = coder.readByte();

        if (fillStyleCount == EXTENDED) {
            fillStyleCount = coder.readUnsignedShort();
        }
        fillStyles = new ArrayList<FillStyle>();
        lineStyles = new ArrayList<LineStyle>();

        final SWFFactory<FillStyle> decoder = context.getRegistry()
                .getFillStyleDecoder();

        for (int i = 0; i < fillStyleCount; i++) {
            decoder.getObject(fillStyles, coder, context);
        }

        int lineStyleCount = coder.readByte();

        if (lineStyleCount == EXTENDED) {
            lineStyleCount = coder.readUnsignedShort();
        }

        for (int i = 0; i < lineStyleCount; i++) {
            lineStyles.add(new LineStyle2(coder, context));
        }

        context.put(Context.ARRAY_EXTENDED, 1);

        if (context.getRegistry().getShapeDecoder() == null) {
            shape = new Shape();
            shape.add(new ShapeData(length - coder.bytesRead(), coder));
        } else {
            shape = new Shape(coder, context);
        }

        context.remove(Context.TRANSPARENT);
        context.remove(Context.ARRAY_EXTENDED);
        context.remove(Context.TYPE);
        coder.check(length);
        coder.unmark();
    }

    /**
     * Creates a DefineShape3 object.
     *
     * @param uid
     *            the unique identifier for the shape in the range 1..65535.
     * @param rect
     *            the bounding rectangle for the shape including the width of
     *            the border lines. Must not be null.
     * @param edges
     *            the bounding rectangle for the shape excluding the line used
     *            to draw the border. Must not be null.
     * @param fills
     *            the list of fill styles used in the shape. Must not be null.
     * @param lines
     *            the list of line styles used in the shape. Must not be null.
     * @param aShape
     *            the shape to be drawn. Must not be null.
     */
    public DefineShape4(final int uid, final Bounds rect, final Bounds edges,
            final List<FillStyle> fills, final List<LineStyle> lines,
            final Shape aShape) {
        setIdentifier(uid);
        setBounds(rect);
        setEdgeBounds(edges);
        setFillStyles(fills);
        setLineStyles(lines);
        setShape(aShape);
    }

    /**
     * Creates and initialises a DefineShape4 object using the values copied
     * from another DefineShape4 object.
     *
     * @param object
     *            a DefineShape4 object from which the values will be
     *            copied.
     */
    public DefineShape4(final DefineShape4 object) {
        identifier = object.identifier;
        bounds = object.bounds;
        edgeBounds = object.edgeBounds;
        fillStyles = new ArrayList<FillStyle>(object.fillStyles.size());
        for (final FillStyle style : object.fillStyles) {
            fillStyles.add(style.copy());
        }
        lineStyles = new ArrayList<LineStyle>(object.lineStyles.size());
        for (final LineStyle style : object.lineStyles) {
            lineStyles.add(style.copy());
        }
        shape = object.shape.copy();
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
     * Get the bounding rectangle that completely enclosed the shape.
     *
     * @return the Bounds that encloses the shape
     */
    public Bounds getBounds() {
        return bounds;
    }

    /**
     * Sets the bounding rectangle that encloses the shape.
     *
     * @param rect
     *            set the bounding rectangle for the shape. Must not be null.
     */
    public void setBounds(final Bounds rect) {
        if (rect == null) {
            throw new IllegalArgumentException();
        }
        bounds = rect;
    }

    /**
     * Get the bounding rectangle that covers the outline of the shape.
     *
     * @return the Bounds that encloses the shape outline, excluding any
     * lines drawn.
     */
    public Bounds getEdgeBounds() {
        return edgeBounds;
    }

    /**
     * Sets the bounding rectangle that encloses the outline of the shape,
     * excluding the width of any lines drawn.
     *
     * @param rect
     *            set the bounding rectangle for the shape. Must not be null.
     */
    public void setEdgeBounds(final Bounds rect) {
        if (rect == null) {
            throw new IllegalArgumentException();
        }
        edgeBounds = rect;
    }

    /**
     * Add a LineStyle to the list of line styles.
     *
     * @param style
     *            a LineStyle2 object. Must not be null or an instance of
     *            LineStyle1.
     *
     * @return this object.
     */
    public DefineShape4 add(final LineStyle style) {
        if (style == null || style instanceof LineStyle1) {
            throw new IllegalArgumentException();
        }
        lineStyles.add(style);
        return this;
    }

    /**
     * Add the fill style to the list of fill styles.
     *
     * @param style
     *            and FillStyle object. Must not be null.
     *
     * @return this object.
     */
    public DefineShape4 add(final FillStyle style) {
        if (style == null) {
            throw new IllegalArgumentException();
        }
        fillStyles.add(style);
        return this;
    }

    /**
     * Get the list fill styles.
     *
     * @return the list of fill styles used in the shape.
     */
    public List<FillStyle> getFillStyles() {
        return fillStyles;
    }

    /**
     * Get the list line styles.
     *
     * @return the list of line styles used in the shape.
     */
    public List<LineStyle> getLineStyles() {
        return lineStyles;
    }

    /**
     * Get the shape.
     *
     * @return the shape.
     */
    public Shape getShape() {
        return shape;
    }

    /**
     * Sets the list fill styles that will be used to draw the shape.
     *
     * @param list
     *            set the fill styles for the shape. Must not be null.
     */
    public void setFillStyles(final List<FillStyle> list) {
        if (list == null) {
            throw new IllegalArgumentException();
        }
        fillStyles = list;
    }

    /**
     * Sets the list of styles that will be used to draw the outline of the
     * shape.
     *
     * @param list
     *            set the line styles for the shape. Must not be null.
     */
    public void setLineStyles(final List<LineStyle> list) {
        if (list == null) {
            throw new IllegalArgumentException();
        }
        lineStyles = list;
    }

    /**
     * Sets the shape.
     *
     * @param aShape
     *            set the shape to be drawn. Must not be null.
     */
    public void setShape(final Shape aShape) {
        if (aShape == null) {
            throw new IllegalArgumentException();
        }
        shape = aShape;
    }

    /**
     * Does the shape use fill winding.
     * @return true if fill winding is used, false otherwise.
     */
    public boolean useWinding() {
        return winding != 0;
    }

    /**
     * Indicates whether the shape uses fill winding.
     * @param use true if fill winding is used, false otherwise.
     */
    public void setWinding(final boolean use) {
        if (use) {
            winding = Coder.BIT2;
        } else {
            winding = 0;
        }
    }

    /** {@inheritDoc} */
    public DefineShape4 copy() {
        return new DefineShape4(this);
    }

    @Override
    public String toString() {
        return String.format(FORMAT, identifier, bounds, edgeBounds,
                fillStyles, lineStyles, useWinding(), shape);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("PMD.NPathComplexity")
    public int prepareToEncode(final Context context) {
        fillBits = Coder.unsignedSize(fillStyles.size());
        lineBits = Coder.unsignedSize(lineStyles.size());

        if (context.contains(Context.POSTSCRIPT)) {
            if (fillBits == 0) {
                fillBits = 1;
            }

            if (lineBits == 0) {
                lineBits = 1;
            }
        }

        context.put(Context.TRANSPARENT, 1);
        // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 1 LINES
        length = 3;
        length += bounds.prepareToEncode(context);
        length += edgeBounds.prepareToEncode(context);

        length += (fillStyles.size() >= EXTENDED) ? EXTENDED_LENGTH : 1;

        for (final FillStyle style : fillStyles) {
            length += style.prepareToEncode(context);
        }

        context.put(Context.SCALING_STROKE, 0);

        length += (lineStyles.size() >= EXTENDED) ? EXTENDED_LENGTH : 1;

        for (final LineStyle style : lineStyles) {
            length += style.prepareToEncode(context);
        }

        scaling = context.get(Context.SCALING_STROKE);

        context.put(Context.ARRAY_EXTENDED, 1);
        context.put(Context.FILL_SIZE, fillBits);
        context.put(Context.LINE_SIZE, lineBits);

        length += shape.prepareToEncode(context);

        context.remove(Context.ARRAY_EXTENDED);
        context.put(Context.FILL_SIZE, 0);
        context.put(Context.LINE_SIZE, 0);
        context.remove(Context.TRANSPARENT);
        context.remove(Context.SCALING_STROKE);

        return (length > Coder.HEADER_LIMIT ? Coder.LONG_HEADER
                : Coder.SHORT_HEADER) + length;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        context.put(Context.TRANSPARENT, 1);

        if (length > Coder.HEADER_LIMIT) {
            coder.writeShort((MovieTypes.DEFINE_SHAPE_4
                    << Coder.LENGTH_FIELD_SIZE) | Coder.IS_EXTENDED);
            coder.writeInt(length);
        } else {
            coder.writeShort((MovieTypes.DEFINE_SHAPE_4
                    << Coder.LENGTH_FIELD_SIZE) | length);
        }
        if (Constants.DEBUG) {
            coder.mark();
        }
        coder.writeShort(identifier);

        bounds.encode(coder, context);
        edgeBounds.encode(coder, context);

        coder.writeByte(winding | scaling);

        if (fillStyles.size() >= EXTENDED) {
            coder.writeByte(EXTENDED);
            coder.writeShort(fillStyles.size());
        } else {
            coder.writeByte(fillStyles.size());
        }

        for (final FillStyle style : fillStyles) {
            style.encode(coder, context);
        }

        if (lineStyles.size() >= EXTENDED) {
            coder.writeByte(EXTENDED);
            coder.writeShort(lineStyles.size());
        } else {
            coder.writeByte(lineStyles.size());
        }

        for (final LineStyle style : lineStyles) {
            style.encode(coder, context);
        }

        context.put(Context.ARRAY_EXTENDED, 1);
        context.put(Context.FILL_SIZE, fillBits);
        context.put(Context.LINE_SIZE, lineBits);

        shape.encode(coder, context);

        context.remove(Context.ARRAY_EXTENDED);
        context.put(Context.FILL_SIZE, 0);
        context.put(Context.LINE_SIZE, 0);
        context.remove(Context.TRANSPARENT);
        if (Constants.DEBUG) {
            coder.check(length);
            coder.unmark();
        }
    }
}
