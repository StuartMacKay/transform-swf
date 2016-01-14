/*
 * FontFace.java
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

package com.flagstone.transform.util.font;

import com.flagstone.transform.Constants;

/**
 * FontFace is a convenience class that can be used to create tables of
 * fonts for use in an application.
 */
public class FontFace {
    /** The family name of the font. */
    private final transient String name;
    /** Is the font bold. */
    private final transient boolean bold;
    /** Is the font italicised. */
    private final transient boolean italic;

    /**
     * Create a new font face.
     *
     * @param fontName the family name of the font.
     * @param isBold is the font bold.
     * @param isItalic is the font italicised.
     */
    public FontFace(final String fontName,
            final boolean isBold, final boolean isItalic) {
        name = fontName;
        bold = isBold;
        italic = isItalic;
    }

    /**
     * Create a new font face.
     *
     * @param fontName the family name of the font.
     * @param style a java.awt.Font constant describing whether the font is
     * normal, bold and/or italicised.
     */
    public FontFace(final String fontName, final int style) {
        name = fontName;
        bold = (style & java.awt.Font.BOLD) != 0;
        italic = (style & java.awt.Font.ITALIC) != 0;
    }

    /**
     * Get the (family) name of the font.
     * @return the family name of the font, e.g. Arial
     */
    public String getName() {
        return name;
    }

    /**
     * Is the font bold or normal.
     * @return true if the font is bold, false if the font has normal weight.
     */
    public boolean isBold() {
        return bold;
    }

    /**
     * Is the font italicised or normal.
     *
     * @return true if the font is italicised, false if the font has normal
     * weight.
     */
    public boolean isItalic() {
        return italic;
    }

    /**
     * Get the font style, as defined in the AWT Font class, either Font.PLAIN
     * or a combination of Font.BOLD and Font.ITALIC.
     *
     * @return the java.awt.Font constant defining the font style.
     */
    public int getStyle() {
        int style = java.awt.Font.PLAIN;

        if (bold) {
            style |= java.awt.Font.BOLD;
        } else if (italic) {
            style |= java.awt.Font.ITALIC;
        }

        return style;
    }

    /**
     * Get name of the font face which contains the name of the font
     * followed by "Bold" for bold fonts and "Italic" for fonts with an italic
     * style.
     *
     * @return a human readable string describing the font.
     */
    @Override
    public String toString() {
        return name + (bold ? " Bold" : "") + (italic ? " Italic" : "");
    }

    @Override
    public boolean equals(final Object object) {
        boolean result;
        FontFace key;

        if (object == null) {
            result = false;
        } else if (object == this) {
            result = true;
        } else if (object instanceof FontFace) {
            key = (FontFace) object;
            result = name.equals(key.name) && bold == key.bold
                    && italic == key.italic;
        } else {
            result = false;
        }
        return result;
    }

    @Override
    public int hashCode() {
        return (name.hashCode() * Constants.PRIME
                + Boolean.valueOf(bold).hashCode()) * Constants.PRIME
                + Boolean.valueOf(italic).hashCode();
    }
}
