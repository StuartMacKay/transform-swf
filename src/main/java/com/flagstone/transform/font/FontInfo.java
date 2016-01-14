/*
 * FontInfo.java
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
 * FontInfo defines the name and face of a font and maps the codes for a given
 * character set to the glyphs that are drawn to represent each character.
 *
 * <p>
 * Three different encoding schemes are supported for the character codes. The
 * ANSI character set is used for Latin languages, SJIS is used for Japanese
 * language characters and Unicode is used for any character set. Since Flash 5
 * Unicode is the preferred encoding scheme.
 * </p>
 *
 * <p>
 * The index of each entry in the codes list matches the index in the
 * corresponding glyph in the shapes list of an DefineFont object, allowing a
 * given character code to be mapped to a given glyph.
 * </p>
 *
 * <p>
 * FontInfo also allows the font associated with a Flash file to be mapped to a
 * font installed on the device where the Flash Player displaying the file is
 * hosted. The use of a font from a device is not automatic but is determined by
 * the HTML tag option <i>deviceFont</i> which is passed to the Flash Player
 * when it is first started. If a device does not support a given font then the
 * glyphs in the DefineFont class are used to render the characters.
 * </p>
 *
 * <p>
 * An important distinction between the host device to specify the font and
 * using the glyphs in an DefineFont object is that the device font is not
 * anti-aliased and the rendering is dependent on the host device. The glyphs in
 * an DefineFont object are anti-aliased and are guaranteed to look identical on
 * every device the text is displayed.
 * </p>
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
public class FontInfo implements MovieTag {

    /** Format string used in toString() method. */
    private static final String FORMAT = "FontInfo: { identifier=%d;"
            + " encoding=%s; small=%s; italic=%s; bold=%s;"
            + " name=%s; codes=%s}";

    /** The unique identifier of the font that the info applies to. */
    private int identifier;
    /** The font name. */
    private String name;
    /** Code for the character encoding used. */
    private int encoding;
    /** Is the font small. */
    private boolean small;
    /** Is the font italicized. */
    private boolean italic;
    /** Is the font bold. */
    private boolean bold;
    /** Mapping of glyphs (index) to character codes. */
    private List<Integer> codes;

    /** The length of the object, minus the header, when it is encoded. */
    private transient int length;
    /** Whether character codes are 8-bit (false) or 16-bit (true). */
    private transient boolean wideCodes = false;

    /**
     * Creates and initialises an FontInfo object using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */

    public FontInfo(final SWFDecoder coder) throws IOException {
        codes = new ArrayList<Integer>();
        length = coder.readUnsignedShort() & Coder.LENGTH_FIELD;
        if (length == Coder.IS_EXTENDED) {
            length = coder.readInt();
        }
        coder.mark();
        identifier = coder.readUnsignedShort();
        final int nameLength = coder.readByte();
        name = coder.readString(nameLength);

        if (name.length() > 0) {
            while (name.charAt(name.length() - 1) == 0) {
                name = name.substring(0, name.length() - 1);
            }
        }

        final int bits = coder.readByte();
        small = (bits & Coder.BIT5) != 0;
        // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 1 LINES
        encoding = (bits >> 3) & Coder.LOWEST3;
        italic = (bits & Coder.BIT2) != 0;
        bold = (bits & Coder.BIT1) != 0;
        wideCodes = (bits & Coder.BIT0) != 0;

        if (wideCodes) {
            while (coder.bytesRead() < length) {
                codes.add(coder.readUnsignedShort());
            }
        } else {
            while (coder.bytesRead() < length) {
                codes.add(coder.readByte());
            }
        }
        coder.check(length);
        coder.unmark();
    }

    /**
     * Constructs a basic FontInfo object specifying only the name and style of
     * the font.
     *
     * @param uid
     *            the unique identifier of the DefineFont that contains the
     *            glyphs for the font.
     * @param fontName
     *            the name assigned to the font, identifying the font family.
     * @param isBold
     *            indicates whether the font weight is bold (true) or normal
     *            (false).
     * @param isItalic
     *            indicates whether the font style is italic (true) or plain
     *            (false).
     */
    public FontInfo(final int uid, final String fontName, final boolean isBold,
            final boolean isItalic) {
        setIdentifier(uid);
        setName(fontName);
        setItalic(isItalic);
        setBold(isBold);
        small = false;
        encoding = 0;
        codes = new ArrayList<Integer>();
    }

    /**
     * Creates and initialises a FontInfo object using the values copied
     * from another FontInfo object.
     *
     * @param object
     *            a FontInfo object from which the values will be
     *            copied.
     */
    public FontInfo(final FontInfo object) {
        identifier = object.identifier;
        name = object.name;
        italic = object.italic;
        bold = object.bold;
        small = object.small;
        encoding = object.encoding;
        codes = new ArrayList<Integer>(object.codes);
    }

    /**
     * Get the unique identifier of the font definition that this font
     * information is for.
     *
     * @return the unique identifier of the font.
     */
    public int getIdentifier() {
        return identifier;
    }

    /**
     * Get the name of the font family.
     *
     * @return the font name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the encoding scheme used for characters rendered in the font,
     * either ASCII, SJIS or UCS2.
     *
     * @return the encoding used for the character codes.
     */
    public CharacterFormat getEncoding() {
        CharacterFormat value;
        switch(encoding) {
        case 0:
            value = CharacterFormat.UCS2;
            break;
        case 1:
            value = CharacterFormat.ANSI;
            break;
        case 2:
            value = CharacterFormat.SJIS;
            break;
        default:
            throw new IllegalStateException();
        }
        return value;
    }

    /**
     * Does the font have a small point size. This is used only with a Unicode
     * font encoding.
     *
     * @return true if the font is small.
     */
    public boolean isSmall() {
        return small;
    }

    /**
     * Sets the font is small. Used only with Unicode fonts ot provide better
     * appearance when the point size is small.
     *
     * @param aBool
     *            true if the font will be aligned on pixel boundaries.
     */
    public void setSmall(final boolean aBool) {
        small = aBool;
    }

    /**
     * Is the font style italics.
     *
     * @return true if the font is in italics.
     */
    public boolean isItalic() {
        return italic;
    }

    /**
     * Is the font weight bold.
     *
     * @return true if the font weight is bold.
     */
    public boolean isBold() {
        return bold;
    }

    /**
     * Get the list of character codes.
     *
     * @return the list of character codes defined in the font.
     */
    public List<Integer> getCodes() {
        return codes;
    }

    /**
     * Sets the identifier of the font that this font information is for.
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
     * Sets the name of the font. The name be omitted (set to an empty string)
     * if the font is embedded in the Flash file, i.e. the corresponding
     * DefineFont object has all the glyph information.
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
     * Sets the font character encoding.
     *
     * @param anEncoding
     *            the encoding used to identify characters, either ASCII, SJIS
     *            or UNICODE.
     */
    public void setEncoding(final CharacterFormat anEncoding) {
        switch(anEncoding) {
        case UCS2:
            encoding = 0;
            break;
        case ANSI:
            encoding = 1;
            break;
        case SJIS:
            encoding = 2;
            break;
        default:
            throw new IllegalArgumentException();
        }
    }

    /**
     * Sets the font is italics.
     *
     * @param aBool
     *            a boolean flag indicating whether the font will be rendered in
     *            italics.
     */
    public void setItalic(final boolean aBool) {
        italic = aBool;
    }

    /**
     * Sets the font is bold.
     *
     * @param aBool
     *            a boolean flag indicating whether the font will be rendered in
     *            bold face.
     */
    public void setBold(final boolean aBool) {
        bold = aBool;
    }

    /**
     * Add a code to the list of codes. The index position of a character code
     * in the list identifies the index of the corresponding glyph in the
     * DefineFont object.
     *
     * @param aCode
     *            a code for a glyph. Must be in the range 0..65535.
     */
    public void addCode(final int aCode) {
        if ((aCode < 0) || (aCode > Coder.USHORT_MAX)) {
            throw new IllegalArgumentRangeException(
                    0, Coder.USHORT_MAX, aCode);
        }
        codes.add(aCode);
    }

    /**
     * Sets the list of character codes. The index position of a character code
     * in the list identifies the index of the corresponding glyph in the
     * DefineFont object.
     *
     * @param list
     *            the list mapping glyphs to particular character codes. Must
     *            not be null.
     */
    public void setCodes(final List<Integer> list) {
        if (list == null) {
            throw new IllegalArgumentException();
        }
        codes = list;
    }

    /** {@inheritDoc} */
    public FontInfo copy() {
        return new FontInfo(this);
    }

    @Override
    public String toString() {
        return String.format(FORMAT, identifier, encoding, small, italic, bold,
                name, codes);
    }


    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        // CHECKSTYLE:OFF
        length = 4;
        length += context.strlen(name);

        wideCodes = false;

        for (final Integer code : codes) {
            if (code.intValue() > 255) {
                wideCodes = true;
            }
        }

        length += codes.size() * (wideCodes ? 2 : 1);

        return (length > Coder.HEADER_LIMIT ? Coder.LONG_HEADER
                : Coder.SHORT_HEADER) + length;
        // CHECKSTYLE:ON
    }


    /** {@inheritDoc} */
    @SuppressWarnings({"PMD.NPathComplexity", "PMD.CyclomaticComplexity" })
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {

        if (length > Coder.HEADER_LIMIT) {
            coder.writeShort((MovieTypes.FONT_INFO
                    << Coder.LENGTH_FIELD_SIZE) | Coder.IS_EXTENDED);
            coder.writeInt(length);
        } else {
            coder.writeShort((MovieTypes.FONT_INFO
                    << Coder.LENGTH_FIELD_SIZE) | length);
        }
        if (Constants.DEBUG) {
            coder.mark();
        }
        coder.writeShort(identifier);
        coder.writeByte(context.strlen(name));
        coder.writeString(name);
        int bits = 0;
        bits |= small ? Coder.BIT5 : 0;
        // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 1 LINES
        bits |= encoding << 3;
        bits |= italic ? Coder.BIT2 : 0;
        bits |= bold ? Coder.BIT1 : 0;
        bits |= wideCodes ? Coder.BIT0 : 0;
        coder.writeByte(bits);

        if (wideCodes) {
            for (final Integer code : codes) {
                coder.writeShort(code.intValue());
            }
        } else {
            for (final Integer code : codes) {
                coder.writeByte(code.intValue());
            }
        }
        if (Constants.DEBUG) {
            coder.check(length);
            coder.unmark();
        }
    }
}
