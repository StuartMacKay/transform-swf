/*
 * FontAlignment.java
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
import java.util.ArrayList;
import java.util.List;

import com.flagstone.transform.Constants;
import com.flagstone.transform.MovieTag;
import com.flagstone.transform.MovieTypes;
import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.exception.IllegalArgumentRangeException;

/**
 * FontAlignment provides the alignment information for the glyphs in a font.
 */
public class FontAlignment implements MovieTag {

    /**
     * StrokeWidth is used to provide hints about the thickness of the line
     * used for rendering glyphs in a font.
     */
    public enum StrokeWidth {
        /** Thin strokes. */
        THIN,
        /** Medium thick strokes. */
        MEDIUM,
        /** Thick strokes. */
        THICK
    };

    /** Format string used in toString() method. */
    private static final String FORMAT = "FontAlignment: { identifier=%d;"
    		+ " strokeWidth=%s; zones=%s}";

    /** The unique identifier of the font that the alignment applies to. */
    private int identifier;
    /** Compound code used for drawing hints. */
    private transient int hints;
    /** Alignment zones for snapping areas of glyphs to pixel boundaries. */
    private List<GlyphAlignment> zones;

    /** The length of the object, minus the header, when it is encoded. */
    private transient int length;

    /**
     * Creates and initialises a FontAlignment object using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public FontAlignment(final SWFDecoder coder) throws IOException {
        length = coder.readUnsignedShort() & Coder.LENGTH_FIELD;
        if (length == Coder.IS_EXTENDED) {
            length = coder.readInt();
        }
        coder.mark();
        identifier = coder.readUnsignedShort();
        hints = coder.readByte();

        zones = new ArrayList<GlyphAlignment>();

        while (coder.bytesRead() < length) {
            zones.add(new GlyphAlignment(coder));
        }
        coder.check(length);
        coder.unmark();
    }

    /**
     * Creates a new FontAlignment object for the referenced font along with
     * information on the stroke width used to draw the glyphs and a list of
     * alignment zones for each glyph.
     * @param uid the unique identifier of the font.
     * @param stroke the typical width used when drawing the glyphs.
     * @param list a list of alignment boxes used for rendering the glyphs.
     */
    public FontAlignment(final int uid, final StrokeWidth stroke,
            final List<GlyphAlignment> list) {
        setIdentifier(uid);
        setStrokeWidth(stroke);
        setZones(list);
    }

    /**
     * Creates and initialises a FontAlignment object using the values copied
     * from another FontAlignment object.
     *
     * @param object
     *            a FontAlignment object from which the values will be
     *            copied.
     */
    public FontAlignment(final FontAlignment object) {
        identifier = object.identifier;
        hints = object.hints;
        zones = new ArrayList<GlyphAlignment>(object.zones);
    }

    /**
     * Get the unique identifier of the font definition that the alignment
     * information is for.
     *
     * @return the unique identifier of the font.
     */
    public int getIdentifier() {
        return identifier;
    }

    /**
     * Sets the identifier of the font that this alignment information is for.
     *
     * @param uid
     *            the unique identifier of the DefineFont that contains the
     *            glyphs for the font. Must be in the range 1..65535.
     */
    public void setIdentifier(final int uid) {
        if ((uid < 1) || (uid > Coder.USHORT_MAX)) {
            throw new IllegalArgumentRangeException(
                    1, Coder.USHORT_MAX, uid);
        }
        identifier = uid;
    }

    /**
     * Get the StrokeWidth that describes how the glyphs in the font are
     * typically drawn.
     * @return a StrokeWidth defining how the font is drawn.
     */
    public StrokeWidth getStrokeWidth() {
        StrokeWidth stroke;
        switch (hints) {
        case Coder.BIT6:
            stroke = StrokeWidth.MEDIUM;
            break;
        case Coder.BIT7:
            stroke = StrokeWidth.THICK;
            break;
        default:
            stroke = StrokeWidth.THIN;
            break;
        }
        return stroke;
    }

    /**
     * Set the StrokeWidth that describes how the glyphs in the font are
     * typically drawn.
     * @param stroke a StrokeWidth defining how the font is drawn.
     */
    public void setStrokeWidth(final StrokeWidth stroke) {
        switch (stroke) {
        case MEDIUM:
            hints = Coder.BIT6;
            break;
        case THICK:
            hints = Coder.BIT7;
            break;
        default:
            hints = 0x00;
            break;
        }
    }

    /**
     * Get the alignment information for each glyph in the font.
     * @return a list of GlyphAliment objects that describe the areas in each
     * glyph which can be snapped to the nearest pixel to improve display
     * quality.
     */
    public List<GlyphAlignment> getZones() {
        return zones;
    }

    /**
     * Set the alignment information for each glyph in the font.
     * @param list a list of GlyphAliment objects that describe the areas in
     * each glyph which can be snapped to the nearest pixel to improve display
     * quality.
     */
    public void setZones(final List<GlyphAlignment> list) {
        if (list == null) {
            throw new IllegalArgumentException();
        }
        zones = list;
    }

    /**
     * Add the alignment information for a glyph to the list.
     * @param zone the alignment information for a glyph.
     * @return this object.
     */
    public FontAlignment addZone(final GlyphAlignment zone) {
        if (zone == null) {
            throw new IllegalArgumentException();
        }
        zones.add(zone);
        return this;
    }

    /** {@inheritDoc} */
    public FontAlignment copy() {
        return new FontAlignment(this);
    }

    @Override
    public String toString() {
        return String.format(FORMAT, identifier, getStrokeWidth(), zones);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 1 LINES
        length = 3;

        for (final GlyphAlignment zone : zones) {
            length += zone.prepareToEncode(context);
        }

        return (length > Coder.HEADER_LIMIT ? Coder.LONG_HEADER
                : Coder.SHORT_HEADER) + length;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {

        if (length > Coder.HEADER_LIMIT) {
            coder.writeShort((MovieTypes.FONT_ALIGNMENT
                    << Coder.LENGTH_FIELD_SIZE) | Coder.IS_EXTENDED);
            coder.writeInt(length);
        } else {
            coder.writeShort((MovieTypes.FONT_ALIGNMENT
                    << Coder.LENGTH_FIELD_SIZE) | length);
        }
        if (Constants.DEBUG) {
            coder.mark();
        }
        coder.writeShort(identifier);
        coder.writeByte(hints);

        for (final GlyphAlignment zone : zones) {
            zone.encode(coder, context);
        }
        if (Constants.DEBUG) {
            coder.check(length);
            coder.unmark();
        }
    }
}
