/*
 * AWTDecoder.java
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

import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;

import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.datatype.Bounds;
import com.flagstone.transform.font.CharacterFormat;
import com.flagstone.transform.shape.Shape;
import com.flagstone.transform.util.shape.Canvas;

/**
 * AWTDecoder decodes Java AWT Fonts so they can be used in a Flash file.
 */
public class AWTDecoder {
    /** Number of edge points from a PathIterator segment. */
    private static final int SEGMENT_COUNT = 6;
    /** x-coordinate of the end point of a move or line. */
    private static final int XCOORD = 0;
    /** y-coordinate of the end point of a move or line. */
    private static final int YCOORD = 1;
    /** x-coordinate of the control point for a quadratic curve. */
    private static final int QUAD_CTRLX = 0;
    /** y-coordinate of the control point for a quadratic curve. */
    private static final int QUAD_CTRLY = 1;
    /** x-coordinate of the anchor point for a quadratic curve. */
    private static final int QUAD_ANCHORX = 2;
    /** y-coordinate of the anchor point for a quadratic curve. */
    private static final int QUAD_ANCHORY = 3;
    /** x-coordinate of the first control point for a cubic curve. */
    private static final int CUBE_CTRL1_X = 0;
    /** y-coordinate of the first control point for a cubic curve. */
    private static final int CUBE_CTRL1_Y = 1;
    /** x-coordinate of the second control point for a cubic curve. */
    private static final int CUBE_CTRL2_X = 2;
    /** y-coordinate of the second control point for a cubic curve. */
    private static final int CUBE_CTRL2_Y = 3;
    /** x-coordinate of the anchor point for a cubic curve. */
    private static final int CUBE_ANCHORX = 4;
    /** y-coordinate of the anchor point for a cubic curve. */
    private static final int CUBE_ANCHORY = 5;
    /** Size of the EM-Square in twips. */
    private static final float EM_SQUARE_SIZE = 1024.0f;

    /** The list of fonts decoded. */
    private final transient List<Font>fonts = new ArrayList<Font>();

    /**
     * Decode an AWT Font.
     * @param font an AWT Font object.
     * @throws IOException if an error occurs decoding the font data.
     * @throws DataFormatException if the font is in a format not supported by
     * the decoder.
     */
    public void read(final java.awt.Font font)
            throws IOException, DataFormatException {
        decode(font);
    }

    /**
     * Get the list of fonts decoded.
     * @return a list of fonts.
     */
    public List<Font> getFonts() {
        return fonts;
    }

    /**
     * Decode the AWT font.
     * @param aFont an AWT Font.
     */
    private void decode(final java.awt.Font aFont) {

        final FontRenderContext fontContext = new FontRenderContext(
                new AffineTransform(), true, true);
        java.awt.Font awtFont = aFont.deriveFont(1.0f);

        final Font font = new Font();

        font.setFace(new FontFace(awtFont.getName(),
                awtFont.isBold(), awtFont.isItalic()));
        font.setEncoding(CharacterFormat.UCS2);

        /*
         * The new font scaled to the EM Square must be derived using the size
         * as well as the transform used for the glyphs otherwise the advance
         * values are not scaled accordingly.
         */
        final AffineTransform affine = AffineTransform.getTranslateInstance(
                0, 0);

        awtFont = awtFont.deriveFont(affine);
        awtFont = awtFont.deriveFont(EM_SQUARE_SIZE);

        final int missingGlyph = awtFont.getMissingGlyphCode();
        final int count = awtFont.getNumGlyphs();

        font.setMissingGlyph(missingGlyph);
        font.setNumberOfGlyphs(count);
        font.setHighestChar((char) Coder.USHORT_MAX);

        int index = 0;
        int code = 0;
        char character;

        // create the glyph for the characters that cannot be displayed

        GlyphVector glyphVector = awtFont.createGlyphVector(fontContext,
                new int[] {missingGlyph});
        java.awt.Shape outline = glyphVector.getGlyphOutline(0);
        int advance = (int) (glyphVector.getGlyphMetrics(0).getAdvance());

        font.addGlyph((char) missingGlyph, new Glyph(convertShape(outline),
                new Bounds(0, 0, 0, 0), advance));

        index = 1;

        float ascent = 0.0f;
        float descent = 0.0f;
        float leading = 0.0f;

        /*
         * Run through all the unicode character codes looking for a
         * corresponding glyph.
         */
        while ((index < count) && (code < Coder.USHORT_MAX)) {
            if (awtFont.canDisplay(code)) {
                character = (char) code;

                glyphVector = awtFont.createGlyphVector(fontContext,
                        new char[] {character});

                outline = glyphVector.getGlyphOutline(0);
                advance = (int) (glyphVector.getGlyphMetrics(0).getAdvance());

                font.addGlyph(character,  new Glyph(convertShape(outline),
                        new Bounds(0, 0, 0, 0), advance));

                if (!awtFont.hasUniformLineMetrics()) {
                    final LineMetrics lineMetrics = awtFont.getLineMetrics(
                            new char[] {character}, 0, 1, fontContext);

                    ascent = Math.max(lineMetrics.getAscent(), ascent);
                    descent = Math.max(lineMetrics.getDescent(), descent);
                    leading = Math.max(lineMetrics.getLeading(), leading);
                }
                index++;

            } else {
                character = (char) missingGlyph;
                font.addMissingGlyph((char) code);
            }
            code++;
        }
        font.setAscent((int) ascent);
        font.setDescent((int) descent);
        font.setLeading((int) leading);

        fonts.add(font);
    }

    /**
     * Trace the outline of the glyph.
     * @param glyph an AWT Shape.
     * @return a Flash Shape.
     */
    private Shape convertShape(final java.awt.Shape glyph) {
        final PathIterator pathIter = glyph.getPathIterator(null);
        final Canvas path = new Canvas();

        final double[] coords = new double[SEGMENT_COUNT];

        while (!pathIter.isDone()) {
            switch (pathIter.currentSegment(coords)) {
            case PathIterator.SEG_MOVETO:
                path.close();
                path.moveForFont((int) coords[XCOORD], (int) coords[YCOORD]);
                break;
            case PathIterator.SEG_LINETO:
                path.line((int) coords[XCOORD], (int) coords[YCOORD]);
                break;
            case PathIterator.SEG_QUADTO:
                path.curve((int) coords[QUAD_CTRLX], (int) coords[QUAD_CTRLY],
                        (int) coords[QUAD_ANCHORX], (int) coords[QUAD_ANCHORY]);
                break;
            case PathIterator.SEG_CUBICTO:
                path.curve(
                        (int) coords[CUBE_CTRL1_X], (int) coords[CUBE_CTRL1_Y],
                        (int) coords[CUBE_CTRL2_X], (int) coords[CUBE_CTRL2_Y],
                        (int) coords[CUBE_ANCHORX], (int) coords[CUBE_ANCHORY]);
                break;
            case PathIterator.SEG_CLOSE:
                path.close();
                break;
            default:
                break;
            }
            pathIter.next();
        }
        return path.getShape();
    }
}
