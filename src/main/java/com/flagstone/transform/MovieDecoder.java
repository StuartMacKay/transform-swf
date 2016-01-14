/*
 * MovieDecoder.java
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

package com.flagstone.transform;

import java.io.IOException;
import java.util.List;

import com.flagstone.transform.button.ButtonColorTransform;
import com.flagstone.transform.button.ButtonSound;
import com.flagstone.transform.button.DefineButton;
import com.flagstone.transform.button.DefineButton2;
import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.CoderException;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFFactory;
import com.flagstone.transform.font.DefineFont;
import com.flagstone.transform.font.DefineFont2;
import com.flagstone.transform.font.DefineFont3;
import com.flagstone.transform.font.DefineFont4;
import com.flagstone.transform.font.FontAlignment;
import com.flagstone.transform.font.FontInfo;
import com.flagstone.transform.font.FontInfo2;
import com.flagstone.transform.font.FontName;
import com.flagstone.transform.image.DefineImage;
import com.flagstone.transform.image.DefineImage2;
import com.flagstone.transform.image.DefineJPEGImage;
import com.flagstone.transform.image.DefineJPEGImage2;
import com.flagstone.transform.image.DefineJPEGImage3;
import com.flagstone.transform.image.DefineJPEGImage4;
import com.flagstone.transform.image.JPEGEncodingTable;
import com.flagstone.transform.movieclip.DefineMovieClip;
import com.flagstone.transform.movieclip.InitializeMovieClip;
import com.flagstone.transform.movieclip.QuicktimeMovie;
import com.flagstone.transform.shape.DefineMorphShape;
import com.flagstone.transform.shape.DefineMorphShape2;
import com.flagstone.transform.shape.DefineShape;
import com.flagstone.transform.shape.DefineShape2;
import com.flagstone.transform.shape.DefineShape3;
import com.flagstone.transform.shape.DefineShape4;
import com.flagstone.transform.shape.PathsArePostscript;
import com.flagstone.transform.sound.DefineSound;
import com.flagstone.transform.sound.SoundStreamBlock;
import com.flagstone.transform.sound.SoundStreamHead;
import com.flagstone.transform.sound.SoundStreamHead2;
import com.flagstone.transform.sound.StartSound;
import com.flagstone.transform.sound.StartSound2;
import com.flagstone.transform.text.DefineText;
import com.flagstone.transform.text.DefineText2;
import com.flagstone.transform.text.DefineTextField;
import com.flagstone.transform.text.TextSettings;
import com.flagstone.transform.video.DefineVideo;
import com.flagstone.transform.video.VideoFrame;

/**
 * MovieDecoder is used to decode the different types of data structure encoded
 * in a movie.
 */
@SuppressWarnings({"PMD.ExcessiveImports",
     "PMD.CyclomaticComplexity",
     "PMD.ExcessiveMethodLength",
     "PMD.NcssMethodCount" })
