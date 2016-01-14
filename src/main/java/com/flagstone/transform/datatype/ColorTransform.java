/*
 * ColorTransform.java
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
 * A ColorTransform is used to change the colour of a shape or button without
 * changing the values in the original definition of the object.
 *
 * <p>
 * Two types of transformation are supported: Add and Multiply. In Add
 * transformations a value is added to each colour channel:
 * </p>
 *
 * <pre>
 * newRed = red + addRedTerm
 * newGreen = green + addGreenTerm
 * newBlue = blue + addBlueTerm
 * newAlpha = alpha + addAlphaTerm
 * </pre>
 *
 * <p>
 * In Multiply transformations each colour channel is multiplied by a given
 * value:
 * </p>
 *
 * <pre>
 * newRed = red * multiplyRedTerm
 * newGreen = green * multiplyGreenTerm
 * newBlue = blue * multiplyBlueTerm
 * newAlpha = alpha * multiplyAlphaTerm
 * </pre>
 *
 * <p>
 * Add and Multiply transforms may be combined in which case the multiply terms
 * are applied to the colour channel before the add terms.
 * </p>
 *
 * <pre>
 * newRed = (red * multiplyRedTerm) + addRedTerm
 * newGreen = (green * multiplyGreenTerm) + addGreenTerm
 * newBlue = (blue * multiplyBlueTerm) + addBlueTerm
 * newAlpha = (alpha * multiplyAlphaTerm) + addAlphaTerm
 * </pre>
 *
 * <p>
 * For each type of transform the result of the calculation is limited to the
 * range 0..255. If the result is less than 0 or greater than 255 then it is
 * clamped at 0 and 255 respectively.
 * </p>
 *
 * <p>
 * Not all objects containing a colour transform use the add or multiply terms
 * defined for the alpha channel. The colour objects defined in an DefineButton,
 * ButtonColorTransform or PlaceObject object do not use the alpha channel while
 * DefineButton2 and PlaceObject2 do. Whether the parent object uses the alpha
 * channel is stored in a SWFContext which is passed when the transform is
 * encoded or decoded.
 * </p>
 *
 */
public class ColorTransform implements SWFEncodeable {
    /** Offset to add to number of bits when calculating number of bytes. */
    private static final int ROUND_TO_BYTES = 7;
    /** Right shift to convert number of bits to number of bytes. */
    private static final int BITS_TO_BYTES = 3;
    /** Format used by toString() to display object representation. */
    private static final String FORMAT = "ColorTransform: {"
            + "multiply=[%f, %f, %f, %f]; add=[%d, %d, %d, %d]}";

    /**
     * Size of bit-field used to specify the number of bits representing
     * encoded transform values.
     */
    private static final int FIELD_SIZE = 4;
    /**
     * Default value for scaling multiply terms so they can be stored
     * internally as integers.
     */
    private static final float SCALE_MULTIPLY = 256.0f;
    /**
     * Default value for multiply terms when only an additive transform is
     * created.
     */
    private static final int DEFAULT_MULTIPLY = 256;
    /**
     * Default value for add terms when only an multiplicative transform is
     * created.
     */
    private static final int DEFAULT_ADD = 0;

    /** Multiply term for the red colour channel. */
    private final transient int multiplyRed;
    /** Multiply term for the green colour channel. */
    private final transient int multiplyGreen;
    /** Multiply term for the blue colour channel. */
    private final transient int multiplyBlue;
    /** Multiply term for the alpha colour channel. */
    private final transient int multiplyAlpha;

    /** Add term for the red colour channel. */
    private final transient int addRed;
    /** Add term for the green colour channel. */
    private final transient int addGreen;
    /** Add term for the blue colour channel. */
    private final transient int addBlue;
    /** Add term for the alpha colour channel. */
    private final transient int addAlpha;

    /**
     * Used in decoding and to optimise encoding so checking whether the
     * transform contains multiply terms is performed only once.
     */
    private final transient boolean hasMultiply;
    /**
     * Used in decoding and to optimise encoding so checking whether the
     * transform contains add terms is performed only once.
     */
    private final transient boolean hasAdd;

    /**
     * Used to store the number of bits required to encode or decode the
     * transform terms.
     */
    private transient int size;
    /**
     * Used to optimise decoding and encoding so checking the context to see
     * whether add and multiply terms for the alpha channel should be read
     * written is only performed once.
     */
    private transient boolean hasAlpha;

