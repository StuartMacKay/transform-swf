/*
 * FilterDecoder.java
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

package com.flagstone.transform.filter;

import java.io.IOException;
import java.util.List;

import com.flagstone.transform.coder.CoderException;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFFactory;

/**
 * FilterDecoder is used to decode the Filter objects encoded in Place3 and
 * ButtonShape objects.
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
public class FilterDecoder implements SWFFactory<Filter> {
    /** {@inheritDoc} */
    public void getObject(final List<Filter> list, final SWFDecoder coder,
            final Context context) throws IOException {

        final int type = coder.readByte();
        Filter filter;

        switch (type) {
        case FilterTypes.DROP_SHADOW:
            filter = new DropShadowFilter(coder, context);
            break;
        case FilterTypes.BLUR:
            filter = new BlurFilter(coder);
            break;
        case FilterTypes.GLOW:
            filter = new GlowFilter(coder, context);
            break;
        case FilterTypes.BEVEL:
            filter = new BevelFilter(coder, context);
            break;
        case FilterTypes.GRADIENT_GLOW:
            filter = new GradientGlowFilter(coder, context);
            break;
        case FilterTypes.CONVOLUTION:
            filter = new ConvolutionFilter(coder, context);
            break;
        case FilterTypes.COLOR_MATRIX:
            filter = new ColorMatrixFilter(coder);
            break;
        case FilterTypes.GRADIENT_BEVEL:
            filter = new GradientBevelFilter(coder, context);
            break;
        default:
            throw new CoderException(coder.mark(),
                    "Unsupported Filter: " + type);
        }
        list.add(filter);
    }
}
