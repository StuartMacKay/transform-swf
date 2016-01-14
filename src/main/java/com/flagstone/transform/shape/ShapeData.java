/*
 * ShapeData.java
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
import java.util.Arrays;

import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;

/**
 * ShapeData is a convenience class for holding a set of encoded ShapeRecords
 * so that a Shape can be lazily decoded.
 */
public class ShapeData implements ShapeRecord {

    /** Format string used in toString() method. */
    private static final String FORMAT = "ShapeData: byte<%d> ...";
    /** The encoded ShapeRecords. */
    private final transient byte[] data;

    /**
     * Create a new ShapeData object initialised with an array of bytes
     * containing the encoded records for a shape.
     *
     * @param size
     *            the number of bytes to read for the encoded shape data.
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while reading the encoded shape data.
     */
    public ShapeData(final int size, final SWFDecoder coder)
            throws IOException {
        if (size < 0) {
            throw new IllegalArgumentException();
        }
        data = coder.readBytes(new byte[size]);
    }

    /**
     * Create a new ShapeData object with an array of encoded ShapeRecords.
     * @param bytes the encoded ShapeRecords.
     */
    public ShapeData(final byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException();
        }
        data = Arrays.copyOf(bytes, bytes.length);
    }

    /**
     * Creates and initialises a ShapeData object using the values copied
     * from another ShapeData object.
     *
     * @param object
     *            a ShapeData object from which the values will be
     *            copied.
     */
    public ShapeData(final ShapeData object) {
        data = object.data;
    }

    /**
     * Get a copy of the encoded data for the action.
     *
     * @return a copy of the encoded shape.
     */
    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }

    /** {@inheritDoc} */
    public ShapeData copy() {
        return new ShapeData(this);
    }

    @Override
    public String toString() {
        return String.format(FORMAT, data.length);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        return data.length;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        coder.writeBytes(data);
    }
}
