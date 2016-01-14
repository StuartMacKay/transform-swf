/*
 * MorphGradient.java
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
import com.flagstone.transform.coder.Copyable;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncodeable;
import com.flagstone.transform.coder.SWFEncoder;

/**
 * MorphGradient defines the control points that is used to specify how a
 * gradient fill is displayed at the start and end of the shape morphing
 * process.
 *
 * <p>
 * The ratio is a number between 0 and 255 - that specifies the relative
 * location in the square. For Linear Gradient Fills a ratio of zero is mapped
 * to the left side of the gradient square and 255 is mapped to the right side
 * of the square. For Radial Gradient Fills a ratio of zero is mapped to the
 * centre of the gradient square and 255 is mapped to the edge of the largest
 * circle that fits inside the gradient square. The color is the colour to be
 * displayed at the point identified by the ratio.
 * </p>
 *
 * <p>
 * The MorphGradient defines ratios and colours for the start and end of the
 * morphing process, the Flash Player performs the interpolation between the two
 * values as the shape is morphed.
 * </p>
 *
 * @see Gradient
 * @see GradientFill
 * @see MorphGradientFill
 */
public class MorphGradient implements SWFEncodeable,
        Copyable<MorphGradient> {

    /** Format string used in toString() method. */
    private static final String FORMAT = "MorphGradient: { start=%s; end=%s}";
    /** The gradient at the start of the morphing process. */
    private Gradient start;
    /** The gradient at the end of the morphing process. */
    private Gradient end;

    /**
     * Creates and initialises a MorphGradient object using values encoded
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
    public MorphGradient(final SWFDecoder coder, final Context context)
            throws IOException {
        start = new Gradient(coder, context);
        end = new Gradient(coder, context);
    }

    /**
     * Creates a MorphGradient object specifying the starting and ending
     * gradients.
     *
     * @param startGradient
     *            the Gradient containing the initial ratio and colour.
     * @param endGradient
     *            the Gradient containing the final ratio and colour.
     */
    public MorphGradient(final Gradient startGradient,
            final Gradient endGradient) {
        setStart(startGradient);
        setEnd(endGradient);
    }

    /**
     * Creates and initialises a MorphGradient object using the values copied
     * from another MorphGradient object.
     *
     * @param object
     *            a MorphGradient object from which the values will be
     *            copied.
     */
    public MorphGradient(final MorphGradient object) {
        start = object.start;
        end = object.end;
    }

    /**
     * Get the gradient containing the ratio and colour at the start of the
     * morphing process.
     *
     * @return the starting gradient.
     */
    public Gradient getStart() {
        return start;
    }

    /**
     * Get the gradient containing the ratio and colour at the end of the
     * morphing process.
     *
     * @return the final gradient.
     */
    public Gradient getEnd() {
        return end;
    }

    /**
     * Sets the gradient at the start of the morphing process.
     *
     * @param gradient
     *            the Gradient containing the ratio and colour at the start of
     *            the morphing process.
     */
    public void setStart(final Gradient gradient) {
        if (gradient == null) {
            throw new IllegalArgumentException();
        }
        start = gradient;
    }

    /**
     * Sets the gradient at the end of the morphing process.
     *
     * @param gradient
     *            the Gradient containing the ratio and colour at the end of the
     *            morphing process.
     */
    public void setEnd(final Gradient gradient) {
        if (gradient == null) {
            throw new IllegalArgumentException();
        }
        end = gradient;
    }

    /** {@inheritDoc} */
    public MorphGradient copy() {
        return new MorphGradient(this);
    }

    @Override
    public String toString() {
        return String.format(FORMAT, start.toString(), end.toString());
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        int length = start.prepareToEncode(context);
        length += end.prepareToEncode(context);

        return length;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        start.encode(coder, context);
        end.encode(coder, context);
    }
}
