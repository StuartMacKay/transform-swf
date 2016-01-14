/*
 * BevelFilter.java
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
 * BevelFilter is used to create a smooth bevel around an object on the display
 * list.
 */
public class BevelFilter implements Filter {
    /** Bit mask for encoding and decoding the filter mode. */
    private static final int MODE_MASK = 0x00D0;

    /**
     * Builder for creating BevelFilter objects.
     */
    public static final class Builder {
        /** The shadow colour. */
        private transient Color shadow;
        /** The highlight colour. */
        private transient Color highlight;
        /** The horizontal blur amount. */
        private transient int blurX;
        /** The vertical blur amount. */
        private transient int blurY;
        /** Angle of shadow in radians. */
        private transient int angle;
        /** The distance of the drop shadow. */
        private transient int distance;
        /** The strength of the drop shadow. */
        private transient int strength;
        /** Compositing mode. */
        private transient int mode;
        /** The number of blur passes. */
        private transient int passes;

        /**
         * Set the colour used for the shadow section of the bevel.
         * @param color the shadow colour.
         * @return this Builder.
         */
        public Builder setShadow(final Color color) {
            shadow = color;
            return this;
        }

        /**
         * Set the colour used for the highlight section of hte bevel.
         * @param color the highlight colour.
         * @return this Builder.
         */
        public Builder setHighlight(final Color color) {
            highlight = color;
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
         * Set the compositing mode for the shadow.
         * @param filterMode the compositing mode, either INNER, KNOCKOUT or
         * TOP.
         * @return this Builder.
         */
        public Builder setMode(final FilterMode filterMode) {
            switch (filterMode) {
            case TOP:
                 mode = Coder.BIT4;
                break;
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
         * Set the shadow angle in radians.
         * @param radians the angle.
         * @return this Builder.
         */
        public Builder setAngle(final float radians) {
            angle = (int) (radians * Coder.SCALE_16);
            return this;
        }

        /**
         * Set the distance from the object that the shadow is displayed.
         * @param width the width of the shadow.
         * @return this Builder.
         */
        public Builder setDistance(final float width) {
            distance = (int) (width * Coder.SCALE_16);
            return this;
        }

        /**
         * Set the shadow strength.
         * @param weight the weight of the shadow.
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
         * Create a BevelFilter object using the parameters defined in the
         * Builder.
         * @return a BevelFilter object.
         */
        public BevelFilter build() {
            return new BevelFilter(this);
        }
    }

    /** Format string used in toString() method. */
    private static final String FORMAT = "BevelFilter: {"
            + " shadow=%s; highlight=%s; blurX=%f; blurY=%f;"
            + " angle=%f; distance=%f; strength=%f; mode=%s; passes=%d}";

    /** The shadow colour. */
    private final transient Color shadow;
    /** The highlight colour. */
    private final transient Color highlight;
    /** The horizontal blur amount. */
    private final transient int blurX;
    /** The vertical blur amount. */
    private final transient int blurY;
    /** Angle of shadow in radians. */
    private final transient int angle;
    /** The distance of the drop shadow. */
    private final transient int distance;
    /** The strength of the drop shadow. */
    private final transient int strength;
    /** Compositing mode. */
    private final transient int mode;
    /** The number of blur passes. */
    private final transient int passes;

    /**
     * Create a BevelFilter and initialize it wit the values defined in
     * the Builder.
     * @param builder a Builder object.
     */
    public BevelFilter(final Builder builder) {
        shadow = builder.shadow;
        highlight = builder.highlight;
        blurX = builder.blurX;
        blurY = builder.blurY;
        angle =  builder.angle;
        distance =  builder.distance;
        strength =  builder.strength;
        mode =  builder.mode;
        passes = builder.passes;
    }

    /**
     * Creates and initialises a BevelFilter object using values encoded
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
    public BevelFilter(final SWFDecoder coder, final Context context)
            throws IOException {
        shadow = new Color(coder, context);
        highlight = new Color(coder, context);
        blurX = coder.readInt();
        blurY = coder.readInt();
        angle = coder.readInt();
        distance = coder.readInt();
        strength = coder.readSignedShort();

        final int value = coder.readByte();

        passes = value & Coder.NIB0;
        mode = value & MODE_MASK;
    }

    /**
     * Get the shadow colour.
     * @return the color of the shadow section of the bevel.
     */
    public Color getShadow() {
        return shadow;
    }

    /**
     * Get the highlight colour.
     * @return the color of the highlight section of the bevel.
     */
    public Color getHightlight() {
        return highlight;
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
     * Get the angle of the shadow.
     * @return the angle of the shadow in radians.
     */
    public float getAngle() {
        return angle / Coder.SCALE_16;
    }

    /**
     * Get the distance of the shadow from the object.
     * @return the width of the shadow.
     */
    public float getDistance() {
        return distance / Coder.SCALE_16;
    }

    /**
     * Get the strength of the shadow.
     * @return the shadow strength.
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
        case Coder.BIT4:
            value = FilterMode.TOP;
            break;
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

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, shadow.toString(), highlight.toString(),
                getAngle(), getDistance(), getStrength(),
                getBlurX(), getBlurY(), mode, passes);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object object) {
        boolean result;
        BevelFilter filter;

        if (object == null) {
            result = false;
        } else if (object == this) {
            result = true;
        } else if (object instanceof BevelFilter) {
            filter = (BevelFilter) object;
            result = shadow.equals(filter.shadow)
                    && highlight.equals(filter.highlight)
                    && (blurX == filter.blurX) && (blurY == filter.blurY)
                    && (angle == filter.angle) && (distance == filter.distance)
                    && (strength == filter.strength) && (mode == filter.mode)
                    && (passes == filter.passes);
        } else {
            result = false;
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return ((((((((shadow.hashCode() * Constants.PRIME)
                + highlight.hashCode()) * Constants.PRIME
                + blurX) * Constants.PRIME
                + blurY) * Constants.PRIME
                + angle) * Constants.PRIME
                + distance) * Constants.PRIME
                + strength) * Constants.PRIME
                + mode) * Constants.PRIME
                + passes;
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 1 LINES
        return 28;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        coder.writeByte(FilterTypes.BEVEL);
        shadow.encode(coder, context);
        highlight.encode(coder, context);
        coder.writeInt(blurX);
        coder.writeInt(blurY);
        coder.writeInt(angle);
        coder.writeInt(distance);
        coder.writeShort(strength);
        coder.writeByte(Coder.BIT5 | mode | passes);
    }
}
