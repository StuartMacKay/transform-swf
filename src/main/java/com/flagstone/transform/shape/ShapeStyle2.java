/*
 * ShapeStyle2.java
 * Transform
 *
 * Copyright (c) 2010 Flagstone Software Ltd. All rights reserved.
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

import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.coder.SWFFactory;
import com.flagstone.transform.exception.IllegalArgumentRangeException;
import com.flagstone.transform.fillstyle.FillStyle;
import com.flagstone.transform.linestyle.LineStyle2;

/**
 * ShapeStyle2 extends the functionality of ShapeStyle by supporting lines
 * drawn with the LineStyle2 object.
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
public class ShapeStyle2 implements ShapeRecord {

    /** Format string used in toString() method. */
    private static final String FORMAT = "ShapeStyle2: { move=(%d, %d);"
            + " fill=%d; alt=%d; line=%d; fillStyles=%s; lineStyles=%s}";

    /**
     * Reserved length for style counts indicated that the number of line
     * or fill styles is encoded in the next 16-bit word.
     */
    private static final int EXTENDED = 255;

    /** Relative move along the x-axis. */
    private Integer moveX;
    /** Relative move along the y-axis. */
    private Integer moveY;
    /** Selected fill style. */
    private Integer fillStyle;
    /** Selected alternate fill style. */
    private Integer altFillStyle;
    /** Selected line style. */
    private Integer lineStyle;
    /** List of fill styles. */
    private List<FillStyle> fillStyles;
    /** List of line styles. */
    private List<LineStyle2> lineStyles;

    /** Indicates whether new line or fill styles are specified. */
    private transient boolean hasStyles;
    /** Indicates whether a line is specified. */
    private transient boolean hasLine;
    /** Indicates whether an alternate fill style is specified. */
    private transient boolean hasAlt;
    /** Indicates whether an fill style is specified. */
    private transient boolean hasFill;
    /** Indicates whether a relative move is specified. */
    private transient boolean hasMove;

    /**
     * Creates and initialises a ShapeStyle object using values encoded
     * in the Flash binary format.
     *
     * @param flags
     *            contains fields identifying which fields are optionally
     *            encoded in the data - decoded by parent object.
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

    public ShapeStyle2(final int flags, final SWFDecoder coder,
            final Context context) throws IOException {
        int numberOfFillBits = context.get(Context.FILL_SIZE);
        int numberOfLineBits = context.get(Context.LINE_SIZE);

        hasStyles = (flags & Coder.BIT4) != 0;
        hasLine = (flags & Coder.BIT3) != 0;
        hasAlt = (flags & Coder.BIT2) != 0;
        hasFill = (flags & Coder.BIT1) != 0;
        hasMove = (flags & Coder.BIT0) != 0;

        if (hasMove) {
            final int moveFieldSize = coder.readBits(5, false);
            moveX = coder.readBits(moveFieldSize, true);
            moveY = coder.readBits(moveFieldSize, true);
        }
        fillStyles = new ArrayList<FillStyle>();
        lineStyles = new ArrayList<LineStyle2>();

        if (hasFill) {
            fillStyle = coder.readBits(numberOfFillBits, false);
        }
        if (hasAlt) {
            altFillStyle = coder.readBits(numberOfFillBits, false);
        }
        if (hasLine) {
            lineStyle = coder.readBits(numberOfLineBits, false);
        }

        if (hasStyles) {
            coder.alignToByte();

            int fillStyleCount = coder.readByte();

            if (context.contains(Context.ARRAY_EXTENDED)
                    && (fillStyleCount == EXTENDED)) {
                fillStyleCount = coder.readUnsignedShort();
            }

            final SWFFactory<FillStyle> decoder = context.getRegistry()
                    .getFillStyleDecoder();

            for (int i = 0; i < fillStyleCount; i++) {
                decoder.getObject(fillStyles, coder, context);
            }

            int lineStyleCount = coder.readByte();

            if (context.contains(Context.ARRAY_EXTENDED)
                    && (lineStyleCount == EXTENDED)) {
                lineStyleCount = coder.readUnsignedShort();
            }

            for (int i = 0; i < lineStyleCount; i++) {
                lineStyles.add(new LineStyle2(coder, context));
            }

            final int sizes = coder.readByte();
            numberOfFillBits = (sizes & Coder.NIB1) >> Coder.TO_LOWER_NIB;
            numberOfLineBits = sizes & Coder.NIB0;

            context.put(Context.FILL_SIZE, numberOfFillBits);
            context.put(Context.LINE_SIZE, numberOfLineBits);
        }
    }

    /**
     * Creates an uninitialised ShapeStyle object.
     */
    public ShapeStyle2() {
        fillStyles = new ArrayList<FillStyle>();
        lineStyles = new ArrayList<LineStyle2>();
    }

    /**
     * Creates and initialises a ShapeStyle object using the values copied
     * from another ShapeStyle object.
     *
     * @param object
     *            a ShapeStyle object from which the values will be
     *            copied.
     */
    public ShapeStyle2(final ShapeStyle2 object) {
        moveX = object.moveX;
        moveY = object.moveY;
        lineStyle = object.lineStyle;
        fillStyle = object.fillStyle;
        altFillStyle = object.altFillStyle;

        lineStyles = new ArrayList<LineStyle2>(object.lineStyles.size());

        for (final LineStyle2 style : object.lineStyles) {
            lineStyles.add(style.copy());
        }

        fillStyles = new ArrayList<FillStyle>(object.fillStyles.size());

        for (final FillStyle style : object.fillStyles) {
            fillStyles.add(style.copy());
        }
    }

    /**
     * Add a LineStyle object to the list of line styles.
     *
     * @param style
     *            and LineStyle object. Must not be null.
     * @return this object.
     */
    public ShapeStyle2 add(final LineStyle2 style) {
        if (style == null) {
            throw new IllegalArgumentException();
        }
        lineStyles.add(style);
        return this;
    }

    /**
     * Add the fill style object to the list of fill styles.
     *
     * @param style
     *            and FillStyle object. Must not be null.
     * @return this object.
     */
    public ShapeStyle2 add(final FillStyle style) {
        if (style == null) {
            throw new IllegalArgumentException();
        }
        fillStyles.add(style);
        return this;
    }

    /**
     * Get the x-coordinate of any relative move or null if no move is
     * specified.
     *
     * @return the relative move in the x direction.
     */
    public Integer getMoveX() {
        return moveX;
    }

    /**
     * Get the y-coordinate of any relative move or null if no move is
     * specified.
     *
     * @return the relative move in the y direction.
     */
    public Integer getMoveY() {
        return moveY;
    }

    /**
     * Get the index of the line style that will be applied to any line
     * drawn. Returns null if no line style is defined.
     *
     * @return the selected line style.
     */
    public Integer getLineStyle() {
        return lineStyle;
    }

    /**
     * Get the index of the fill style that will be applied to any area
     * filled. Returns null if no fill style is defined.
     *
     * @return the selected fill style.
     */
    public Integer getFillStyle() {
        return fillStyle;
    }

    /**
     * Get the index of the fill style that will be applied to any
     * overlapping area filled. Returns null if no alternate fill style is
     * defined.
     *
     * @return the selected alternate fill style.
     */
    public Integer getAltFillStyle() {
        return altFillStyle;
    }

    /**
     * Get the list of new line styles.
     *
     * @return the list of line styles.
     */
    public List<LineStyle2> getLineStyles() {
        return lineStyles;
    }

    /**
     * Returns the list of new fill styles.
     *
     * @return the list of fill styles.
     */
    public List<FillStyle> getFillStyles() {
        return fillStyles;
    }

    /**
     * Sets the x-coordinate of any relative move.
     *
     * @param coord
     *            move the current point by aNumber in the x direction. Must be
     *            in the range -65535..65535.
     * @return this object.
     */
    public ShapeStyle2 setMoveX(final Integer coord) {
        if ((coord != null)
                && ((coord < Shape.MIN_COORD) || (coord > Shape.MAX_COORD))) {
            throw new IllegalArgumentRangeException(
                    Shape.MIN_COORD, Shape.MAX_COORD, coord);
        }
        moveX = coord;
        return this;
    }

    /**
     * Sets the x-coordinate of any relative move.
     *
     * @param coord
     *            move the current point by aNumber in the x direction. Must be
     *            in the range -65535..65535.
     * @return this object.
     */
    public ShapeStyle2 setMoveY(final Integer coord) {
        if ((coord != null)
                && ((coord < Shape.MIN_COORD) || (coord > Shape.MAX_COORD))) {
            throw new IllegalArgumentRangeException(
                    Shape.MIN_COORD, Shape.MAX_COORD, coord);
        }
        moveY = coord;
        return this;
    }

    /**
     * Sets the coordinates of any relative move.
     *
     * @param xCoord
     *            move the current point by aNumber in the x direction. Must be
     *            in the range -65535..65535.
     *
     * @param yCoord
     *            move the current point by aNumber in the y direction. Must be
     *            in the range -65535..65535.
     * @return this object.
     */
    public ShapeStyle2 setMove(final Integer xCoord, final Integer yCoord) {
        if (((xCoord == null) && (yCoord != null))
                || ((xCoord != null) && (yCoord == null))) {
            throw new IllegalArgumentException();
        }
        if ((xCoord != null)
                && ((xCoord < Shape.MIN_COORD) || (xCoord > Shape.MAX_COORD))) {
            throw new IllegalArgumentRangeException(
                    Shape.MIN_COORD, Shape.MAX_COORD, xCoord);
        }
        if ((yCoord != null)
                && ((yCoord < Shape.MIN_COORD) || (yCoord > Shape.MAX_COORD))) {
            throw new IllegalArgumentRangeException(
                    Shape.MIN_COORD, Shape.MAX_COORD, yCoord);
        }
        moveX = xCoord;
        moveY = yCoord;
        return this;
    }

    /**
     * Sets the index of the fill style that will be applied to any area filled.
     * May be set to zero if no style is selected or null if the line style
     * remains unchanged.
     *
     * @param anIndex
     *            selects the fill style at anIndex in the fill styles list of
     *            the parent Shape object.
     * @return this object.
     */
    public ShapeStyle2 setFillStyle(final Integer anIndex) {
        fillStyle = anIndex;
        return this;
    }

    /**
     * Sets the index of the fill style that will be applied to any overlapping
     * area filled. May be set to zero if no style is selected or null if the ~
     * line style remains unchanged.
     *
     * @param anIndex
     *            selects the alternate fill style at anIndex in the fill styles
     *            list of the parent Shape object.
     * @return this object.
     */
    public ShapeStyle2 setAltFillStyle(final Integer anIndex) {
        altFillStyle = anIndex;
        return this;
    }

    /**
     * Sets the index of the line style that will be applied to any line drawn.
     * May be set to zero if no style is selected or null if the line style
     * remains unchanged.
     *
     * @param anIndex
     *            selects the line style at anIndex in the line styles list of
     *            the parent Shape object.
     * @return this object.
     */
    public ShapeStyle2 setLineStyle(final Integer anIndex) {
        lineStyle = anIndex;
        return this;
    }

    /**
     * Sets the list of new line styles. May be set to null if no styles are
     * being defined.
     *
     * @param list
     *            a list of LineStyle objects. Must not be null.
     * @return this object.
     */
    public ShapeStyle2 setLineStyles(final List<LineStyle2> list) {
        if (list == null) {
            throw new IllegalArgumentException();
        }
        lineStyles = list;
        return this;
    }

    /**
     * Sets the list of new fill styles. May be set to null if no styles are
     * being defined.
     *
     * @param list
     *            a list of fill style objects. Must not be null.
     * @return this object.
     */
    public ShapeStyle2 setFillStyles(final List<FillStyle> list) {
        if (list == null) {
            throw new IllegalArgumentException();
        }
        fillStyles = list;
        return this;
    }

    /** {@inheritDoc} */
    public ShapeStyle2 copy() {
        return new ShapeStyle2(this);
    }

    @Override
    public String toString() {
        return String.format(FORMAT, moveX, moveY, fillStyle, altFillStyle,
                lineStyle, fillStyles, lineStyles);
    }

    /** {@inheritDoc} */
    @SuppressWarnings({"PMD.NPathComplexity", "PMD.CyclomaticComplexity" })
    public int prepareToEncode(final Context context) {
        // CHECKSTYLE:OFF
        hasLine = lineStyle != null;
        hasFill = fillStyle != null;
        hasAlt = altFillStyle != null;
        hasMove = (moveX != null) && (moveY != null);
        hasStyles = !lineStyles.isEmpty() || !fillStyles.isEmpty();

        int numberOfBits = 6;

        if (hasMove) {
            final int fieldSize = Math.max(Coder.size(moveX), Coder
                    .size(moveY));
            numberOfBits += 5 + fieldSize * 2;
        }

        numberOfBits += hasFill ? context.get(Context.FILL_SIZE) : 0;
        numberOfBits += hasAlt ? context.get(Context.FILL_SIZE) : 0;
        numberOfBits += (hasLine) ? context.get(Context.LINE_SIZE) : 0;

        context.put(Context.SHAPE_SIZE, context.get(Context.SHAPE_SIZE)
                + numberOfBits);

        if (hasStyles) {
            int numberOfFillBits = Coder.unsignedSize(fillStyles.size());
            int numberOfLineBits = Coder.unsignedSize(lineStyles.size());

            if ((numberOfFillBits == 0)
                    && context.contains(Context.POSTSCRIPT)) {
                numberOfFillBits = 1;
            }

            if ((numberOfLineBits == 0)
                    && context.contains(Context.POSTSCRIPT)) {
                numberOfLineBits = 1;
            }

            final boolean countExtended = context
                    .contains(Context.ARRAY_EXTENDED);

            int numberOfStyleBits = 0;
            final int flushBits = context.get(Context.SHAPE_SIZE);

            numberOfStyleBits += (flushBits % 8 > 0)
            ? 8 - (flushBits % 8) : 0;
            numberOfStyleBits += (countExtended
                    && (fillStyles.size() >= EXTENDED)) ? 24
                    : 8;

            for (final FillStyle style : fillStyles) {
                numberOfStyleBits += style.prepareToEncode(context) * 8;
            }

            numberOfStyleBits += (countExtended
                    && (lineStyles.size() >= EXTENDED)) ? 24
                    : 8;

            for (final LineStyle2 style : lineStyles) {
                numberOfStyleBits += style.prepareToEncode(context) * 8;
            }

            numberOfStyleBits += 8;

            context.put(Context.FILL_SIZE, numberOfFillBits);
            context.put(Context.LINE_SIZE, numberOfLineBits);
            context.put(Context.SHAPE_SIZE, context.get(Context.SHAPE_SIZE)
                    + numberOfStyleBits);

            numberOfBits += numberOfStyleBits;
        }
        return numberOfBits;
        // CHECKSTYLE:ON
    }

    /** {@inheritDoc} */
    @SuppressWarnings({"PMD.NPathComplexity", "PMD.CyclomaticComplexity" })
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        coder.writeBits(0, 1);
        coder.writeBits(hasStyles ? 1 : 0, 1);
        coder.writeBits(hasLine ? 1 : 0, 1);
        coder.writeBits(hasAlt ? 1 : 0, 1);
        coder.writeBits(hasFill ? 1 : 0, 1);
        coder.writeBits(hasMove ? 1 : 0, 1);

        if (hasMove) {
            final int fieldSize = Math.max(Coder.size(moveX), Coder
                    .size(moveY));
            // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 1 LINES
            coder.writeBits(fieldSize, 5);
            coder.writeBits(moveX, fieldSize);
            coder.writeBits(moveY, fieldSize);
        }

        if (hasFill) {
            coder.writeBits(fillStyle, context.get(Context.FILL_SIZE));
        }

        if (hasAlt) {
            coder.writeBits(altFillStyle, context.get(Context.FILL_SIZE));
        }

        if (hasLine) {
            coder.writeBits(lineStyle, context.get(Context.LINE_SIZE));
        }

        if (hasStyles) {
            final boolean countExtended = context
                    .contains(Context.ARRAY_EXTENDED);

            coder.alignToByte();

            if (countExtended && (fillStyles.size() >= EXTENDED)) {
                coder.writeByte(EXTENDED);
                coder.writeShort(fillStyles.size());
            } else {
                coder.writeByte(fillStyles.size());
            }

            for (final FillStyle style : fillStyles) {
                style.encode(coder, context);
            }

            if (countExtended && (lineStyles.size() >= EXTENDED)) {
                coder.writeByte(EXTENDED);
                coder.writeShort(lineStyles.size());
            } else {
                coder.writeByte(lineStyles.size());
            }

            for (final LineStyle2 style : lineStyles) {
                style.encode(coder, context);
            }

            int numberOfFillBits = Coder.unsignedSize(fillStyles.size());
            int numberOfLineBits = Coder.unsignedSize(lineStyles.size());

            if (context.contains(Context.POSTSCRIPT)) {
                if (numberOfFillBits == 0) {
                    numberOfFillBits = 1;
                }

                if (numberOfLineBits == 0) {
                    numberOfLineBits = 1;
                }
            }

            coder.writeByte((numberOfFillBits << Coder.TO_UPPER_NIB)
                    | numberOfLineBits);

            // Update the stream with the new numbers of line and fill bits
            context.put(Context.FILL_SIZE, numberOfFillBits);
            context.put(Context.LINE_SIZE, numberOfLineBits);
        }
    }
}
