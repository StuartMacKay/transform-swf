/*
 * EnableDebugger.java
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

/**
 * Enables a movie to be debugged when played using the Flash authoring tool,
 * allowing the variables defined in the arrays of actions specified in object
 * to be inspected. Note that the Flash Player does not support debugging.
 *
 * <p>
 * In order to use the debugger a password must be supplied. When encrypted
 * using the MD5 algorithm it must match the value stored in the password
 * attribute.
 * </p>
 */
public class EnableDebugger implements MovieTag {

    /** Format string used in toString() method. */
    private static final String FORMAT = "EnableDebugger: { password=%s}";
    /** The MD5 hash of the password used to enable the debugger. */
    private String password;

    /** The length of the object, minus the header, when it is encoded. */
    private transient int length;

    /**
     * Creates and initialises an EnableDebugger object using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public EnableDebugger(final SWFDecoder coder) throws IOException {
        length = coder.readUnsignedShort() & Coder.LENGTH_FIELD;
        if (length == Coder.IS_EXTENDED) {
            length = coder.readInt();
        }
        coder.mark();
        coder.readUnsignedShort();
        password = coder.readString();
        coder.check(length);
        coder.unmark();
    }

    /**
     * Creates a EnableDebugger2 object with an MD5 encrypted password.
     *
     * @param pass
     *            the string defining the password. Must not be an empty string
     *            or null.
     */
    public EnableDebugger(final String pass) {
        setPassword(pass);
    }

    /**
     * Creates and initialises an EnableDebugger object using the password
     * from another EnableDebugger object.
     *
     * @param object
     *            a EnableDebugger object from which the password will be
     *            copied.
     */
    public EnableDebugger(final EnableDebugger object) {
        password = object.password;
    }

    /**
     * Get the MD5 hashed password.
     *
     * @return the password hash.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the MD5 encrypted password.
     *
     * @param aString
     *            the string defining the password. Must not be an empty string
     *            or null.
     */
    public void setPassword(final String aString) {
        if (aString == null || aString.length() == 0) {
            throw new IllegalArgumentException();
        }
        password = aString;
    }

    /** {@inheritDoc} */
    public EnableDebugger copy() {
        return new EnableDebugger(this);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, password);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        length = 2 + context.strlen(password);

        return (length > Coder.HEADER_LIMIT ? Coder.LONG_HEADER
                : Coder.SHORT_HEADER) + length;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        if (length > Coder.HEADER_LIMIT) {
            coder.writeShort((MovieTypes.ENABLE_DEBUGGER
                    << Coder.LENGTH_FIELD_SIZE) | Coder.IS_EXTENDED);
            coder.writeInt(length);
        } else {
            coder.writeShort((MovieTypes.ENABLE_DEBUGGER
                    << Coder.LENGTH_FIELD_SIZE) | length);
        }
        coder.writeShort(0);
        coder.writeString(password);
    }
}
