/*
 * PathsArePostscript.java
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

import com.flagstone.transform.MovieTag;
import com.flagstone.transform.MovieTypes;
import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;

/**
 * The PathsArePostscript class is used to notify the Flash Player that the
 * glyphs encoded in a font definition were derived from a PostScript-based font
 * definition.
 *
 * <p>
 * The PathsArePostscript is not documented in the current Macromedia Flash
 * (SWF) File Format Specification. It was referenced in earlier editions but
 * its exact function was not known. It is thought that is used to signal to the
 * Flash Player that the paths describing the outlines of the glyphs in a font
 * were derived from a font defined using Postscript. The information can be
 * used to provide better rendering of the glyphs.
 * </P>
 *
 */
public class PathsArePostscript implements MovieTag {

    /** Format string used in toString() method. */
    private static final String FORMAT = "PathsArePostscript";
    /** Singleton. */
    private static final PathsArePostscript INSTANCE = new PathsArePostscript();

    /**
     * Returns a singleton.
     *
     * @return an object that can safely be shared among objects.
     */
    public static PathsArePostscript getInstance() {
        return INSTANCE;
    }

    /**
     * Returns a singleton.
     *
     * @param coder
     *            an SWFEncoder object.
     *
     * @param context a Context object used to obtain the total number of
     * frames in a movie.
     *
     * @return an object that can safely be shared among objects.
     *
     * @throws IOException if an error occurs while encoding the object.
     */
    public static PathsArePostscript getInstance(final SWFDecoder coder,
            final Context context) throws IOException {
        context.put(Context.POSTSCRIPT, 1);
        coder.readUnsignedShort();
        return INSTANCE;
    }

    /** Private constructor used to create singleton.  */
    private PathsArePostscript() {
    }

    /** {@inheritDoc} */
    public PathsArePostscript copy() {
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return FORMAT;
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        context.put(Context.POSTSCRIPT, 1);
        return 2;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        coder.writeShort(MovieTypes.PATHS_ARE_POSTSCRIPT
                << Coder.LENGTH_FIELD_SIZE);
   }
}
