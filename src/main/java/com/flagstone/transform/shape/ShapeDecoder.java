/*
 * ShapeDecoder.java
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

package com.flagstone.transform.shape;

import java.io.IOException;
import java.util.List;

import com.flagstone.transform.MovieTypes;
import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFFactory;

/**
 * ShapeDecoder is used to decode the ShapeRecords that describe how a
 * Shape is drawn.
 */
public class ShapeDecoder implements SWFFactory<ShapeRecord> {
    /** {@inheritDoc} */
    public void getObject(final List<ShapeRecord> list, final SWFDecoder coder,
            final Context context) throws IOException {

        final int type = coder.readBits(2, false);
        ShapeRecord record = null;

        if (type == Coder.BIT1) {
            record = new Curve(coder);
        } else if (type == (Coder.BIT0 | Coder.BIT1)) {
            record = new Line(coder);
        } else {
            // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 2 LINES
            final int flags = (type << Coder.TO_UPPER_NIB)
                    + coder.readBits(4, false);

            final int tag = context.get(Context.TYPE);
            if (tag == MovieTypes.DEFINE_SHAPE_4
                    || tag == MovieTypes.DEFINE_MORPH_SHAPE_2) {
                record = new ShapeStyle2(flags, coder, context);
            } else {
                record = new ShapeStyle(flags, coder, context);
            }
        }
       list.add(record);
    }
}
