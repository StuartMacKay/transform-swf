/*
 * Free.java
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

import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.exception.IllegalArgumentRangeException;

/**
 * Free deletes the object with the specified identifier, freeing up resources
 * in the Flash Player.
 */
public class Free implements MovieTag {

    /** Format string used in toString() method. */
    private static final String FORMAT = "Free: { identifier=%d}";

    /** The unique identifier of the object that will be deleted. */
    private int identifier;

    /**
     * Creates and initialises a Free object using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public Free(final SWFDecoder coder) throws IOException {
        if ((coder.readUnsignedShort() & Coder.LENGTH_FIELD)
        		== Coder.IS_EXTENDED) {
            coder.readInt();
        }
        identifier = coder.readUnsignedShort();
    }

    /**
     * Creates a Free object with the specified identifier.
     *
     * @param uid
     *            the unique identifier of the object to be deleted. Must be in
     *            the range 1..65535.
     */
    public Free(final int uid) {
        setIdentifier(uid);
    }

    /**
     * Creates a Free initialised with a copy of the data from another object.
     *
     * @param object
     *            a Free object used to initialise this one.
     */
    public Free(final Free object) {
        identifier = object.identifier;
    }

    /**
     * Get the identifier of the object to be deleted.
     *
     * @return the object identifier.
     */
    public int getIdentifier() {
        return identifier;
    }

    /**
     * Sets the identifier of the object to be deleted.
     *
     * @param uid
     *            the identifier of the object to be deleted. Must be in the
     *            range 1..65535.
     */
    public void setIdentifier(final int uid) {
        if ((uid < 1) || (uid > Coder.USHORT_MAX)) {
            throw new IllegalArgumentRangeException(
                    1, Coder.USHORT_MAX, uid);
        }
        identifier = uid;
    }

    /** {@inheritDoc} */
    @Override
	public Free copy() {
        return new Free(this);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, identifier);
    }

    /** {@inheritDoc} */
    @Override
	public int prepareToEncode(final Context context) {
        // CHECKSTYLE:OFF
        return 4;
        // CHECKSTYLE:ON
    }

    /** {@inheritDoc} */
    @Override
	public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        coder.writeShort((MovieTypes.FREE
                << Coder.LENGTH_FIELD_SIZE) | 2);
        coder.writeShort(identifier);
    }
}
