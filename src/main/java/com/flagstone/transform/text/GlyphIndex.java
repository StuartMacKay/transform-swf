/*
 * GlyphIndex.java
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

package com.flagstone.transform.text;

import java.io.IOException;

import com.flagstone.transform.Constants;
import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncodeable;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.exception.IllegalArgumentRangeException;

/**
 * <p>
 * GlyphIndex is used to display a text character in a span of text. Each
 * GlyphIndex specifies the glyph to be displayed (rather than the character
 * code) along with the distance to the next Character to be displayed, if any.
 * </p>
 *
 * <p>
 * A single lines of text is displayed using an {@link TextSpan} object which
 * contains a list of Character objects. Blocks of text can be created by
 * combining one or more TextSpan objects which specify the size, colour and
 * relative position of each line.
 * </p>
 *
 * @see TextSpan
 */
public final class GlyphIndex implements SWFEncodeable {

    /** Format string used in toString() method. */
    private static final String FORMAT = "GlyphIndex: { glyphIndex=%d;"
    		+ " advance=%d}";

    /** The index of the glyph to display. */
    private final transient int index;
    /** The advance to the next glyph. */
    private final transient int advance;

    /**
     * Creates and initialises a GlyphIndex object using values encoded
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
    public GlyphIndex(final SWFDecoder coder, final Context context)
            throws IOException {
        index = coder.readBits(context.get(Context.GLYPH_SIZE), false);
        advance = coder.readBits(context.get(Context.ADVANCE_SIZE), false);
    }

    /**
     * Creates a Character specifying the index of the glyph to be displayed and
     * the spacing to the next glyph.
     *
     * @param anIndex
     *            the index into the list of Shapes in a font definition object
     *            that defines the glyph that represents the character to be
     *            displayed.
     *
     * @param anAdvance
     *            the relative position in twips, from the origin of the glyph
     *            representing this character to the next glyph to be displayed.
     */
    public GlyphIndex(final int anIndex, final int anAdvance) {
        if (anIndex < 0 || anIndex > Coder.USHORT_MAX) {
            throw new IllegalArgumentRangeException(
                    0, Coder.USHORT_MAX, anIndex);
        }
        if (anAdvance <  Coder.SHORT_MIN
                || anAdvance > Coder.SHORT_MAX) {
            throw new IllegalArgumentRangeException(
                    Coder.SHORT_MIN, Coder.SHORT_MAX, anAdvance);
        }
        index = anIndex;
        advance = anAdvance;
    }

    /**
     * Get the index of the glyph, in a font definition object, that will
     * displayed to represent this character.
     *
     * @return the glyph index.
     */
    public int getGlyphIndex() {
        return index;
    }

    /**
     * Get the spacing in twips between the glyph representing this
     * character and the next.
     *
     * @return the advance to the next character.
     */
    public int getAdvance() {
        return advance;
    }

    @Override
    public String toString() {
        return String.format(FORMAT, index, advance);
    }

    @Override
    public boolean equals(final Object other) {
        boolean result;
        GlyphIndex object;

        if (other == null) {
            result = false;
        } else if (other == this) {
            result = true;
        } else if (other instanceof GlyphIndex) {
            object = (GlyphIndex) other;
            result = (index == object.index) && (advance == object.advance);
        } else {
            result = false;
        }
        return result;
    }

    @Override
    public int hashCode() {
        return (index * Constants.PRIME) + advance;
    }

    /** {@inheritDoc} */
    @Override
	public int prepareToEncode(final Context context) {
        return context.get(Context.GLYPH_SIZE)
                + context.get(Context.ADVANCE_SIZE);
    }

    /** {@inheritDoc} */
    @Override
	public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        coder.writeBits(index, context.get(Context.GLYPH_SIZE));
        coder.writeBits(advance, context.get(Context.ADVANCE_SIZE));
    }
}
