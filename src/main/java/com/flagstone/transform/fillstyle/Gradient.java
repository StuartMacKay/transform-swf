/*
 * Gradient.java
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

import com.flagstone.transform.Constants;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncodeable;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.datatype.Color;
import com.flagstone.transform.exception.IllegalArgumentRangeException;

/**
 * Gradient defines a control point that is used to specify how a gradient
 * colour is displayed.
 *
 * <p>
 * Two or more control points are used to define how the colour changes across
 * the gradient square. Each control point specifies the ratio indicating the
 * location of the control point across the gradient square and the colour to be
 * displayed at that point.
 * </p>
 *
 * <p>
 * The ratio is a number between 0 and 255 - that specifies the relative
 * location in the square. For Linear Gradient Fills a ratio of zero is mapped
 * to the left side of the gradient square and 255 is mapped to the right side
 * of the square. For Radial Gradient Fills a ratio of zero is mapped to the
 * centre of the gradient square and 255 is mapped to the edge of the largest
 * circle that fits inside the gradient square. A ratio is used rather than
 * specifying coordinates within the gradient square as the coordinate space is
 * transformed to fit the shape that the gradient is being displayed in.
 * </p>
 *
 * <p>
 * Note that the object used to create the shape definition determines whether
 * the alpha channel is encoded in the gradient colours. Simply specifying the
 * level of transparency in the Color object is not sufficient.
 * </p>
 *
 * @see GradientFill
 */
public class Gradient implements SWFEncodeable {

    /** Maximum number of gradient records. */
    public static final int MAX_GRADIENTS = 15;
    /** Maximum ratio along the gradient square, range 0..255. */
    private static final int MAX_RATIO = 255;

    /** Format string used in toString() method. */
    private static final String FORMAT = "Gradient: { ratio=%d; color=%s}";
    /** The ratio across the gradient square. */
    private final transient int ratio;
    /** The colour for gradient. */
    private final transient Color color;

    /**
     * Creates and initialises a Gradient object using values encoded
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
    public Gradient(final SWFDecoder coder, final Context context)
            throws IOException {
        ratio = coder.readByte();
        color = new Color(coder, context);
    }

    /**
     * Creates a Gradient object with the specified ratio and color.
     *
     * @param aRatio
     *            the ratio along the gradient square. Must be in the range
     *            0..255.
     * @param aColor
     *            the color at the control point. Must not be null.
     */
    public Gradient(final int aRatio, final Color aColor) {
        if ((aRatio < 0) || (aRatio > MAX_RATIO)) {
            throw new IllegalArgumentRangeException(0, MAX_RATIO, aRatio);
        }
        ratio = aRatio;

        if (aColor == null) {
            throw new IllegalArgumentException();
        }
        color = aColor;
    }

    /**
     * Get the ratio that defines the relative point across the gradient
     * square.
     *
     * @return the ratio for the gradient in the range 0..255.
     */
    public int getRatio() {
        return ratio;
    }

    /**
     * Get the colour that is displayed at the control point across the
     * gradient square defined by the ratio.
     *
     * @return the colour for the gradient at the ratio point.
     */
    public Color getColor() {
        return color;
    }

    @Override
    public String toString() {
        return String.format(FORMAT, ratio, color);
    }

    @Override
    public boolean equals(final Object object) {
        boolean result;
        Gradient gradient;

        if (object == null) {
            result = false;
        } else if (object == this) {
            result = true;
        } else if (object instanceof Gradient) {
            gradient = (Gradient) object;
            result = (ratio == gradient.ratio) && color.equals(gradient.color);
        } else {
            result = false;
        }
        return result;
    }

    @Override
    public int hashCode() {
        return (ratio * Constants.PRIME) + color.hashCode();
    }

    /** {@inheritDoc} */
    @Override
	public int prepareToEncode(final Context context) {
        return 1 + color.prepareToEncode(context);
    }

    /** {@inheritDoc} */
    @Override
	public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        coder.writeByte(ratio);
        color.encode(coder, context);
    }
}
