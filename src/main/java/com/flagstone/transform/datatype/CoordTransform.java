/*
 * CoordTransform.java
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
 * CoordTransform is used to specify a two-dimensional coordinate transform
 * which allows an object to be scaled, rotated or moved without changing the
 * original definition of how the object is drawn.
 *
 * <p>
 * A two-dimensional transform is defined using a 3x3 matrix and the new values
 * for a pair of coordinates (x,y) are calculated using the following matrix
 * multiplication:
 * </p>
 *
 * <img src="doc-files/transform.gif">
 *
 * <p>
 * Different transformations such as scaling, rotation, shearing and translation
 * can be performed using the above matrix multiplication. More complex
 * transformations can be defined by performing successive matrix
 * multiplications in a process known as compositing. This allows a complex
 * transformations to performed on an object. However not that compositing
 * transforms is not commutative, the order in which transformations are applied
 * will affect the final result.
 * </p>
 */
public class CoordTransform implements SWFEncodeable {
    /** Offset to add to number of bits when calculating number of bytes. */
    private static final int ROUND_TO_BYTES = 7;
    /** Right shift to convert number of bits to number of bytes. */
    private static final int BITS_TO_BYTES = 3;
    /** Format used by toString() to display object representation. */
    private static final String FORMAT = "CoordTransform: { scaleX=%f;"
            + " scaleY=%f; shearX=%f; shearY=%f; transX=%d; transY=%d}";

    /**
     * The default value used for the scaling terms when a translation or
     * shearing transform is created.
     */
    public static final float DEFAULT_SCALE = 1.0f;
    /**
     * The default value used for the shearing terms when a translation or
     * scaling transform is created.
     */
    public static final float DEFAULT_SHEAR = 0.0f;
    /**
     * The default value used for the translation terms when a scaling or
     * shearing transform is created.
     */
    public static final int DEFAULT_COORD = 0;
    /**
     * The factor applied to real numbers used for scaling terms when storing
     * them as fixed point values.
     */
    public static final float SCALE_FACTOR = 65536.0f;
    /**
     * The factor applied to real numbers used for shearing terms when storing
     * them as fixed point values.
     */
    public static final float SHEAR_FACTOR = 65536.0f;

    /**
     * Size of bit-field used to specify the number of bits representing
     * encoded transform values.
     */
    private static final int FIELD_SIZE = 5;
    /** Default value for scaling terms. */
    private static final int DEFAULT_INT_SCALE = 65536;
    /** Default value for shearing terms. */
    private static final int DEFAULT_INT_SHEAR = 0;

    /**
     * Create a new coordinate transform by multiplying two matrices together to
     * calculate the product. Since matrix multiplication is not commutative the
     * order in which the arguments are passed is important.
     *
     * @param left
     *            a 3x3 matrix
     * @param right
     *            a 3x3 matrix
     * @return a new 3x3 matrix contains the product of the two arguments.
     */
    public static float[][] product(final float[][] left,
            final float[][] right) {
        return new float[][] {
                {
                        left[0][0] * right[0][0] + left[0][1] * right[1][0]
                                + left[0][2] * right[2][0],
                        left[0][0] * right[0][1] + left[0][1] * right[1][1]
                                + left[0][2] * right[2][1],
                        left[0][0] * right[0][2] + left[0][1] * right[1][2]
                                + left[0][2] * right[2][2] },
                {
                        left[1][0] * right[0][0] + left[1][1] * right[1][0]
                                + left[1][2] * right[2][0],
                        left[1][0] * right[0][1] + left[1][1] * right[1][1]
                                + left[1][2] * right[2][1],
                        left[1][0] * right[0][2] + left[1][1] * right[1][2]
                                + left[1][2] * right[2][2] },
                {
                        left[2][0] * right[0][0] + left[2][1] * right[1][0]
                                + left[2][2] * right[2][0],
                        left[2][0] * right[0][1] + left[2][1] * right[1][1]
                                + left[2][2] * right[2][1],
                        left[2][0] * right[0][2] + left[2][1] * right[1][2]
                                + left[2][2] * right[2][2] } };
    }

    /**
     * Create a translation transform.
     *
     * @param xCoord
     *            the x coordinate of the transformation.
     * @param yCoord
     *            the y coordinate of the transformation.
     * @return a CoordTransform containing the translation.
     */
    public static CoordTransform translate(final int xCoord, final int yCoord) {
        return new CoordTransform(1.0f, 1.0f, 0.0f, 0.0f, xCoord, yCoord);
    }

