/*
 * GradientFill.java
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
import java.util.ArrayList;
import java.util.List;

import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.datatype.CoordTransform;

/**
 * GradientFill defines how a colour changes across an area to be filled with
 * colour. Two types of gradient fill are supported:
 *
 * <ol>
 * <li>Linear - where the gradient changes in one direction across the area to
 * be filled.</li>
 *
 * <li>Radial - where the gradient changes radially from the centre of the area
 * to be filled.</li>
 * </ol>
 *
 * <p>
 * Gradients are defined in terms of a standard space called the gradient
 * square, centred at (0,0) and extending from (-16384, -16384) to (16384,
 * 16384).
 * </p>
 *
 * <img src="doc-files/gradientSquare.gif">
 *
 * <p>
 * A coordinate transform is required to map the gradient square to the
 * coordinates of the filled area. The transformation is applied in two steps.
 * First the gradient square is scaled so the colour covers the shape followed
 * by a translation to map the gradient square coordinates to the coordinate
 * range of the shape.
 * </p>
 *
 * <img src="gradientMapping.gif">
 *
 * <p>
 * A series of gradient points is used to control how the colour displayed
 * changes across the gradient. At least two points are required to define a
 * gradient - one for the starting colour and one for the final colour. When the
 * Flash Player displays the control points they are sorted by the ratio defined
 * in each Gradient object, with the smallest ratio value displayed first.
 * </p>
 *
 * @see Gradient
 */
public class GradientFill implements FillStyle {

    /** Bit mask for extracting the spread field in gradient fills. */
    private static final int SPREAD_MASK = 0x00C0;
    /** Bit mask for extracting the interpolation field in gradient fills. */
    private static final int INTER_MASK = 0x0030;
    /** Bit mask for extracting the interpolation field in gradient fills. */
    private static final int GRADIENT_MASK = 0x000F;
    /** Format string used in toString() method. */
    private static final String FORMAT = "GradientFill: { transform=%s;"
            + " gradients=%s}";

    /** Code used to identify the fill style when it is encoded. */
    private transient int type;
    /** Code for the Spread type. */
    private int spread;
    /** Interpolation for colour changes. */
    private int interpolation;
    /** Maps the Gradient Square to real coordinates. */
    private CoordTransform transform;
    /** List of gradients defining the colour changes. */
    private List<Gradient> gradients;

    /** Number of gradients in list. */
    private transient int count;

    /**
     * Creates and initialises a GradientFill fill style using values encoded
     * in the Flash binary format.
     *
     * @param fillType the value used to identify the fill style when it is
     * encoded.
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
    public GradientFill(final int fillType, final SWFDecoder coder,
            final Context context) throws IOException {
        type = fillType;
        transform = new CoordTransform(coder);
        count = coder.readByte();
        spread = count & SPREAD_MASK;
        interpolation = count & INTER_MASK;
        count = count & GRADIENT_MASK;
        gradients = new ArrayList<Gradient>(count);

        for (int i = 0; i < count; i++) {
            gradients.add(new Gradient(coder, context));
        }
    }

    /**
     * Creates a GradientFill object specifying the type, coordinate transform
     * and list of gradient points.
     *
     * @param gradientType
     *            identifies whether the gradient is rendered linearly or
     *            radially.
     * @param matrix
     *            the coordinate transform mapping the gradient square onto
     *            physical coordinates. Must not be null.
     * @param list
     *            a list of Gradient objects defining the control points for
     *            the gradient. For Flash 7 and earlier versions there can be up
     *            to 8 Gradients. For Flash 8 onwards this number was increased
     *            to 15. Must not be null.
     */
    public GradientFill(final GradientType gradientType,
            final CoordTransform matrix, final List<Gradient> list) {
        setType(gradientType);
        setTransform(matrix);
        setGradients(list);
    }

    /**
     * Creates a GradientFill object specifying the type, coordinate transform
     * and list of gradient points.
     *
     * @param gradientType
     *            identifies whether the gradient is rendered linearly or
     *            radially.
     * @param matrix
     *            the coordinate transform mapping the gradient square onto
     *            physical coordinates. Must not be null.
     * @param spreadType
     *            To be documented.
     * @param interpolationType
     *            how the changes in colours across the gradient are calculated.
     * @param list
     *            a list of Gradient objects defining the control points for
     *            the gradient. For Flash 7 and earlier versions there can be up
     *            to 8 Gradients. For Flash 8 onwards this number was increased
     *            to 15. Must not be null.
     */
    public GradientFill(final GradientType gradientType,
            final CoordTransform matrix,
            final Spread spreadType,
            final Interpolation interpolationType,
            final List<Gradient> list) {
        setType(gradientType);
        setTransform(matrix);
        setSpread(spreadType);
        setInterpolation(interpolationType);
        setGradients(list);
    }

