/*
 * DoABC.java
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
import java.util.Arrays;

import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;

/**
 * DoABC is used to define scripts containing Actionscript 3.0 byte-codes.
 *
 * <p>
 * Execution of the script may be deferred until it is explicitly called using
 * the assigned name.
 * </p>
 */
public class DoABC implements MovieTag {

    /** Format string used in toString() method. */
    private static final String FORMAT = "DoABC: { name=%s; deferred=%d;"
            + " actions=byte<%d> ...}";

    /** The name of the script. */
    private String name;
    /** Is loading deferred until the script is called. */
    private int deferred;
    /** The encoded actionscript 3 bytes codes. */
    private byte[] data;

    /** The length of the object, minus the header, when it is encoded. */
    private transient int length;

    /**
     * Creates and initialises an DoABC using values encoded in the Flash
     * binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public DoABC(final SWFDecoder coder) throws IOException {
        length = coder.readUnsignedShort() & Coder.LENGTH_FIELD;
        if (length == Coder.IS_EXTENDED) {
            length = coder.readInt();
        }
        coder.mark();
        deferred = coder.readInt();
        name = coder.readString();
        data = coder.readBytes(new byte[length - coder.bytesRead()]);
        coder.check(length);
        coder.unmark();
    }

    /**
     * Creates a DoABC object with the name and compiled Actionscript 3.0
     * byte-codes.
     *
     * @param scriptName
     *            the name used to identify the script.
     * @param defer
     *            whether execution of the script is deferred.
     * @param script
     *            the compiled Actionscript 3.0 byte-codes.
     */
    public DoABC(final String scriptName, final boolean defer,
            final byte[] script) {
        setName(scriptName);
        setDeferred(defer);
        setData(script);
    }

    /**
     * Creates a DoABC initialised with a copy of the data from another object.
     *
     * @param object
     *            a DoABC object used to initialize this one.
     */
    public DoABC(final DoABC object) {
        name = object.name;
        deferred = object.deferred;
        data = object.data;
    }

    /**
     * Get the name of the script.
     *
     * @return the name used to call the script.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the script.
     *
     * @param aString
     *            the name assigned to the script so it can be referred to. Must
     *            not be null or an empty string.
     */
    public void setName(final String aString) {
        if (aString == null || aString.length() == 0) {
            throw new IllegalArgumentException();
        }
        name = aString;
    }

    /**
     * Is loading of the script deferred until it is called.
     *
     * @return true if loading of the script is deferred, false if it is loaded
     * immediately.
     */
    public boolean isDeferred() {
        return (deferred & 1) != 0;
    }

    /**
     * Sets whether execution of the script is deferred.
     *
     * @param defer
     *            execution of the script is deferred (true) or executed
     *            immediately (false).
     */
    public void setDeferred(final boolean defer) {
        if (defer) {
            deferred = 1;
        } else {
            deferred = 0;
        }
    }

    /**
     * Get a copy of the array containing the Actionscript byte-codes.
     *
     * @return a copy of the encoded actionscript.
     */
    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }

    /**
     * Sets the script containing compiled Actionscript 3.0 byte-codes.
     *
     * @param bytes
     *            a list of byte-codes. Must not be null.
     */
    public void setData(final byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException();
        }
        data = Arrays.copyOf(bytes, bytes.length);
    }

    /** {@inheritDoc} */
    public DoABC copy() {
        return new DoABC(this);
    }

    @Override
    public String toString() {
        return String.format(FORMAT, name, deferred, data.length);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        // CHECKSTYLE:OFF
        length = 4 + context.strlen(name) + data.length;

        return (length > Coder.HEADER_LIMIT ? Coder.LONG_HEADER
                : Coder.SHORT_HEADER) + length;
        // CHECKSTYLE:ON
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {

        if (length > Coder.HEADER_LIMIT) {
            coder.writeShort((MovieTypes.DO_ABC
                    << Coder.LENGTH_FIELD_SIZE) | Coder.IS_EXTENDED);
            coder.writeInt(length);
        } else {
            coder.writeShort((MovieTypes.DO_ABC
                    << Coder.LENGTH_FIELD_SIZE) | length);
        }
        if (Constants.DEBUG) {
            coder.mark();
        }
        coder.writeInt(deferred);
        coder.writeString(name);
        coder.writeBytes(data);
        if (Constants.DEBUG) {
            coder.check(length);
            coder.unmark();
        }
    }
}
