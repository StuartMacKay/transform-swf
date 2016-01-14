/*
 * DefineFont4.java
 * Transform
 *
 * Copyright (c) 2010 Flagstone Software Ltd. All rights reserved.
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
import java.util.Arrays;

import com.flagstone.transform.Constants;
import com.flagstone.transform.DefineTag;
import com.flagstone.transform.MovieTypes;
import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.exception.IllegalArgumentRangeException;

/**
 * DefineFont4 is used to pass OpenType font data directly to the text rendering
 * engine added in Flash Player 10.
 */
public class DefineFont4 implements DefineTag {

    /** Format string used in toString() method. */
    private static final String FORMAT = "DefineFont4: { identifier=%d;"
    		+ " italic=%b; bold=%b; name=%s; data=byte<%d> ...}";

    /** The unique identifier for this object. */
    private int identifier;
    /** Is the font italicized. */
    private boolean italic;
    /** Is the font bold. */
    private boolean bold;
    /** The font name. */
    private String name;
    /** The OpenType font data. */
    private byte[] data;

    /** The length of the object, minus the header, when it is encoded. */
    private transient int length;

    /**
     * Creates and initialises a DefineFont4 object using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public DefineFont4(final SWFDecoder coder)
            throws IOException {
        length = coder.readUnsignedShort() & Coder.LENGTH_FIELD;
        if (length == Coder.IS_EXTENDED) {
            length = coder.readInt();
        }
        coder.mark();
        identifier = coder.readUnsignedShort();

        final int bits = coder.readByte();

        bold = (bits & 0x00000001) == 1;
        italic = (bits & 0x00000002) == 2;
        name = coder.readString();
        data = coder.readBytes(new byte[length - coder.bytesRead()]);
        coder.check(length);
        coder.unmark();
    }

    /**
     * Creates a new DefineFont4 with the specified identifier, font name and
     * style and OpenType font data.
     * @param uid the unique identifier for the font.
     * @param fontName the name of the font.
     * @param isItalic does the font use italics
     * @param isBold is the font bold.
     * @param font the OpenType font data.
     */
    public DefineFont4(final int uid, final String fontName,
            final boolean isItalic, final boolean isBold, final byte[] font) {
        setIdentifier(uid);
        setItalic(isItalic);
        setBold(isBold);
        setName(fontName);
        setData(font);
    }

    /**
     * Creates and initialises a DefineFont4 object using the values copied
     * from another DefineFont3 object.
     *
     * @param object
     *            a DefineFont4 object from which the values will be
     *            copied.
     */
    public DefineFont4(final DefineFont4 object) {
        identifier = object.identifier;
        italic = object.italic;
        bold = object.bold;
        name = object.name;
        data = object.data;
    }

    /** {@inheritDoc} */
    @Override
	public int getIdentifier() {
        return identifier;
    }

    /** {@inheritDoc} */
    @Override
	public void setIdentifier(final int uid) {
        if ((uid < 1) || (uid > Coder.USHORT_MAX)) {
            throw new IllegalArgumentRangeException(
                    1, Coder.USHORT_MAX, uid);
        }
        identifier = uid;
    }

    /**
     * Is the font italicised.
     *
     * @return a boolean indicating whether the font is rendered in italics.
     */
    public boolean isItalic() {
        return italic;
    }

    /**
     * Is the font bold.
     *
     * @return a boolean indicating whether the font is rendered in a bold face.
     */
    public boolean isBold() {
        return bold;
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
     * Set the font is italicised.
     *
     * @param aBool
     *            a boolean flag indicating whether the font will be rendered in
     *            italics
     */
    public void setItalic(final boolean aBool) {
        italic = aBool;
    }

    /**
     * Set the font is bold.
     *
     * @param aBool
     *            a boolean flag indicating whether the font will be rendered in
     *            bold face.
     */
    public void setBold(final boolean aBool) {
        bold = aBool;
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
     * Get the OpenType font definition data.
     * @return a copy of the binary data containing the definition of the font.
     */
    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }

    /**
     * Set the OpenType font definition data.
     * @param bytes the binary data containing the definition of the font.
     */
    public void setData(final byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException();
        }
        data = Arrays.copyOf(bytes, bytes.length);
    }

    /** {@inheritDoc} */
    @Override
	public DefineFont4 copy() {
        return new DefineFont4(this);
    }

    @Override
    public String toString() {
        return String.format(FORMAT, identifier, italic, bold,
                name, data.length);
    }

    /** {@inheritDoc} */
    @Override
	public int prepareToEncode(final Context context) {
        // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 1 LINES
        length = 3 + context.strlen(name) + data.length;
        return (length > Coder.HEADER_LIMIT ? Coder.LONG_HEADER
                : Coder.SHORT_HEADER) + length;
    }

    /** {@inheritDoc} */
    @Override
	@SuppressWarnings("PMD.NPathComplexity")
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {

        if (length > Coder.HEADER_LIMIT) {
            coder.writeShort((MovieTypes.DEFINE_FONT_4
                    << Coder.LENGTH_FIELD_SIZE) | Coder.IS_EXTENDED);
            coder.writeInt(length);
        } else {
            coder.writeShort((MovieTypes.DEFINE_FONT_4
                    << Coder.LENGTH_FIELD_SIZE) | length);
        }
        if (Constants.DEBUG) {
            coder.mark();
        }
        coder.writeShort(identifier);
        int bits = 0;
        bits |= data.length > 0 ? Coder.BIT2 : 0;
        bits |= italic ? Coder.BIT1 : 0;
        bits |= bold ? Coder.BIT0 : 0;
        coder.writeByte(bits);
        coder.writeString(name);
        coder.writeBytes(data);
        if (Constants.DEBUG) {
            coder.check(length);
            coder.unmark();
        }
    }
}
