/*
 * Bounds.java
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
package com.flagstone.transform.datatype;

import java.io.IOException;

import com.flagstone.transform.Constants;
import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncodeable;
import com.flagstone.transform.coder.SWFEncoder;

/**
 * <p>
 * The Bounds class defines the area inside which shapes, text fields and
 * characters are drawn.
 * </p>
 *
 * <p>
 * In Flash the axes are specified relative to the top left corner of the screen
 * and the bounding area is defined by two pairs of coordinates that identify
 * the top left and bottom right corners of a rectangle.
 * </p>
 *
 * <img src="doc-files/bounds.gif">
 *
 * <p>
 * The coordinates for each corner also specify the coordinate range so
 * specifying a bounding rectangle with the points (-100,-100) and (100,100)
 * defines a rectangle 200 twips by 200 twips with the point (0,0) located in
 * the centre. Specifying the points (0,0) and (200,200) defines a rectangle
 * with the same size however the centre is now located at (100,100).
 * </p>
 *
 * <p>
 * The bounding rectangle does not clip the object when it is drawn. Lines and
 * curves drawn outside of the rectangle will still be displayed. However if the
 * position of the object is changed or another object is displayed in front of
 * it then only the pixels inside of the bounding box will be repainted.
 * </p>
 */
public class Bounds implements SWFEncodeable {
    /** Offset to add to number of bits when calculating number of bytes. */
    private static final int ROUND_TO_BYTES = 7;
    /** Right shift to convert number of bits to number of bytes. */
    private static final int BITS_TO_BYTES = 3;
    /** Format used by toString() to display object representation. */
    private static final String FORMAT = "Bounds: (%d, %d) (%d, %d)";

    /**
     * Size of bit-field used to specify the number of bits representing
     * encoded bounding box values.
     */
    private static final int FIELD_SIZE = 5;

    /**
     * Create a Bounds by applying a padding factor to all sides of the
     * bounding box.
     *
     * @param rect the Bounds to adjust.
     * @param padding the margin to add to the coordinates of the bounds.
     * @return the adjusted Bounds.
     */
    public static Bounds pad(final Bounds rect, final int padding) {
        return new Bounds(rect.getMinX() - padding,
                rect.getMinY() - padding,
                rect.getMaxX() + padding,
                rect.getMaxY() + padding);
    }

    /**
     * Create a Bounds by applying a padding factor to all sides of the
     * bounding box.
     *
     * @param rect the Bounds to adjust.
     * @param top the to apply to the top of the bounding box.
     * @param right the to apply to the right of the bounding box.
     * @param bottom the to apply to the bottom of the bounding box.
     * @param left the to apply to the left of the bounding box.
     * @return the adjusted Bounds.
     */
    public static Bounds pad(final Bounds rect, final int top,
            final int right, final int bottom, final int left) {
        return new Bounds(rect.getMinX() - left,
                rect.getMinY() - top,
                rect.getMaxX() + right,
                rect.getMaxY() + bottom);
    }

    /** X-coordinate of upper left corner of bounding box. */
    private final transient int minX;
    /** Y-coordinate of upper left corner of bounding box. */
    private final transient int minY;
    /** X-coordinate of lower right corner of bounding box. */
    private final transient int maxX;
    /** Y-coordinate of lower right corner of bounding box. */
    private final transient int maxY;

    /**
     * Holds the field size for bounding box values when encoding and
     * decoding objects.
     */
    private transient int size;

    /**
     * Creates and initialises a Bounds using values encoded in the Flash binary
     * format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public Bounds(final SWFDecoder coder) throws IOException {
        size = coder.readBits(FIELD_SIZE, false);
        minX = coder.readBits(size, true);
        maxX = coder.readBits(size, true);
        minY = coder.readBits(size, true);
        maxY = coder.readBits(size, true);
        coder.alignToByte();
    }

    /**
     * Creates a Bounds object representing a rectangle with the corners at
     * (xmin,ymin) and (xmax,ymax).
     *
     * @param xmin
     *            x-coordinate of the top left corner.
     * @param ymin
     *            y-coordinate of the top left corner.
     * @param xmax
     *            x-coordinate of bottom right corner.
     * @param ymax
     *            y-coordinate of bottom right corner.
     */
    public Bounds(final int xmin, final int ymin,
            final int xmax, final int ymax) {
        minX = xmin;
        minY = ymin;
        maxX = xmax;
        maxY = ymax;
    }

    /**
     * Returns the x-coordinate of the top left corner of the bounding rectangle
     * as seen on a screen.
     *
     * @return the x-coordinate of the upper left corner.
     */
    public int getMinX() {
        return minX;
    }

    /**
     * Returns the x-coordinate of the bottom right corner of the bounding
     * rectangle as seen on a screen.
     *
     * @return the x-coordinate of the lower right corner.
     */
    public int getMaxX() {
        return maxX;
    }

    /**
     * Returns the y-coordinate of the top left corner of the bounding rectangle
     * as seen on a screen.
     *
     * @return the y-coordinate of the upper left corner.
     */
    public int getMinY() {
        return minY;
    }

    /**
     * Returns the y-coordinate of the bottom right corner of the bounding
     * rectangle as seen on a screen.
     *
     * @return the y-coordinate of the lower right corner.
     */
    public int getMaxY() {
        return maxY;
    }

    /**
     * Returns the width of the rectangle, measured in twips.
     *
     * @return the width of the bounding box in twips.
     */
    public int getWidth() {
        return maxX - minX;
    }

    /**
     * Returns the height of the rectangle, measured in twips.
     *
     * @return the height of the bounding box in twips.
     */
    public int getHeight() {
        return maxY - minY;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, minX, minY, maxX, maxY);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object object) {
        boolean result;
        Bounds bounds;

        if (object == null) {
            result = false;
        } else if (object == this) {
            result = true;
        } else if (object instanceof Bounds) {
            bounds = (Bounds) object;
            result = (minX == bounds.minX) && (minY == bounds.minY)
                    && (maxX == bounds.maxX) && (maxY == bounds.maxY);
        } else {
            result = false;
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return ((minX * Constants.PRIME + minY) * Constants.PRIME + maxX)
            * Constants.PRIME + maxY;
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        size = Coder.maxSize(minX, minY, maxX, maxY);
        return (FIELD_SIZE + ROUND_TO_BYTES
                    + (size << 2)) >> BITS_TO_BYTES;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        coder.writeBits(size, FIELD_SIZE);
        coder.writeBits(minX, size);
        coder.writeBits(maxX, size);
        coder.writeBits(minY, size);
        coder.writeBits(maxY, size);
        coder.alignToByte();
    }
}
