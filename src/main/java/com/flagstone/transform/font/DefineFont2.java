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
import java.util.ArrayList;
import java.util.List;

import com.flagstone.transform.Constants;
import com.flagstone.transform.DefineTag;
import com.flagstone.transform.MovieTypes;
import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.datatype.Bounds;
import com.flagstone.transform.exception.IllegalArgumentRangeException;
import com.flagstone.transform.shape.Shape;
import com.flagstone.transform.shape.ShapeData;
import com.flagstone.transform.text.Language;

/**
 * <p>
 * DefineFont2 defines the shapes and layout of the glyphs used in a font. It
 * extends the functionality provided by DefineFont and FontInfo by:
 * </p>
 *
 * <ul>
 * <li>allowing more than 65535 glyphs in a particular font.</li>
 * <li>including the functionality provided by the FontInfo class.</li>
 * <li>specifying ascent, descent and leading for the font.</li>
 * <li>specifying advances for each glyph.</li>
 * <li>specifying bounding rectangles for each glyph.</li>
 * <li>specifying kerning pairs adjusting the distance between glyph pairs.</li>
 * </ul>
 *
 * @see FontInfo
 * @see DefineFont
 */
@SuppressWarnings({"PMD.TooManyFields", "PMD.TooManyMethods",
    "PMD.CyclomaticComplexity" })
public class DefineFont2 implements DefineTag {
    /** Last version of FLash which does not support Language field. */
    private static final int LANGUAGE_VERSION = 5;
    /** Format string used in toString() method. */
    private static final String FORMAT = "DefineFont2: { identifier=%d;"
    		+ " encoding=%s; small=%s; italic=%s; bold=%s; language=%s;"
    	    + " name=%s; shapes=%s; codes=%s; ascent=%d; descent=%d;"
    	    + " leading=%d; advances=%s; bounds=%s; kernings=%s}";

    /** The unique identifier for this object. */
    private int identifier;
    /** Code for the character encoding used. */
    private int encoding;
    /** Is the font small. */
    private boolean small;
    /** Is the font italicized. */
    private boolean italic;
    /** Is the font bold. */
    private boolean bold;
    /** Code representing the spoken language - used for line breaking. */
    private int language;
    /** The font name. */
    private String name;
    /** The list of font glyphs. */
    private List<Shape> shapes;
    /** The list of character codes that map to each glyph - ascending order. */
    private List<Integer> codes;
    /** Height of the font above the baseline. */
    private int ascent;
    /** Height of the font below the baseline. */
    private int descent;
    /** Spacing between successive lines. */
    private int leading;
    /** Advances for each glyph. */
    private List<Integer> advances;
    /** Bounding boxes for each glyph. */
    private List<Bounds> bounds;
    /** Kernings for selected pairs of glyphs. */
    private List<Kerning> kernings;

    /** The length of the object, minus the header, when it is encoded. */
    private transient int length;
    /** Table of offsets to each glyph when encoded. */
    private transient int[] table;
    /** Whether offsets are 16-bit (false) or 32-bit (true). */
    private transient boolean wideOffsets;
    /** Whether character codes are 8-bit (false) or 16-bit (true). */
    private transient boolean wideCodes;

