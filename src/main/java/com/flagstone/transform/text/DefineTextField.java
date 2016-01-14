/*
 * DefineTextField.java
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
import com.flagstone.transform.DefineTag;
import com.flagstone.transform.MovieTypes;
import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.datatype.Bounds;
import com.flagstone.transform.datatype.Color;
import com.flagstone.transform.datatype.HorizontalAlign;
import com.flagstone.transform.exception.IllegalArgumentRangeException;

/**
 * DefineTextField defines an editable text field.
 *
 * <p>
 * The value entered into the text field is assigned to a specified variable
 * allowing the creation of forms to accept values entered by a person viewing
 * the Flash file.
 * </p>
 *
 * <p>
 * The class contains a complex set of attributes which allows a high degree of
 * control over how a text field is displayed:
 * </p>
 *
 * <table class="datasheet">
 *
 * <tr>
 * <td valign="top">wordWrap</td>
 * <td>Indicates whether the text should be wrapped.</td>
 * </tr>
 *
 * <tr>
 * <td valign="top">multiline</td>
 * <td>Indicates whether the text field contains multiple lines.</td>
 * </tr>
 *
 * <tr>
 * <td valign="top">password</td>
 * <td>Indicates whether the text field will be used to display a password.</td>
 * </tr>
 *
 * <tr>
 * <td valign="top">readOnly</td>
 * <td>Indicates whether the text field is read only.</td>
 * </tr>
 *
 * <tr>
 * <td valign="top">selectable</td>
 * <td>Indicates whether the text field is selectable.</td>
 * </tr>
 *
 * <tr>
 * <td valign="top">bordered</td>
 * <td>Indicates whether the text field is bordered.</td>
 * </tr>
 *
 * <tr>
 * <td valign="top">HTML</td>
 * <td>Indicates whether the text field contains HTML.</td>
 * </tr>
 *
 * <tr>
 * <td valign="top">useFontGlyphs</td>
 * <td>Use either the glyphs defined in the movie to display the text or load
 * the specified from the platform on which the Flash Player is hosted.</td>
 * </tr>
 *
 * <tr>
 * <td valign="top">autosize</td>
 * <td>Indicates whether the text field will resize automatically to fit the
 * text entered.</td>
 * </tr>
 *
 * <tr>
 * <td valign="top">maxLength</td>
 * <td>The maximum length of the text field. May be set to zero is not maximum
 * length is defined.</td>
 * </tr>
 *
 * </table>
 *
 * <p>
 * Additional layout information for the spacing of the text relative to the
 * text field borders can also be specified through the following set of
 * attributes:
 * </p>
 *
 * <table class="datasheet">
 *
 * <tr>
 * <td valign="top">alignment</td>
 * <td>The text in the field is left-aligned, right-aligned, centred.</td>
 * </tr>
 * <tr>
 * <td valign="top">leftMargin</td>
 * <td>Left margin in twips.</td>
 * </tr>
 * <tr>
 * <td valign="top">rightMargin</td>
 * <td>Right margin in twips.</td>
 * </tr>
 * <tr>
 * <td valign="top">indent</td>
 * <td>Text indentation in twips.</td>
 * </tr>
 * <tr>
 * <td valign="top">leading</td>
 * <td>Leading in twips.</td>
 * </tr>
 * </table>
 *
 * <p>
 * <b>HTML Support</b><br/>
 * Setting the HTML flag to true allows text marked up with a limited set of
 * HTML tags to be displayed in the text field. The following tags are
 * supported:
 * </p>
 *
 * <table>
 * <tr>
 * <td>&lt;p&gt;&lt;/p&gt;</td>
 * <td>Delimits a paragraph. Only the align attribute is supported:<br>
 * <p [align = left | right | center ]> </p></td>
 * </tr>
 *
 * <tr>
 * <td valign="top" nowrap>&lt;br&gt;</td>
 * <td>Inserts a line break.</td>
 * </tr>
 *
 * <tr>
 * <td valign="top" nowrap>&lt;a&gt;&lt;/a&gt;</td>
 * <td>Define a hyperlink. Two attributes are supported:
 * <ul>
 * <li>href - the URL of the link.</li>
 * <li>target - name of a window or frame. (optional)</li>
 * </ul>
 * </td>
 * </tr>
 *
 * <tr>
 * <td valign="top" nowrap>&lt;font&gt;&lt;/font&gt;</td>
 * <td>Format enclosed text using the font. Three attributes are supported:
 * <ul>
 * <li>name - must match the name of a font defined using the DefineFont2 class.
 * </li>
 * <li>size - the height of the font in twips.</li>
 * <li>color - the colour of the text in the hexadecimal format #RRGGBB.</li>
 * </ul>
 * </td>
 * </tr>
 *
 * <tr>
 * <td valign="top" nowrap>&lt;b&gt;&lt;/b&gt;</td>
 * <td>Delimits text that should be displayed in bold.</td>
 * </tr>
 *
 * <tr>
 * <td valign="top" nowrap>&lt;b&gt;&lt;/b&gt;</td>
 * <td>Delimits text that should be displayed in italics.</td>
 * </tr>
 *
 * <tr>
 * <td valign="top" nowrap>&lt;b&gt;&lt;/b&gt;</td>
 * <td>Delimits text that should be displayed underlined.</td>
 * </tr>
 *
 * <tr>
 * <td valign="top" nowrap>&lt;li&gt;&lt;/li&gt;</td>
 * <td>Display bulleted paragraph. Strictly speaking this is not an HTML list.
 * The &lt;ul&gt; tag is not required and no list formats are supported.</td>
 * </tr>
 *
 * </table>
 *
 */
