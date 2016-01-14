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
import com.flagstone.transform.datatype.Bounds;
import com.flagstone.transform.exception.IllegalArgumentRangeException;

/**
 * MovieHeader contains the attributes that make up the header fields of a Flash
 * file. Previously these were attributes of the Movie class.
 */
public class MovieHeader implements MovieTag {

    /** Format string used in toString() method. */
    private static final String FORMAT = "Header: { version=%d; compressed=%b;"
    		+ " frameSize=%s; frameRate=%f; frameCount=%d}";

    /** The Flash version number. */
    private int version;
    /** The Flash Player screen coordinates. */
    private Bounds frameSize;
    /** The frame rate of the movie. */
    private int frameRate;
    /** The number of frames in the movie. */
    private int frameCount;
    /** Flag indicating whether the movie is compressed. */
    private boolean compressed;

    /**
     * Creates and initialises a MovieAttributes object using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @param context
     *            a Context object used to manage the decoders for different
     *            type of object and to pass information on how objects are
     *            decoded.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public MovieHeader(final SWFDecoder coder, final Context context)
            throws IOException {
        version = context.get(Context.VERSION);
        compressed = context.get(Context.COMPRESSED) == 1;
        frameSize = new Bounds(coder);
        frameRate = coder.readUnsignedShort();
        frameCount = coder.readUnsignedShort();
    }

    /**
     * Construct a new MovieHeader with Flash version and compression set to
     * the values supported in this version of Transform SWF.
     */
    public MovieHeader() {
        version = Movie.VERSION;
        compressed = true;
    }

    /**
     * Creates and initialises a MovieAttributes object using the values copied
     * from another MovieAttributes object.
     *
     * @param object
     *            a MovieAttributes object from which the values will be
     *            copied.
     */
    public MovieHeader(final MovieHeader object) {
        version = object.version;
        compressed = object.compressed;
        frameSize = object.frameSize;
        frameRate = object.frameRate;
        frameCount = object.frameCount;
    }

    /**
     * Get the number representing the version of Flash that the movie
     * represents.
     *
     * @return the version number of Flash that this movie contains.
     */
    public int getVersion() {
        return version;
    }

    /**
     * Sets the Flash version supported in this Movie. Note that there are no
     * restrictions on the objects that can be used in a coder. Using objects
     * that are not supported by an earlier version of the Flash file format may
     * cause the Player to not display the movie correctly or even crash the
     * Player.
     *
     * @param aNumber
     *            the version of the Flash file format that this movie utilises.
     */
    public void setVersion(final int aNumber) {
        if (aNumber < 0) {
            throw new IllegalArgumentRangeException(
                    0, Integer.MAX_VALUE, aNumber);
        }
        version = aNumber;
    }

    /**
     * Get the bounding rectangle that defines the size of the player
     * screen.
     *
     * @return the bounding box that defines the screen.
     */
    public Bounds getFrameSize() {
        return frameSize;
    }

    /**
     * Sets the bounding rectangle that defines the size of the player screen.
     * The coordinates of the bounding rectangle are also used to define the
     * coordinate range. For example if a 400 x 400 pixel rectangle is defined,
     * specifying the values for the x and y coordinates the range -200 to 200
     * sets the centre of the screen at (0,0), if the x and y coordinates are
     * specified in the range 0 to 400 then the centre of the screen will be at
     * (200, 200).
     *
     * @param rect
     *            the Bounds object that defines the frame size. Must not be
     *            null.
     */
    public void setFrameSize(final Bounds rect) {
        if (rect == null) {
            throw new IllegalArgumentException();
        }
        frameSize = rect;
    }

    /**
     * Get the number of frames played per second that the movie will be
     * displayed at.
     *
     * @return the movie frame rate.
     */
    public float getFrameRate() {
        return frameRate / Coder.SCALE_8;
    }

    /**
     * Sets the number of frames played per second that the Player will display
     * the coder.
     *
     * @param rate
     *            the number of frames per second that the movie is played.
     */
    public void setFrameRate(final float rate) {
        frameRate = (int) (rate * Coder.SCALE_8);
    }

    /**
     * Get the number of frames in the movie. The value returned is only valid
     * for movies that have just been decoded or encoded. The number returned
     * will not be valid if the movie was edited.
     *
     * @return the number of frames.
     */
    public int getFrameCount() {
        return frameCount;
    }

    /**
     * Set the number of frames in the movie. This method is not usually called
     * since the number of frames will be set automatically when the movie is
     * encoded.
     *
     * @param count the number of frames in the movie.
     */
    public void setFrameCount(final int count) {
        frameCount = count;
    }

    /**
     * Is the movie compressed.
     *
     * @return true if the movie contains zlib compressed data or false if it
     * is not compressed.
     */
    public boolean isCompressed() {
        return compressed;
    }

    /**
     * Set whether the movie should be compressed when encoded.
     *
     * @param compress true if the movie will be compressed, false if no
     * compression will be applied.
     */
    public void setCompressed(final boolean compress) {
        compressed = compress;
    }

    /** {@inheritDoc} */
    public MovieHeader copy() {
        return new MovieHeader(this);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, version, compressed, frameSize,
                getFrameRate(), frameCount);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 1 LINES
        return 4 + frameSize.prepareToEncode(context);
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        frameSize.encode(coder, context);
        coder.writeShort(frameRate);
        coder.writeShort(frameCount);
    }
}
