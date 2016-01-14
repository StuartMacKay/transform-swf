/*
 * MorphFillStyleDecoder.java
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

package com.flagstone.transform.fillstyle;


import java.io.IOException;
import java.util.List;

import com.flagstone.transform.coder.CoderException;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFFactory;

/**
 * MorphFillStyleDecoder is used to decode the different types of fill styles
 * used in morphing shapes.
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
public class MorphFillStyleDecoder implements SWFFactory<FillStyle> {
    /** {@inheritDoc} */
    public void getObject(final List<FillStyle> list, final SWFDecoder coder,
            final Context context) throws IOException {

        final int type = coder.readByte();
        FillStyle style;

        switch (type) {
        case FillStyleTypes.SOLID_COLOR:
            style = new MorphSolidFill(coder, context);
            break;
        case FillStyleTypes.LINEAR_GRADIENT:
            style = new MorphGradientFill(type, coder, context);
            break;
        case FillStyleTypes.RADIAL_GRADIENT:
            style = new MorphGradientFill(type, coder, context);
            break;
        case FillStyleTypes.FOCAL_GRADIENT:
            style = new MorphFocalGradientFill(coder, context);
            break;
        case FillStyleTypes.TILED_BITMAP:
            style = new MorphBitmapFill(type, coder);
            break;
        case FillStyleTypes.CLIPPED_BITMAP:
            style = new MorphBitmapFill(type, coder);
            break;
        case FillStyleTypes.UNSMOOTH_TILED:
            style = new MorphBitmapFill(type, coder);
            break;
        case FillStyleTypes.UNSMOOTH_CLIPPED:
            style = new MorphBitmapFill(type, coder);
            break;
        default:
            throw new CoderException(coder.mark(),
                    "Unsupported MorphFillStyle: " + type);
        }
        list.add(style);
    }
}
