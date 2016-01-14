/*
 * BlurFilter.java
 * Transform
 *
 * Copyright (c) 2009-2010 Flagstone Software Ltd. All rights reserved.
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

package com.flagstone.transform.filter;


import java.io.IOException;

import com.flagstone.transform.Constants;
import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.exception.IllegalArgumentRangeException;

/**
 * BlurFilter is used to apply a Box filter to the pixel of an object on the
 * display list.
 */
public class BlurFilter implements Filter {
    /** Maximum number of passes to blur an object. */
    private static final int MAX_BLUR_COUNT = 31;

    /** Format string used in toString() method. */
    private static final String FORMAT = "BlurFilter: { blurX=%f; blurY=%f;"
            + " passes=%d}";

    /** The horizontal blur amount. */
    private final transient int blurX;
    /** The vertical blur amount. */
    private final transient int blurY;
    /** The number of blur passes. */
    private final transient int passes;

    /**
     * Creates and initialises a BlueFilter object using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public BlurFilter(final SWFDecoder coder)
            throws IOException {
        blurX = coder.readInt();
        blurY = coder.readInt();
        // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 1 LINES
        passes = coder.readByte() >>> 3;
    }

    /**
     * Create a BlurFilter with the horizontal and vertical blur values and the
     * number of passes.
     *
     * @param xBlur the blur amount in the x-direction.
     * @param yBlur the blue amount in the y-direction.
     * @param count the number of passes to apply.
     */
    public BlurFilter(final float xBlur, final float yBlur, final int count) {
        blurX = (int) (xBlur * Coder.SCALE_16);
        blurY = (int) (yBlur * Coder.SCALE_16);

        if ((count < 0) || (count > MAX_BLUR_COUNT)) {
            throw new IllegalArgumentRangeException(0, MAX_BLUR_COUNT, count);
        }
        passes = count;
    }

    /**
     * Get the blur amount in the x-direction.
     * @return the horizontal blur amount.
     */
    public float getBlurX() {
        return blurX / Coder.SCALE_16;
    }

    /**
     * Get the blur amount in the y-direction.
     * @return the vertical blur amount.
     */
    public float getBlurY() {
        return blurY / Coder.SCALE_16;
    }

    /**
     * Get the number of passes.
     * @return the times the blurring is applied.
     */
    public int getPasses() {
        return passes;
    }

    @Override
    public String toString() {
        return String.format(FORMAT, getBlurX(), getBlurY(), passes);
    }

    @Override
    public boolean equals(final Object object) {
        boolean result;
        BlurFilter filter;

        if (object == null) {
            result = false;
        } else if (object == this) {
            result = true;
        } else if (object instanceof BlurFilter) {
            filter = (BlurFilter) object;
            result = (blurX == filter.blurX) && (blurY == filter.blurY)
                    && (passes == filter.passes);
        } else {
            result = false;
        }
        return result;
    }

    @Override
    public int hashCode() {
        return ((blurX * Constants.PRIME) + blurY) * Constants.PRIME + passes;
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 1 LINES
        return 10;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        coder.writeByte(FilterTypes.BLUR);
        coder.writeInt(blurX);
        coder.writeInt(blurY);
        // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 1 LINES
        coder.writeByte(passes << 3);

    }
}
