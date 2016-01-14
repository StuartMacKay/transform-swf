/*
 * Property.java
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
package com.flagstone.transform.action;

/**
 * Property defines the set of attributes that can accessed for movies and movie
 * clips when executing actions.
 *
 * @see Push
 */
public class Property {

    /** Format string used in toString() method. */
    private static final String FORMAT = "Property: { value=%d}";
    /** The first version of Flash that uses integers values for properties. */
    public static final int VERSION_WITH_INTS = 5;

    /** Value used to identify an X Property when it is encoded. */
    private static final int XCOORD_VAL = 0;
    /** Value used to identify an Y Property when it is encoded. */
    private static final int YCOORD_VAL = 1;
    /** Value used to identify an XSCALE Property when it is encoded. */
    private static final int XSCALE_VAL = 2;
    /** Value used to identify a YSCALE Property when it is encoded. */
    private static final int YSCALE_VAL = 3;
    /** Value used to identify a CURRENT_FRAME Property when it is encoded. */
    private static final int CUR_FRAME_VAL = 4;
    /** Value used to identify a TOTAL_FRAMES Property when it is encoded. */
    private static final int TOT_FRAMES_VAL = 5;
    /** Value used to identify a ALPHA Property when it is encoded. */
    private static final int ALPHA_VAL = 6;
    /** Value used to identify a VISIBLE Property when it is encoded. */
    private static final int VISIBLE_VAL = 7;
    /** Value used to identify a WIDTH Property when it is encoded. */
    private static final int WIDTH_VAL = 8;
    /** Value used to identify a HEIGHT Property when it is encoded. */
    private static final int HEIGHT_VAL = 9;
    /** Value used to identify a ROTATION Property when it is encoded. */
    private static final int ROTATION_VAL = 10;
    /** Value used to identify a TARGET Property when it is encoded. */
    private static final int TARGET_VAL = 11;
    /** Value used to identify a FRAMES_LOADED Property when it is encoded. */
    private static final int LOADED_VAL = 12;
    /** Value used to identify a NAME Property when it is encoded. */
    private static final int NAME_VAL = 13;
    /** Value used to identify a DROP_TARGET Property when it is encoded. */
    private static final int DROP_TARGET_VAL = 14;
    /** Value used to identify a URL Property when it is encoded. */
    private static final int URL_VAL = 15;
    /** Value used to identify a HIGH_QUALITY Property when it is encoded. */
    private static final int HI_QUALITY_VAL = 16;
    /** Value used to identify a FOCUS_RECT Property when it is encoded. */
    private static final int FOCUS_RECT_VAL = 17;
    /** Value used to identify a SOUND_BUF_TIME Property when it is encoded. */
    private static final int SOUND_TIME_VAL = 18;
    /** Value used to identify a QUALITY Property when it is encoded. */
    private static final int QUALITY_VAL = 19;
    /** Value used to identify a XMOUSE Property when it is encoded. */
    private static final int XMOUSE_VAL = 20;
    /** Value used to identify YMOUSE X Property when it is encoded. */
    private static final int YMOUSE_VAL = 21;

