/*
 * JPEGEncodingTable.java
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

package com.flagstone.transform.image;

import java.io.IOException;
import java.util.Arrays;

import com.flagstone.transform.Constants;
import com.flagstone.transform.MovieTag;
import com.flagstone.transform.MovieTypes;
import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;

/**
 * JPEGEncodingTable defines the Huffman encoding table for JPEG images.
 *
 * <p>
 * The encoding table is shared between all images defined using the
 * DefineJPEGImage class so there should only be one JPEGEncodingTable object
 * defined in a movie.
 * </p>
 *
 * <p>
 * The JPEGEncodingTable class is not essential to define JPEG encoded images in
 * a movie using the DefineJPEGImage class. You can still display an image if it
 * contains the encoding table. There is no need to separate it and add it to a
 * JPEGEncodingTable object, particularly since different images contain
 * different encoding tables.
 * </p>
 *
 * @see DefineJPEGImage
 */
public class JPEGEncodingTable implements MovieTag {

    /** Format string used in toString() method. */
    private static final String FORMAT = "JPEGEncodingTable: {"
                + " table=byte<%d> ...}";

    /** Huffman encoding table. */
    private byte[] table;

    /** The length of the object, minus the header, when it is encoded. */
    private transient int length;

    /**
     * Creates and initialises a JPEGEncodingTable object using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public JPEGEncodingTable(final SWFDecoder coder) throws IOException {
        length = coder.readUnsignedShort() & Coder.LENGTH_FIELD;
        if (length == Coder.IS_EXTENDED) {
            length = coder.readInt();
        }
        coder.mark();
        table = coder.readBytes(new byte[length]);
        coder.check(length);
        coder.unmark();
    }

    /**
     * Creates a JPEGEncodingTable object with the encoding table data.
     *
     * @param bytes
     *            a list of bytes contains the data for the encoding table.
     *            Must not be null.
     */
    public JPEGEncodingTable(final byte[] bytes) {
        setTable(bytes);
    }

    /**
     * Creates and initialises a JPEGEncodingTable object using the values
     * copied from another JPEGEncodingTable object.
     *
     * @param object
     *            a JPEGEncodingTable object from which the values will be
     *            copied.
     */
    public JPEGEncodingTable(final JPEGEncodingTable object) {
        table = object.table;
    }

    /**
     * Get a copy of the encoding table.
     *
     * @return a copy of the table data.
     */
    public byte[] getTable() {
        return Arrays.copyOf(table, table.length);
    }

    /**
     * Sets the encoding table.
     *
     * @param bytes
     *            a list of bytes contains the data for the encoding table.
     *            Must not be null or zero length.
     */
    public void setTable(final byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException();
        }
        table = Arrays.copyOf(bytes, bytes.length);
    }

    /** {@inheritDoc} */
    public JPEGEncodingTable copy() {
        return new JPEGEncodingTable(this);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, table.length);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        length = table.length;

        return (length > Coder.HEADER_LIMIT ? Coder.LONG_HEADER
                : Coder.SHORT_HEADER) + length;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {

        if (length > Coder.HEADER_LIMIT) {
            coder.writeShort((MovieTypes.JPEG_TABLES
                    << Coder.LENGTH_FIELD_SIZE) | Coder.IS_EXTENDED);
            coder.writeInt(length);
        } else {
            coder.writeShort((MovieTypes.JPEG_TABLES
                    << Coder.LENGTH_FIELD_SIZE) | length);
        }
        if (Constants.DEBUG) {
            coder.mark();
        }
        coder.writeBytes(table);
        if (Constants.DEBUG) {
            coder.check(length);
            coder.unmark();
        }
    }
}
