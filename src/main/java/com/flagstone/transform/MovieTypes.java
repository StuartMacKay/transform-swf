/*
 * MovieTypes.java
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

package com.flagstone.transform;

/**
 * MovieTypes defines the constants that identify a MovieTag when it is encoded
 * according to the Flash file format specification.
 */
@SuppressWarnings("PMD.LongVariable")
public class MovieTypes {
    /** Marker for the end of a Movie. */
    public static final int END = 0;
    /** Identifies ShowFrame objects when they are encoded. */
    public static final int SHOW_FRAME = 1;
    /** Identifies DefineShape objects when they are encoded. */
    public static final int DEFINE_SHAPE = 2;
    /** Identifies Free objects when they are encoded. */
    public static final int FREE = 3;
    /** Identifies Place objects when they are encoded. */
    public static final int PLACE = 4;
    /** Identifies Remove objects when they are encoded. */
    public static final int REMOVE = 5;
    /** Identifies DefineJPEGImage objects when they are encoded. */
    public static final int DEFINE_JPEG_IMAGE = 6;
    /** Identifies DefineButton objects when they are encoded. */
    public static final int DEFINE_BUTTON = 7;
    /** Identifies JPEGTables objects when they are encoded. */
    public static final int JPEG_TABLES = 8;
    /** Identifies SetBackgroundColor objects when they are encoded. */
    public static final int SET_BACKGROUND_COLOR = 9;
    /** Identifies DefineFont objects when they are encoded. */
    public static final int DEFINE_FONT = 10;
    /** Identifies DefineText objects when they are encoded. */
    public static final int DEFINE_TEXT = 11;
    /** Identifies DoAction objects when they are encoded. */
    public static final int DO_ACTION = 12;
    /** Identifies FontInfo objects when they are encoded. */
    public static final int FONT_INFO = 13;
    /** Identifies DefineSound objects when they are encoded. */
    public static final int DEFINE_SOUND = 14;
    /** Identifies StartSound objects when they are encoded. */
    public static final int START_SOUND = 15;
    /** Identifies ButtonSound objects when they are encoded. */
    public static final int BUTTON_SOUND = 17;
    /** Identifies SoundStreamHead objects when they are encoded. */
    public static final int SOUND_STREAM_HEAD = 18;
    /** Identifies SoundStreamBlock objects when they are encoded. */
    public static final int SOUND_STREAM_BLOCK = 19;
    /** Identifies DefineImage objects when they are encoded. */
    public static final int DEFINE_IMAGE = 20;
    /** Identifies DefineJPEGImage2 objects when they are encoded. */
    public static final int DEFINE_JPEG_IMAGE_2 = 21;
    /** Identifies DefineShape2 objects when they are encoded. */
    public static final int DEFINE_SHAPE_2 = 22;
    /** Identifies ButtonColorTransform objects when they are encoded. */
    public static final int BUTTON_COLOR_TRANSFORM = 23;
    /** Identifies Protect objects when they are encoded. */
    public static final int PROTECT = 24;
    /** Identifies PathsArePostscript objects when they are encoded. */
    public static final int PATHS_ARE_POSTSCRIPT = 25;
    /** Identifies Place2 objects when they are encoded. */
    public static final int PLACE_2 = 26;
    /** Identifies Remove2 objects when they are encoded. */
    public static final int REMOVE_2 = 28;
    /** Identifies DefineShape3 objects when they are encoded. */
    public static final int DEFINE_SHAPE_3 = 32;
    /** Identifies DefineText2 objects when they are encoded. */
    public static final int DEFINE_TEXT_2 = 33;
    /** Identifies DefineButton2 objects when they are encoded. */
    public static final int DEFINE_BUTTON_2 = 34;
    /** Identifies DefineJPEGImage3 objects when they are encoded. */
    public static final int DEFINE_JPEG_IMAGE_3 = 35;
    /** Identifies DefineImage2 objects when they are encoded. */
    public static final int DEFINE_IMAGE_2 = 36;
    /** Identifies DefineTextField objects when they are encoded. */
    public static final int DEFINE_TEXT_FIELD = 37;
    /** Identifies QuicktimeMovie objects when they are encoded. */
    public static final int QUICKTIME_MOVIE = 38;
    /** Identifies DefineMovieClip objects when they are encoded. */
    public static final int DEFINE_MOVIE_CLIP = 39;
    /** Identifies SerialNumber objects when they are encoded. */
    public static final int SERIAL_NUMBER = 41;
    /** Identifies FrameLabel objects when they are encoded. */
    public static final int FRAME_LABEL = 43;
    /** Identifies SoundStreamHead2 objects when they are encoded. */
    public static final int SOUND_STREAM_HEAD_2 = 45;
    /** Identifies DefineMorphShape objects when they are encoded. */
    public static final int DEFINE_MORPH_SHAPE = 46;
    /** Identifies DefineFont2 objects when they are encoded. */
    public static final int DEFINE_FONT_2 = 48;
    /** Identifies Export objects when they are encoded. */
    public static final int EXPORT = 56;
    /** Identifies Import objects when they are encoded. */
    public static final int IMPORT = 57;
    /** Identifies EnableDebugger objects when they are encoded. */
    public static final int ENABLE_DEBUGGER = 58;
    /** Identifies Initialise objects when they are encoded. */
    public static final int INITIALIZE = 59;
    /** Identifies DefineVideo objects when they are encoded. */
    public static final int DEFINE_VIDEO = 60;
    /** Identifies VideoFrame objects when they are encoded. */
    public static final int VIDEO_FRAME = 61;
    /** Identifies FontInfo2 objects when they are encoded. */
    public static final int FONT_INFO_2 = 62;
    /** Identifies EnableDebugger2 objects when they are encoded. */
    public static final int ENABLE_DEBUGGER_2 = 64;
    /** Identifies LimitScript objects when they are encoded. */
    public static final int LIMIT_SCRIPT = 65;
    /** Identifies TabOrder objects when they are encoded. */
    public static final int TAB_ORDER = 66;
    /** Identifies FileAttributes objects when they are encoded. */
    public static final int FILE_ATTRIBUTES = 69;
    /** Identifies Place3 objects when they are encoded. */
    public static final int PLACE_3 = 70;
    /** Identifies Import2 objects when they are encoded. */
    public static final int IMPORT_2 = 71;
    /** Identifies FontAlignment objects. */
    public static final int FONT_ALIGNMENT = 73;
    /** Identifies TextSettings objects. */
    public static final int TEXT_SETTINGS = 74;
    /** Identifies DefineFont3 objects when they are encoded. */
    public static final int DEFINE_FONT_3 = 75;
    /** Identifies SymbolClass objects when they are encoded. */
    public static final int SYMBOL = 76;
    /** Identifies MovieMetaData objects when they are encoded. */
    public static final int METADATA = 77;
    /** Identifies ScalingGrid objects when they are encoded. */
    public static final int DEFINE_SCALING_GRID = 78;
    /** Identifies DoABC objects when they are encoded. */
    public static final int DO_ABC = 82;
    /** Identifies DefineShape4 objects when they are encoded. */
    public static final int DEFINE_SHAPE_4 = 83;
    /** Identifies DefineMorphShape2 objects when they are encoded. */
    public static final int DEFINE_MORPH_SHAPE_2 = 84;
    /** Identifies ScenesAndLabels objects when they are encoded. */
    public static final int SCENES_AND_LABELS = 86;
    /** Identifies DefineData objects when they are encoded. */
    public static final int DEFINE_BINARY_DATA = 87;
    /** Identifies FontName objects when they are encoded. */
    public static final int FONT_NAME = 88;
    /** Identifies StartSound2 objects when they are encoded. */
    public static final int START_SOUND_2 = 89;
    /** Identifies DefineJPEGImage4 objects when they are encoded. */
    public static final int DEFINE_JPEG_IMAGE_4 = 90;
    /** Identifies DefineFont4 objects when they are encoded. */
    public static final int DEFINE_FONT_4 = 91;

    /**
     * Private constructor.
     */
    private MovieTypes() {
        // Class only contains constants
    }
}
