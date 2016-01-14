/*
 * BitmapFill.java
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

import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.datatype.CoordTransform;
import com.flagstone.transform.exception.IllegalArgumentRangeException;

/**
 * BitmapFill is used to display an image inside a shape. An image cannot be
 * displayed directly, it must be displayed inside of a shape using this style.
 * The style controls how the image is displayed inside the shape. Images may be
 * clipped if they are too large to fit or tiled across the available area if
 * they are too small. A coordinate transform can also be used to change its
 * size, location relative to the origin of the shape, orientation, etc., when
 * it is displayed. Four types of bitmap fill are supported:
 *
 * <ol>
 * <li>Clipped - If the image is larger than the shape then it will be clipped.
 * Conversely if the area to be filled is larger than the image the colour at
 * the edge of the image is used to fill the remainder of the shape.</li>
 *
 * <li>Tiled - if the area to be filled is larger than the image then the image
 * is tiled to fill the area, otherwise as with the Clipped style the colour at
 * the edge of the image will be use to fill the space available.</li>
 *
 * <li>Unsmoothed Clipped - Same as Clipped but if the image is smaller than the
 * shape the colour used to fill the space available is not smoothed. This style
 * was added to increase performance with few visible artifacts.</li>
 *
 * <li>Unsmoothed Tiled - Same as Tiled but no smoothing is applied if the
 * colour at the edge of the image is used to fill the space available. Again
 * this was introduced to increase performance.</li>
 * </ol>
 *
 * <p>
 * The most common use of the coordinate transform is to scale an image so it
 * displayed at the correct resolution. When an image is loaded its width and
 * height default to twips rather than pixels. An image 300 x 200 pixels will be
 * displayed as 300 x 200 twips (15 x 10 pixels). Scaling the image by 20 (20
 * twips = 1 pixel) using the CoordTransform object will restore it to its
 * original size.
 * </p>
 *
 * <p>
 * The coordinate transform is also used to control the image registration. An
 * image is drawn with the top left corner placed at the origin (0, 0) of the
 * shape being filled. The transform can be used to apply different translations
 * to the image so its position can be adjusted relative to the origin of the
 * enclosing shape.
 * </p>
 */
public class BitmapFill implements FillStyle {

    /** Format string used in toString() method. */
    private static final String FORMAT = "BitmapFill: { identifier=%d;"
    		+ " transform=%s}";
    /** Bit mask for tiled or clipped field in bitmap fills. */
    private static final int CLIPPED_MASK = 1;
    /** Bit mask for smoothed or unsmoothed field in bitmap fills. */
    private static final int SMOOTHED_MASK = 2;

    /** Code used to identify the fill style when it is encoded. */
    private transient int type;
    /** The unique identifier of the image that will be displayed. */
    private int identifier;
    /** The coordinate transform for scaling the image when it is displayed. */
    private CoordTransform transform;

    /**
     * Creates and initialises a BitmapFill fill style using values encoded
     * in the Flash binary format.
     *
     * @param fillType the value used to identify the fill style when it is
     * encoded.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public BitmapFill(final int fillType, final SWFDecoder coder)
                throws IOException {
        type = fillType;
        identifier = coder.readUnsignedShort();
        transform = new CoordTransform(coder);
    }

    /**
     * Creates a BitmapFill object, setting the fill type, the unique identifier
     * for the image and the coordinate transform used to set the scale and
     * registration of the image.
     *
     * @param tiled
     *            whether the image will be repeated if it smaller than the area
     *            to be filled.
     * @param smoothed
     *            whether the image will be smoothed to improve display quality.
     * @param uid
     *            the unique identifier of the object containing the image to be
     *            displayed. Must be in the range 1..65535.
     * @param position
     *            a CoordTransform object that typically changes the size and
     *            location and position of the image inside the parent shape.
     */
    public BitmapFill(final boolean tiled, final boolean smoothed,
            final int uid, final CoordTransform position) {
        type = FillStyleTypes.TILED_BITMAP;
        setTiled(tiled);
        setSmoothed(smoothed);
        setIdentifier(uid);
        setTransform(position);
    }

    /**
     * Creates and initialises a BitmapFill fill style using the values copied
     * from another BitmapFill object.
     *
     * @param object
     *            a BitmapFill fill style from which the values will be
     *            copied.
     */
    public BitmapFill(final BitmapFill object) {
        type = object.type;
        identifier = object.identifier;
        transform = object.transform;
    }

    /**
     * Is the image tiled across the area defined in the shape.
     * @return true if the image is tiled, false otherwise.
     */
    public boolean isTiled() {
        return (type & CLIPPED_MASK) != 0;
    }

    /**
     * Sets whether the image tiled across the area defined in the shape.
     * @param tiled true if the image should be tiled, false if it is clipped.
     */
    public void setTiled(final boolean tiled) {
        if (tiled) {
            type &= ~CLIPPED_MASK;
        } else {
            type |= CLIPPED_MASK;
        }
    }

    /**
     * Is the image smoothed to increase quality.
     * @return true if the image is smoothed, false otherwise.
     */
    public boolean isSmoothed() {
        return (type & SMOOTHED_MASK) != 0;
    }

    /**
     * Sets whether the image smoothed when it is displayed.
     * @param smoothed true if the image should be smoothed, false if no
     * smoothing will be applied.
     */
    public void setSmoothed(final boolean smoothed) {
        if (smoothed) {
            type &= ~SMOOTHED_MASK;
        } else {
            type |= SMOOTHED_MASK;
        }
    }

    /**
     * Get the unique identifier of the object containing the image to be
     * displayed.
     *
     * @return the unique identifier for the image to be displayed.
     */
    public int getIdentifier() {
        return identifier;
    }

    /**
     * Sets the unique identifier of the object containing the image to be
     * displayed.
     *
     * @param uid
     *            the unique identifier of the object containing the image to be
     *            displayed which must be in the range 1..65535.
     */
    public void setIdentifier(final int uid) {
        if ((uid < 1) || (uid > Coder.USHORT_MAX)) {
            throw new IllegalArgumentRangeException(
                    1, Coder.USHORT_MAX, uid);
        }
        identifier = uid;
    }

    /**
     * Get the coordinate transform that will be applied to the image when
     * it is displayed.
     *
     * @return the coordinate transform applied to the image.
     */
    public CoordTransform getTransform() {
        return transform;
    }

    /**
     * Sets the coordinate transform applied to the image to display it inside
     * the shape. Typically the transform will scale the image by a factor of 20
     * so that the image is displayed at the correct screen resolution.
     *
     * @param matrix
     *            a CoordTransform object that changes the appearance and
     *            location of the image inside the shape. Must not be null.
     */
    public void setTransform(final CoordTransform matrix) {
        if (matrix == null) {
            throw new IllegalArgumentException();
        }
        transform = matrix;
    }

    /** {@inheritDoc} */
    public BitmapFill copy() {
        return new BitmapFill(this);
    }

    @Override
    public String toString() {
        return String.format(FORMAT, identifier, transform);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        // CHECKSTYLE:OFF
        return 3 + transform.prepareToEncode(context);
        // CHECKSTYLE:ON
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        coder.writeByte(type);
        coder.writeShort(identifier);
        transform.encode(coder, context);
    }
}
