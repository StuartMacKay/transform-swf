/*
 * Color.java
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

import com.flagstone.transform.Constants;

import java.io.IOException;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncodeable;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.exception.IllegalArgumentRangeException;

/**
 * Color is used to represent 32-bit colours in the RGB colour space with 8 bits
 * per channel and an optional alpha channel.
 *
 * <p>
 * Whether a colour contains transparency information is determined by the
 * object that contains the colour. For example the colours in a DefineShape or
 * DefineShape2 do not use the alpha channel while those in DefineShape3 do. The
 * Context object, passed to each Color object, when it is encoded or decoded
 * signals whether the alpha channel should be included.
 * </p>
 */
public class Color implements SWFEncodeable {

    /**
     * The number of channels in an opaque Color object. Only used within the
     * framework or when adding a new class.
     */
    public static final int RGB = 3;
    /**
     * The number of channels in a transparent Color object. Only used within
     * the framework or when adding a new class.
     */
    public static final int RGBA = 4;

    /** Format used by toString() to display object representation. */
    private static final String FORMAT = "Color: {"
            + " red=%d; green=%d; blue=%d; alpha=%d}";

    /** The minimum value that can be assigned to a colour channel. */
    public static final int MIN_LEVEL = 0;
    /** The maximum value that can be assigned to a colour channel. */
    public static final int MAX_LEVEL = 255;

    /** Holds the value for the red colour channel. */
    private final transient int red;
    /** Holds the value for the green colour channel. */
    private final transient int green;
    /** Holds the value for the blue colour channel. */
    private final transient int blue;
    /**
     * Holds the value for the alpha colour channel. Defaults to MAX_LEVEL
     * (255) when used in classes such as DefineShape or DefineShape2
     * that only support opaque colours. */
    private final transient int alpha;

    /**
     * Creates and initialises a Color object using values encoded
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
    public Color(final SWFDecoder coder, final Context context)
            throws IOException {
        red = coder.readByte();
        green = coder.readByte();
        blue = coder.readByte();

        if (context.contains(Context.TRANSPARENT)) {
            alpha = coder.readByte();
        } else {
            alpha = MAX_LEVEL;
        }
    }

    /**
     * Creates a Color object containing red, green and blue channels. The alpha
     * channel defaults to the value 255 - defining an opaque colour.
     *
     * @param rVal
     *            value for the red channel, in the range 0..255.
     * @param gVal
     *            value for the green channel, in the range 0..255.
     * @param bVal
     *            value for the blue channel, in the range 0..255.
     */
    public Color(final int rVal, final int gVal, final int bVal) {
        this(rVal, gVal, bVal, MAX_LEVEL);
    }

    /**
     * Creates a transparent Color object containing red, green, blue and alpha
     * channels.
     *
     * @param rVal
     *            value for the red channel, in the range 0..255.
     * @param gVal
     *            value for the green channel, in the range 0..255.
     * @param bVal
     *            value for the blue channel, in the range 0..255.
     * @param aVal
     *            value for the alpha channel, in the range 0..255.
     */
    public Color(final int rVal, final int gVal,
            final int bVal, final int aVal) {
        if ((rVal < MIN_LEVEL) || (rVal > MAX_LEVEL)) {
            throw new IllegalArgumentRangeException(MIN_LEVEL, MAX_LEVEL, rVal);
        }
        if ((gVal < MIN_LEVEL) || (gVal > MAX_LEVEL)) {
            throw new IllegalArgumentRangeException(MIN_LEVEL, MAX_LEVEL, gVal);
        }
        if ((bVal < MIN_LEVEL) || (bVal > MAX_LEVEL)) {
            throw new IllegalArgumentRangeException(MIN_LEVEL, MAX_LEVEL, bVal);
        }
        if ((aVal < MIN_LEVEL) || (aVal > MAX_LEVEL)) {
            throw new IllegalArgumentRangeException(MIN_LEVEL, MAX_LEVEL, aVal);
        }

        red = rVal;
        green = gVal;
        blue = bVal;
        alpha = aVal;
    }

    /**
     * Returns the value for the red colour channel.
     *
     * @return the level for the red colour channel in the range 0..255.
     */
    public int getRed() {
        return red;
    }

    /**
     * Returns the value for the green colour channel.
     *
     * @return the level for the green colour channel in the range 0..255.
     */
    public int getGreen() {
        return green;
    }

    /**
     * Returns the value for the blue colour channel.
     *
     * @return the level for the blue colour channel in the range 0..255.
     */
    public int getBlue() {
        return blue;
    }

    /**
     * Returns the value for the alpha colour channel.
     *
     * @return the level for the alpha channel in the range 0..255.
     */
    public int getAlpha() {
        return alpha;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, red, green, blue, alpha);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object object) {
        boolean result;
        Color color;

        if (object == null) {
            result = false;
        } else if (object == this) {
            result = true;
        } else if (object instanceof Color) {
            color = (Color) object;
            result = (red == color.red) && (green == color.green)
                    && (blue == color.blue) && (alpha == color.alpha);
        } else {
            result = false;
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return ((red * Constants.PRIME + green) * Constants.PRIME + blue)
            * Constants.PRIME + alpha;
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        int size;
        if (context.contains(Context.TRANSPARENT)) {
            size = RGBA;
        } else {
            size = RGB;
        }
        return size;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        coder.writeByte(red);
        coder.writeByte(green);
        coder.writeByte(blue);

        if (context.contains(Context.TRANSPARENT)) {
            coder.writeByte(alpha);
        }
    }
}
