/*
 * ActionObject.java
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

package com.flagstone.transform.action;

import java.io.IOException;
import java.util.Arrays;

import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;

/**
 * ActionObject is a general-purpose class that can be used to represent any
 * action. It allow actions not supported in the current version of Transform to
 * be decoded and encoded from movies until direct support is provided in the
 * framework.
 */
public class ActionObject implements Action {

    /** Format string used in toString() method. */
    private static final String FORMAT = "ActionObject: {"
        + "type=%d; data=byte[%s] {...} }";

    /** The type used to identify the action. */
    private final transient int type;
    /** The encoded arguments, if any, used by the action. */
    private final transient byte[] data;

    /**
     * Creates and initialises an ActionObject using values encoded in the Flash
     * binary format.
     *
     * @param actionType the value that identifies the action when it is
     * encoded.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public ActionObject(final int actionType, final SWFDecoder coder)
                throws IOException {
        type = actionType;

        if (type > ActionTypes.HIGHEST_BYTE_CODE) {
            data = coder.readBytes(new byte[coder.readUnsignedShort()]);
        } else {
            data = new byte[0];
        }
    }

    /**
     * Creates an ActionObject specifying only the type.
     *
     * @param actionType
     *            the value identifying the action when it is encoded.
     */
    public ActionObject(final int actionType) {
        type = actionType;
        data = new byte[0];
    }

    /**
     * Creates an ActionObject specifying the type and the data that represents
     * the body of the action encoded in the Flash binary format.
     *
     * @param actionType
     *            the value identifying the action when it is encoded.
     * @param bytes
     *            the body of the action when it is encoded in the Flash format.
     */
    public ActionObject(final int actionType, final byte[] bytes) {
        type = actionType;
        data = Arrays.copyOf(bytes, bytes.length);
    }

    /**
     * Creates an ActionObject by copying an existing one.
     *
     * @param object
     *            an ActionObject.
     */
    public ActionObject(final ActionObject object) {
        type = object.type;
        data = object.data;
    }

    /**
     * Returns the type that identifies the type of action when it is encoded in
     * the Flash binary format.
     *
     * @return the value identifying the action when it is encoded.
     */
    public int getType() {
        return type;
    }

    /**
     * Returns the encoded data for the action.
     *
     * @return the bytes representing the encoded arguments of the action.
     */
    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }

    /** {@inheritDoc} */
    @Override
	public ActionObject copy() {
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, type, data.length);
    }

    /** {@inheritDoc} */
    @Override
	public int prepareToEncode(final Context context) {
        int length;
        if (type > ActionTypes.HIGHEST_BYTE_CODE) {
            length = Coder.ACTION_HEADER + data.length;
        } else {
            length = 1;
        }
        return length;
    }

    /** {@inheritDoc} */
    @Override
	public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        coder.writeByte(type);

        if (type > ActionTypes.HIGHEST_BYTE_CODE) {
            coder.writeShort(data.length);
            coder.writeBytes(data);
        }
    }
}
