/*
 * LineStyle2.java
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

package com.flagstone.transform.linestyle;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.coder.SWFFactory;
import com.flagstone.transform.datatype.Color;
import com.flagstone.transform.exception.IllegalArgumentRangeException;
import com.flagstone.transform.fillstyle.FillStyle;

/**
 * LineStyle2 extends LineStyle1 by supporting different styles for line joins
 * and line ends, a fill style for the stroke and whether the stroke thickness
 * is scaled if an object is resized.
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
public class LineStyle2 implements LineStyle {

    /** Format string used in toString() method. */
    private static final String FORMAT = "LineStyle2: { width=%d; color=%s;"
            + " fillStyle=%s; startCap=%s; endCap=%s; joinStyle=%s;"
            + " scaledHorizontally=%b; scaledVertically=%b;"
            + " pixelAligned=%b; lineClosed=%b; miterLimit=%d}";

    /** Width of the line in twips. */
    private int width;
    /** Colour used to draw the line. */
    private Color color;

    /** Code for the cap style used for the start of the line. */
    private int startCap;
    /** Code for the cap style used for the end of the line. */
    private int endCap;
    /** Code for the style used to join two line together. */
    private int joinStyle;
    /** Fill style used to draw the stroke. */
    private FillStyle fillStyle;

    /** Does the line allow scaling horizontally. */
    private boolean horizontal;
    /** Does the line allow scaling vertically. */
    private boolean vertical;
    /** Is the line drawn along pixel boundaries. */
    private boolean pixelAligned;
    /** Should the line be closed if the start and end points coincide. */
    private boolean lineClosed;
    /** Parameter controlling the mitering when joining two lines. */
    private int miterLimit;

    /** Indicates the style contains a fill style. */
    private transient boolean hasFillStyle;
    /** Indicates the style contains a mitering limit. */
    private transient boolean hasMiter;

    /**
     * Creates and initialises a LineStyle2 object using values encoded
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
    public LineStyle2(final SWFDecoder coder, final Context context)
            throws IOException {

        width = coder.readUnsignedShort();

        int bits = coder.readByte();
        if ((bits & Coder.BIT6) > 0) {
            startCap = 1;
        } else if ((bits & Coder.BIT7) > 0) {
            startCap = 2;
        } else {
            startCap = 0;
        }

        if ((bits & Coder.BIT4) > 0) {
            joinStyle = 1;
            hasMiter = false;
        } else if ((bits & Coder.BIT5) > 0) {
            joinStyle = 2;
            hasMiter = true;
        } else {
            joinStyle = 0;
            hasMiter = false;
        }

        hasFillStyle = (bits & Coder.BIT3) != 0;
        horizontal = (bits & Coder.BIT2) == 0;
        vertical = (bits & Coder.BIT1) == 0;
        pixelAligned = (bits & Coder.BIT0) != 0;

        bits = coder.readByte();
        lineClosed = (bits & Coder.BIT2) == 0;
        endCap = bits & Coder.PAIR0;

        if (hasMiter) {
            coder.readUnsignedShort();
        }

        if (hasFillStyle) {
            final SWFFactory<FillStyle> decoder = context.getRegistry()
                    .getFillStyleDecoder();
            final List<FillStyle> styles = new ArrayList<FillStyle>();
            decoder.getObject(styles, coder, context);
            fillStyle = styles.get(0);
        } else {
            color = new Color(coder, context);
        }
    }

    /**
     * Create a new LineStyle2 object with the stroke thickness and color.
     * @param lineWidth the width of the line.
     * @param lineColor the colour used to draw the line.
     */
    public LineStyle2(final int lineWidth, final Color lineColor) {
        super();

        setWidth(lineWidth);
        setColor(lineColor);

        vertical = true;
        vertical = true;
        lineClosed = true;
    }

    /**
     * Create a new LineStyle2 object with the stroke thickness and fill style.
     * @param lineWidth the width of the line.
     * @param style the fill style used to draw the line.
     */
    public LineStyle2(final int lineWidth, final FillStyle style) {
        super();

        setWidth(lineWidth);
        setFillStyle(style);

        vertical = true;
        vertical = true;
        lineClosed = true;
    }

     /**
     * Creates and initialises a LineStyle2 object using the values copied
     * from another LineStyle2 object.
     *
     * @param object
     *            a LineStyle2 object from which the values will be
     *            copied.
     */
    public LineStyle2(final LineStyle2 object) {
        width = object.width;
        color = object.color;

        if (object.fillStyle != null) {
            fillStyle = object.fillStyle.copy();
        }

        startCap = object.startCap;
        endCap = object.endCap;
        joinStyle = object.joinStyle;

        horizontal = object.horizontal;
        vertical = object.vertical;
        pixelAligned = object.pixelAligned;
        lineClosed = object.lineClosed;
        miterLimit = object.miterLimit;
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
     * Sets the width of the line.
     *
     * @param thickness
     *            the width of the line. Must be in the range 0..65535.
     */
    public void setWidth(final int thickness) {
        if ((thickness < 0) || (thickness > Coder.USHORT_MAX)) {
            throw new IllegalArgumentRangeException(0,
                    Coder.USHORT_MAX, thickness);
        }
        width = thickness;
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

    /**
     * Get the CapStyle used for the start of the line.
     * @return the CapStyle that specifies how the start of the line is drawn.
     */
    public CapStyle getStartCap() {
        CapStyle style;
        if (startCap == 1) {
            style = CapStyle.NONE;
        } else if (startCap == 2) {
            style = CapStyle.SQUARE;
        } else {
            style = CapStyle.ROUND;
        }
        return style;
    }

    /**
     * Set the CapStyle used for the start of the line.
     * @param style the CapStyle that specifies how the start of the line
     * is drawn.
     */
    public void setStartCap(final CapStyle style) {
        switch (style) {
        case NONE:
            startCap = 1;
            break;
        case SQUARE:
            startCap = 2;
            break;
        default:
            startCap = 0;
            break;
        }
    }

    /**
     * Get the CapStyle used for the end of the line.
     * @return the CapStyle that specifies how the end of the line is drawn.
     */
   public CapStyle getEndCap() {
        CapStyle style;
        if (endCap == 1) {
            style = CapStyle.NONE;
        } else if (endCap == 2) {
            style = CapStyle.SQUARE;
        } else {
            style = CapStyle.ROUND;
        }
        return style;
    }

   /**
    * Set the CapStyle used for the end of the line.
    * @param style the CapStyle that specifies how the end of the line
    * is drawn.
    */
    public void setEndCap(final CapStyle style) {
        switch (style) {
        case NONE:
            endCap = 1;
            break;
        case SQUARE:
            endCap = 2;
            break;
        default:
            endCap = 0;
            break;
        }
    }

    /**
     * Get the JoinStyle used when joining with another line or curve.
     * @return the JoinStyle used to connect with another line or curve.
     */
    public JoinStyle getJoinStyle() {
        JoinStyle style;
        if (endCap == 1) {
            style = JoinStyle.BEVEL;
        } else if (endCap == 2) {
            style = JoinStyle.MITER;
        } else {
            style = JoinStyle.ROUND;
        }
        return style;
    }

    /**
     * Set the JoinStyle used when joining with another line or curve.
     * @param style the JoinStyle used to connect with another line or curve.
     */
    public void setJoinStyle(final JoinStyle style) {
        switch (style) {
        case BEVEL:
            joinStyle = 1;
            break;
        case MITER:
            joinStyle = 2;
            break;
        default:
            joinStyle = 0;
            break;
        }
    }

    /**
     * Is the stroke scaled horizontally if the shape is redrawn.
     * @return true if the stroke is scaled horizontally, false if the stroke
     * thickness does not change.
     */
    public boolean isHorizontal() {
        return horizontal;
    }

    /**
     * Indicates whether the stroke is scaled horizontally if the shape is
     * redrawn.
     * @param scale true if the stroke is scaled horizontally, false if the
     * stroke thickness does not change.
     */
    public void setHorizontal(final boolean scale) {
        horizontal = scale;
    }

    /**
     * Is the stroke scaled vertically if the shape is redrawn.
     * @return true if the stroke is scaled vertically, false if the stroke
     * thickness does not change.
     */
    public boolean isVertical() {
        return vertical;
    }

    /**
     * Indicates whether the stroke is scaled vertically if the shape is
     * redrawn.
     * @param scale true if the stroke is scaled vertically, false if the
     * stroke thickness does not change.
     */
    public void setVertical(final boolean scale) {
        vertical = scale;
    }

    /**
     * Are the end points of the line aligned to pixel boundaries.
     * @return true if the end points are aligned to full pixels, false
     * otherwise.
     */
    public boolean isPixelAligned() {
        return pixelAligned;
    }

    /**
     * Indicates whether the end points of the line aligned to pixel boundaries.
     * @param align true if the end points are aligned to full pixels, false
     * otherwise.
     */
    public void setPixelAligned(final boolean align) {
        pixelAligned = align;
    }

    /**
     * Is the path closed if the end point matches the starting point. If true
     * then the line will be joined, otherwise an end cap is drawn.
     * @return true if the line will be closed, false if the path remains open.
     */
    public boolean isLineClosed() {
        return lineClosed;
    }

    /**
     * Indicates whether the path closed if the end point matches the starting
     * point. If true then the line will be joined, otherwise an end cap is
     * drawn.
     * @param close true if the line will be closed, false if the path remains
     * open.
     */
    public void setLineClosed(final boolean close) {
        lineClosed = close;
    }

    /**
     * Get the limit for drawing miter joins.
     * @return the value controlling how miter joins are drawn.
     */
    public int getMiterLimit() {
        return miterLimit;
    }

    /**
     * Set the limit for drawing miter joins.
     * @param limit the value controlling how miter joins are drawn.
     */
    public void setMiterLimit(final int limit) {
        if ((limit < 0) || (limit > Coder.USHORT_MAX)) {
            throw new IllegalArgumentRangeException(
                    0, Coder.USHORT_MAX, limit);
        }
        miterLimit = limit;
    }

    /**
     * Get the FillStyle used for the line stroke.
     * @return the FillStyle used to draw the line.
     */
    public FillStyle getFillStyle() {
        return fillStyle;
    }

    /**
     * Set the FillStyle used for the line stroke.
     * @param style the FillStyle used to draw the line.
     */
    public void setFillStyle(final FillStyle style) {
        fillStyle = style;
    }

    /** {@inheritDoc} */
    public LineStyle2 copy() {
        return new LineStyle2(this);
    }

    @Override
    public String toString() {
        return String.format(FORMAT, width, color, fillStyle, startCap, endCap,
                joinStyle, horizontal, vertical, pixelAligned,
                lineClosed, miterLimit);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        // CHECKSTYLE:OFF
        hasFillStyle = fillStyle != null;
        hasMiter = joinStyle == 2;

        int length = 4;

        if (hasMiter) {
            length += 2;
        }

        if (hasFillStyle) {
            length += fillStyle.prepareToEncode(context);
        } else {
            length += 4;
        }

        int scaling = context.get(Context.SCALING_STROKE);

        if (horizontal || vertical) {
            scaling |= Coder.BIT1;
        } else {
            scaling |= Coder.BIT0;
        }
        context.put(Context.SCALING_STROKE, scaling);

        return length;
        // CHECKSTYLE:ON
    }

    /** {@inheritDoc} */
    @SuppressWarnings({"PMD.NPathComplexity", "PMD.CyclomaticComplexity" })
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        coder.writeShort(width);

        int value = 0;

        if (startCap == 1) {
            value |= Coder.BIT6;
        } else if (startCap == 2) {
            value |= Coder.BIT7;
        }

        if (joinStyle == 1) {
            value |= Coder.BIT4;
        } else if (joinStyle == 2) {
            value |= Coder.BIT5;
        }

        value |= fillStyle == null ? 0 : Coder.BIT3;
        value |= horizontal ? 0 : Coder.BIT2;
        value |= vertical ? 0 : Coder.BIT1;
        value |= pixelAligned ? Coder.BIT0 : 0;

        coder.writeByte(value);

        value = lineClosed ? 0 : Coder.BIT2;
        value |= endCap;
        coder.writeByte(value);

        if (hasMiter) {
            coder.writeShort(miterLimit);
        }

        if (hasFillStyle) {
            fillStyle.encode(coder, context);
        } else {
            color.encode(coder, context);
        }
    }
}
