/*
 * MovieAttributes.java
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
 * The MovieAttributes tag defines characteristics of a Movie. It contains
 * several flags to indicate types of objects in the movie and whether any
 * hardware acceleration should be used if available.
 *
 * For Flash Version 8 and above it must be the first object after the
 * MovieHeader.
 */
public class MovieAttributes implements MovieTag {

    /** Format string used in toString() method. */
    private static final String FORMAT = "MovieAttributes: {"
    		+ " metadata=%b;  as3=%b;  network=%b; gpu=%b; directBlit=%b}";
    /** The set of encoded attributes. */
    private transient int attributes;

    /**
     * Creates a new MovieAttributes object.
     */
    public MovieAttributes() {
        // Empty
    }

    /**
     * Creates and initialises a MovieAttributes object using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public MovieAttributes(final SWFDecoder coder)
            throws IOException {
        int length = coder.readUnsignedShort() & Coder.LENGTH_FIELD;
        if (length == Coder.IS_EXTENDED) {
            length = coder.readInt();
        }
        attributes = coder.readByte();
        coder.skip(length - 1);
    }

    /**
     * Creates and initialises a MovieAttributes object using the values copied
     * from another MovieAttributes object.
     *
     * @param object
     *            a MovieAttributes object from which the values will be
     *            copied.
     */
    public MovieAttributes(final MovieAttributes object) {
        attributes = object.attributes;
    }

    /**
     * Does the Movie contain Actionscript 3 code.
     *
     * @return true if the movie contains at least one DoABC tag
     * containing Actionscript 3 byte-codes.
     */
    public boolean hasMetaData() {
        return (attributes & Coder.BIT4) != 0;
    }

    /**
     * Does the Movie contain meta-data.
     *
     * @return true if the movie contains a MovieMetaData tag.
     */
    public boolean hasAS3() {
        return (attributes & Coder.BIT3) != 0;
    }

    /**
     * Does the Flash Player use direct bit block transfer to accelerate
     * graphics.
     *
     * @return true if the Flash Player will use direct bit block transfer.
     */
    public boolean useDirectBlit() {
        return (attributes & Coder.BIT6) != 0;
    }

    /**
     * Instruct the Flash Player to use direct bit block transfer to accelerate
     * graphics.
     *
     * @param useBlit use graphics hardware accelerations.
     */
    public void setUseDirectBlit(final boolean useBlit) {
        if (useBlit) {
            attributes |= Coder.BIT6;
        } else {
            attributes &= ~Coder.BIT6;
        }
    }

    /**
     * Does the Flash Player use the graphics processor to accelerate
     * compositing - if available.
     *
     * @return true if the Flash Player will use the graphics process for
     * compositing.
     */
    public boolean useGPU() {
        return (attributes & Coder.BIT5) != 0;
    }

    /**
     * Instruct the Flash Player to use the graphics processor to accelerate
     * compositing - if available.
     *
     * @param useGPU use graphics processor for compositing.
     */
    public void setUseGPU(final boolean useGPU) {
        if (useGPU) {
            attributes |= Coder.BIT5;
        } else {
            attributes &= ~Coder.BIT5;
        }
    }

    /**
     * Does the Flash Player use the network for loading resources even if the
     * movie is loaded from the local file system.
     * @return true if the network will be used even if the movie is loaded
     * locally, false otherwise.
     */
    public boolean useNetwork() {
        return (attributes & Coder.BIT0) != 0;
    }

    /**
     * Instructor the Flash Player use the network for loading resources even
     * if the movie is loaded from the local file system.
     * @param useNetwork use the network even if the movie is loaded locally.
     */
    public void setUseNetwork(final boolean useNetwork) {
        if (useNetwork) {
            attributes |= Coder.BIT0;
        } else {
            attributes &= ~Coder.BIT0;
        }
    }

    /** {@inheritDoc} */
    public MovieAttributes copy() {
        return new MovieAttributes(this);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, hasMetaData(), hasAS3(), useNetwork(),
                useGPU(), useDirectBlit());
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 1 LINES
        return 6;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 2 LINES
        coder.writeShort((MovieTypes.FILE_ATTRIBUTES
                << Coder.LENGTH_FIELD_SIZE) | 4);
        coder.writeByte(attributes);
        coder.writeByte(0);
        coder.writeByte(0);
        coder.writeByte(0);
    }
}