    /**
     * Create a scaling transform.
     *
     * @param xScale
     *            the scaling factor along the x-axis.
     * @param yScale
     *            the scaling factor along the y-axis
     * @return a CoordTransform containing the scaling transform.
     */
    public static CoordTransform scale(final float xScale, final float yScale) {
        return new CoordTransform(xScale, yScale, 0.0f, 0.0f, 0, 0);
    }

    /**
     * Create a CoordTransform initialised for a shearing operation.
     *
     * @param xShear
     *            the shearing factor along the x-axis.
     * @param yShear
     *            the shearing factor along the y-axis
     * @return a CoordTransform containing the shearing transform.
     */
    public static CoordTransform shear(final float xShear, final float yShear) {
        return new CoordTransform(1.0f, 1.0f, xShear, yShear, 0, 0);
    }

    /**
     * Create a CoordTransform initialised for a rotation in degrees.
     *
     * @param angle
     *            the of rotation in degrees.
     * @return a CoordTransform containing the rotation.
     */
    public static CoordTransform rotate(final int angle) {

        final double radians = Math.toRadians(angle);
        final double cos =  Math.cos(radians);
        final double sin = Math.sin(radians);

        return new CoordTransform((float) cos, (float) cos,
                (float) sin, -(float) sin, 0, 0);
    }

    /** Holds the value for scaling in the x-direction. */
    private final transient int scaleX;
    /** Holds the value for scaling in the y-direction. */
    private final transient int scaleY;
    /** Holds the value for shearing in the x-direction. */
    private final transient int shearX;
    /** Holds the value for shearing in the y-direction. */
    private final transient int shearY;
    /** Holds the value for a translation in the x-direction. */
    private final transient int translateX;
    /** Holds the value for a translation in the x-direction. */
    private final transient int translateY;

    /**
     * Flag used to optimise encoding so checking whether scaling terms are
     * set is performed only once.
     */
    private transient boolean hasScale;
    /**
     * Flag used to optimise encoding so checking whether shearing terms are
     * set is performed only once.
     */
    private transient boolean hasShear;

    /**
     * Used to store the number of bits required to encode or decode
     * scaling terms.
     */
    private transient int scaleSize;
    /**
     * Used to store the number of bits required to encode or decode
     * shearing terms.
     */
    private transient int shearSize;
    /**
     * Used to store the number of bits required to encode or decode
     * translation terms.
     */
    private transient int transSize;


    /**
     * Creates and initialises a CoordTransform object using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public CoordTransform(final SWFDecoder coder) throws IOException {

        coder.alignToByte();

        hasScale = coder.readBits(1, false) != 0;

        if (hasScale) {
            scaleSize = coder.readBits(FIELD_SIZE, false);
            scaleX = coder.readBits(scaleSize, true);
            scaleY = coder.readBits(scaleSize, true);
        } else {
            scaleX = DEFAULT_INT_SCALE;
            scaleY = DEFAULT_INT_SCALE;
        }

        hasShear = coder.readBits(1, false) != 0;

        if (hasShear) {
            shearSize = coder.readBits(FIELD_SIZE, false);
            shearX = coder.readBits(shearSize, true);
            shearY = coder.readBits(shearSize, true);
        } else {
            shearX = DEFAULT_INT_SHEAR;
            shearY = DEFAULT_INT_SHEAR;
        }

        transSize = coder.readBits(FIELD_SIZE, false);
        translateX = coder.readBits(transSize, true);
        translateY = coder.readBits(transSize, true);

        coder.alignToByte();
    }

    /**
     * Creates and initialises a CoordTransform object using a 3x3 matrix.
     *
     * @param matrix
     *            a 3x3 matrix containing the transform values.
     */
    public CoordTransform(final float[][] matrix) {
        scaleX = (int) (matrix[0][0] * SCALE_FACTOR);
        scaleY = (int) (matrix[1][1] * SCALE_FACTOR);
        shearX = (int) (matrix[1][0] * SHEAR_FACTOR);
        shearY = (int) (matrix[0][1] * SHEAR_FACTOR);
        translateX = (int) matrix[0][2];
        translateY = (int) matrix[1][2];
    }

    /**
     * Creates an initialises a CoordTransform with scaling, shearing and
     * translation values.
     *
     * @param xScale
     *            the scaling factor along the x-axis.
     * @param yScale
     *            the scaling factor along the y-axis
     * @param xShear
     *            the shearing factor along the x-axis.
     * @param yShear
     *            the shearing factor along the y-axis
     * @param xCoord
     *            the x coordinate of the transformation.
     * @param yCoord
     *            the y coordinate of the transformation.
     */
    public CoordTransform(final float xScale, final float yScale,
            final float xShear, final float yShear, final int xCoord,
            final int yCoord) {
        scaleX = (int) (xScale * SCALE_FACTOR);
        scaleY = (int) (yScale * SCALE_FACTOR);
        shearX = (int) (xShear * SHEAR_FACTOR);
        shearY = (int) (yShear * SHEAR_FACTOR);
        translateX = xCoord;
        translateY = yCoord;
    }

