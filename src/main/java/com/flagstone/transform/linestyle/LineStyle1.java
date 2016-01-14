/*
 * LineStyle.java
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

package com.flagstone.transform.linestyle;


import java.io.IOException;

import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.datatype.Color;
import com.flagstone.transform.exception.IllegalArgumentRangeException;

/**
 * LineStyle1 defines the width and colour of a line that is used when drawing
 * the outline of a shape.
 *
 * <p>
 * All lines are drawn with rounded corners and end caps. Different join and
 * line end styles can be created by drawing line segments as a sequence of
 * filled shapes. With 1 twip equal to 1/20th of a pixel this technique can
 * easily be used to draw the narrowest of visible lines. Note that specific
 * join and cap styles can be specified with the {@link LineStyle2} class.
 * </p>
 *
 * <p>
 * Whether the alpha channel in the colour is used is determined by the class
 * used to define the shape. Transparent colours are only supported from Flash 3
 * onwards. Simply specifying the level of transparency in the Color object is
 * not sufficient.
 * </p>
 *
 * <p>
 * Flash only supports contiguous lines. Dashed line styles can be created by
 * drawing the line as a series of short line segments by interspersing
 * ShapeStyle objects to move the current point in between the Line objects that
 * draw the line segments.
 * </p>
 *
 * @see LineStyle2
 */
public class LineStyle1 implements LineStyle {

    /** Format string used in toString() method. */
    private static final String FORMAT = "LineStyle : { width=%d; color=%s}";

    /** Width of the line in twips. */
    private int width;
    /** Colour used to draw the line. */
    private Color color;

    /**
     * Creates and initialises a LineStyle object using values encoded
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
    public LineStyle1(final SWFDecoder coder, final Context context)
            throws IOException {
        width = coder.readUnsignedShort();
        color = new Color(coder, context);
    }

    /**
     * Creates a LineStyle, specifying the width and colour of the line.
     *
     * @param aWidth
     *            the width of the line. Must be in the range 0..65535.
     * @param aColor
     *            the colour of the line. Must not be null.
     */
    public LineStyle1(final int aWidth, final Color aColor) {
        setWidth(aWidth);
        setColor(aColor);
    }

    /**
     * Creates and initialises a LineStyle object using the values copied
     * from another LineStyle object.
     *
     * @param object
     *            a LineStyle object from which the values will be
     *            copied.
     */
    public LineStyle1(final LineStyle1 object) {
        width = object.width;
        color = object.color;
    }

    /**
     * Get the width of the line.
     *
     * @return the stroke width.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get the colour of the line.
     *
     * @return the line colour.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets the width of the line.
     *
     * @param aNumber
     *            the width of the line. Must be in the range 0..65535.
     */
    public void setWidth(final int aNumber) {
        if ((aNumber < 0) || (aNumber > Coder.USHORT_MAX)) {
            throw new IllegalArgumentRangeException(
                    0, Coder.USHORT_MAX, aNumber);
        }
        width = aNumber;
    }

    /**
     * Sets the colour of the line.
     *
     * @param aColor
     *            the colour of the line. Must be not be null.
     */
    public void setColor(final Color aColor) {
        if (aColor == null) {
            throw new IllegalArgumentException();
        }
        color = aColor;
    }

    /** {@inheritDoc} */
    public LineStyle1 copy() {
        return new LineStyle1(this);
    }

    @Override
    public String toString() {
        return String.format(FORMAT, width, color);
    }


    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 1 LINES
        return 2 + (context.contains(Context.TRANSPARENT) ? 4 : 3);
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        coder.writeShort(width);
        color.encode(coder, context);
    }
}