@SuppressWarnings({"PMD.TooManyFields",
    "PMD.TooManyMethods",
    "PMD.CyclomaticComplexity",
    "PMD.ExcessivePublicCount" })
public class DefineTextField implements DefineTag {

    /** The maximum size of a dimension when defining the field layout. */
    private static final int MAX_SPACE = 65535;

    /** The unique identifier for this object. */
    private int identifier;
    /** The bounding box for the field. */
    private Bounds bounds;
    /** Is the field word-wrapped. */
    private boolean wordWrapped;
    /** Does the field contain more than one line. */
    private boolean multiline;
    /** Are * characters displayed for each key pressed. */
    private boolean password;
    /** Is the field read-only. */
    private boolean readOnly;
    /** Reserved. */
    private transient int reserved1;
    /** Can the text be selected. */
    private boolean selectable;
    /** Is a border drawn around the field. */
    private boolean bordered;
    /** Reserved. */
    private transient boolean reserved2;
    /** Does the text contain HTML markup. */
    private boolean html;
    /** Does the text use an embedded font. */
    private boolean embedded;
    /** Does the field resize automatically to display the text. */
    private boolean autoSize;
    /** The unique identifier for the font. */
    private int fontIdentifier;
    /** The name of the actionscript 3 class that provides the font. */
    private String fontClass;
    /** The height of the font in twips. */
    private int fontHeight;
    /** The colour of the text. */
    private Color color;
    /** The maximum number of characters than can be entered. */
    private int maxLength;
    /** Code representing the text alignment. */
    private int alignment;
    /** The padding between the text and the left side of the field. */
    private Integer leftMargin;
    /** The padding between the text and the right side of the field. */
    private Integer rightMargin;
    /** The initial indent for the first line of any paragraph. */
    private Integer indent;
    /** The spacing between lines. */
    private Integer leading;
    /** The name of the actionscript variable. */
    private String variableName = "";
    /** The initial contents of the field. */
    private String initialText = "";

    /** The length of the object, minus the header, when it is encoded. */
    private transient int length;