    /**
     * Creates and initialises a ColorTransform object using values encoded
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
    public ColorTransform(final SWFDecoder coder, final Context context)
            throws IOException {

        hasAlpha = context.contains(Context.TRANSPARENT);
        hasAdd = coder.readBits(1, false) != 0;
        hasMultiply = coder.readBits(1, false) != 0;
        size = coder.readBits(FIELD_SIZE, false);

        if (hasMultiply) {
            multiplyRed = coder.readBits(size, true);
            multiplyGreen = coder.readBits(size, true);
            multiplyBlue = coder.readBits(size, true);

            if (hasAlpha) {
                multiplyAlpha = coder.readBits(size, true);
            } else {
                multiplyAlpha = DEFAULT_MULTIPLY;
            }
        } else {
            multiplyRed = DEFAULT_MULTIPLY;
            multiplyGreen = DEFAULT_MULTIPLY;
            multiplyBlue = DEFAULT_MULTIPLY;
            multiplyAlpha = DEFAULT_MULTIPLY;
        }

        if (hasAdd) {
            addRed = coder.readBits(size, true);
            addGreen = coder.readBits(size, true);
            addBlue = coder.readBits(size, true);

            if (hasAlpha) {
                addAlpha = coder.readBits(size, true);
            } else {
                addAlpha = DEFAULT_ADD;
            }
        } else {
            addRed = DEFAULT_ADD;
            addGreen = DEFAULT_ADD;
            addBlue = DEFAULT_ADD;
            addAlpha = DEFAULT_ADD;
        }

        coder.alignToByte();
    }

    /**
     * Creates an add colour transform.
     *
     * @param rAdd
     *            value to add to the red colour channel.
     * @param gAdd
     *            value to add to the green colour channel.
     * @param bAdd
     *            value to add to the blue colour channel.
     * @param aAdd
     *            value to add to the alpha colour channel.
     */
    public ColorTransform(final int rAdd, final int gAdd,
            final int bAdd, final int aAdd) {
        multiplyRed = DEFAULT_MULTIPLY;
        multiplyGreen = DEFAULT_MULTIPLY;
        multiplyBlue = DEFAULT_MULTIPLY;
        multiplyAlpha = DEFAULT_MULTIPLY;

        addRed = rAdd;
        addGreen = gAdd;
        addBlue = bAdd;
        addAlpha = aAdd;

        hasMultiply = false;
        hasAdd = true;
    }

    /**
     * Creates a multiply colour transform.
     *
     * @param rMul
     *            value to multiply the red colour channel by.
     * @param gMul
     *            value to multiply the green colour channel by.
     * @param bMul
     *            value to multiply the blue colour channel by.
     * @param aMul
     *            value to multiply the alpha colour channel by.
     */
    public ColorTransform(final float rMul, final float gMul,
            final float bMul, final float aMul) {
        multiplyRed = (int) (rMul * SCALE_MULTIPLY);
        multiplyGreen = (int) (gMul * SCALE_MULTIPLY);
        multiplyBlue = (int) (bMul * SCALE_MULTIPLY);
        multiplyAlpha = (int) (aMul * SCALE_MULTIPLY);

        addRed = DEFAULT_ADD;
        addGreen = DEFAULT_ADD;
        addBlue = DEFAULT_ADD;
        addAlpha = DEFAULT_ADD;

        hasMultiply = true;
        hasAdd = false;
    }

    /**
     * Creates a colour transform that contains add and multiply terms.
     *
     * @param rMul
     *            value to multiply the red colour channel by.
     * @param gMul
     *            value to multiply the green colour channel by.
     * @param bMul
     *            value to multiply the blue colour channel by.
     * @param aMul
     *            value to multiply the alpha colour channel by.
     * @param rAdd
     *            value to add to the red colour channel.
     * @param gAdd
     *            value to add to the green colour channel.
     * @param bAdd
     *            value to add to the blue colour channel.
     * @param aAdd
     *            value to add to the alpha colour channel.
     */
    // CHECKSTYLE:OFF
    public ColorTransform(final int rAdd, final int gAdd,
            final int bAdd, final int aAdd, final float rMul,
            final float gMul, final float bMul, final float aMul) {
    // CHECKSTYLE:ON

        addRed = rAdd;
        addGreen = gAdd;
        addBlue = bAdd;
        addAlpha = aAdd;

        multiplyRed = (int) (rMul * SCALE_MULTIPLY);
        multiplyGreen = (int) (gMul * SCALE_MULTIPLY);
        multiplyBlue = (int) (bMul * SCALE_MULTIPLY);
        multiplyAlpha = (int) (aMul * SCALE_MULTIPLY);

        hasMultiply = true;
        hasAdd = true;
   }

    /**
     * Returns the value of the multiply term for the red channel.
     *
     * @return the value the red colour channel will be multiplied by.
     */
    public float getMultiplyRed() {
        return multiplyRed / SCALE_MULTIPLY;
    }

    /**
     * Returns the value of the multiply term for the green channel.
     *
     * @return the value the green colour channel will be multiplied by.
     */
    public float getMultiplyGreen() {
        return multiplyGreen / SCALE_MULTIPLY;
    }

