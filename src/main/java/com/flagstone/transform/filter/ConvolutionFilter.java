/*
 * ConvolutionFilter.java
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
import java.util.Arrays;

import com.flagstone.transform.Constants;
import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.datatype.Color;

/**
 * ConvolutionFilter is used to apply a two-dimensional discrete convolution on
 * the pixels of the object on the display list.
 */
public class ConvolutionFilter implements Filter {

    /**
     * Builder for creating ConvolutionFilter objects.
     */
    public static final class Builder {
        /** The convolution matrix. */
        private transient float[][] matrix;
        /** The divisor for the convolution equation. */
        private transient float divisor;
        /** The bias for the convolution equation. */
        private transient float bias;
        /** The default colour used for pixels outside the display object. */
        private transient Color color;
        /** Whether outside pixels are clamped to the nearest inside one. */
        private transient boolean clamp;
        /** Whether transparency is preserved. */
        private transient boolean alpha;

        /**
         * Set the matrix used for the convolution.
         *
         * @param aMatrix a 2D matrix.
         * @return this Builder.
         */
        public Builder setMatrix(final float[][] aMatrix) {
            final int rows = aMatrix.length;
            final int cols = aMatrix[0].length;
            matrix = new float[rows][cols];
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    matrix[i][j] = aMatrix[i][j];
                }
            }
            return this;
        }

        /**
         * Set the divisor for the convolution.
         * @param value the divisor
         * @return this Builder.
         */
        public Builder setDivisor(final float value) {
            divisor = value;
            return this;
        }

        /**
         * Set the bias for the convolution.
         * @param value the bias
         * @return this Builder.
         */
        public Builder setBias(final float value) {
            bias = value;
            return this;
        }

        /**
         * Set the default colour applied to the pixels outside of the image.
         * @param aColor the default colour.
         * @return this Builder.
         */
        public Builder setColor(final Color aColor) {
            color = aColor;
            return this;
        }

        /**
         * Indicate whether the pixels outside the image will be clamped to the
         * nearest pixel value (true) or to the default colour (false).
         * @param nearest if true clamp to the nearest pixel, false use the
         * default colour.
         * @return this Builder.
         */
        public Builder setClamp(final boolean nearest) {
            clamp = nearest;
            return this;
        }

        /**
         * Indicate whether the alpha value of the pixels should be preserved.
         * @param preserve if true preserve the alpha values.
         * @return this Builder.
         */
        public Builder setAlpha(final boolean preserve) {
            alpha = preserve;
            return this;
        }

        /**
         * Generate an instance of ConvolutionFilter using the parameters set
         * in the Builder.
         * @return a ConvolutionFilter object.
         */
        public ConvolutionFilter build() {
            return new ConvolutionFilter(this);
        }
    }

    /** Format string used in toString() method. */
    private static final String FORMAT = "ConvolutionFilter: { matrix=%s;"
            + " divisor=%f; bias=%f; color=%s; clamp=%b; alpha=%b}";

    /** The convolution matrix. */
    private final transient float[][] matrix;
    /** The divisor for the convolution equation. */
    private final transient float divisor;
    /** The bias for the convolution equation. */
    private final transient float bias;
    /** The default colour used for pixels outside the display object. */
    private final transient Color color;
    /** Whether outside pixels are clamped to the nearest inside one. */
    private final transient boolean clamp;
    /** Whether transparency is preserved. */
    private final transient boolean alpha;

    /** The number of rows in the matrix. */
    private transient int rows;
    /** The number of columns in the matrix. */
    private transient int cols;

    /**
     * Create a new ConvolutionFilter object using the parameters defined in
     * the Builder.
     * @param builder a Builder containing the parameters for the instance.
     */
    public ConvolutionFilter(final Builder builder) {
        matrix = builder.matrix;
        divisor = builder.divisor;
        bias = builder.bias;
        color = builder.color;
        clamp = builder.clamp;
        alpha = builder.alpha;
    }

    /**
     * Creates and initialises a ConvolutionFilter object using values encoded
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
    public ConvolutionFilter(final SWFDecoder coder, final Context context)
            throws IOException {
        cols = coder.readByte();
        rows = coder.readByte();
        divisor = Float.intBitsToFloat(coder.readInt());
        bias = Float.intBitsToFloat(coder.readInt());
        matrix = new float[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = Float.intBitsToFloat(coder.readInt());
            }
        }
        color = new Color(coder, context);
        final int bits = coder.readByte();
        clamp = (bits & Coder.BIT1) != 0;
        alpha = (bits & Coder.BIT0) != 0;
    }

    /**
     * Get the divisor for the convolution equation.
     * @return the divisor value.
     */
    public float getDivisor() {
        return divisor;
    }

    /**
     * Get the bias for the convolution equation.
     * @return the bias value.
     */
    public float getBias() {
        return divisor;
    }

    /**
     * Get a copy of the convolution matrix.
     * @return a copy of the matrix.
     */
    public float[][] getMatrix() {
        return matrix.clone();
    }

    /**
     * Get the default colour used for pixel outside the display object.
     * @return the colour used for outside pixels.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Are outside pixels clamped to the nearest inside one (true) or to the
     * default colour (false).
     * @return true if the pixels are clamped, false if the default colour is
     * used.
     */
    public boolean isClamp() {
        return clamp;
    }

    /**
     * Is the alpha value of the pixels in the display list object preserved
     * in the output of the convolution.
     * @return true if alpha is preserved, false if not.
     */
    public boolean isAlpha() {
        return alpha;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < matrix.length; i++) {
        	builder.append(Arrays.toString(matrix[i]));
        }
        builder.append("]");

        return String.format(FORMAT, builder.toString(),
        		divisor, bias, color, clamp, alpha);
    }

    @Override
    public boolean equals(final Object object) {
        boolean result;
        ConvolutionFilter filter;

        if (object == null) {
            result = false;
        } else if (object == this) {
            result = true;
        } else if (object instanceof ConvolutionFilter) {
            filter = (ConvolutionFilter) object;
            result = Arrays.deepEquals(matrix, filter.matrix)
                    && (divisor == filter.divisor) && (bias == filter.bias)
                    && color.equals(filter.color) && (clamp == filter.clamp)
                    && (alpha == filter.alpha);
        } else {
            result = false;
        }
        return result;
    }

    @Override
    public int hashCode() {
        return ((((Arrays.deepHashCode(matrix) * Constants.PRIME
                + Float.floatToIntBits(divisor)) * Constants.PRIME
                + Float.floatToIntBits(bias)) * Constants.PRIME
                + color.hashCode()) * Constants.PRIME
                + Boolean.valueOf(clamp).hashCode()) * Constants.PRIME
                + Boolean.valueOf(alpha).hashCode();
    }

    /** {@inheritDoc} */
    @Override
	public int prepareToEncode(final Context context) {
        rows = matrix.length;
        cols = matrix[0].length;
        // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 1 LINES
        return 16 + rows * cols * 4;
    }

    /** {@inheritDoc} */
    @Override
	public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        coder.writeByte(FilterTypes.CONVOLUTION);
        coder.writeByte(cols);
        coder.writeByte(rows);
        coder.writeInt(Float.floatToIntBits(divisor));
        coder.writeInt(Float.floatToIntBits(bias));
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                coder.writeInt(Float.floatToIntBits(matrix[i][j]));
            }
        }
        color.encode(coder, context);
        int bits = 0;
        bits |= clamp ? Coder.BIT1 : 0;
        bits |= alpha ? Coder.BIT0 : 0;
        coder.writeByte(bits);
    }
}
