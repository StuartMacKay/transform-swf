/*
 * TextSpan.java
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
import java.util.ArrayList;
import java.util.List;

import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.Copyable;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncodeable;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.datatype.Color;
import com.flagstone.transform.exception.IllegalArgumentRangeException;

/**
 * TextSpan is used to display a group of characters with a selected font and
 * colour. TextSpan objects are used in {@link DefineText} and
 * {@link DefineText2} to display a line or block of text.
 *
 * <p>
 * TextSpan contains a list of Character objects which identify the glyphs
 * that will be displayed along with style information that sets the colour of
 * the text, the size of the font and the relative placement of the line within
 * a block of text.
 * </p>
 *
 * <p>
 * Whether the alpha channel in the colour needs to be specified depends on the
 * class the Text is added to. The DefineText2 class supports transparent text
 * while DefineText class does not.
 * </p>
 *
 * <p>
 * The x and y offsets are used to control how several TextSpan objects are laid
 * out to create a block of text. The y offset is specified relative to the
 * bottom edge of the bounding rectangle, which is actually closer to the top of
 * the screen as the direction of the y-axis is from the top to the bottom of
 * the screen. In this respect Flash is counter-intuitive. Lines with higher
 * offset values are displayed below lines with lower offsets.
 * </p>
 *
 * <p>
 * The x and y offsets are also optional and may be set to the constant
 * VALUE_NOT_SET when more than one TextSpan object is created. If the y offset
 * is not specified then a TextSpan object is displayed on the same line as the
 * previous TextSpan. If the x offset is not specified then the TextSpan is
 * displayed after the previous TextSpan. This makes it easy to lay text out on
 * a single line.
 * </p>
 *
 * <p>
 * Similarly the font and colour information is optional. The values from a
 * previous TextSpan object will be used if they are not set.
 * </p>
 *
 * <p>
 * The creation and layout of the glyphs to create the text is too onerous to
 * perform from scratch. It is easier and more convenient to use the
 * {@link com.flagstone.transform.util.text.TextTable} class to create the
 * TextSpan objects.
 * </p>
 *
 * @see DefineText
 * @see DefineText2
 * @see com.flagstone.transform.util.text.TextTable
 * @see com.flagstone.transform.util.font.Font
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
public class TextSpan implements SWFEncodeable, Copyable<TextSpan> {

    /** Format string used in toString() method. */
    private static final String FORMAT = "TextSpan: { identifier=%d; color=%s;"
            + " offsetX=%d; offsetY=%d; height=%d; characters=%s}";

    /** The colour used to draw the text. */
    private Color color;
    /** The offset on the x-axis of the text span, within the text block. */
    private Integer offsetX;
    /** The offset on the y-axis of the text span, within the text block. */
    private Integer offsetY;
    /** The unique identifier of the font used to render the text. */
    private Integer identifier;
    /** The height of the text in twips. */
    private Integer height;

    /** The list of characters to be displayed. */
    private List<GlyphIndex> characters;

    /** Indicates whether the text contains font or layout information. */
    private transient boolean hasStyle;
    /** Indicate that a font is specified. */
    private transient boolean hasFont;
    /** Indicate that a colour is specified. */
    private transient boolean hasColor;
    /** Indicate that an x offset is specified. */
    private transient boolean hasX;
    /** Indicate that an y offset is specified. */
    private transient boolean hasY;

    /**
     * Creates and initialises a TextSpan object using values encoded
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
    public TextSpan(final SWFDecoder coder,
            final Context context) throws IOException {

        final int bits = coder.readByte();
        hasFont = (bits & Coder.BIT3) != 0;
        hasColor = (bits & Coder.BIT2) != 0;
        hasY = (bits & Coder.BIT1) != 0;
        hasX = (bits & Coder.BIT0) != 0;

        if (hasFont) {
            identifier = coder.readUnsignedShort();
        }
        if (hasColor) {
            color = new Color(coder, context);
        }
        if (hasX) {
            offsetX = coder.readSignedShort();
        }
        if (hasY) {
            offsetY = coder.readSignedShort();
        }
        if (hasFont) {
            height = coder.readSignedShort();
        }

        final int charCount = coder.readByte();

        characters = new ArrayList<GlyphIndex>(charCount);

        for (int i = 0; i < charCount; i++) {
            characters.add(new GlyphIndex(coder, context));
        }

        coder.alignToByte();
    }

    /**
     * Creates a Text object, specifying the colour and position of the
     * following Text.
     *
     * @param uid
     *            the identifier of the font that the text will be rendered in.
     *            Must be in the range 1..65535.
     * @param aHeight
     *            the height of the text in the chosen font. Must be in the
     *            range 1..65535.
     * @param aColor
     *            the colour of the text.
     * @param xOffset
     *            the location of the text relative to the left edge of the
     *            bounding rectangle enclosing the text.
     * @param yOffset
     *            the location of the text relative to the bottom edge of the
     *            bounding rectangle enclosing the text.
     * @param list
     *            a list of Character objects. Must not be null.
     */
    public TextSpan(final Integer uid, final Integer aHeight,
            final Color aColor, final Integer xOffset, final Integer yOffset,
            final List<GlyphIndex> list) {
        setIdentifier(uid);
        setHeight(aHeight);
        setColor(aColor);
        setOffsetX(xOffset);
        setOffsetY(yOffset);
        setCharacters(list);
    }

    /**
     * Creates and initialises a TextSpan object using the values copied
     * from another TextSpan object.
     *
     * @param object
     *            a TextSpan object from which the values will be
     *            copied.
     */
    public TextSpan(final TextSpan object) {
        identifier = object.identifier;
        color = object.color;
        offsetX = object.offsetX;
        offsetY = object.offsetY;
        height = object.height;
        characters = new ArrayList<GlyphIndex>(object.characters);
    }

    /**
     * Get the identifier of the font in which the text will be displayed.
     *
     * @return the unique identifier of the font.
     */
    public Integer getIdentifier() {
        return identifier;
    }

    /**
     * Get the colour used to display the text.
     *
     * @return the text colour.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Get the location of the start of the text relative to the left edge
     * of the bounding rectangle in twips.
     *
     * @return the left offset.
     */
    public Integer getOffsetX() {
        return offsetX;
    }

    /**
     * Get the location of the start of the text relative to the bottom edge
     * of the bounding rectangle in twips.
     *
     * @return the top offset.
     */
    public Integer getOffsetY() {
        return offsetY;
    }

    /**
     * Get the height of the text.
     *
     * @return the size, in twips, of the font used to display the text.
     */
    public Integer getHeight() {
        return height;
    }

    /**
     * Sets the identifier of the font in which the text will be displayed.
     *
     * @param uid
     *            the identifier of the font that the text will be rendered in.
     *            Must be in the range 1..65535.
     */
    public void setIdentifier(final Integer uid) {
        if ((uid != null) && ((uid < 1) || (uid > Coder.USHORT_MAX))) {
             throw new IllegalArgumentRangeException(
                     1, Coder.USHORT_MAX, uid);
        }
        identifier = uid;
    }

    /**
     * Sets the colour of the font in which the text will be displayed.
     *
     * @param aColor
     *            the colour of the text.
     */
    public void setColor(final Color aColor) {
        color = aColor;
    }

    /**
     * Sets the location of the start of the text relative to the left edge of
     * the bounding rectangle in twips.
     *
     * @param offset
     *            the location of the text relative to the left edge of the
     *            bounding rectangle enclosing the text. Must be in the range
     *            -32768..32767 or null if no offset is specified.
     */
    public void setOffsetX(final Integer offset) {
        if ((offset != null)
                && ((offset < Coder.SHORT_MIN)
                || (offset > Coder.SHORT_MAX))) {
            throw new IllegalArgumentRangeException(
                    Coder.SHORT_MIN, Coder.SHORT_MAX, offset);
        }
        offsetX = offset;
    }

    /**
     * Sets the location of the start of the text relative to the bottom edge of
     * the bounding rectangle in twips.
     *
     * @param offset
     *            the location of the text relative to the bottom edge of the
     *            bounding rectangle enclosing the text. Must be in the range
     *            -32768..32767 or null if no offset is specified.
     */
    public void setOffsetY(final Integer offset) {
        if ((offset != null)
                && ((offset < Coder.SHORT_MIN)
                || (offset > Coder.SHORT_MAX))) {
            throw new IllegalArgumentRangeException(
                    Coder.SHORT_MIN, Coder.SHORT_MAX, offset);
        }
        offsetY = offset;
    }

    /**
     * Sets the height of the text in twips.
     *
     * @param aHeight
     *            the height of the text in the chosen font. Must be in the
     *            range 0..65535.
     */
    public void setHeight(final Integer aHeight) {
        if ((aHeight < 0) || (aHeight > Coder.USHORT_MAX)) {
            throw new IllegalArgumentRangeException(
                    0, Coder.USHORT_MAX, aHeight);
        }
        height = aHeight;
    }

    /**
     * Adds an Character object to the list of characters.
     *
     * @param aCharacter
     *            an Character object. Must not be null.
     * @return this object.
     */
    public TextSpan add(final GlyphIndex aCharacter) {
        characters.add(aCharacter);
        return this;
    }

    /**
     * Returns the list of characters to be displayed.
     *
     * @return the list of Character objects.
     */
    public List<GlyphIndex> getCharacters() {
        return characters;
    }

    /**
     * Sets the list of characters to be displayed.
     *
     * @param list
     *            a list of Character objects. Must not be null.
     */
    public void setCharacters(final List<GlyphIndex> list) {
        if (list == null) {
            throw new IllegalArgumentException();
        }
        characters = list;
    }

    /** {@inheritDoc} */
    public TextSpan copy() {
        return new TextSpan(this);
    }

    @Override
    public String toString() {
        return String.format(FORMAT, identifier, color, offsetX, offsetY,
                height, characters);
    }


    /** {@inheritDoc} */
    @SuppressWarnings({"PMD.NPathComplexity", "PMD.CyclomaticComplexity" })
    public int prepareToEncode(final Context context) {
        // CHECKSTYLE:OFF
        hasFont = (identifier != null) && (height != null);
        hasColor = color != null;
        hasX = offsetX != null;
        hasY = offsetY != null;
        hasStyle = hasFont || hasColor || hasX || hasY;

        int length = 1;
        if (hasStyle) {
            length += (hasFont) ? 2 : 0;
            length += (hasColor) ? (context.contains(Context.TRANSPARENT) ? 4
                    : 3) : 0;
            length += (hasY) ? 2 : 0;
            length += (hasX) ? 2 : 0;
            length += (hasFont) ? 2 : 0;
        }

        length += 1;

        if (!characters.isEmpty()) {
            final int glyphSize = context.get(Context.GLYPH_SIZE);
            final int advanceSize = context.get(Context.ADVANCE_SIZE);

            int numberOfBits = (glyphSize + advanceSize) * characters.size();
            numberOfBits += (numberOfBits % 8 > 0) ? 8 - (numberOfBits % 8) : 0;

            length += numberOfBits >> 3;
        }
        return length;
        // CHECKSTYLE:ON
    }


    /** {@inheritDoc} */
    @SuppressWarnings({"PMD.NPathComplexity", "PMD.CyclomaticComplexity" })
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {

        int bits = Coder.BIT7;
        bits |= hasFont ? Coder.BIT3 : 0;
        bits |= hasColor ? Coder.BIT2 : 0;
        bits |= hasY ? Coder.BIT1 : 0;
        bits |= hasX ? Coder.BIT0 : 0;
        coder.writeByte(bits);

        if (hasStyle) {
            if (hasFont) {
                coder.writeShort(identifier);
            }

            if (hasColor) {
                color.encode(coder, context);
            }

            if (hasX) {
                coder.writeShort(offsetX);
            }

            if (hasY) {
                coder.writeShort(offsetY);
            }

            if (hasFont) {
                coder.writeShort(height);
            }
        }

        coder.writeByte(characters.size());

        for (final GlyphIndex index : characters) {
            index.encode(coder, context);
        }

        coder.alignToByte();
    }

    /**
     * The number of bits used to encode the glyph indices.
     * @return the number of bits used to encode each glyph index.
     */
    protected int glyphBits() {
        int numberOfBits = 0;

        for (final GlyphIndex index : characters) {
            numberOfBits = Math.max(numberOfBits, Coder.unsignedSize(index
                    .getGlyphIndex()));
        }

        return numberOfBits;
    }

    /**
     * The number of bits used to encode the advances.
     * @return the number of bits used to encode each advance.
     */
   protected int advanceBits() {
        int numberOfBits = 0;

        for (final GlyphIndex index : characters) {
            numberOfBits = Math.max(numberOfBits, Coder.size(index
                    .getAdvance()));
        }

        return numberOfBits;
    }
}