    /**
     * Returns the value of the multiply term for the blue channel.
     *
     * @return the value the blue colour channel will be multiplied by.
     */
    public float getMultiplyBlue() {
        return multiplyBlue / SCALE_MULTIPLY;
    }

    /**
     * Returns the value of the multiply term for the alpha channel.
     *
     * @return the value the alpha channel will be multiplied by.
     */
    public float getMultiplyAlpha() {
        return multiplyAlpha / SCALE_MULTIPLY;
    }

    /**
     * Returns the value of the add term for the red channel.
     *
     * @return the value that will be added to the red colour channel.
     */
    public int getAddRed() {
        return addRed;
    }

    /**
     * Returns the value of the add term for the green channel.
     *
     * @return the value that will be added to the green colour channel.
     */
    public int getAddGreen() {
        return addGreen;
    }

    /**
     * Returns the value of the add term for the blue channel.
     *
     * @return the value that will be added to the blue colour channel.
     */
    public int getAddBlue() {
        return addBlue;
    }

    /**
     * Returns the value of the add term for the alpha channel.
     *
     * @return the value that will be added to the alpha channel.
     */
    public int getAddAlpha() {
        return addAlpha;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, getMultiplyRed(), getMultiplyGreen(),
                getMultiplyBlue(), getMultiplyAlpha(),
                addRed, addGreen, addBlue, addAlpha);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object object) {
        boolean result;
        ColorTransform transform;

        if (object == null) {
            result = false;
        } else if (object == this) {
            result = true;
        } else if (object instanceof ColorTransform) {
            transform = (ColorTransform) object;
            result = (addRed == transform.addRed)
                    && (addGreen == transform.addGreen)
                    && (addBlue == transform.addBlue)
                    && (addAlpha == transform.addAlpha)
                    && (multiplyRed == transform.multiplyRed)
                    && (multiplyGreen == transform.multiplyGreen)
                    && (multiplyBlue == transform.multiplyBlue)
                    && (multiplyAlpha == transform.multiplyAlpha);
        } else {
            result = false;
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return ((((((addRed * Constants.PRIME + addGreen)
                * Constants.PRIME + addBlue)
                * Constants.PRIME + addAlpha)
                * Constants.PRIME + multiplyRed)
                * Constants.PRIME + multiplyGreen)
                * Constants.PRIME + multiplyBlue)
                * Constants.PRIME + multiplyAlpha;
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {

        int numberOfBits = 2 + FIELD_SIZE + ROUND_TO_BYTES;

        hasAlpha = context.contains(Context.TRANSPARENT);
        size = 0;

        int numberOfBytes;

        if (hasAlpha) {
            numberOfBytes = Color.RGBA;
        } else {
            numberOfBytes = Color.RGB;
        }

        if (hasMultiply) {
            sizeTerms(multiplyRed, multiplyGreen, multiplyBlue, multiplyAlpha);
        }

        if (hasAdd) {
            sizeTerms(addRed, addGreen, addBlue, addAlpha);
        }

        if (hasMultiply) {
            numberOfBits += size * numberOfBytes;
        }

        if (hasAdd) {
            numberOfBits += size * numberOfBytes;
        }

        return numberOfBits >> BITS_TO_BYTES;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {

        coder.writeBits(hasAdd ? 1 : 0, 1);
        coder.writeBits(hasMultiply ? 1 : 0, 1);
        coder.writeBits(size, FIELD_SIZE);

        if (hasMultiply) {
            encodeTerms(multiplyRed, multiplyGreen, multiplyBlue,
                    multiplyAlpha, coder);
        }

        if (hasAdd) {
            encodeTerms(addRed, addGreen, addBlue, addAlpha, coder);
        }

        coder.alignToByte();
    }

    /**
     * Calculate the number of bits to encode either the add or multiply terms.
     *
     * @param red the term for the red channel.
     * @param green the term for the green channel.
     * @param blue the term for the blue channel.
     * @param alpha the term for the alpha channel.
     */
    private void sizeTerms(final int red, final int green, final int blue,
            final int alpha) {
        size = Math.max(size, Coder.size(red));
        size = Math.max(size, Coder.size(green));
        size = Math.max(size, Coder.size(blue));

        if (hasAlpha) {
            size = Math.max(size, Coder.size(alpha));
        }
    }

    /**
     * Encode the add or multiply terms.
     *
     * @param red the term for the red channel.
     * @param green the term for the green channel.
     * @param blue the term for the blue channel.
     * @param alpha the term for the alpha channel.
     * @param coder the Coder used to encode the data.
     *
     * @throws IOException if there is an error writing to the underlying
     * stream.
     */
    private void encodeTerms(final int red, final int green, final int blue,
            final int alpha, final SWFEncoder coder) throws IOException {
        coder.writeBits(red, size);
        coder.writeBits(green, size);
        coder.writeBits(blue, size);

        if (hasAlpha) {
            coder.writeBits(alpha, size);
        }
    }
}
