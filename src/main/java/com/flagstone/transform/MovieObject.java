/*
 * MovieObject.java
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

import java.io.IOException;
import java.util.Arrays;

import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;

/**
 * MovieObject is used to represent any object decoded from a Movie that is not
 * directly supported by Transform.
 *
 * <p>
 * This allow a certain amount of forward compatibility where file which use a
 * version of Flash greater than the one supported by Transform can be decoded
 * and encoded.
 * </p>
 */
public class MovieObject implements MovieTag {

    /** Format string used in toString() method. */
    private static final String FORMAT = "MovieObject: { type=%d;"
            + " data=byte<%d> ...}";

    /** The type identifying the MovieTag. */
    private final transient int type;
    /** The encoded data that make up the body of the tag. */
    private final transient byte[] data;

    /** The length of the object, minus the header, when it is encoded. */
    private transient int length;

    /**
     * Creates and initialises a MovieObject object using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public MovieObject(final SWFDecoder coder) throws IOException {
        type = coder.scanUnsignedShort() >>> Coder.LENGTH_FIELD_SIZE;
        length = coder.readUnsignedShort() & Coder.LENGTH_FIELD;
        if (length == Coder.IS_EXTENDED) {
            length = coder.readInt();
        }
        data = coder.readBytes(new byte[length]);
    }

    /**
     * Creates and initialises a MovieObject object using the specified type
     * and encoded data.
     *
     * @param aType the type that identifies the MovieTag when it is encoded.
     * @param bytes the encoded bytes that form the body of the object.
     */
    public MovieObject(final int aType, final byte[] bytes) {
        type = aType;

        if (bytes == null) {
            throw new IllegalArgumentException();
        }
        data = Arrays.copyOf(bytes, bytes.length);
    }

    /**
     * Creates and initialises a MovieObject object using the values copied
     * from another MovieObject object.
     *
     * @param object
     *            a MovieObject object from which the values will be
     *            copied.
     */
    public MovieObject(final MovieObject object) {
        type = object.type;
        data = object.data;
    }

    /**
     * Get the type that identifies the object when it is encoded.
     * @return the type that identifies the encoded data structure.
     */
    public int getType() {
        return type;
    }

    /**
     * Get a copy of the encoded data for the movie tag object.
     * @return a copy of the encoded data.
     */
    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }

    /** {@inheritDoc} */
    public MovieObject copy() {
        return new MovieObject(this);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, type, data.length);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        length = data.length;
        return (length > Coder.HEADER_LIMIT ? Coder.LONG_HEADER
                : Coder.SHORT_HEADER) + length;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        if (length > Coder.HEADER_LIMIT) {
            coder.writeShort((type
                    << Coder.LENGTH_FIELD_SIZE) | Coder.IS_EXTENDED);
            coder.writeInt(length);
        } else {
            coder.writeShort((type
                    << Coder.LENGTH_FIELD_SIZE) | length);
        }
        coder.writeBytes(data);
    }
}
