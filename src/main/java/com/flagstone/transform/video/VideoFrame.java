/*
 * VideoFrame.java
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
package com.flagstone.transform.video;

import java.io.IOException;
import java.util.Arrays;

import com.flagstone.transform.MovieTag;
import com.flagstone.transform.MovieTypes;
import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.exception.IllegalArgumentRangeException;

/**
 * VideoFrame contains the video data displayed in a single frame of a Flash
 * movie (.swf).
 *
 * <p>
 * Each frame of video is displayed whenever display list is updated using the
 * ShowFrame object - any timing information stored within the video data is
 * ignored. Since the video is updated at the same time as the display list the
 * frame rate of the video may be the same or less than the frame rate of the
 * Flash movie but not higher.
 * </p>
 *
 * @see DefineVideo
 */
public class VideoFrame implements MovieTag {

    /** Format string used in toString() method. */
    private static final String FORMAT = "VideoFrame: { identifier=%d;"
            + " frameNumber=%d; data=%d}";

    /** The unique identifier of the video that this frame belongs to. */
    private int identifier;
    /** The frame number in the video. */
    private int frameNumber;
    /** The encoded video data. */
    private byte[] data;

    /** The length of the object, minus the header, when it is encoded. */
    private transient int length;

    /**
     * Creates and initialises a VideoFrame object using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public VideoFrame(final SWFDecoder coder) throws IOException {
        length = coder.readUnsignedShort() & Coder.LENGTH_FIELD;
        if (length == Coder.IS_EXTENDED) {
            length = coder.readInt();
        }
        identifier = coder.readUnsignedShort();
        frameNumber = coder.readUnsignedShort();
        // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 1 LINES
        data = coder.readBytes(new byte[length - 4]);
    }

    /**
     * Constructs a new VideoFrame object which will display the specified frame
     * of video data in the DefineVideo object that matches the identifier.
     *
     * @param uid
     *            the unique identifier of the DefineVideo object. Must be in
     *            the range 1..65535.
     * @param frame
     *            the number of the frame. Must be in the range 1..65535.
     * @param videoData
     *            the encoded video data. For Flash 6 this is encoded in the
     *            H263 format. In Flash 7 H263 and ScreenVideo is supported.
     */
    public VideoFrame(final int uid, final int frame,
                final byte[] videoData) {
        setIdentifier(uid);
        setFrameNumber(frame);
        setData(videoData);
    }

    /**
     * Creates and initialises a VideoFrame object using the values copied
     * from another VideoFrame object.
     *
     * @param object
     *            a VideoFrame object from which the values will be
     *            copied.
     */
    public VideoFrame(final VideoFrame object) {
        identifier = object.identifier;
        frameNumber = object.frameNumber;
        data = object.data;
    }

    /**
     * Get the identifier of the DefineVideo object where the frame will be
     * displayed.
     *
     * @return the unique identifier of the video.
     */
    public int getIdentifier() {
        return identifier;
    }

    /**
     * Sets the identifier of the DefineVideo object where the frame will be
     * displayed.
     *
     * @param uid
     *            the unique identifier of the DefineVideo object. Must be in
     *            the range 1..65535.
     */
    public void setIdentifier(final int uid) {
        if ((uid < 1) || (uid > Coder.USHORT_MAX)) {
            throw new IllegalArgumentRangeException(
                    1, Coder.USHORT_MAX, uid);
        }
        identifier = uid;
    }

    /**
     * Get the number of the frame.
     *
     * @return the frame number.
     */
    public int getFrameNumber() {
        return frameNumber;
    }

    /**
     * Sets the number of the frame.
     *
     * @param number
     *            the frame number. Must be in the range 1..65535.
     */
    public void setFrameNumber(final int number) {
        if ((number < 1) || (number > Coder.USHORT_MAX)) {
            throw new IllegalArgumentRangeException(
                    1, Coder.USHORT_MAX, number);
        }
        frameNumber = number;
    }

    /**
     * Get a copy of the encoded video data. In Flash 6 modified H263
     * encoded video is supported. Flash 7 supports both modified H263 and
     * ScreenVideo.
     *
     * @return a copy of the video data.
     */
    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }

    /**
     * Sets the encoded video data. In Flash 6 modified H263 encoded video is
     * supported. Flash 7 supports both modified H263 and ScreenVideo,
     *
     * @param frameData
     *            the encoded video data. Must not be null.
     */
    public void setData(final byte[] frameData) {
        if (frameData == null) {
            throw new IllegalArgumentException();
        }
        data =  Arrays.copyOf(frameData, frameData.length);
    }

    /** {@inheritDoc} */
    public VideoFrame copy() {
        return new VideoFrame(this);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, identifier, frameNumber, data.length);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 1 LINES
        length = 4 + data.length;

        return (length > Coder.HEADER_LIMIT
                ? Coder.LONG_HEADER : Coder.SHORT_HEADER) + length;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
         if (length > Coder.HEADER_LIMIT) {
            coder.writeShort((MovieTypes.VIDEO_FRAME
                    << Coder.LENGTH_FIELD_SIZE) | Coder.IS_EXTENDED);
            coder.writeInt(length);
        } else {
            coder.writeShort((MovieTypes.VIDEO_FRAME
                    << Coder.LENGTH_FIELD_SIZE) | length);
        }
        coder.writeShort(identifier);
        coder.writeShort(frameNumber);
        coder.writeBytes(data);
    }
}
