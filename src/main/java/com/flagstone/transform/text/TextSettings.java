/*
 * TextSettings.java
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

import com.flagstone.transform.MovieTag;
import com.flagstone.transform.MovieTypes;
import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.exception.IllegalArgumentRangeException;

/**
 * TextSettings allows you to control how individual text fields are rendered.
 *
 * <p>
 * There are four parameters that control how the text is rendered:
 * </p>
 * <ol>
 * <li>Advanced Rendering - whether the text is rendered using the advanced
 * anti-aliasing engine added in Flash 8.</li>
 * <li>Grid Alignment - how letters are aligned with respect to the pixel grid
 * used in LCD monitors.</li>
 * <li>Thickness - a parameter used to control the thickness of the line when
 * anti-aliasing is used.</li>
 * <li>Sharpness - a parameter used to control the sharpness of the line when
 * anti-aliasing is used.</li>
 * </ol>
 * <p>
 * The thickness and sharpness control the how the text is rendered:
 *
 * <pre>
 *    outsideCutoff = (0.5 * sharpness - thickness) * fontSize
 *    insideCutoff = (-0.5 * sharpness - thickness) * fontSize
 * </pre>
 *
 * Note that Adobe reports the results can be poor when the text is scaled by a
 * significant amount and so the default values of 0.0 should be used for the
 * thickness and sharpness values.
 * </p>
 */
public class TextSettings implements MovieTag {
    /**
     * Grid specifies how letters are aligned with respect to the pixel grid on
     * a screen.
     */
    public enum Grid {
        /** Do not use grid fitting. */
        NONE,
        /** Align letters on pixel boundaries. */
        PIXEL,
        /** Align letters on 1/3 pixel boundaries. */
        SUBPIXEL
    }

    /** Format string used in toString() method. */
    private static final String FORMAT = "TextSettings: { identifier=%d;"
            + " useAdvanced=%s; grid=%s; thickness=%f; sharpness=%f}";

    /** The unique identifier of the text field. */
    private transient int identifier;
    /** Compound code for the rendering settings. */
    private transient int rendering;
    /** Control for the thickness of the line. */
    private transient int thickness;
    /** Control for the sharpness of the line. */
    private transient int sharpness;

    /**
     * Creates and initialises an TextSettings using values encoded in the Flash
     * binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public TextSettings(final SWFDecoder coder) throws IOException {
        if ((coder.readUnsignedShort() & Coder.LENGTH_FIELD)
        		== Coder.IS_EXTENDED) {
            coder.readInt();
        }
        identifier = coder.readUnsignedShort();
        rendering = coder.readByte();
        thickness = coder.readInt();
        sharpness = coder.readInt();
        coder.readByte();
    }

    /**
     * Creates a TextSettings object with the specified values.
     *
     * @param uid
     *            the unique identifier of an existing text field.
     * @param advanced
     *            whether the advanced rendering engine will be used to display
     *            the text.
     * @param grid
     *            how letters are aligned with respect to the pixel grid.
     * @param thick
     *            the thickness used when anti-aliasing the text.
     * @param sharp
     *            the sharpness used when anti-aliasing the text.
     */
    public TextSettings(final int uid, final boolean advanced, final Grid grid,
            final float thick, final float sharp) {
        setIdentifier(uid);
        useAdvanced(advanced);
        setGrid(grid);
        setThickness(thick);
        setSharpness(sharp);
    }

    /**
     * Creates an TextSettings object and initialised it by copying the values
     * from an existing one.
     *
     * @param object
     *            a TextSettings object.
     */
    public TextSettings(final TextSettings object) {
        identifier = object.identifier;
        rendering = object.rendering;
        thickness = object.thickness;
        sharpness = object.sharpness;
    }

    /**
     * Get the unique identifier of the text definition that this object
     * applies to.
     *
     * @return the unique identifier of the text object.
     */
    public int getIdentifier() {
        return identifier;
    }