    /**
     * Creates and initialises a DefineFont2 object using values encoded
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
    public DefineFont2(final SWFDecoder coder, final Context context)
            throws IOException {
        length = coder.readUnsignedShort() & Coder.LENGTH_FIELD;
        if (length == Coder.IS_EXTENDED) {
            length = coder.readInt();
        }
        coder.mark();
        identifier = coder.readUnsignedShort();
        shapes = new ArrayList<Shape>();
        codes = new ArrayList<Integer>();
        advances = new ArrayList<Integer>();
        bounds = new ArrayList<Bounds>();
        kernings = new ArrayList<Kerning>();

        final int bits = coder.readByte();
        final boolean containsLayout = (bits & Coder.BIT7) != 0;
        final int format = (bits >> Coder.TO_LOWER_NIB) & Coder.LOWEST3;

        encoding = 0;

        if (format == 1) {
            encoding = 1;
        } else if (format == 2) {
            small = true;
            // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 1 LINES
        } else if (format == 4) {
            encoding = 2;
        }

        wideOffsets = (bits & Coder.BIT3) != 0;
        wideCodes = (bits & Coder.BIT2) != 0;
        italic = (bits & Coder.BIT1) != 0;
        bold = (bits & Coder.BIT0) != 0;

        if (wideCodes) {
            context.put(Context.WIDE_CODES, 1);
        }

        language = coder.readByte();
        final int nameLength = coder.readByte();
        name = coder.readString(nameLength);

        if (name.length() > 0) {
            while (name.charAt(name.length() - 1) == 0) {
                name = name.substring(0, name.length() - 1);
            }
        }

        final int glyphCount = coder.readUnsignedShort();
        final int[] offset = new int[glyphCount + 1];

        if (wideOffsets) {
            for (int i = 0; i < glyphCount; i++) {
                offset[i] = coder.readInt();
            }
        } else {
            for (int i = 0; i < glyphCount; i++) {
                offset[i] = coder.readUnsignedShort();
            }
        }

        if (wideOffsets) {
            offset[glyphCount] = coder.readInt();
        } else {
            offset[glyphCount] = coder.readUnsignedShort();
        }

        Shape shape;
        for (int i = 0; i < glyphCount; i++) {
            shape = new Shape();
            shape.add(new ShapeData(offset[i + 1] - offset[i], coder));
            shapes.add(shape);
        }

        if (wideCodes) {
            for (int i = 0; i < glyphCount; i++) {
                codes.add(coder.readUnsignedShort());
            }
        } else {
            for (int i = 0; i < glyphCount; i++) {
                codes.add(coder.readByte());
            }
        }

        if (containsLayout) {
            ascent = coder.readSignedShort();
            descent = coder.readSignedShort();
            leading = coder.readSignedShort();

            for (int i = 0; i < glyphCount; i++) {
                advances.add(coder.readSignedShort());
            }

            for (int i = 0; i < glyphCount; i++) {
                bounds.add(new Bounds(coder));
            }

            final int kerningCount = coder.readUnsignedShort();

            for (int i = 0; i < kerningCount; i++) {
                kernings.add(new Kerning(coder, context));
            }
        }

        context.remove(Context.WIDE_CODES);
        coder.check(length);
        coder.unmark();
    }

    /**
     * Creates a DefineFont2 object specifying only the name of the font.
     *
     * If none of the remaining attributes are set the Flash Player will load
     * the font from the system on which it is running or substitute a suitable
     * font if the specified font cannot be found. This is particularly useful
     * when defining fonts that will be used to display text in DefineTextField
     * objects.
     *
     * The font will be defined to use Unicode encoding. The flags which define
     * the font's face will be set to false. The arrays of glyphs which define
     * the shapes and the code which map the character codes to a particular
     * glyph will remain empty since the font is loaded from the system on which
     * it is displayed.
     *
     * @param uid
     *            the unique identifier for this font object.
     * @param fontName
     *            the name of the font.
     */
    public DefineFont2(final int uid, final String fontName) {
        setIdentifier(uid);
        setName(fontName);

        shapes = new ArrayList<Shape>();
        codes = new ArrayList<Integer>();
        advances = new ArrayList<Integer>();
        bounds = new ArrayList<Bounds>();
        kernings = new ArrayList<Kerning>();
    }

    /**
     * Creates and initialises a DefineFont2 object using the values copied
     * from another DefineFont2 object.
     *
     * @param object
     *            a DefineFont2 object from which the values will be
     *            copied.
     */
    public DefineFont2(final DefineFont2 object) {
        identifier = object.identifier;
        encoding = object.encoding;
        small = object.small;
        italic = object.italic;
        bold = object.bold;
        language = object.language;
        name = object.name;
        ascent = object.ascent;
        descent = object.descent;
        leading = object.leading;
        shapes = new ArrayList<Shape>(object.shapes.size());
        for (final Shape shape : object.shapes) {
            shapes.add(shape.copy());
        }
        codes = new ArrayList<Integer>(object.codes);
        advances = new ArrayList<Integer>(object.advances);
        bounds = new ArrayList<Bounds>(object.bounds);
        kernings = new ArrayList<Kerning>(object.kernings);
    }

    /** {@inheritDoc} */
    public int getIdentifier() {
        return identifier;
    }

    /** {@inheritDoc} */
    public void setIdentifier(final int uid) {
        if ((uid < 1) || (uid > Coder.USHORT_MAX)) {
            throw new IllegalArgumentRangeException(
                    1, Coder.USHORT_MAX, uid);
        }
        identifier = uid;
    }

    /**
     * Add a character code and the corresponding glyph that will be displayed.
     * Character codes should be added to the font in ascending order.
     *
     * @param code
     *            the character code. Must be in the range 0..65535.
     * @param obj
     *            the shape that represents the glyph displayed for the
     *            character code.
     * @return this object.
     */
    public DefineFont2 addGlyph(final int code, final Shape obj) {
        if ((code < 0) || (code > Coder.USHORT_MAX)) {
            throw new IllegalArgumentRangeException(
                    0, Coder.USHORT_MAX, code);
        }
        codes.add(code);

        if (obj == null) {
            throw new IllegalArgumentException();
        }
        shapes.add(obj);

        return this;
    }

