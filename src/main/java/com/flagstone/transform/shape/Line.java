/*
 * Line.java
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
 * Line defines a straight line. The line is drawn from the current drawing
 * point to the end point specified in the Line object which is specified
 * relative to the current drawing point. Once the line is drawn, the end of the
 * line is now the current drawing point.
 */
public class Line implements ShapeRecord {

    /** Format string used in toString() method. */
    private static final String FORMAT = "Line: (%d, %d)";
    /** x-coordinate of the end of the line. */
    private transient int xCoord;
    /** y-coordinate of the end of the line. */
    private transient int yCoord;

    /** Is the line vertical. */
    private transient boolean vertical;
    /** Is the line horizontal. */
    private transient boolean general;
    /** Number of bits used to encode the x and y coordinates. */
    private transient int size;

    /**
     * Creates and initialises a Line object using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */

    public Line(final SWFDecoder coder) throws IOException {
        // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 1 LINES
        size = coder.readBits(4, false) + 2;

        if (coder.readBits(1, false) == 0) {
            if (coder.readBits(1, false) == 0) {
                xCoord = coder.readBits(size, true);
                yCoord = 0;
            } else {
                xCoord = 0;
                yCoord = coder.readBits(size, true);
            }
        } else {
            xCoord = coder.readBits(size, true);
            yCoord = coder.readBits(size, true);
        }
    }

    /**
     * Creates a Line with the specified relative coordinates.
     *
     * @param coordX
     *            the x-coordinate of the end point, specified relative to the
     *            current drawing point. Must be in the range -65536..65535.
     * @param coordY
     *            the y-coordinate of the end point, specified relative to the
     *            current drawing point. Must be in the range -65536..65535.
     */
    public Line(final int coordX, final int coordY) {
        setPoint(coordX, coordY);
    }

    /**
     * Creates and initialises a Line object using the values copied
     * from another Line object.
     *
     * @param object
     *            a Line object from which the values will be
     *            copied.
     */
    public Line(final Line object) {
        xCoord = object.xCoord;
        yCoord = object.yCoord;
    }

    /**
     * Get the relative x-coordinate of the end-point of the line.
     *
     * @return the x-coordinate of the line end.
     */
    public int getX() {
        return xCoord;
    }

    /**
     * Get the relative y-coordinate of the end-point of the line.
     *
     * @return the y-coordinate of the line end.
     */
    public int getY() {
        return yCoord;
    }

    /**
     * Sets the relative x and y coordinates.
     *
     * @param coordX
     *            the x-coordinate of the end point. Must be in the range
     *            -65536..65535.
     * @param coordY
     *            the y-coordinate of the end point. Must be in the range
     *            -65536..65535.
     */
    public void setPoint(final int coordX, final int coordY) {
        if ((coordX < Shape.MIN_COORD) || (coordX > Shape.MAX_COORD)) {
            throw new IllegalArgumentRangeException(
                    Shape.MIN_COORD, Shape.MAX_COORD, coordX);
        }
        xCoord = coordX;

        if ((coordY < Shape.MIN_COORD) || (coordY > Shape.MAX_COORD)) {
            throw new IllegalArgumentRangeException(
                    Shape.MIN_COORD, Shape.MAX_COORD, coordY);
        }
       yCoord = coordY;
    }

    /** {@inheritDoc} */
    public Line copy() {
        return new Line(this);
    }

    @Override
    public String toString() {
        return String.format(FORMAT, xCoord, yCoord);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        vertical = xCoord == 0;
        general = (xCoord != 0) && (yCoord != 0);
        size = Coder.maxSize(xCoord, yCoord, 1);

        // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 1 LINES
        int numberOfBits = 7;

        if (general) {
            numberOfBits += size << 1;
        } else {
            numberOfBits += 1 + size;
        }

        context.put(Context.SHAPE_SIZE, context.get(Context.SHAPE_SIZE)
                + numberOfBits);

        return numberOfBits;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 2 LINES
        coder.writeBits(3, 2);
        coder.writeBits(size - 2, 4);
        coder.writeBits(general ? 1 : 0, 1);

        if (general) {
            coder.writeBits(xCoord, size);
            coder.writeBits(yCoord, size);
        } else {
            coder.writeBits(vertical ? 1 : 0, 1);
            coder.writeBits(vertical ? yCoord : xCoord, size);
        }
    }
}
