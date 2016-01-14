/*
 * DefineMorphShape.java
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
import com.flagstone.transform.linestyle.MorphLineStyle;

/**
 * DefineMorphShape defines a shape that will morph from one form into another.
 *
 * <p>
 * Only the start and end shapes are defined the Flash Player will perform the
 * interpolation that transforms the shape at each staging in the morphing
 * process.
 * </p>
 *
 * <p>
 * Morphing can be applied to any shape, however there are a few restrictions:
 * </p>
 *
 * <ul>
 * <li>The start and end shapes must have the same number of edges (Line and
 * Curve objects).</li>
 * <li>The fill style (Solid, Bitmap or Gradient) must be the same in the start
 * and end shape.</li>
 * <li>If a bitmap fill style is used then the same image must be used in the
 * start and end shapes.</li>
 * <li>If a gradient fill style is used then the gradient must contain the same
 * number of points in the start and end shape.</li>
 * <li>Start and end shape must contain the same set of ShapeStyle objects.</li>
 * </ul>
 *
 * <p>
 * To perform the morphing of a shape the shape is placed in the display list
 * using a PlaceObject2 object. The ratio attribute in the PlaceObject2 object
 * defines the progress of the morphing process. The ratio ranges between 0 and
 * 65535 where 0 represents the start of the morphing process and 65535, the
 * end.
 * </p>
 *
 * <p>
 * The edges in the shapes may change their type when a shape is morphed.
 * Straight edges can become curves and vice versa.
 * </p>
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
public class DefineMorphShape implements ShapeTag {

    /**
     * Reserved length for style counts indicated that the number of line
     * or fill styles is encoded in the next 16-bit word.
     */
    private static final int EXTENDED = 255;
    /** Number of bytes used to encode the number of styles. */
    private static final int EXTENDED_LENGTH = 3;


    /** Format string used in toString() method. */
    private static final String FORMAT = "DefineMorphShape: { identifier=%d;"
            + " startBounds=%s; endBounds=%s; fillStyles=%s; lineStyles=%s;"
            + " startShape=%s; endShape=%s}";

    /** The unique identifier for this object. */
    private int identifier;
    /** The bounding box for the shape at the start of the morphing process. */
    private Bounds bounds;
    /** The bounding box for the shape at the end of the morphing process. */
    private Bounds endBounds;

    /** The list of fill styles for the shape. */
    private List<FillStyle> fillStyles;
    /** The list of line styles for the shape. */
    private List<LineStyle> lineStyles;
    /** The shape at the start of the morphing process. */
    private Shape shape;
    /** The shape at the end of the morphing process. */
    private Shape endShape;

    /** The length of the object, minus the header, when it is encoded. */
    private transient int length;
    /** The number of bits to encode indices into the fill style list. */
    private transient int fillBits;
    /** The number of bits to encode indices into the line style list. */
    private transient int lineBits;
    /** Offset in bytes to the end shape. */
    private transient int offset;

    /**
     * Creates and initialises a DefineMorphShape object using values encoded
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

    public DefineMorphShape(final SWFDecoder coder, final Context context)
            throws IOException {
        length = coder.readUnsignedShort() & Coder.LENGTH_FIELD;
        if (length == Coder.IS_EXTENDED) {
            length = coder.readInt();
        }
        coder.mark();
        coder.mark();
        context.put(Context.TRANSPARENT, 1);
        context.put(Context.ARRAY_EXTENDED, 1);
        context.put(Context.TYPE, MovieTypes.DEFINE_MORPH_SHAPE);

        identifier = coder.readUnsignedShort();

        bounds = new Bounds(coder);
        endBounds = new Bounds(coder);
        fillStyles = new ArrayList<FillStyle>();
        lineStyles = new ArrayList<LineStyle>();

        final int offsetToEnd = coder.readInt();
        coder.mark();

        int fillStyleCount = coder.readByte();

        if (context.contains(Context.ARRAY_EXTENDED)
                && (fillStyleCount == EXTENDED)) {
            fillStyleCount = coder.readUnsignedShort();
        }

        final SWFFactory<FillStyle> decoder = context.getRegistry()
                .getMorphFillStyleDecoder();

        for (int i = 0; i < fillStyleCount; i++) {
            decoder.getObject(fillStyles, coder, context);
        }

        int lineStyleCount = coder.readByte();

        if (context.contains(Context.ARRAY_EXTENDED)
                && (lineStyleCount == EXTENDED)) {
            lineStyleCount = coder.readUnsignedShort();
        }

        for (int i = 0; i < lineStyleCount; i++) {
            lineStyles.add(new MorphLineStyle(coder, context));
        }

        int skipLimit;

        if (context.getRegistry().getShapeDecoder() == null) {
            skipLimit = 37;

            int size;
            if (offsetToEnd == 0) {
                size = 0;
            } else {
                size = offsetToEnd - coder.bytesRead();
            }
            coder.unmark();

            shape = new Shape();
            shape.add(new ShapeData(size, coder));

            if (offsetToEnd == 0) {
                size = 0;
            } else {
                size = length - coder.bytesRead();
            }
            coder.unmark();

            endShape = new Shape();
            endShape.add(new ShapeData(size, coder));
        } else {
            skipLimit = 33;

            shape = new Shape(coder, context);
            endShape = new Shape(coder, context);
            coder.unmark();
            coder.unmark();
        }

        context.remove(Context.TRANSPARENT);
        context.put(Context.ARRAY_EXTENDED, 1);
        context.remove(Context.TYPE);

        // known bug - empty objects may be added to Flash file.
        // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 2 LINES
        if (length - coder.bytesRead() == skipLimit) {
            coder.skip(skipLimit);
        } else {
            coder.check(length);
        }
        coder.unmark();
    }

    /**
     * Creates a DefineMorphShape object.
     *
     * @param uid
     *            an unique identifier for this object. Must be in the range
     *            1..65535.
     * @param startRect
     *            the bounding rectangle enclosing the start shape. Must not be
     *            null.
     * @param endRect
     *            the bounding rectangle enclosing the end shape. Must not be
     *            null.
     * @param fills
     *            a list of MorphSolidFill, MorphBitmapFill and
     *            MorphGradientFill objects. Must not be null.
     * @param lines
     *            a list of MorphLineStyle objects. Must not be null.
     * @param initialShape
     *            the shape at the start of the morphing process. Must not be
     *            null.
     * @param finalShape
     *            the shape at the end of the morphing process. Must not be
     *            null.
     */
    public DefineMorphShape(final int uid, final Bounds startRect,
            final Bounds endRect, final List<FillStyle> fills,
            final List<LineStyle> lines, final Shape initialShape,
            final Shape finalShape) {
        setIdentifier(uid);
        setBounds(startRect);
        setEndBounds(endRect);
        setFillStyles(fills);
        setLineStyles(lines);
        setShape(initialShape);
        setEndShape(finalShape);
    }

    /**
     * Creates and initialises a DefineMorphShape object using the values copied
     * from another DefineMorphShape object.
     *
     * @param object
     *            a DefineMorphShape object from which the values will be
     *            copied.
     */
    public DefineMorphShape(final DefineMorphShape object) {
        identifier = object.identifier;
        bounds = object.bounds;
        endBounds = object.endBounds;
        fillStyles = new ArrayList<FillStyle>(object.fillStyles.size());
        for (final FillStyle style : object.fillStyles) {
            fillStyles.add(style.copy());
        }
        lineStyles = new ArrayList<LineStyle>(object.lineStyles.size());
        for (final LineStyle style : object.lineStyles) {
            lineStyles.add(style.copy());
        }
        shape = object.shape.copy();
        endShape = object.endShape.copy();
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
     * Add a LineStyle object to the list of line styles.
     *
     * @param style
     *            a MorphLineStyle object. Must not be null. Must be an instance
     *            of MorphLineStyle.
     *
     * @return this object.
     */
    @Override
	public DefineMorphShape add(final LineStyle style) {
        if (!(style instanceof MorphLineStyle)) {
            throw new IllegalArgumentException();
        }
        lineStyles.add(style);
        return this;
    }

    /**
     * Add the fill style object to the list of fill styles.
     *
     * @param aFillStyle
     *            an FillStyle object. Must not be null.
     *
     * @return this object.
     */
    @Override
	public DefineMorphShape add(final FillStyle aFillStyle) {
        fillStyles.add(aFillStyle);
        return this;
    }

    /**
     * Get the Bounds object that defines the bounding rectangle enclosing
     * the start shape.
     *
     * @return the bounding box for the starting shape.
     */
    @Override
	public Bounds getBounds() {
        return bounds;
    }

    /**
     * Get the Bounds object that defines the bounding rectangle enclosing
     * the end shape.
     *
     * @return the bounding box for the final shape.
     */
    public Bounds getEndBounds() {
        return endBounds;
    }

    /**
     * Get the list of fill styles (MorphSolidFill, MorphBitmapFill and
     * MorphGradientFill objects) for the shapes.
     *
     * @return the list of fill styles used in the shape.
     */
    @Override
	public List<FillStyle> getFillStyles() {
        return fillStyles;
    }

    /**
     * Get the list of line styles (MorphLineStyle objects) for the shapes.
     *
     * @return the list of line styles used in the shape.
     */
    @Override
	public List<LineStyle> getLineStyles() {
        return lineStyles;
    }

    /**
     * Get shape displayed at the start of the morphing process.
     *
     * @return the starting shape.
     */
    @Override
	public Shape getShape() {
        return shape;
    }

    /**
     * Get shape displayed at the end of the morphing process.
     *
     * @return the final shape.
     */
    public Shape getEndShape() {
        return endShape;
    }

    /**
     * Sets the starting bounds of the shape.
     *
     * @param rect
     *            the bounding rectangle enclosing the start shape. Must not be
     *            null.
     */
    @Override
	public void setBounds(final Bounds rect) {
        if (rect == null) {
            throw new IllegalArgumentException();
        }
        bounds = rect;
    }

    /**
     * Sets the ending bounds of the shape.
     *
     * @param rect
     *            the bounding rectangle enclosing the end shape. Must not be
     *            null.
     */
    public void setEndBounds(final Bounds rect) {
        if (rect == null) {
            throw new IllegalArgumentException();
        }
        endBounds = rect;
    }

    /**
     * Sets the list of morph fill styles.
     *
     * @param list
     *            a list of MorphSolidFill, MorphBitmapFill and
     *            MorphGradientFill objects. Must not be null.
     */
    @Override
	public void setFillStyles(final List<FillStyle> list) {
        if (list == null) {
            throw new IllegalArgumentException();
        }
        fillStyles = list;
    }

    /**
     * Sets the list of morph line styles.
     *
     * @param list
     *            a list of MorphLineStyle objects. Must not be null.
     */
    @Override
	public void setLineStyles(final List<LineStyle> list) {
        if (list == null) {
            throw new IllegalArgumentException();
        }
        lineStyles = list;
    }

    /**
     * Sets the shape that will be displayed at the start of the morphing
     * process.
     *
     * @param aShape
     *            the shape at the start of the morphing process. Must not be
     *            null.
     */
    @Override
	public void setShape(final Shape aShape) {
        if (aShape == null) {
            throw new IllegalArgumentException();
        }
        shape = aShape;
    }

    /**
     * Sets the shape that will be displayed at the end of the morphing process.
     *
     * @param aShape
     *            the shape at the end of the morphing process. Must not be
     *            null.
     */
    public void setEndShape(final Shape aShape) {
        if (aShape == null) {
            throw new IllegalArgumentException();
        }
        endShape = aShape;
    }

    /** {@inheritDoc} */
    @Override
	public DefineMorphShape copy() {
        return new DefineMorphShape(this);
    }

    @Override
    public String toString() {
        return String.format(FORMAT, identifier, bounds, endBounds,
                fillStyles, lineStyles, shape, endShape);
    }


    /** {@inheritDoc} */
    @Override
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
        length = 6 + bounds.prepareToEncode(context);
        length += endBounds.prepareToEncode(context);
        offset = length;

        length += (fillStyles.size() >= EXTENDED) ? EXTENDED_LENGTH : 1;

        for (final FillStyle style : fillStyles) {
            length += style.prepareToEncode(context);
        }

        length += (lineStyles.size() >= EXTENDED) ? EXTENDED_LENGTH : 1;

        for (final LineStyle style : lineStyles) {
            length += style.prepareToEncode(context);
        }

        context.put(Context.ARRAY_EXTENDED, 1);
        context.put(Context.FILL_SIZE, fillBits);
        context.put(Context.LINE_SIZE, lineBits);

        length += shape.prepareToEncode(context);
        offset = length - offset;
        // Number of Fill and Line bits is zero for end shape.
        context.put(Context.FILL_SIZE, 0);
        context.put(Context.LINE_SIZE, 0);

        length += endShape.prepareToEncode(context);

        context.remove(Context.ARRAY_EXTENDED);
        context.remove(Context.TRANSPARENT);

        return (length > Coder.HEADER_LIMIT ? Coder.LONG_HEADER
                : Coder.SHORT_HEADER) + length;
    }


    /** {@inheritDoc} */
    @Override
	public void encode(final SWFEncoder coder, final Context context)
            throws IOException {

        if (length > Coder.HEADER_LIMIT) {
            coder.writeShort((MovieTypes.DEFINE_MORPH_SHAPE
                    << Coder.LENGTH_FIELD_SIZE) | Coder.IS_EXTENDED);
            coder.writeInt(length);
        } else {
            coder.writeShort((MovieTypes.DEFINE_MORPH_SHAPE
                    << Coder.LENGTH_FIELD_SIZE) | length);
        }
        if (Constants.DEBUG) {
            coder.mark();
        }
        coder.writeShort(identifier);
        context.put(Context.TRANSPARENT, 1);

        bounds.encode(coder, context);
        endBounds.encode(coder, context);
        coder.writeInt(offset);

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

        // Number of Fill and Line bits is zero for end shape.
        context.put(Context.FILL_SIZE, 0);
        context.put(Context.LINE_SIZE, 0);

        endShape.encode(coder, context);

        context.remove(Context.ARRAY_EXTENDED);
        context.remove(Context.TRANSPARENT);
        if (Constants.DEBUG) {
            coder.check(length);
            coder.unmark();
        }
    }
}
