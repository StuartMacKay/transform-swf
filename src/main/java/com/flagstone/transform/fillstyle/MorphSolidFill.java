/*
 * MorphSolidFill.java
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

package com.flagstone.transform.fillstyle;


import java.io.IOException;

import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.datatype.Color;

/**
 * MorphSolidFill defines the solid colours that are used to fill a shape at the
 * start and end of the morphing process.
 */
public class MorphSolidFill implements FillStyle {

    /** Format string used in toString() method. */
    private static final String FORMAT = "MorphSolidFill: { start=%s;"
    		+ " end=%s}";
    /** The colour at the start of the morphing process. */
    private Color startColor;
    /** The colour at the end of the morphing process. */
    private Color endColor;

    /**
     * Creates and initialises a MorphSolidFill fill style using values encoded
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
    public MorphSolidFill(final SWFDecoder coder, final Context context)
            throws IOException {
        startColor = new Color(coder, context);
        endColor = new Color(coder, context);
    }

    /**
     * Creates a MorphSolidFill object specifying the starting and ending
     * colours.
     *
     * @param start
     *            the colour at the start of the morphing process.
     * @param end
     *            the colour at the end of the morphing process.
     */
    public MorphSolidFill(final Color start, final Color end) {
        setStartColor(start);
        setEndColor(end);
    }

    /**
     * Creates and initialises a MorphSolidFill fill style using the values
     * copied from another MorphSolidFill object.
     *
     * @param object
     *            a MorphSolidFill fill style from which the values will be
     *            copied.
     */
    public MorphSolidFill(final MorphSolidFill object) {
        startColor = object.startColor;
        endColor = object.endColor;
    }

    /**
     * Get the colour at the start of the morphing process.
     *
     * @return the starting fill colour.
     */
    public Color getStartColor() {
        return startColor;
    }

    /**
     * Get the colour at the end of the morphing process.
     *
     * @return the final fill colour.
     */
    public Color getEndColor() {
        return endColor;
    }

    /**
     * Sets the colour at the start of the morphing process.
     *
     * @param aColor
     *            the start colour. Must not be null.
     */
    public void setStartColor(final Color aColor) {
        if (aColor == null) {
            throw new IllegalArgumentException();
        }
        startColor = aColor;
    }

    /**
     * Sets the colour at the end of the morphing process.
     *
     * @param aColor
     *            the end colour. Must not be null.
     */
    public void setEndColor(final Color aColor) {
        if (aColor == null) {
            throw new IllegalArgumentException();
        }
        endColor = aColor;
    }

    /** {@inheritDoc} */
    public MorphSolidFill copy() {
        return new MorphSolidFill(this);
    }

    @Override
    public String toString() {
        return String.format(FORMAT, startColor, endColor);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        // CHECKSTYLE:OFF
        return 9;
        // CHECKSTYLE:ON
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        coder.writeByte(FillStyleTypes.SOLID_COLOR);
        startColor.encode(coder, context);
        endColor.encode(coder, context);
    }
}