public class MovieDecoder implements SWFFactory<MovieTag> {
    /** {@inheritDoc} */
    @Override
	public void getObject(final List<MovieTag> list, final SWFDecoder coder,
            final Context context) throws IOException {

        MovieTag obj;

        switch (coder.scanUnsignedShort() >> Coder.LENGTH_FIELD_SIZE) {
        case MovieTypes.SHOW_FRAME:
            obj = ShowFrame.getInstance(coder, context);
            break;
        case MovieTypes.DEFINE_SHAPE:
            obj = new DefineShape(coder, context);
            break;
        case MovieTypes.PLACE:
            obj = new Place(coder, context);
            break;
        case MovieTypes.REMOVE:
            obj = new Remove(coder);
            break;
        case MovieTypes.DEFINE_JPEG_IMAGE:
            obj = new DefineJPEGImage(coder);
            break;
        case MovieTypes.DEFINE_BUTTON:
            obj = new DefineButton(coder, context);
            break;
        case MovieTypes.JPEG_TABLES:
            obj = new JPEGEncodingTable(coder);
            break;
        case MovieTypes.SET_BACKGROUND_COLOR:
            obj = new Background(coder, context);
            break;
        case MovieTypes.DEFINE_FONT:
            obj = new DefineFont(coder);
            break;
        case MovieTypes.DEFINE_TEXT:
            obj = new DefineText(coder, context);
            break;
        case MovieTypes.DO_ACTION:
            obj = new DoAction(coder, context);
            break;
        case MovieTypes.FONT_INFO:
            obj = new FontInfo(coder);
            break;
        case MovieTypes.DEFINE_SOUND:
            obj = new DefineSound(coder);
            break;
        case MovieTypes.START_SOUND:
            obj = new StartSound(coder);
            break;
        case MovieTypes.SOUND_STREAM_HEAD:
            obj = new SoundStreamHead(coder);
            break;
        case MovieTypes.SOUND_STREAM_BLOCK:
            obj = new SoundStreamBlock(coder);
            break;
        case MovieTypes.BUTTON_SOUND:
            obj = new ButtonSound(coder);
            break;
        case MovieTypes.DEFINE_IMAGE:
            obj = new DefineImage(coder);
            break;
        case MovieTypes.DEFINE_JPEG_IMAGE_2:
            obj = new DefineJPEGImage2(coder);
            break;
        case MovieTypes.DEFINE_SHAPE_2:
            obj = new DefineShape2(coder, context);
            break;
        case MovieTypes.BUTTON_COLOR_TRANSFORM:
            obj = new ButtonColorTransform(coder, context);
            break;
        case MovieTypes.PROTECT:
            obj = new Protect(coder);
            break;
        case MovieTypes.FREE:
            obj = new Free(coder);
            break;
        case MovieTypes.PLACE_2:
            obj = new Place2(coder, context);
            break;
        case MovieTypes.REMOVE_2:
            obj = new Remove2(coder);
            break;
        case MovieTypes.DEFINE_SHAPE_3:
            obj = new DefineShape3(coder, context);
            break;
        case MovieTypes.DEFINE_TEXT_2:
            obj = new DefineText2(coder, context);
            break;
        case MovieTypes.DEFINE_BUTTON_2:
            obj = new DefineButton2(coder, context);
            break;
        case MovieTypes.DEFINE_JPEG_IMAGE_3:
            obj = new DefineJPEGImage3(coder);
            break;
        case MovieTypes.DEFINE_IMAGE_2:
            obj = new DefineImage2(coder);
            break;
        case MovieTypes.DEFINE_MOVIE_CLIP:
            obj = new DefineMovieClip(coder, context);
            break;
        case MovieTypes.FRAME_LABEL:
            obj = new FrameLabel(coder);
            break;
        case MovieTypes.SOUND_STREAM_HEAD_2:
            obj = new SoundStreamHead2(coder);
            break;
        case MovieTypes.DEFINE_MORPH_SHAPE:
            obj = new DefineMorphShape(coder, context);
            break;
        case MovieTypes.DEFINE_FONT_2:
            obj = new DefineFont2(coder, context);
            break;
        case MovieTypes.PATHS_ARE_POSTSCRIPT:
            obj = PathsArePostscript.getInstance(coder, context);
            break;
        case MovieTypes.DEFINE_TEXT_FIELD:
            obj = new DefineTextField(coder, context);
            break;
        case MovieTypes.QUICKTIME_MOVIE:
            obj = new QuicktimeMovie(coder);
            break;
        case MovieTypes.SERIAL_NUMBER:
            obj = new SerialNumber(coder);
            break;
        case MovieTypes.ENABLE_DEBUGGER:
            obj = new EnableDebugger(coder);
            break;
        case MovieTypes.EXPORT:
            obj = new Export(coder);
            break;
        case MovieTypes.IMPORT:
            obj = new Import(coder);
            break;
        case MovieTypes.INITIALIZE:
            obj = new InitializeMovieClip(coder, context);
            break;
        case MovieTypes.DEFINE_VIDEO:
            obj = new DefineVideo(coder);
            break;
        case MovieTypes.VIDEO_FRAME:
            obj = new VideoFrame(coder);
            break;
        case MovieTypes.FONT_INFO_2:
            obj = new FontInfo2(coder);
            break;
        case MovieTypes.ENABLE_DEBUGGER_2:
            obj = new EnableDebugger2(coder);
            break;
        case MovieTypes.LIMIT_SCRIPT:
            obj = new LimitScript(coder);
            break;
        case MovieTypes.TAB_ORDER:
            obj = new TabOrder(coder);
            break;
        case MovieTypes.FILE_ATTRIBUTES:
            obj = new MovieAttributes(coder);
            break;
        case MovieTypes.PLACE_3:
            obj = new Place3(coder, context);
            break;
        case MovieTypes.IMPORT_2:
            obj = new Import2(coder);
            break;
        case MovieTypes.FONT_ALIGNMENT:
            obj = new FontAlignment(coder);
            break;
        case MovieTypes.TEXT_SETTINGS:
            obj = new TextSettings(coder);
            break;
        case MovieTypes.DEFINE_FONT_3:
            obj = new DefineFont3(coder, context);
            break;
        case MovieTypes.SYMBOL:
            obj = new SymbolClass(coder);
            break;
        case MovieTypes.METADATA:
            obj = new MovieMetaData(coder);
            break;
        case MovieTypes.DEFINE_SCALING_GRID:
            obj = new ScalingGrid(coder);
            break;
        case MovieTypes.SCENES_AND_LABELS:
            obj = new ScenesAndLabels(coder);
            break;
        case MovieTypes.DO_ABC:
            obj = new DoABC(coder);
            break;
        case MovieTypes.DEFINE_SHAPE_4:
            obj = new DefineShape4(coder, context);
            break;
        case MovieTypes.DEFINE_MORPH_SHAPE_2:
            obj = new DefineMorphShape2(coder, context);
            break;
        case MovieTypes.DEFINE_BINARY_DATA:
            obj = new DefineData(coder);
            break;
        case MovieTypes.FONT_NAME:
            obj = new FontName(coder);
            break;
        case MovieTypes.START_SOUND_2:
            obj = new StartSound2(coder);
            break;
        case MovieTypes.DEFINE_JPEG_IMAGE_4:
            obj = new DefineJPEGImage4(coder);
            break;
        case MovieTypes.DEFINE_FONT_4:
            obj = new DefineFont4(coder);
            break;
        default:
            obj = new MovieObject(coder);
            break;
        }

        list.add(obj);

        if (coder.getDelta() != 0) {
            throw new CoderException(coder.getLocation(),
                    coder.getExpected(), coder.getDelta());
        }
    }
}
