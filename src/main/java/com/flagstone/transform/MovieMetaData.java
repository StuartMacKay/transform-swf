/*
 * MetaData.java
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

import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;

/**
 * MetaData is used to add a user-defined information into a Flash file. The
 * information uses the Resource Description Format (RDF) and is compliant
 * with Adobe's Extensible Metadata Platform, see
 * http://www.adobe.com/products/xmp.
 */
public class MovieMetaData implements MovieTag {

    /** Format string used in toString() method. */
    private static final String FORMAT = "MetaData: { metadata=%s}";
    /** The meta-data for the movie. */
    private String metaData;

    /** The length of the object, minus the header, when it is encoded. */
    private transient int length;

    /**
     * Creates and initialises a MoveMetaData object using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public MovieMetaData(final SWFDecoder coder) throws IOException {
        length = coder.readUnsignedShort() & Coder.LENGTH_FIELD;
        if (length == Coder.IS_EXTENDED) {
            length = coder.readInt();
        }
        coder.mark();
        metaData = coder.readString(length - 1);
        coder.readByte();
        coder.check(length);
        coder.unmark();
    }

    /**
     * Creates a MoveMetaData object with the specified string containing the
     * meta-data for the movie.
     *
     * @param aString
     *            an arbitrary string containing the meta-data. Must not be
     *            null.
     */
    public MovieMetaData(final String aString) {
        setMetaData(aString);
    }

    /**
     * Creates and initialises a MovieMetaData object using the values copied
     * from another MovieMetaData object.
     *
     * @param object
     *            a MovieMetaData object from which the values will be
     *            copied.
     */
    public MovieMetaData(final MovieMetaData object) {
        metaData = object.metaData;
    }

    /**
     * Get the meta-data for the movie.
     *
     * @return the string containing the meta-data.
     */
    public String getMetaData() {
        return metaData;
    }

    /**
     * Set the meta-data for the movie.
     *
     * @param aString a string containing the meta-data.
     */
    public void setMetaData(final String aString) {
        if (aString == null) {
            throw new IllegalArgumentException();
        }
        metaData = aString;
    }

    /** {@inheritDoc} */
    public MovieMetaData copy() {
        return new MovieMetaData(this);
    }

    @Override
    public String toString() {
        return String.format(FORMAT, metaData);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        length = context.strlen(metaData);

        return (length > Coder.HEADER_LIMIT ? Coder.LONG_HEADER
                : Coder.SHORT_HEADER) + length;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {

        if (length > Coder.HEADER_LIMIT) {
            coder.writeShort((MovieTypes.METADATA
                    << Coder.LENGTH_FIELD_SIZE) | Coder.IS_EXTENDED);
            coder.writeInt(length);
        } else {
            coder.writeShort((MovieTypes.METADATA
                    << Coder.LENGTH_FIELD_SIZE) | length);
        }
        coder.writeString(metaData);
    }
}