    /**
     * Creates and initialises a GradientFill fill style using the values copied
     * from another GradientFill object.
     *
     * @param object
     *            a  GradientFill fill style from which the values will be
     *            copied.
     */
    public GradientFill(final GradientFill object) {
        type = object.type;
        transform = object.transform;
        gradients = new ArrayList<Gradient>(object.gradients);
    }

    /**
     * Get the GradientType that identifies whether the gradient is linear or
     * radial.
     *
     * @return the GradientType for the fill, either LINEAR or RADIAL.
     */
    public GradientType getType() {
        GradientType value;
        if (type == FillStyleTypes.LINEAR_GRADIENT) {
            value = GradientType.LINEAR;
        } else {
            value = GradientType.RADIAL;
        }
        return value;
    }

    /**
     * Set the GradientType that identifies whether the gradient is linear or
     * radial.
     *
     * @param gradientType
     *            the GradientType for the fill, either LINEAR or RADIAL.
     */
    public void setType(final GradientType gradientType) {
        if (gradientType == GradientType.LINEAR) {
            type = FillStyleTypes.LINEAR_GRADIENT;
        } else {
            type = FillStyleTypes.RADIAL_GRADIENT;
        }
    }

    /**
     * Get the Spread describing how the gradient fills the area: PAD - the
     * last colour fills the remaining area; REPEAT - the gradient is repeated;
     * REFLECT - the gradient is repeated but reflected (reversed) each time.
     *
     * @return the Spread, either PAD, REFLECT or REPEAT.
     */
    public Spread getSpread() {
        return Spread.fromInt(spread);
    }

    /**
     * Set the Spread describing how the gradient fills the area: either by
     * using the last gradient colour to fill the area (PAD); repeating the
     * gradient (REPEAT) or repeating but reversing it each time (REFLECT).
     *
     * @param spreadType the Spread, either PAD, REFLECT or REPEAT.
     */
    public void setSpread(final Spread spreadType) {
        spread = spreadType.getValue();
    }

    /**
     * Get the method used to calculate the colour changes across the gradient.
     *
     * @return the Interpolation that describes how colours change.
     */
    public Interpolation getInterpolation() {
        return Interpolation.fromInt(interpolation);
    }

    /**
     * Set the method used to calculate the colour changes across the gradient.
     *
     * @param interpolationType
     *            the Interpolation that describes how colours change.
     */
    public void setInterpolation(final Interpolation interpolationType) {
        interpolation = interpolationType.getValue();
    }

    /**
     * Get the coordinate transform mapping the gradient square onto
     * physical coordinates.
     *
     * @return the coordinate transform that defines the gradient displayed.
     */
    public CoordTransform getTransform() {
        return transform;
    }

    /**
     * Get the list of Gradient objects defining the points for the
     * gradient fill.
     *
     * @return the list of points defining the gradient.
     */
    public List<Gradient> getGradients() {
        return gradients;
    }

    /**
     * Sets the coordinate transform mapping the gradient square onto physical
     * coordinates.
     *
     * @param matrix
     *            the coordinate transform. Must not be null.
     */
    public void setTransform(final CoordTransform matrix) {
        if (matrix == null) {
            throw new IllegalArgumentException();
        }
        transform = matrix;
    }

    /**
     * Sets the list of control points that define the gradient. For Flash 7
     * and earlier this list can contain up to 8 Gradient objects. For Flash 8
     * onwards this limit was increased to 15.
     *
     * @param list
     *            a list of Gradient objects. Must not be null.
     */
    public void setGradients(final List<Gradient> list) {
        if (list == null) {
            throw new IllegalArgumentException();
        }
        if (list.size() > Gradient.MAX_GRADIENTS) {
            throw new IllegalStateException(
                    "Maximum number of gradients exceeded.");
        }
        gradients = list;
    }

    /**
     * Add a Gradient object to the list of gradient objects. For Flash 7 and
     * earlier versions there can be up to 8 Gradients. For Flash 8 onwards this
     * number was increased to 15.
     *
     * @param aGradient
     *            an Gradient object. Must not be null.
     *
     * @return this object.
     */
    public GradientFill add(final Gradient aGradient) {
        if (aGradient == null) {
            throw new IllegalArgumentException();
        }
        if (gradients.size() == Gradient.MAX_GRADIENTS) {
            throw new IllegalStateException(
                    "Maximum number of gradients exceeded.");
        }
        gradients.add(aGradient);
        return this;
    }

    /** {@inheritDoc} */
    public GradientFill copy() {
        return new GradientFill(this);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, transform, gradients);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        // CHECKSTYLE:OFF
        count = gradients.size();
        return 2
                + transform.prepareToEncode(context)
                + (count * (context.contains(
                        Context.TRANSPARENT) ? 5 : 4));
        // CHECKSTYLE:ON
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        coder.writeByte(type);
        transform.encode(coder, context);
        coder.writeByte(count | spread | interpolation);

        for (final Gradient gradient : gradients) {
            gradient.encode(coder, context);
        }
    }
}
