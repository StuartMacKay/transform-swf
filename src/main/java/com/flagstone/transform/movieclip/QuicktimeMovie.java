/*
 * QuicktimeMovie.java
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

package com.flagstone.transform.movieclip;


import java.io.IOException;

import com.flagstone.transform.Constants;
import com.flagstone.transform.MovieTag;
import com.flagstone.transform.MovieTypes;
import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;

/**
 * The QuicktimeMovie defines the path to an Quicktime movie to be played.
 */
public class QuicktimeMovie implements MovieTag {

    /** Format string used in toString() method. */
    private static final String FORMAT = "QuicktimeMovie: { name=%s}";

    /** The path to the Quicktime file. */
    private String path;

    /** The length of the object, minus the header, when it is encoded. */
    private transient int length;

    /**
     * Creates and initialises a QuicktimeMovie object using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public QuicktimeMovie(final SWFDecoder coder) throws IOException {
        length = coder.readUnsignedShort() & Coder.LENGTH_FIELD;
        if (length == Coder.IS_EXTENDED) {
            length = coder.readInt();
        }
        coder.mark();
        path = coder.readString(length);
        coder.check(length);
        coder.unmark();
    }

    /**
     * Creates a QuicktimeMovie object referencing the specified file.
     *
     * @param aString
     *            the file or URL where the file is located. Must not be null.
     */
    public QuicktimeMovie(final String aString) {
        setPath(aString);
    }

    /**
     * Creates and initialises a QuicktimeMovie object using the values copied
     * from another QuicktimeMovie object.
     *
     * @param object
     *            a QuicktimeMovie object from which the values will be
     *            copied.
     */
    public QuicktimeMovie(final QuicktimeMovie object) {
        path = object.path;
    }

    /**
     * Get the reference to the file containing the movie.
     *
     * @return the path to the Quicktime movie.
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the reference to the file containing the movie.
     *
     * @param aString
     *            the file or URL where the file is located. Must not be null.
     */
    public void setPath(final String aString) {
        if (aString == null) {
            throw new IllegalArgumentException();
        }
        path = aString;
    }

    /** {@inheritDoc} */
    public QuicktimeMovie copy() {
        return new QuicktimeMovie(this);
    }

    @Override
    public String toString() {
        return String.format(FORMAT, path);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        length = context.strlen(path);

        return (length > Coder.HEADER_LIMIT ? Coder.LONG_HEADER
                : Coder.SHORT_HEADER) + length;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {

        if (length > Coder.HEADER_LIMIT) {
            coder.writeShort((MovieTypes.QUICKTIME_MOVIE
                    << Coder.LENGTH_FIELD_SIZE) | Coder.IS_EXTENDED);
            coder.writeInt(length);
        } else {
            coder.writeShort((MovieTypes.QUICKTIME_MOVIE
                    << Coder.LENGTH_FIELD_SIZE) | length);
        }
        if (Constants.DEBUG) {
            coder.mark();
        }
        coder.writeString(path);
        if (Constants.DEBUG) {
            coder.check(length);
            coder.unmark();
        }
    }
}
