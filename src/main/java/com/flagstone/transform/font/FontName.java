/*
 * DefineFont2.java
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

package com.flagstone.transform.font;


import java.io.IOException;

import com.flagstone.transform.Constants;
import com.flagstone.transform.MovieTag;
import com.flagstone.transform.MovieTypes;
import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.exception.IllegalArgumentRangeException;

/**
 * FontName is used to hold the name and copyright information for a font.
 */
public class FontName implements MovieTag {

    /** Format string used in toString() method. */
    private static final String FORMAT = "DefineFontName: { identifier=%d;"
            + " name=%s; copyright=%s}";

    /** The unique identifier for this object. */
    private int identifier;
    /** The name of the font. */
    private String name;
    /** The copyright notice. */
    private String copyright;

    /** The length of the object, minus the header, when it is encoded. */
    private transient int length;

    /**
     * Creates and initialises a DefineFontName object using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public FontName(final SWFDecoder coder) throws IOException {
        length = coder.readUnsignedShort() & Coder.LENGTH_FIELD;
        if (length == Coder.IS_EXTENDED) {
            length = coder.readInt();
        }
        coder.mark();
        identifier = coder.readUnsignedShort();
        name = coder.readString();
        copyright = coder.readString();
        coder.check(length);
        coder.unmark();
    }

    /**
     * Create a new FontName object with the name and copyright information for
     * an existing font definition.
     *
     * @param uid the unique identifier of the font definition.
     * @param fontName the name of the font.
     * @param copyrightNotice the copyright notice for the font.
     */
    public FontName(final int uid, final String fontName,
            final String copyrightNotice) {
        setIdentifier(uid);
        setName(fontName);
        setCopyright(copyrightNotice);
    }

    /**
     * Creates and initialises a DefineFontName object using the values copied
     * from another DefineFontName object.
     *
     * @param object
     *            a DefineFontName object from which the values will be
     *            copied.
     */
    public FontName(final FontName object) {
        identifier = object.identifier;
        name = object.name;
        copyright = object.copyright;
    }

    /**
     * Get the unique identifier of the font this object is for.
     * @return the unique identifier of the font definition.
     */
    public int getIdentifier() {
        return identifier;
    }

    /**
     * Set the unique identifier of the font this object is for.
     * @param uid the unique identifier of the font definition.
     */
    public void setIdentifier(final int uid) {
        if ((uid < 1) || (uid > Coder.USHORT_MAX)) {
            throw new IllegalArgumentRangeException(
                    1, Coder.USHORT_MAX, uid);
        }
        identifier = uid;
    }

    /**
     * Returns the name of the font family.
     *
     * @return the name of the font.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the font.
     *
     * @param aString
     *            the name assigned to the font, identifying the font family.
     *            Must not be null.
     */
    public void setName(final String aString) {
        if (aString == null) {
            throw new IllegalArgumentException();
        }
        name = aString;
    }

    /**
     * Get the copyright notice.
     * @return a string describing the copyright information.
     */
    public String getCopyright() {
        return copyright;
    }

    /**
     * Set the copyright notice.
     * @param notice a string describing the copyright information.
     */
    public void setCopyright(final String notice) {
        if (notice == null) {
            throw new IllegalArgumentException();
        }
        copyright = notice;
    }

    /** {@inheritDoc} */
    public FontName copy() {
        return new FontName(this);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, identifier, name, copyright);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        length = 2 + context.strlen(name) + context.strlen(copyright);
        return (length > Coder.HEADER_LIMIT ? Coder.LONG_HEADER
                : Coder.SHORT_HEADER) + length;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {

        if (length > Coder.HEADER_LIMIT) {
            coder.writeShort((MovieTypes.FONT_NAME
                    << Coder.LENGTH_FIELD_SIZE) | Coder.IS_EXTENDED);
            coder.writeInt(length);
        } else {
            coder.writeShort((MovieTypes.FONT_NAME
                    << Coder.LENGTH_FIELD_SIZE) | length);
        }
        if (Constants.DEBUG) {
            coder.mark();
        }
        coder.writeShort(identifier);
        coder.writeString(name);
        coder.writeString(copyright);
        if (Constants.DEBUG) {
            coder.check(length);
            coder.unmark();
        }
    }
}
