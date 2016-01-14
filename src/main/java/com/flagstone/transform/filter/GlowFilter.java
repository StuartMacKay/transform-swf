/*
 * GlowFilter.java
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
import com.flagstone.transform.datatype.Color;

/**
 * GlowFilter is used to create a glow effect around an object on the display
 * list.
 */
public class GlowFilter implements Filter {

    /**
     * Builder for creating GlowFilter objects.
     */
    public static final class Builder {
        /** The glow colour. */
        private transient Color color;
        /** The horizontal blur amount. */
        private transient int blurX;
        /** The vertical blur amount. */
        private transient int blurY;
        /** Strength of the glow. */
        private transient int strength;
        /** Compositing mode. */
        private transient int mode;
        /** The number of blur passes. */
        private transient int passes;

        /**
         * Set the colour of the glow.
         * @param aColor the glow colour.
         * @return this Builder.
         */
        public Builder setColor(final Color aColor) {
            color = aColor;
            return this;
        }

        /**
         * Set the blur amounts.
         * @param xAmount the horizontal blur amount.
         * @param yAmount the vertical blur amount.
         * @return this Builder.
         */
        public Builder setBlur(final float xAmount, final float yAmount) {
            blurX = (int) (xAmount * Coder.SCALE_16);
            blurY = (int) (yAmount * Coder.SCALE_16);
            return this;
        }

        /**
         * Set the compositing mode for the glow.
         * @param filterMode the compositing mode, either INNER, KNOCKOUT or
         * TOP.
         * @return this Builder.
         */
        public Builder setMode(final FilterMode filterMode) {
            switch (filterMode) {
            case KNOCKOUT:
                mode = Coder.BIT6;
                break;
            case INNER:
                mode = Coder.BIT7;
                break;
            default:
                throw new IllegalArgumentException();
            }
            return this;
        }

        /**
         * Set the glow strength.
         * @param weight the weight of the glow.
         * @return this Builder.
         */
        public Builder setStrength(final float weight) {
            strength = (int) (weight * Coder.SCALE_8);
            return this;
        }

        /**
         * Set the number of passes for creating the blur.
         * @param count the number of blur passes.
         * @return this Builder.
         */
        public Builder setPasses(final int count) {
            passes = count;
            return this;
        }

        /**
         * Create a GlowFilter object using the parameters defined in the
         * Builder.
         * @return a GlowFilter object.
         */
        public GlowFilter build() {
            return new GlowFilter(this);
        }
    }

    /** Bit mask for encoding and decoding the filter mode. */
    private static final int MODE_MASK = 0x00E0;

    /** Format string used in toString() method. */
    private static final String FORMAT = "GlowFilter: {"
            + " color=%s; blurX=%f; blurY=%f;"
            + " strength=%f; mode=%s; passes=%d}";

    /** The glow colour. */
    private final transient Color color;
    /** The horizontal blur amount. */
    private final transient int blurX;
    /** The vertical blur amount. */
    private final transient int blurY;
    /** Strength of the glow. */
    private final transient int strength;
    /** Compositing mode. */
    private final transient int mode;
    /** The number of blur passes. */
    private final transient int passes;

    /**
     * Create a GlowFilter and initialize it wit the values defined in
     * the Builder.
     * @param builder a Builder object.
     */
    public GlowFilter(final Builder builder) {
        color = builder.color;
        blurX = builder.blurX;
        blurY = builder.blurY;
        strength =  builder.strength;
        mode =  builder.mode;
        passes = builder.passes;
    }

    /**
     * Creates and initialises a GlowFilter object using values encoded
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
    public GlowFilter(final SWFDecoder coder, final Context context)
            throws IOException {
        color = new Color(coder, context);
        blurX = coder.readInt();
        blurY = coder.readInt();
        strength = coder.readSignedShort();
        final int value = coder.readByte();
        passes = value & Coder.LOWEST5;
        mode = value & MODE_MASK;
    }

    /**
     * Get the glow colour.
     * @return the color of the glow.
     */
    public Color getShadow() {
        return color;
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
     * Get the strength of the glow.
     * @return the glow strength.
     */
    public float getStrength() {
        return strength / Coder.SCALE_8;
    }

    /**
     * Get the compositing mode.
     * @return the mode used for compositing, either TOP, INNER or KNOCKOUT.
     */
    public FilterMode getMode() {
        FilterMode value;
        switch (mode) {
        case Coder.BIT6:
            value = FilterMode.KNOCKOUT;
            break;
        case Coder.BIT7:
            value = FilterMode.INNER;
            break;
        default:
            throw new IllegalStateException();
        }
        return value;
    }

    /**
     * Get the number of passes for generating the blur.
     * @return the number of blur passes.
     */
    public int getPasses() {
        return passes;
    }

    @Override
    public String toString() {
        return String.format(FORMAT, color.toString(),
                getBlurX(), getBlurY(), getStrength(), mode, passes);
    }

    @Override
    public boolean equals(final Object object) {
        boolean result;
        GlowFilter filter;

        if (object == null) {
            result = false;
        } else if (object == this) {
            result = true;
        } else if (object instanceof GlowFilter) {
            filter = (GlowFilter) object;
            result = color.equals(filter.color) && (blurX == filter.blurX)
                    && (blurY == filter.blurY) && (strength == filter.strength)
                    && (mode == filter.mode) && (passes == filter.passes);
        } else {
            result = false;
        }
        return result;
    }

    @Override
    public int hashCode() {
        return (((((color.hashCode() * Constants.PRIME)
                + blurX) * Constants.PRIME
                + blurY) * Constants.PRIME
                + strength) * Constants.PRIME
                + mode) * Constants.PRIME
                + passes;
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 1 LINES
        return 16;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        coder.writeByte(FilterTypes.GLOW);
        color.encode(coder, context);
        coder.writeInt(blurX);
        coder.writeInt(blurY);
        coder.writeShort(strength);
        coder.writeByte(Coder.BIT5 | mode | passes);
    }
}