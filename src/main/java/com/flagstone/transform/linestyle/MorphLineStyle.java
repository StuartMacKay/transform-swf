/*
 * MorphLineStyle.java
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
 * MorphLineStyle defines the width and colour of a line drawn for a shape is it
 * is morphed.
 *
 * <p>
 * MorphLineStyle specifies the width and colour of the line at the start and
 * end of the morphing process. The transparency value for the colour should
 * also be specified. The Flash Player performs the interpolation as the shape
 * is morphed.
 * </p>
 */
public class MorphLineStyle implements LineStyle {

    /** Format string used in toString() method. */
    private static final String FORMAT = "MorphSolidLine: { startWidth=%d;"
    		+ " endWidth=%d; startColor=%s; endColor=%s}";

    /** Width of the line at the start of the morph. */
    private int startWidth;
    /** Width of the line at the end of the morph. */
    private int endWidth;
    /** Color of the line at the start of the morph. */
    private Color startColor;
    /** Color of the line at the end of the morph. */
    private Color endColor;

    /**
     * Creates and initialises a MorphLineStyle object using values encoded
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
    public MorphLineStyle(final SWFDecoder coder, final Context context)
            throws IOException {
        startWidth = coder.readUnsignedShort();
        endWidth = coder.readUnsignedShort();
        startColor = new Color(coder, context);
        endColor = new Color(coder, context);
    }

    /**
     * Creates a MorphLineStyle object specifying the starting and ending widths
     * and colours.
     *
     * @param initialWidth
     *            the width of the line at the start of the morphing process.
     * @param finalWidth
     *            the width of the line at the end of the morphing process.
     * @param initialColor
     *            the colour of the line at the start of the morphing process.
     * @param finalColor
     *            the colour of the line at the end of the morphing process.
     */
    public MorphLineStyle(final int initialWidth, final int finalWidth,
            final Color initialColor, final Color finalColor) {
        super();

        setStartWidth(initialWidth);
        setEndWidth(finalWidth);
        setStartColor(initialColor);
        setEndColor(finalColor);
    }

    /**
     * Creates and initialises a MorphLineStyle object using the values copied
     * from another MorphLineStyle object.
     *
     * @param object
     *            a MorphLineStyle object from which the values will be
     *            copied.
     */
    public MorphLineStyle(final MorphLineStyle object) {
        startWidth = object.startWidth;
        endWidth = object.endWidth;
        startColor = object.startColor;
        endColor = object.endColor;
    }

    /**
     * Get the width of the line at the start of the morphing process.
     *
     * @return the starting stroke width.
     */
    public int getStartWidth() {
        return startWidth;
    }

    /**
     * Get the width of the line at the end of the morphing process.
     *
     * @return the final stroke width.
     */
    public int getEndWidth() {
        return endWidth;
    }

    /**
     * Get the colour of the line at the start of the morphing process.
     *
     * @return the starting stroke colour.
     */
    public Color getStartColor() {
        return startColor;
    }

    /**
     * Returns the colour of the line at the end of the morphing process.
     *
     * @return the final stroke colour.
     */
    public Color getEndColor() {
        return endColor;
    }

    /**
     * Sets the width of the line at the start of the morphing process.
     *
     * @param aNumber
     *            the starting width of the line. Must be in the range 0..65535.
     */
    public void setStartWidth(final int aNumber) {
        if ((aNumber < 0) || (aNumber > Coder.USHORT_MAX)) {
            throw new IllegalArgumentRangeException(
                    0, Coder.USHORT_MAX, aNumber);
        }
        startWidth = aNumber;
    }

    /**
     * Sets the width of the line at the end of the morphing process.
     *
     * @param aNumber
     *            the ending width of the line. Must be in the range 0..65535.
     */
    public void setEndWidth(final int aNumber) {
        if ((aNumber < 0) || (aNumber > Coder.USHORT_MAX)) {
            throw new IllegalArgumentRangeException(
                    0, Coder.USHORT_MAX, aNumber);
        }
        endWidth = aNumber;
    }

    /**
     * Returns the colour of the line at the start of the morphing process.
     *
     * @param aColor
     *            the starting colour of the line. Must not be null.
     */
    public void setStartColor(final Color aColor) {
        if (aColor == null) {
            throw new IllegalArgumentException();
        }
        startColor = aColor;
    }

    /**
     * Sets the colour of the line at the end of the morphing process.
     *
     * @param aColor
     *            the ending colour of the line. Must not be null.
     */
    public void setEndColor(final Color aColor) {
        if (aColor == null) {
            throw new IllegalArgumentException();
        }
        endColor = aColor;
    }

    /** {@inheritDoc} */
    public MorphLineStyle copy() {
        return new MorphLineStyle(this);
    }

    @Override
    public String toString() {
        return String
                .format(FORMAT, startWidth, endWidth, startColor, endColor);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 1 LINES
        return 12;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        coder.writeShort(startWidth);
        coder.writeShort(endWidth);
        startColor.encode(coder, context);
        endColor.encode(coder, context);
    }
}