    /**
     * Creates and initialises a DefineTextField object using values encoded
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

    public DefineTextField(final SWFDecoder coder, final Context context)
            throws IOException {
        length = coder.readUnsignedShort() & Coder.LENGTH_FIELD;
        if (length == Coder.IS_EXTENDED) {
            length = coder.readInt();
        }
        coder.mark();
        identifier = coder.readUnsignedShort();
        context.put(Context.TRANSPARENT, 1);

        bounds = new Bounds(coder);

        int bits = coder.readByte();
        final boolean containsText = (bits & Coder.BIT7) != 0;
        wordWrapped = (bits & Coder.BIT6) != 0;
        multiline = (bits & Coder.BIT5) != 0;
        password = (bits & Coder.BIT4) != 0;
        readOnly = (bits & Coder.BIT3) != 0;
        final boolean containsColor = (bits & Coder.BIT2) != 0;
        final boolean containsMaxLength = (bits & Coder.BIT1) != 0;
        final boolean containsFont = (bits & Coder.BIT0) != 0;

        bits = coder.readByte();
        final boolean containsClass = (bits & Coder.BIT7) != 0;
        autoSize = (bits & Coder.BIT6) != 0;
        final boolean containsLayout = (bits & Coder.BIT5) != 0;
        selectable = (bits & Coder.BIT4) != 0;
        bordered = (bits & Coder.BIT3) != 0;
        reserved2 = (bits & Coder.BIT2) != 0;
        html = (bits & Coder.BIT1) != 0;
        embedded = (bits & Coder.BIT0) != 0;

        if (containsFont) {
            fontIdentifier = coder.readUnsignedShort();

            if (containsClass) {
                fontClass = coder.readString();
            }
            fontHeight = coder.readUnsignedShort();
        }

        if (containsColor) {
            color = new Color(coder, context);
        }

        if (containsMaxLength) {
            maxLength = coder.readUnsignedShort();
        }

        if (containsLayout) {
            alignment = coder.readByte();
            leftMargin = coder.readUnsignedShort();
            rightMargin = coder.readUnsignedShort();
            indent = coder.readUnsignedShort();
            leading = coder.readSignedShort();
        }

        variableName = coder.readString();

        if (containsText) {
            initialText = coder.readString();
        }

        context.remove(Context.TRANSPARENT);
        coder.check(length);
        coder.unmark();
    }

    /**
     * Creates an DefineTextField object with the specified identifier.
     *
     * @param uid the unique identifier for the text field.
     */
    public DefineTextField(final int uid) {
        setIdentifier(uid);
    }

