/*
 * Curve.java
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

import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.exception.IllegalArgumentRangeException;

/**
 * <p>
 * Curve is used to define a curve. Curved lines are constructed using a
 * Quadratic Bezier curve. The curve is specified using two points, an off-curve
 * control point, relative to the current point and an on-curve anchor point
 * which defines the end-point of the curve, and which is specified relative to
 * the anchor point.
 * </p>
 *
 * <img src="doc-files/quadratic.gif">
 *
 * <p>
 * Flash does not directly support Cubic Bezier curves. Converting a Cubic
 * Bezier curve to a Quadratic curve is a non trivial process, however the
 * Canvas class contains a method to perform the conversion simplifying the
 * create of Shape outlines in Flash from other graphics formats.
 * </p>
 *
 * @see com.flagstone.transform.util.shape.Canvas
 */
public class Curve implements ShapeRecord {

    /** Format string used in toString() method. */
    private static final String FORMAT = "Curve: (%d, %d) (%d, %d)";

    /** Number of bits used to encode the size field. */
    private static final int SIZE_WIDTH = 4;

    /** The x-coordinate of the control point. */
    private transient int controlX;
    /** The y-coordinate of the control point. */
    private transient int controlY;
    /** The x-coordinate of the anchor point. */
    private transient int anchorX;
    /** The y-coordinate of the anchor point. */
    private transient int anchorY;

    /** Variable used to record the number of bits for each coordinate. */
    private transient int size;

    /**
     * Creates and initialises a Curve object using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
   public Curve(final SWFDecoder coder) throws IOException {
        size = coder.readBits(SIZE_WIDTH, false) + 2;
        controlX = coder.readBits(size, true);
        controlY = coder.readBits(size, true);
        anchorX = coder.readBits(size, true);
        anchorY = coder.readBits(size, true);
    }

    /**
     * Creates a Curve object specifying the anchor and control point
     * coordinates.
     *
     * @param xControl
     *            the x-coordinate of the control point, specified relative to
     *            the current drawing point. Must be in the range -65535..65535.
     * @param yControl
     *            the y-coordinate of the control point, specified relative to
     *            the current drawing point.Must be in the range -65535..65535.
     * @param xAnchor
     *            the x-coordinate of the anchor point, specified relative to
     *            the control point.Must be in the range -65535..65535.
     * @param yAnchor
     *            the y-coordinate of the anchor point, specified relative to
     *            the control point.Must be in the range -65535..65535.
     */
    public Curve(final int xControl, final int yControl,
            final int xAnchor, final int yAnchor) {
        setPoints(xControl, yControl, xAnchor, yAnchor);
    }

    /**
     * Creates and initialises a Curve object using the values copied
     * from another Curve object.
     *
     * @param object
     *            a Curve object from which the values will be
     *            copied.
     */
    public Curve(final Curve object) {
        controlX = object.controlX;
        controlY = object.controlY;
        anchorX = object.anchorX;
        anchorY = object.anchorY;
    }

    /**
     * Get the x-coordinate of the control point relative to the current
     * drawing point.
     *
     * @return the x-coordinate of the control point.
     */
    public int getControlX() {
        return controlX;
    }

    /**
     * Get the y-coordinate of the control point relative to the current
     * drawing point.
     *
     * @return the y-coordinate of the control point.
     */
    public int getControlY() {
        return controlY;
    }

    /**
     * Get the x-coordinate of the anchor point relative to the control
     * point.
     *
     * @return the x-coordinate of the anchor point.
     */
    public int getAnchorX() {
        return anchorX;
    }

    /**
     * Get the y-coordinate of the anchor point relative to the control
     * point.
     *
     * @return the y-coordinate of the anchor point.
     */
    public int getAnchorY() {
        return anchorY;
    }

    /**
     * Sets the x and y coordinates of the control and anchor points. Values
     * must be in the range -65535..65535.
     *
     * @param xControl
     *            the x-coordinate of the control point.
     * @param yControl
     *            the y-coordinate of the control point.
     * @param xAnchor
     *            the x-coordinate of the anchor point.
     * @param yAnchor
     *            the y-coordinate of the anchor point.
     */
    public void setPoints(final int xControl, final int yControl,
            final int xAnchor, final int yAnchor) {
        if ((xControl < Shape.MIN_COORD) || (xControl > Shape.MAX_COORD)) {
            throw new IllegalArgumentRangeException(
                    Shape.MIN_COORD, Shape.MAX_COORD, xControl);
        }
        controlX = xControl;

        if ((yControl < Shape.MIN_COORD) || (yControl > Shape.MAX_COORD)) {
            throw new IllegalArgumentRangeException(
                    Shape.MIN_COORD, Shape.MAX_COORD, yControl);
        }
        controlY = yControl;

        if ((xAnchor < Shape.MIN_COORD) || (xAnchor > Shape.MAX_COORD)) {
            throw new IllegalArgumentRangeException(
                    Shape.MIN_COORD, Shape.MAX_COORD, xAnchor);
        }
        anchorX = xAnchor;

        if ((yAnchor < Shape.MIN_COORD) || (yAnchor > Shape.MAX_COORD)) {
            throw new IllegalArgumentRangeException(
                    Shape.MIN_COORD, Shape.MAX_COORD, yAnchor);
        }
        anchorY = yAnchor;
    }

    /** {@inheritDoc} */
    public Curve copy() {
        return new Curve(this);
    }

    @Override
    public String toString() {
        return String.format(FORMAT, controlX, controlY, anchorX, anchorY);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 1 LINES
        int numberOfBits = 6;

        size = Coder.maxSize(controlX, controlY, anchorX, anchorY, 1);

        numberOfBits += size << 2;

        context.put(Context.SHAPE_SIZE,
                context.get(Context.SHAPE_SIZE) + numberOfBits);

        return numberOfBits;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        coder.writeBits(2, 2); // shapeType, edgeType
        coder.writeBits(size - 2, SIZE_WIDTH);
        coder.writeBits(controlX, size);
        coder.writeBits(controlY, size);
        coder.writeBits(anchorX, size);
        coder.writeBits(anchorY, size);
    }
}