    /**
     * Returns the scaling factor along the x-axis.
     *
     * @return the scaling factor in the x-direction.
     */
    public float getScaleX() {
        return scaleX / SCALE_FACTOR;
    }

    /**
     * Returns the scaling factor along the y-axis.
     *
     * @return the scaling factor in the y-direction.
     */
    public float getScaleY() {
        return scaleY / SCALE_FACTOR;
    }

    /**
     * Returns the shearing factor along the x-axis.
     *
     * @return the shear factor in the x-direction.
     */
    public float getShearX() {
        return shearX / SHEAR_FACTOR;
    }

    /**
     * Returns the shearing factor along the y-axis.
     *
     * @return the shear factor in the y-direction.
     */
    public float getShearY() {
        return shearY / SHEAR_FACTOR;
    }

    /**
     * Returns the translation in the x direction.
     *
     * @return the translation, measured in twips, in the x-direction.
     */
    public int getTranslateX() {
        return translateX;
    }

    /**
     * Returns the translation along the y-axis.
     *
     * @return the translation, measured in twips, in the y-direction.
     */
    public int getTranslateY() {
        return translateY;
    }

    /**
     * Returns a matrix that can be used to create composite transforms.
     *
     * @return the 3 X 3 array that is used to store the transformation values.
     */
    public float[][] getMatrix() {
        return new float[][] {
            {scaleX / SCALE_FACTOR, shearY / SHEAR_FACTOR, translateX },
            {shearX / SHEAR_FACTOR, scaleY / SCALE_FACTOR, translateY },
            {0.0f, 0.0f, 1.0f } };
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, getScaleX(), getScaleY(),
                getShearX(), getShearY(), translateX, translateY);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object object) {
        boolean result;
        CoordTransform transform;

        if (object == null) {
            result = false;
        } else if (object == this) {
            result = true;
        } else if (object instanceof CoordTransform) {
            transform = (CoordTransform) object;
            result = (scaleX == transform.scaleX)
                    && (scaleY == transform.scaleY)
                    && (shearX == transform.shearX)
                    && (shearY == transform.shearY)
                    && (translateX == transform.translateX)
                    && (translateY == transform.translateY);
        } else {
            result = false;
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return ((((scaleX * Constants.PRIME + scaleY)
                * Constants.PRIME + shearX)
                * Constants.PRIME + shearY)
                * Constants.PRIME + translateX)
                * Constants.PRIME + translateY;
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {

        int numberOfBits = 2 + FIELD_SIZE + ROUND_TO_BYTES;

        hasScale = (scaleX != DEFAULT_INT_SCALE)
                || (scaleY != DEFAULT_INT_SCALE);
        hasShear = (shearX != 0) || (shearY != 0);

        if (hasScale || hasShear || ((translateX != 0) || (translateY != 0))) {
            transSize = Math.max(Coder.size(translateX),
                    Coder.size(translateY));
        } else {
            transSize = 0;
        }

        numberOfBits += transSize << 1;

        if (hasScale) {
            scaleSize = Math.max(Coder.size(scaleX),
                    Coder.size(scaleY));
            numberOfBits += FIELD_SIZE + (scaleSize << 1);
        }

        if (hasShear) {
            shearSize = Math.max(Coder.size(shearX),
                    Coder.size(shearY));
            numberOfBits += FIELD_SIZE + (shearSize << 1);
        }

        return numberOfBits >> BITS_TO_BYTES;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {

        if (hasScale) {
            coder.writeBits(1, 1);
            coder.writeBits(scaleSize, FIELD_SIZE);
            coder.writeBits(scaleX, scaleSize);
            coder.writeBits(scaleY, scaleSize);
        } else {
            coder.writeBits(0, 1);
        }

        if (hasShear) {
            coder.writeBits(1, 1);
            coder.writeBits(shearSize, FIELD_SIZE);
            coder.writeBits(shearX, shearSize);
            coder.writeBits(shearY, shearSize);
        } else {
            coder.writeBits(0, 1);
        }

        coder.writeBits(transSize, FIELD_SIZE);
        coder.writeBits(translateX, transSize);
        coder.writeBits(translateY, transSize);

        coder.alignToByte();
    }
}