    /**
     * Add an advance to the list of advances. The index position of the entry
     * in the advance list is also used to identify the corresponding glyph and
     * vice-versa.
     *
     * @param anAdvance
     *            an advance for a glyph. Must be in the range -32768..32767.
     * @return this object.
     */
    public DefineFont2 addAdvance(final int anAdvance) {
        if ((anAdvance < Coder.SHORT_MIN)
                || (anAdvance > Coder.SHORT_MAX)) {
            throw new IllegalArgumentRangeException(
                    Coder.SHORT_MIN, Coder.SHORT_MAX, anAdvance);
        }
        advances.add(anAdvance);
        return this;
    }

    /**
     * Add a bounds object to the list of bounds for each glyph. The index
     * position of the entry in the bounds list is also used to identify the
     * corresponding glyph and vice-versa.
     *
     * @param rect
     *            an Bounds. Must not be null.
     * @return this object.
     */
    public DefineFont2 add(final Bounds rect) {
        if (rect == null) {
            throw new IllegalArgumentException();
        }
        bounds.add(rect);
        return this;
    }

    /**
     * Add a kerning object to the list of kernings for pairs of glyphs.
     *
     * @param anObject
     *            an Kerning. Must not be null.
     * @return this object.
     */
    public DefineFont2 add(final Kerning anObject) {
        if (anObject == null) {
            throw new IllegalArgumentException();
        }
        kernings.add(anObject);
        return this;
    }

    /**
     * Get the encoding scheme used for characters rendered in the font,
     * either ASCII, SJIS or UCS2.
     *
     * @return the encoding used for character codes.
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
     * @return a boolean indicating whether the font will be aligned on pixel
     *         boundaries.
     */
    public boolean isSmall() {
        return small;
    }

    /**
     * Sets the font is small. Used only with Unicode fonts.
     *
     * @param aBool
     *            a boolean flag indicating the font will be aligned on pixel
     *            boundaries.
     */
    public void setSmall(final boolean aBool) {
        small = aBool;
    }

    // End Flash 7

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

    // Flash 6
    /**
     * Returns the language code identifying the type of spoken language for the
     * font.
     *
     * @return the Language used to determine how line-breaks are inserted
     *         into text rendered using the font. Returns NONE if the object was
     *         decoded from a movie contains Flash 5 or less.
     */
    public Language getLanguage() {
        return Language.fromInt(language);
    }

    /**
     * Sets the language code used to determine the position of line-breaks in
     * text rendered using the font.
     *
     * NOTE: The language attribute is ignored if the object is encoded in a
     * Flash 5 movie.
     *
     * @param lang the Language identifying the spoken language for the text
     * rendered using the font.
     */
    public void setLanguage(final Language lang) {
        language = lang.getValue();
    }

    // End Flash 6

    /**
     * Returns the name of the font family.
     *
     * @return the name of the font.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the list of shapes used to define the outlines of each font
     * glyph.
     *
     * @return a list of Shape objects
     */
    public List<Shape> getShapes() {
        return shapes;
    }

    /**
     * Returns the list of codes used to identify each glyph in the font. The
     * ordinal position of each Integer representing a code identifies a
     * particular glyph in the shapes list.
     *
     * @return a list of Integer objects that contain the character codes for
     *         each glyph in the font.
     */
    public List<Integer> getCodes() {
        return codes;
    }

    /**
     * Get the ascent for the font in twips.
     *
     * @return the ascent for the font.
     */
    public int getAscent() {
        return ascent;
    }

    /**
     * Get the descent for the font in twips.
     *
     * @return the descent for the font.
     */
    public int getDescent() {
        return descent;
    }

    /**
     * Returns the leading for the font in twips.
     *
     * @return the leading for the font.
     */
    public int getLeading() {
        return leading;
    }

    /**
     * Returns the list of advances defined for each glyph in the font.
     *
     * @return a list of Integers that contain the advance for each
     *         glyph in the font.
     */
    public List<Integer> getAdvances() {
        return advances;
    }

    /**
     * Returns the list of bounding rectangles defined for each glyph in the
     * font.
     *
     * @return a list of Bounds objects.
     */
    public List<Bounds> getBounds() {
        return bounds;
    }