    /**
     * The x-origin of the movie clip relative to the parent clip. This is
     * equivalent to the _x property in actionscript.
     */
    public static final Property XCOORD = new Property(XCOORD_VAL);
    /**
     * The y-origin of the movie clip relative to the parent clip. This is
     * equivalent to the _y property in actionscript.
     */
    public static final Property YCOORD = new Property(YCOORD_VAL);
    /**
     * The scaling factor of the movie clip in the x direction. This is
     * equivalent to the _xscale property in actionscript.
     */
    public static final Property XSCALE = new Property(XSCALE_VAL);
    /**
     * The scaling factor of the movie clip in the x direction. This is
     * equivalent to the _yscale property in actionscript.
     */
    public static final Property YSCALE = new Property(YSCALE_VAL);
    /**
     * The number of the current frame playing in the movie clip. This is
     * equivalent to the _currentframe property in actionscript.
     */
    public static final Property CURRENT_FRAME = new Property(CUR_FRAME_VAL);
    /**
     * The total number of frames in the movie clip. This is equivalent to the
     * _totalframes property in actionscript.
     */
    public static final Property TOTAL_FRAMES = new Property(TOT_FRAMES_VAL);
    /**
     * The transparency of the movie clip. This is equivalent to the _alpha
     * property in actionscript.
     */
    public static final Property ALPHA = new Property(ALPHA_VAL);
    /**
     * Whether the movie clip is visible. This is equivalent to the _visible
     * property in actionscript.
     */
    public static final Property VISIBLE = new Property(VISIBLE_VAL);
    /**
     * The width of the movie clip in pixels. This is equivalent to the _width
     * property in actionscript.
     */
    public static final Property WIDTH = new Property(WIDTH_VAL);
    /**
     * The height of the movie clip in pixels. This is equivalent to the _height
     * property in actionscript.
     */
    public static final Property HEIGHT = new Property(HEIGHT_VAL);
    /**
     * The angle of rotation of the movie clip in degrees. This is equivalent to
     * the _height property in actionscript.
     */
    public static final Property ROTATION = new Property(ROTATION_VAL);
    /**
     * The path of the movie clip relative to the root movie in the Player. This
     * is equivalent to the _rotation property in actionscript.
     */
    public static final Property TARGET = new Property(TARGET_VAL);
    /**
     * The number of frames form the movie clip loaded. This is equivalent to
     * the _framesloaded property in actionscript.
     */
    public static final Property FRAMES_LOADED = new Property(LOADED_VAL);
    /**
     * The name of movie clip. This is equivalent to the _name property in
     * actionscript.
     */
    public static final Property NAME = new Property(NAME_VAL);
    /**
     * The name of the movie clip currently being dragged. This is equivalent to
     * the _target property in actionscript.
     */
    public static final Property DROP_TARGET = new Property(DROP_TARGET_VAL);
    /**
     * The URL from which the movie clip was loaded. This is equivalent to the
     * _url property in actionscript.
     */
    public static final Property URL = new Property(URL_VAL);
    /**
     * Identifies the level of aliasing being performed by the Player. This is
     * equivalent to the _highquality property in actionscript.
     */
    public static final Property HIGH_QUALITY = new Property(HI_QUALITY_VAL);
    /**
     * Identifies whether a rectangle is drawn around a button or text field
     * that has the current focus This is equivalent to the _focusrect property
     * in actionscript. .
     */
    public static final Property FOCUS_RECT = new Property(FOCUS_RECT_VAL);
    /**
     * The amount of time streaming sound is buffered by the Player before
     * playing. This is equivalent to the _soundbuftime property in
     * actionscript.
     */
    public static final Property SOUND_BUF_TIME = new Property(SOUND_TIME_VAL);
    /**
     * Identifies the level of rendering quality being performed by the Player.
     * This is equivalent to the _quality property in actionscript.
     */
    public static final Property QUALITY = new Property(QUALITY_VAL);
    /**
     * The current x-coordinate of the mouse pointer on the Player screen. This
     * is equivalent to the _xmouse property in actionscript.
     */
    public static final Property XMOUSE = new Property(XMOUSE_VAL);
    /**
     * The current y-coordinate of the mouse pointer on the Player screen. This
     * is equivalent to the _ymouse property in actionscript.
     */
    public static final Property YMOUSE = new Property(YMOUSE_VAL);

    /** Encoded Property value. */
    private final transient int value;

    /**
     * Creates a Property object with the specified value.
     *
     * @param pval
     *            the value for the property.
     */
    public Property(final int pval) {
        value = pval;
    }

    /**
     * Get value used to encode the Property.
     *
     * @return the encoded property value.
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns value of the Property as it would be written to a Flash file.
     * For Flash version 4 and earlier Properties were encoded as floating
     * point values while for Flash 5 and later they are encoded as integers.
     *
     * @param version the Flash version that the property will be encoded for.
     *
     * @return the value the Property will be encoded as.
     */
    public int getValue(final int version) {
        int val;
        if (version < VERSION_WITH_INTS) {
            val = Float.floatToIntBits(value);
        } else {
            val = value;
        }
        return val;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, value);
    }
}
