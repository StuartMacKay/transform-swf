/*
 * AlignmentZone.java
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

package com.flagstone.transform.font;

import java.io.IOException;

import com.flagstone.transform.Constants;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncodeable;
import com.flagstone.transform.coder.SWFEncoder;

/**
 * AlignmentZone defines a bounding box that is used by the advanced text
 * rendering engine in the Flash Player to snap glyphs to the nearest pixel.
 */
public class AlignmentZone implements SWFEncodeable {

    /** Format string used in toString() method. */
    private static final String FORMAT = "AlignmentZone: {"
                + " coordinate=%f; range=%f}";

    /** The position of the edge of the zone. */
    private final transient float coordinate;
    /** The width or height of the zone. */
    private final transient float range;

    /**
     * Creates and initialises an AlignmentZone object using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public AlignmentZone(final SWFDecoder coder) throws IOException {
        coordinate = coder.readHalf();
        range = coder.readHalf();
    }

    /**
     * Creates a new AlignmentZone with the specified coordinate and size.
     *
     * @param coord the x or y coordinate of the left edge or bottom of the box.
     * @param size the width or height of the box.
     */
    public AlignmentZone(final float coord, final float size) {
        coordinate = coord;
        range = size;
    }

    /**
     * Get the coordinate of the left or bottom edge of the alignment box.
     * @return the x or y coordinate of the box.
     */
    public float getCoordinate() {
        return coordinate;
    }

    /**
     * Get the width or height of the alignment box.
     * @return the size of the box.
     */
    public float getRange() {
        return range;
    }

    @Override
    public String toString() {
        return String.format(FORMAT, coordinate, range);
    }

    @Override
    public boolean equals(final Object object) {
        boolean result;

        if (object == null) {
            result = false;
        } else if (object == this) {
            result = true;
        } else if (object instanceof AlignmentZone) {
            final AlignmentZone zone = (AlignmentZone) object;
            result = (coordinate == zone.coordinate) && (range == zone.range);
        } else {
            result = false;
        }

        return result;
    }

    @Override
    public int hashCode() {
        return (Float.floatToIntBits(coordinate) * Constants.PRIME)
                + Float.floatToIntBits(range);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 1 LINES
        return 4;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        coder.writeHalf(coordinate);
        coder.writeHalf(range);
    }
}