    /**
     * Returns the list of kerning records that define the spacing between
     * glyph pairs.
     *
     * @return a list of Kerning objects that define the spacing adjustment
     *         between pairs of glyphs.
     */
    public List<Kerning> getKernings() {
        return kernings;
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
     * Set the list of shape records that define the outlines of the characters
     * used from the font.
     *
     * @param list
     *            a list of Shape objects that define the glyphs for the font.
     *            Must not be null.
     */
    public void setShapes(final List<Shape> list) {
        if (list == null) {
            throw new IllegalArgumentException();
        }
        shapes = list;
    }

    /**
     * Sets the codes used to identify each glyph in the font.
     *
     * @param list
     *            sets the code table that maps a particular glyph to a
     *            character code. Must not be null.
     */
    public void setCodes(final List<Integer> list) {
        if (list == null) {
            throw new IllegalArgumentException();
        }
        codes = list;
    }

    /**
     * Sets the ascent for the font in twips.
     *
     * @param aNumber
     *            the ascent for the font in the range -32768..32767.
     */
    public void setAscent(final int aNumber) {
        if ((aNumber < Coder.SHORT_MIN)
                || (aNumber > Coder.SHORT_MAX)) {
            throw new IllegalArgumentRangeException(
                    Coder.SHORT_MIN, Coder.SHORT_MAX, aNumber);
        }
        ascent = aNumber;
    }

    /**
     * Sets the descent for the font in twips.
     *
     * @param aNumber
     *            the descent for the font in the range -32768..32767.
     */
    public void setDescent(final int aNumber) {
        if ((aNumber < Coder.SHORT_MIN)
                || (aNumber > Coder.SHORT_MAX)) {
            throw new IllegalArgumentRangeException(
                    Coder.SHORT_MIN, Coder.SHORT_MAX, aNumber);
        }
        descent = aNumber;
    }

    /**
     * Sets the leading for the font in twips.
     *
     * @param aNumber
     *            the descent for the font in the range -32768..32767.
     */
    public void setLeading(final int aNumber) {
        if ((aNumber < Coder.SHORT_MIN)
                || (aNumber > Coder.SHORT_MAX)) {
            throw new IllegalArgumentRangeException(
                    Coder.SHORT_MIN, Coder.SHORT_MAX, aNumber);
        }
        leading = aNumber;
    }

    /**
     * Sets the list of advances for each glyph in the font.
     *
     * @param list
     *            of Integer objects that define the spacing between glyphs.
     *            Must not be null.
     */
    public void setAdvances(final List<Integer> list) {
        if (list == null) {
            throw new IllegalArgumentException();
        }
        advances = list;
    }

    /**
     * Sets the list of bounding rectangles for each glyph in the font.
     *
     * @param list
     *            a list of Bounds objects that define the bounding rectangles
     *            that enclose each glyph in the font. Must not be null.
     */
    public void setBounds(final List<Bounds> list) {
        if (list == null) {
            throw new IllegalArgumentException();
        }
        bounds = list;
    }

    /**
     * Sets the list of kerning records for pairs of glyphs in the font.
     *
     * @param list
     *            a list of Kerning objects that define an adjustment applied
     *            to the spacing between pairs of glyphs. Must not be null.
     */
    public void setKernings(final List<Kerning> list) {
        if (list == null) {
            throw new IllegalArgumentException();
        }
        kernings = list;
    }

    /** {@inheritDoc} */
    public DefineFont2 copy() {
        return new DefineFont2(this);
    }

    @Override
    public String toString() {
        return String.format(FORMAT, identifier, encoding, small, italic, bold,
                language, name, shapes, codes, ascent, descent, leading,
                advances, bounds, kernings);
    }


    /** {@inheritDoc} */
    @SuppressWarnings("PMD.NPathComplexity")
    public int prepareToEncode(final Context context) {
        // CHECKSTYLE:OFF
        wideCodes = (context.get(Context.VERSION) > 5)
                || encoding != 1;

        context.put(Context.FILL_SIZE, 1);
        context.put(Context.LINE_SIZE, context.contains(Context.POSTSCRIPT) ? 1
                : 0);

        if (wideCodes) {
            context.put(Context.WIDE_CODES, 1);
        }

        final int count = shapes.size();
        int index = 0;
        int tableEntry;
        int shapeLength;

        if (wideOffsets) {
            tableEntry = (count << 2) + 4;
        } else {
            tableEntry = (count << 1) + 2;
        }

        table = new int[count + 1];

        int glyphLength = 0;

        for (final Shape shape : shapes) {
            table[index++] = tableEntry;
            shapeLength = shape.prepareToEncode(context);
            glyphLength += shapeLength;
            tableEntry += shapeLength;
        }

        table[index++] = tableEntry;

        wideOffsets = (shapes.size() * 2 + glyphLength)
                > Coder.USHORT_MAX;

        length = 5;
        length += context.strlen(name);
        length += 2;
        length += shapes.size() * (wideOffsets ? 4 : 2);
        length += wideOffsets ? 4 : 2;
        length += glyphLength;
        length += shapes.size() * (wideCodes ? 2 : 1);

        if (containsLayoutInfo()) {
            length += 6;
            length += advances.size() * 2;

            for (final Bounds bound : bounds) {
                length += bound.prepareToEncode(context);
            }

            length += 2;
            length += kernings.size() * (wideCodes ? 6 : 4);
        }

        context.put(Context.FILL_SIZE, 0);
        context.put(Context.LINE_SIZE, 0);
        context.remove(Context.WIDE_CODES);

        return (length > Coder.HEADER_LIMIT ? Coder.LONG_HEADER
                : Coder.SHORT_HEADER) + length;
        // CHECKSTYLE:ON
    }


    /** {@inheritDoc} */
    @SuppressWarnings({"PMD.NPathComplexity", "PMD.ExcessiveMethodLength" })
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        int format;
        if (encoding == 1) {
            format = 1;
        } else if (small) {
            format = 2;
        } else if (encoding == 2) {
            // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 1 LINES
            format = 4;
        } else {
            format = 0;
        }

        if (length > Coder.HEADER_LIMIT) {
            coder.writeShort((MovieTypes.DEFINE_FONT_2
                    << Coder.LENGTH_FIELD_SIZE) | Coder.IS_EXTENDED);
            coder.writeInt(length);
        } else {
            coder.writeShort((MovieTypes.DEFINE_FONT_2
                    << Coder.LENGTH_FIELD_SIZE) | length);
        }
        if (Constants.DEBUG) {
            coder.mark();
        }
        coder.writeShort(identifier);
        context.put(Context.FILL_SIZE, 1);
        context.put(Context.LINE_SIZE, context.contains(Context.POSTSCRIPT) ? 1
                : 0);

        if (wideCodes) {
            context.put(Context.WIDE_CODES, 1);
        }

        int bits = 0;
        bits |= containsLayoutInfo() ? Coder.BIT7 : 0;
        bits |= format << Coder.TO_UPPER_NIB;
        bits |= wideOffsets ? Coder.BIT3 : 0;
        bits |= wideCodes ? Coder.BIT2 : 0;
        bits |= italic ? Coder.BIT1 : 0;
        bits |= bold ? Coder.BIT0 : 0;
        coder.writeByte(bits);

        coder.writeByte(context.get(Context.VERSION)
                > LANGUAGE_VERSION ? language : 0);
        coder.writeByte(context.strlen(name));

        coder.writeString(name);
        coder.writeShort(shapes.size());

        if (wideOffsets) {
            for (int i = 0; i < table.length; i++) {
                coder.writeInt(table[i]);
            }
        } else {
            for (int i = 0; i < table.length; i++) {
                coder.writeShort(table[i]);
            }
        }

        for (final Shape shape : shapes) {
            shape.encode(coder, context);
        }

        if (wideCodes) {
            for (final Integer code : codes) {
                coder.writeShort(code.intValue());
            }
        } else {
            for (final Integer code : codes) {
                coder.writeByte(code.intValue());
            }
        }

        if (containsLayoutInfo()) {
            coder.writeShort(ascent);
            coder.writeShort(descent);
            coder.writeShort(leading);

            for (final Integer advance : advances) {
                coder.writeShort(advance.intValue());
            }

            for (final Bounds bound : bounds) {
                bound.encode(coder, context);
            }

            coder.writeShort(kernings.size());

            for (final Kerning kerning : kernings) {
                kerning.encode(coder, context);
            }
        }

        context.put(Context.FILL_SIZE, 0);
        context.put(Context.LINE_SIZE, 0);
        context.remove(Context.WIDE_CODES);
        if (Constants.DEBUG) {
            coder.check(length);
            coder.unmark();
        }
    }

    /**
     * Does the font contain layout information for the glyphs.
     * @return true if the font contains layout information, false otherwise.
     */
    private boolean containsLayoutInfo() {
        final boolean layout = (ascent != 0) || (descent != 0)
                || (leading != 0) || !advances.isEmpty() || !bounds.isEmpty()
                || !kernings.isEmpty();

        return layout;
    }
}