    /**
     * Creates and initialises a DefineTextField object using the values copied
     * from another DefineTextField object.
     *
     * @param object
     *            a DefineTextField object from which the values will be
     *            copied.
     */
    public DefineTextField(final DefineTextField object) {
        identifier = object.identifier;
        bounds = object.bounds;
        wordWrapped = object.wordWrapped;
        multiline = object.multiline;
        password = object.password;
        readOnly = object.readOnly;
        reserved1 = object.reserved1;
        selectable = object.selectable;
        bordered = object.bordered;
        reserved2 = object.reserved2;
        html = object.html;
        embedded = object.embedded;
        autoSize = object.autoSize;
        fontIdentifier = object.fontIdentifier;
        fontClass = object.fontClass;
        fontHeight = object.fontHeight;
        color = object.color;
        maxLength = object.maxLength;
        alignment = object.alignment;
        leftMargin = object.leftMargin;
        rightMargin = object.rightMargin;
        indent = object.indent;
        leading = object.leading;
        variableName = object.variableName;
        initialText = object.initialText;
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
     * Returns the bounding rectangle that completely encloses the text field.
     *
     * @return the bounding rectangle of the text.
     */
    public Bounds getBounds() {
        return bounds;
    }

    /**
     * Does the text field support word wrapping.
     *
     * @return true if the field will wrap the text.
     */
    public boolean isWordWrapped() {
        return wordWrapped;
    }

    /**
     * Does the text field support multiple lines of text.
     *
     * @return true if the field contains more than one line.
     */
    public boolean isMultiline() {
        return multiline;
    }

    /**
     * Does the text field protect passwords being entered.
     *
     * @return true if the field obscures the characters typed.
     */
    public boolean isPassword() {
        return password;
    }

    /**
     * Is the text field read-only.
     *
     * @return true if the text cannot be edited.
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Is the text field selectable.
     *
     * @return true if the text can be selected with the mouse.
     */
    public boolean isSelectable() {
        return selectable;
    }

    /**
     * Is the text field bordered.
     *
     * @return true if the field has a border.
     */
    public boolean isBordered() {
        return bordered;
    }

    /**
     * Does the text field contain HTML.
     *
     * @return true if the field displays HTML.
     */
    public boolean isHtml() {
        return html;
    }

    /**
     * Does the text field resize to fit the contents.
     *
     * @return true if the field will automatically resize to fit the text.
     */
    public boolean isAutoSize() {
        return autoSize;
    }

    /**
     * Sets whether the text field will resize to fit the contents.
     *
     * @param aFlag
     *            indicate whether the text field will resize automatically.
     * @return this object.
     */
    public DefineTextField setAutoSize(final boolean aFlag) {
        autoSize = aFlag;
        return this;
    }

    /**
     * Indicates whether the text will be displayed using the font defined in
     * the movie or whether a font defined on the host platform will be used.
     *
     * @return true if the text will be displayed using the glyphs from the font
     *         defined in the movie, false if the glyphs will be loaded from the
     *         platform on which the Flash Player is hosted.
     */
    public boolean isEmbedded() {
        return embedded;
    }

    /**
     * Get the identifier of the font used to display the characters.
     *
     * @return the unique identifier of the font.
     */
    public int getFontIdentifier() {
        return fontIdentifier;
    }

    /**
     * Get the name of the Actionscript 3 class that provides the font.
     * @return the name the Actionscript class.
     */
    public String getFontClass() {
        return fontClass;
    }

    /**
     * Get the size of the font used to display the text.
     *
     * @return the height of the font in twips.
     */
    public int getFontHeight() {
        return fontHeight;
    }

    /**
     * Get the text color.
     *
     * @return the colour used to display the text.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Get the maximum number of characters displayed in the field.
     *
     * @return the maximum number of characters displayed.
     */
    public int getMaxLength() {
        return maxLength;
    }

    /**
     * Get the alignment of the text, either AlignLeft, AlignRight,
     * AlignCenter or AlignJustify.
     *
     * @return the alignment of the text.
     */
    public HorizontalAlign getAlignment() {
        HorizontalAlign value;
        switch (alignment) {
        case 0:
            value = HorizontalAlign.LEFT;
            break;
        case Coder.BIT0:
            value = HorizontalAlign.RIGHT;
            break;
        case Coder.BIT1:
            value = HorizontalAlign.CENTER;
            break;
        case Coder.BIT0 | Coder.BIT1:
            value = HorizontalAlign.JUSTIFY;
            break;
        default:
            throw new IllegalStateException();
        }
        return value;
    }

    /**
     * Get the left margin in twips.
     *
     * @return the padding between the text and the left edge of the field.
     */
    public int getLeftMargin() {
        return leftMargin;
    }

    /**
     * Get the right margin in twips.
     *
     * @return the padding between the text and the right edge of the field.
     */
    public int getRightMargin() {
        return rightMargin;
    }

    /**
     * Get the indentation of the first line of text in twips.
     *
     * @return the initial indent for the first line of text.
     */
    public int getIndent() {
        return indent;
    }

    /**
     * Get the leading in twips.
     *
     * @return the spacing between lines.
     */
    public int getLeading() {
        return leading;
    }

    /**
     * Get the name of the variable the value in the text field will be
     * assigned to.
     *
     * @return the name of the actionscript variable that the field contents
     * are assigned to.
     */
    public String getVariableName() {
        return variableName;
    }

    /**
     * Get the default text displayed in the field.
     *
     * @return the sting initially displayed in the field.
     */
    public String getInitialText() {
        return initialText;
    }

    /**
     * Sets the bounding rectangle of the text field.
     *
     * @param rect
     *            the bounding rectangle enclosing the text field. Must not be
     *            null.
     * @return this object.
     */
    public DefineTextField setBounds(final Bounds rect) {
        if (rect == null) {
            throw new IllegalArgumentException();
        }
        bounds = rect;
        return this;
    }

    /**
     * Set whether the text field supports word wrapping.
     *
     * @param aFlag
     *            set whether the text field is word wrapped.
     * @return this object.
     */
    public DefineTextField setWordWrapped(final boolean aFlag) {
        wordWrapped = aFlag;
        return this;
    }

    /**
     * Set whether the text field contains multiple lines of text.
     *
     * @param aFlag
     *            set whether the text field is multiline.
     * @return this object.
     */
    public DefineTextField setMultiline(final boolean aFlag) {
        multiline = aFlag;
        return this;
    }

    /**
     * Set whether the text field should protect passwords entered.
     *
     * @param aFlag
     *            set whether the text field is password protected.
     * @return this object.
     */
    public DefineTextField setPassword(final boolean aFlag) {
        password = aFlag;
        return this;
    }

    /**
     * Set whether the text field is read-only.
     *
     * @param aFlag
     *            set whether the text field is read-only.
     * @return this object.
     */
    public DefineTextField setReadOnly(final boolean aFlag) {
        readOnly = aFlag;
        return this;
    }

    /**
     * Set whether the text field is selectable.
     *
     * @param aFlag
     *            set whether the text field is selectable.
     * @return this object.
     */
    public DefineTextField setSelectable(final boolean aFlag) {
        selectable = aFlag;
        return this;
    }

    /**
     * Set whether the text field is bordered.
     *
     * @param aFlag
     *            set whether the text field is bordered.
     * @return this object.
     */
   public DefineTextField setBordered(final boolean aFlag) {
        bordered = aFlag;
        return this;
    }

    /**
     * Set whether the text field contains HTML.
     *
     * @param aFlag
     *            set whether the text field contains HTML.
     * @return this object.
     */
    public DefineTextField setHtml(final boolean aFlag) {
        html = aFlag;
        return this;
    }

    /**
     * Set whether the text field characters are displayed using the font
     * defined in the movie or whether the Flash Player uses a font definition
     * loaded from the platform on which it is hosted.
     *
     * @param aFlag
     *            set whether the text field characters will be drawn using the
     *            font in the movie (true) or use a font loaded by the Flash
     *            Player (false).
     * @return this object.
     */
    public DefineTextField setEmbedded(final boolean aFlag) {
        embedded = aFlag;
        return this;
    }

    /**
     * Sets the identifier of the font used to display the characters.
     *
     * @param uid
     *            the identifier for the font that the text will be rendered in.
     *            Must be in the range 1..65535.
     * @return this object.
     */
    public DefineTextField setFontIdentifier(final int uid) {
        if ((uid < 1) || (uid > Coder.USHORT_MAX)) {
             throw new IllegalArgumentRangeException(
                     1, Coder.USHORT_MAX, uid);
        }
        fontIdentifier = uid;
        fontClass = null;  //NOPMD
        return this;
    }


    /**
     * Set the name of the Actionscript 3 class that provides the font.
     * @param name the name the Actionscript class.
     * @return this object.
     */
    public DefineTextField setFontClass(final String name) {
        fontClass = name;
        fontIdentifier = 0;
        return this;
    }

    /**
     * Sets the height of the characters.
     *
     * @param aNumber
     *            the height of the font. Must be in the range 0..65535.
     * @return this object.
     */
    public DefineTextField setFontHeight(final int aNumber) {
        if ((aNumber < 0) || (aNumber > Coder.USHORT_MAX)) {
            throw new IllegalArgumentRangeException(
                    0, Coder.USHORT_MAX, aNumber);
        }
        fontHeight = aNumber;
        return this;
    }

    /**
     * Sets the text color. If set to null then the text color defaults to
     * black.
     *
     * @param aColor
     *            the colour object that defines the text colour.
     * @return this object.
     */
    public DefineTextField setColor(final Color aColor) {
        if (aColor == null) {
            color = new Color(0, 0, 0);
        } else {
            color = aColor;
        }
        return this;
    }

    /**
     * Sets the maximum length of the text displayed. May be set to zero if no
     * maximum length is defined.
     *
     * @param aNumber
     *            the maximum number of characters displayed in the field. Must
     *            be in the range 0..65535.
     * @return this object.
     */
    public DefineTextField setMaxLength(final int aNumber) {
        if ((aNumber < 0) || (aNumber > MAX_SPACE)) {
            throw new IllegalArgumentRangeException(0, MAX_SPACE, aNumber);
        }
        maxLength = aNumber;
        return this;
    }

    /**
     * Sets the alignment of the text, either AlignLeft, AlignRight, AlignCenter
     * or AlignJustify.
     *
     * @param align
     *            the type of alignment. Must be either ALIGN_LEFT, ALIGN_RIGHT
     *            or ALIGN_JUSTIFY.
     * @return this object.
     */
    public DefineTextField setAlignment(final HorizontalAlign align) {
        switch(align) {
        case LEFT:
            alignment = 0;
            break;
        case RIGHT:
            alignment = Coder.BIT0;
            break;
        case CENTER:
            alignment = Coder.BIT1;
            break;
        case JUSTIFY:
            alignment = Coder.BIT0 | Coder.BIT1;
            break;
        default:
            throw new IllegalArgumentException();
        }
        return this;
    }

    /**
     * Sets the left margin in twips.
     *
     * @param aNumber
     *            the width of the left margin. Must be in the range 0..65535.
     * @return this object.
     */
    public DefineTextField setLeftMargin(final Integer aNumber) {
        if ((aNumber != null) && ((aNumber < 0) || (aNumber > MAX_SPACE))) {
            throw new IllegalArgumentRangeException(0, MAX_SPACE, aNumber);
        }
        leftMargin = aNumber;
        return this;
    }

    /**
     * Sets the right margin in twips.
     *
     * @param aNumber
     *            the width of the right margin. Must be in the range 0..65535.
     * @return this object.
     */
    public DefineTextField setRightMargin(final Integer aNumber) {
        if ((aNumber != null) && ((aNumber < 0) || (aNumber > MAX_SPACE))) {
            throw new IllegalArgumentRangeException(0, MAX_SPACE, aNumber);
        }
        rightMargin = aNumber;
        return this;
    }

    /**
     * Returns the indentation of the first line of text in twips.
     *
     * @param aNumber
     *            the indentation for the first line. Must be in the range
     *            0..65535.
     * @return this object.
     */
    public DefineTextField setIndent(final Integer aNumber) {
        if ((aNumber != null) && ((aNumber < 0) || (aNumber > MAX_SPACE))) {
            throw new IllegalArgumentRangeException(0, MAX_SPACE, aNumber);
        }
        indent = aNumber;
        return this;
    }

    /**
     * Sets the spacing between lines, measured in twips.
     *
     * @param aNumber
     *            the value for the leading. Must be in the range -32768..32767.
     * @return this object.
     */
    public DefineTextField setLeading(final Integer aNumber) {
        if ((aNumber < Coder.SHORT_MIN)
                || (aNumber > Coder.SHORT_MAX)) {
            throw new IllegalArgumentRangeException(
                    Coder.SHORT_MIN, Coder.SHORT_MAX, aNumber);
        }
        leading = aNumber;
        return this;
    }

    /**
     * Sets the name of the variable the value in the text field will be
     * assigned to.
     *
     * @param aString
     *            the name of the variable.
     * @return this object.
     */
    public DefineTextField setVariableName(final String aString) {
        variableName = aString;
        return this;
    }

    /**
     * Sets the value that will initially be displayed in the text field.
     *
     * @param aString
     *            the initial text displayed.
     * @return this object.
     */
    public DefineTextField setInitialText(final String aString) {
        initialText = aString;
        return this;
    }

    /** {@inheritDoc} */
    public DefineTextField copy() {
        return new DefineTextField(this);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();

        builder.append("DefineTextField: { identifier = ").append(identifier);
        builder.append("; bounds = ").append(bounds.toString());
        builder.append("; wordWrapped = ").append(wordWrapped);
        builder.append("; multiline = ").append(multiline);
        builder.append("; password = ").append(password);
        builder.append("; readOnly = ").append(readOnly);
        builder.append("; autoSize = ").append(autoSize);
        builder.append("; selectable = ").append(selectable);
        builder.append("; bordered = ").append(bordered);
        builder.append("; HTML = ").append(html);
        builder.append("; embedded = ").append(embedded);
        builder.append("; fontIdentifier = ").append(fontIdentifier)
                .append(";");
        builder.append("; fontHeight = ").append(fontHeight);
        builder.append("; color = ").append(color);
        builder.append("; maxLength = ").append(maxLength);
        builder.append("; alignment = ").append(alignment);
        builder.append("; leftMargin = ").append(leftMargin);
        builder.append("; rightMargin = ").append(rightMargin);
        builder.append("; indent = ").append(indent);
        builder.append("; leading = ").append(leading);
        builder.append("; variableName = ").append(variableName);
        builder.append("; initalText = ").append(initialText);
        builder.append(" }");

        return builder.toString();
    }

    /** {@inheritDoc} */
    @SuppressWarnings("PMD.NPathComplexity")
    public int prepareToEncode(final Context context) {
        // CHECKSTYLE:OFF
        context.put(Context.TRANSPARENT, 1);

        length = 2 + bounds.prepareToEncode(context);
        length += 2;
        length += (fontIdentifier == 0) ? 0 : 4;
        length += fontClass == null ? 0 : context.strlen(fontClass) + 2;
        length += color == null ? 0 : 4;
        length += (maxLength > 0) ? 2 : 0;
        length += (containsLayout()) ? 9 : 0;
        length += context.strlen(variableName);
        length += (initialText == null) ? 0 : context.strlen(initialText);

        context.remove(Context.TRANSPARENT);

        return (length > Coder.HEADER_LIMIT ? Coder.LONG_HEADER
                : Coder.SHORT_HEADER) + length;
        // CHECKSTYLE:ON
    }

    /** {@inheritDoc} */
    @SuppressWarnings("PMD.CyclomaticComplexity")
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {

        if (length > Coder.HEADER_LIMIT) {
            coder.writeShort((MovieTypes.DEFINE_TEXT_FIELD
                    << Coder.LENGTH_FIELD_SIZE) | Coder.IS_EXTENDED);
            coder.writeInt(length);
        } else {
            coder.writeShort((MovieTypes.DEFINE_TEXT_FIELD
                    << Coder.LENGTH_FIELD_SIZE) | length);
        }
        if (Constants.DEBUG) {
            coder.mark();
        }
        context.put(Context.TRANSPARENT, 1);

        coder.writeShort(identifier);
        bounds.encode(coder, context);
        int bits = 0;
        bits |= initialText == null ? 0 : Coder.BIT7;
        bits |= wordWrapped ? Coder.BIT6 : 0;
        bits |= multiline ? Coder.BIT5 : 0;
        bits |= password ? Coder.BIT4 : 0;
        bits |= readOnly ? Coder.BIT3 : 0;
        bits |= color == null ? 0 : Coder.BIT2;
        bits |= maxLength > 0 ? Coder.BIT1 : 0;
        bits |= fontIdentifier == 0 ? 0 : Coder.BIT0;
        coder.writeByte(bits);

        bits = 0;
        bits |= fontClass == null ? 0 : Coder.BIT7;
        bits |= autoSize ? Coder.BIT6 : 0;
        bits |= containsLayout() ? Coder.BIT5 : 0;
        bits |= selectable ? Coder.BIT4 : 0;
        bits |= bordered ? Coder.BIT3 : 0;
        bits |= reserved2 ? Coder.BIT2 : 0;
        bits |= html ? Coder.BIT1 : 0;
        bits |= embedded ? Coder.BIT0 : 0;
        coder.writeByte(bits);

        if (fontIdentifier > 0) {
            coder.writeShort(fontIdentifier);
            coder.writeShort(fontHeight);
        } else if (fontClass != null) {
            coder.writeString(fontClass);
            coder.writeShort(fontHeight);
        }

        if (color != null)  {
            color.encode(coder, context);
        }

        if (maxLength > 0) {
            coder.writeShort(maxLength);
        }

        if (containsLayout()) {
            coder.writeByte(alignment);
            coder.writeShort(leftMargin == null ? 0 : leftMargin);
            coder.writeShort(rightMargin == null ? 0 : rightMargin);
            coder.writeShort(indent == null ? 0 : indent);
            coder.writeShort(leading == null ? 0 : leading);
        }

        coder.writeString(variableName);

        if (initialText != null) {
            coder.writeString(initialText);
        }
        context.remove(Context.TRANSPARENT);
        if (Constants.DEBUG) {
            coder.check(length);
            coder.unmark();
        }
    }

    /**
     * Indicates whether the field contains layout information.
     * @return if the field contains values that control the layout.
     */
    private boolean containsLayout() {
        return (leftMargin != null)
                || (rightMargin != null) || (indent != null)
                || (leading != null);
    }
}