    /**
     * Sets the identifier of the text definition that this object applies to.
     *
     * @param uid
     *            the unique identifier of an DefineText, DefineText2 or
     *            DefineTextField object. Must be in the range 1..65535.
     */
    public void setIdentifier(final int uid) {
        if ((uid < 1) || (uid > Coder.USHORT_MAX)) {
            throw new IllegalArgumentRangeException(
                    1, Coder.USHORT_MAX, uid);
        }
        identifier = uid;
    }

    /**
     * Will the advanced text rendering engine, introduced in Flash 8
     * be used.
     *
     * @return true if advanced text rendering is used, false if the standard
     * rendering engine is used.
     */
    public boolean useAdvanced() {
        return (rendering & Coder.BIT6) != 0;
    }

    /**
     * Sets whether the advanced text rendering engine (true) or standard engine
     * (false) will be used to render the text.
     *
     * @param flag set true to select the advanced text rendering engine, false
     * for the standard rendering engine.
     */
    public void useAdvanced(final boolean flag) {
        rendering |= Coder.BIT6;
    }

    /**
     * Returns the alignment of letters with respect to the pixel grid.
     *
     * @return the alignment, either NONE, PIXEL or SUBPIXEL.
     */
    public Grid getGrid() {
        Grid alignment;

        if ((rendering & Coder.BIT4) > 0) {
            alignment = Grid.SUBPIXEL;
        } else if ((rendering & Coder.BIT3) > 0) {
            alignment = Grid.PIXEL;
        } else {
            alignment = Grid.NONE;
        }
        return alignment;
    }

    /**
     * Selects how the text letters will be aligned with respect to the pixel
     * grid used in LCD screens.
     *
     * @param alignment
     *            the alignment with respect to the pixel grid, either NONE,
     *            PIXEL or SUBPIXEL.
     */
    public void setGrid(final Grid alignment) {

        rendering &= ~(Coder.BIT3 | Coder.BIT4 | Coder.BIT5 | Coder.BIT6);

        switch (alignment) {
        case PIXEL:
            rendering |= Coder.BIT3;
            break;
        case SUBPIXEL:
            rendering |= Coder.BIT4;
            break;
        default:
            break;
        }
    }

    /**
     * Get the value used to control the thickness of a line when rendered.
     * May be set to 0.0 if the default anti-aliasing value will be used.
     *
     * @return the adjustment applied to the line thickness.
     */
    public float getThickness() {
        return Float.intBitsToFloat(thickness);
    }

    /**
     * Sets the value used to control the thickness of a line when rendered. May
     * be set to 0.0 if the default anti-aliasing value will be used.
     *
     * @param level
     *            the value of the thickness parameter used by the rendering
     *            engine.
     */
    public void setThickness(final float level) {
        thickness = Float.floatToIntBits(level);
    }

    /**
     * Get the value used to control the sharpness of a line when rendered.
     * May be set to 0.0 if the default anti-aliasing value will be used.
     *
     * @return the adjustment applied to the line sharpness.
     */
    public float getSharpness() {
        return Float.intBitsToFloat(sharpness);
    }

    /**
     * Sets the value used to control the sharpness of a line when rendered. May
     * be set to 0.0 if the default anti-aliasing value will be used.
     *
     * @param level
     *            the value of the sharpness parameter used by the rendering
     *            engine.
     */
    public void setSharpness(final float level) {
        this.sharpness = Float.floatToIntBits(level);
    }

    /** {@inheritDoc} */
    public TextSettings copy() {
        return new TextSettings(this);
    }

    @Override
    public String toString() {
        return String.format(FORMAT, identifier, String.valueOf(useAdvanced()),
                getGrid(), thickness / Coder.SCALE_16,
                sharpness / Coder.SCALE_16);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 1 LINES
        return 14;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 2 LINES
        coder.writeShort((MovieTypes.TEXT_SETTINGS << Coder.LENGTH_FIELD_SIZE)
                | 12);
        coder.writeShort(identifier);
        coder.writeByte(rendering);
        coder.writeInt(thickness);
        coder.writeInt(sharpness);
        coder.writeByte(0);
    }
}
