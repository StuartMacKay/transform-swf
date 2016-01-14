/*
 * GradientGlowFilter.java
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
import java.util.ArrayList;
import java.util.List;

import com.flagstone.transform.Constants;
import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.datatype.Color;
import com.flagstone.transform.fillstyle.Gradient;

/**
 * GlowFilter is used to create a glow effect using a gradient colour around an
 * object on the display list.
 */
public class GradientGlowFilter implements Filter {

    /**
     * Builder for creating GradientGlowFilter objects.
     */
    public static final class Builder {
        /** The list of gradients for the colour. */
        private final transient List<Gradient>gradients;
        /** The horizontal blur amount. */
        private transient int blurX;
        /** The vertical blur amount. */
        private transient int blurY;
        /** Angle of gradient glow in radians. */
        private transient int angle;
        /** The distance of the gradient glow. */
        private transient int distance;
        /** The strength of the gradient glow. */
        private transient int strength;
        /** Compositing mode. */
        private transient int mode;
        /** The number of blur passes. */
        private transient int passes;

        /**
         * Creates a new Builder.
         */
        public Builder() {
            gradients = new ArrayList<Gradient>();
        }

        /**
         * Add a Gradient to the list.
         * @param gradient a Gradient object.
         * @return this Builder.
         */
        public Builder addGradient(final Gradient gradient) {
            gradients.add(gradient);
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
         * Set the glow angle in radians.
         * @param radians the angle.
         * @return this Builder.
         */
        public Builder setAngle(final float radians) {
            angle = (int) (radians * Coder.SCALE_16);
            return this;
        }

        /**
         * Set the distance of the glow from the object.
         * @param width the width of the glow.
         * @return this Builder.
         */
        public Builder setDistance(final float width) {
            distance = (int) (width * Coder.SCALE_16);
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
         * Create a GradientGlowFilter object using the parameters defined in
         * the Builder.
         * @return a GradientGlowFilter object.
         */
        public GradientGlowFilter build() {
            return new GradientGlowFilter(this);
        }
    }

    /** Bit mask for encoding and decoding the filter mode. */
    private static final int MODE_MASK = 0x00D0;

    /** Format string used in toString() method. */
    private static final String FORMAT = "GradientGlowFilter: {"
            + " gradients=%s; blurX=%f; blurY=%f;"
            + " angle=%f; distance=%f; strength=%f; mode=%s; passes=%d}";

    /** The list of gradients. */
    private final transient List<Gradient> gradients;
    /** The horizontal blur amount. */
    private final transient int blurX;
    /** The vertical blur amount. */
    private final transient int blurY;
    /** Angle of gradient glow in radians. */
    private final transient int angle;
    /** Distance of the gradient glow. */
    private final transient int distance;
    /** Strength of the gradient glow. */
    private final transient int strength;
    /** Compositing mode. */
    private final transient int mode;
    /** The number of blur passes. */
    private final transient int passes;

    /**
     * Create a GradientGlowFilter and initialize it wit the values defined in
     * the Builder.
     * @param builder a Builder object.
     */
    public GradientGlowFilter(final Builder builder) {
        gradients = builder.gradients;
        blurX = builder.blurX;
        blurY = builder.blurY;
        angle = builder.angle;
        distance = builder.distance;
        strength =  builder.strength;
        mode =  builder.mode;
        passes = builder.passes;
    }

    /**
     * Creates and initialises a GradientGlowFilter object using values encoded
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
    public GradientGlowFilter(final SWFDecoder coder, final Context context)
            throws IOException {
        final int count = coder.readByte();
        final Color[] colors = new Color[count];
        final int[] ratioes = new int[count];

        for (int i = 0; i < count; i++) {
            colors[i] = new Color(coder, context);
        }
        for (int i = 0; i < count; i++) {
            ratioes[i] = coder.readByte();
        }

        gradients = new ArrayList<Gradient>(count);
        for (int i = 0; i < count; i++) {
            gradients.add(new Gradient(ratioes[i], colors[i]));
        }

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
     * Get the list of gradients used to create the glow colour.
     * @return the list of Gradient objects.
     */
    public List<Gradient> getGradients() {
        return gradients;
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
     * Get the angle of the glow.
     * @return the angle of the glow in radians.
     */
    public float getAngle() {
        return angle / Coder.SCALE_16;
    }

    /**
     * Get the distance of the glow from the object.
     * @return the width of the glow.
     */
    public float getDistance() {
        return distance / Coder.SCALE_16;
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

    @Override
    public String toString() {
        return String.format(FORMAT, gradients.toString(),
                getBlurX(), getBlurY(),
                getAngle(), getDistance(), getStrength(), mode, passes);
    }

    @Override
    public boolean equals(final Object object) {
        boolean result;
        GradientGlowFilter filter;

        if (object == null) {
            result = false;
        } else if (object == this) {
            result = true;
        } else if (object instanceof GradientGlowFilter) {
            filter = (GradientGlowFilter) object;
            result = gradients.equals(filter.gradients)
                    && (blurX == filter.blurX) && (blurY == filter.blurY)
                    && (angle == filter.angle) && (distance == filter.distance)
                    && (strength == filter.strength) && (mode == filter.mode)
                    && (passes == filter.passes);
        } else {
            result = false;
        }
        return result;
    }

    @Override
    public int hashCode() {
        return (((((((gradients.hashCode() * Constants.PRIME)
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
        return 21 + 5 * gradients.size();
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        coder.writeByte(FilterTypes.GRADIENT_GLOW);
        coder.writeByte(gradients.size());

        for (final Gradient gradient : gradients) {
            gradient.getColor().encode(coder, context);
        }

        for (final Gradient gradient : gradients) {
            coder.writeByte(gradient.getRatio());
        }

        coder.writeInt(blurX);
        coder.writeInt(blurY);
        coder.writeInt(angle);
        coder.writeInt(distance);
        coder.writeShort(strength);
        coder.writeByte(Coder.BIT5 | mode | passes);
    }
}
