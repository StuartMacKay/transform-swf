/*
 * DefineVideo.java
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

import com.flagstone.transform.Constants;
import com.flagstone.transform.DefineTag;
import com.flagstone.transform.MovieTypes;
import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.exception.IllegalArgumentRangeException;

/**
 * The DefineVideo class is used to define a video stream within a Flash file.
 *
 * <p>
 * Video objects contain a unique identifier and are treated in the same way as
 * shapes, buttons, images, etc. The video data displayed is define using the
 * VideoFrame class. Each frame of video is displayed whenever display list is
 * updated using the ShowFrame object - any timing information stored within the
 * video data is ignored. The actual video data is encoded using the VideoFrame
 * class.
 * </p>
 */
public class DefineVideo implements DefineTag {

    /** Format string used in toString() method. */
    private static final String FORMAT = "DefineVideo: { identifier=%d;"
            + " frameCount=%d; width=%d; height=%d; deblocking=%s;"
            + " smoothing=%s; codec=%s}";

    /** The unique identifier for this object. */
    private int identifier;
    /** The number of frames in the video. */
    private int frameCount;
    /** The width of the frame. */
    private int width;
    /** The height of the frame. */
    private int height;
    /** Code indicating whether deblocking is applied. */
    private int deblocking;
    /** Is smoothing applied. */
    private boolean smoothed;
    /** Code representing the codec used. */
    private int codec;

    /** The length of the object, minus the header, when it is encoded. */
    private transient int length;

    /**
     * Creates and initialises a DefineVideo object using values encoded in the
     * Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public DefineVideo(final SWFDecoder coder) throws IOException {
        length = coder.readUnsignedShort() & Coder.LENGTH_FIELD;
        if (length == Coder.IS_EXTENDED) {
            length = coder.readInt();
        }
        coder.mark();
        identifier = coder.readUnsignedShort();
        frameCount = coder.readUnsignedShort();
        width = coder.readUnsignedShort();
        height = coder.readUnsignedShort();

        final int info = coder.readByte();
        deblocking = (info & 0x06) >> 1;
        smoothed = (info & 0x01) == 1;
        codec = coder.readByte();
        coder.check(length);
        coder.unmark();
    }

    /**
     * Creates a DefineVideo object with the specified parameters.
     *
     * @param uid
     *            the unique identifier for this object. Must be in the range
     *            1..65535.
     * @param count
     *            the number of video frames. Must be in the range 0..65535.
     * @param frameWidth
     *            the width of each frame in pixels. Must be in the range
     *            0..65535.
     * @param frameHeight
     *            the height of each frame in pixels. Must be in the range
     *            0..65535.
     * @param deblock
     *            controls whether the Flash Player's deblocking filter is used,
     *            either Off, On or UseVideo to allow the video data to specify
     *            whether the deblocking filter is used.
     * @param smoothing
     *            turns smoothing on or off to improve the quality of the
     *            displayed image.
     * @param videoCodec
     *            the format of the video data. Flash 6 supports H263. Support
     *            for Macromedia's ScreenVideo format was added in Flash 7.
     */
    public DefineVideo(final int uid, final int count, final int frameWidth,
            final int frameHeight, final Deblocking deblock,
            final boolean smoothing, final VideoFormat videoCodec) {
        setIdentifier(uid);
        setFrameCount(count);
        setWidth(frameWidth);
        setHeight(frameHeight);
        setDeblocking(deblock);
        setSmoothed(smoothing);
        setCodec(videoCodec);
    }

    /**
     * Creates and initialises an DefineVideo object using the values
     * copied from another DefineVideo object.
     *
     * @param object
     *            a DefineVideo object from which the values will be
     *            copied.
     */
    public DefineVideo(final DefineVideo object) {
        identifier = object.identifier;
        frameCount = object.frameCount;
        width = object.width;
        height = object.height;
        deblocking = object.deblocking;
        smoothed = object.smoothed;
        codec = object.codec;
    }

    /** {@inheritDoc} */
    public int getIdentifier() {
        return identifier;
    }

    /** {@inheritDoc} */
    public void setIdentifier(final int uid) {
        if ((uid < 1) || (uid > Coder.USHORT_MAX)) {
            throw new IllegalArgumentRangeException(
                    1, Coder.USHORT_MAX, uid);
        }
        identifier = uid;
    }

    /**
     * Get the number of frames in the video.
     *
     * @return the number of frames.
     */
    public int getFrameCount() {
        return frameCount;
    }

    /**
     * Sets the number of frames in the video.
     *
     * @param count
     *            the number of video frames. Must be in the range 0..65535.
     */
    public void setFrameCount(final int count) {
        if ((count < 0) || (count > Coder.USHORT_MAX)) {
            throw new IllegalArgumentRangeException(
                    0, Coder.USHORT_MAX, count);
        }
        frameCount = count;
    }

    /**
     * Get the width of each frame in pixels.
     *
     * @return the frame width.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Sets the width of each frame in pixels.
     *
     * @param size
     *            the width of the frame. Must be in the range 0..65535.
     */
    public void setWidth(final int size) {
        if ((size < 0) || (size > Coder.USHORT_MAX)) {
            throw new IllegalArgumentRangeException(
                    0, Coder.USHORT_MAX, size);
        }
        width = size;
    }

    /**
     * Get the height of each frame in pixels.
     *
     * @return the frame height.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Sets the height of each frame in pixels.
     *
     * @param size
     *            the height of the frame. Must be in the range 0..65535.
     */
    public void setHeight(final int size) {
        if ((size < 0) || (size > Coder.USHORT_MAX)) {
            throw new IllegalArgumentRangeException(
                    0, Coder.USHORT_MAX, size);
        }
        height = size;
    }

    /**
     * Get the method used to control the Flash Player's deblocking filter,
     * either OFF, ON or USE_VIDEO.
     *
     * @return the deblocking applied to the frame.
     */
    public Deblocking getDeblocking() {
        Deblocking value;
        switch (deblocking) {
        case 1:
            value = Deblocking.OFF;
            break;
        case 2:
            value = Deblocking.ON;
            break;
        case 3:
            value = Deblocking.LEVEL2;
            break;
        case 4:
            value = Deblocking.LEVEL3;
            break;
        case 5:
            value = Deblocking.LEVEL4;
            break;
        default:
            value = Deblocking.VIDEO;
            break;
        }
        return value;
    }

    /**
     * Sets the method used to control the Flash Player's deblocking filter.
     *
     * @param value
     *            the deblocking filter control, either OFF, ON or USE_VIDEO to
     *            allow the video data to specify whether the deblocking filter
     *            is used.
     */
    public void setDeblocking(final Deblocking value) {
        switch (value) {
        case VIDEO:
            deblocking = 0;
            break;
        case OFF:
            deblocking = 1;
            break;
        case ON:
            deblocking = 2;
            break;
        case LEVEL2:
            deblocking = 3;
            break;
        case LEVEL3:
            deblocking = 4;
            break;
        case LEVEL4:
            deblocking = 5;
            break;
        default:
            throw new IllegalArgumentException();
        }
    }

    /**
     * Will the Flash Player will apply smoothing to the video when it is
     * played.
     *
     * @return true if smoothing is applied.
     */
    public boolean isSmoothed() {
        return smoothed;
    }

    /**
     * Sets whether Flash Player's smoothing filter is on or off when the video
     * is played.
     *
     * @param smoothing
     *            true if smoothing is turned on, false if it is turned off.
     */
    public void setSmoothed(final boolean smoothing) {
        smoothed = smoothing;
    }

    /**
     * Get the format used to encode the video data.
     *
     * @return the format used to encode the video.
     */
    public VideoFormat getCodec() {
        VideoFormat value;
        switch (codec) {
        case Coder.BIT2 | Coder.BIT0:
            value = VideoFormat.VP6ALPHA;
            break;
        case Coder.BIT2:
            value = VideoFormat.VP6;
            break;
        case Coder.BIT1:
            value = VideoFormat.H263;
            break;
        case Coder.BIT0 | Coder.BIT1:
            value = VideoFormat.SCREEN;
            break;
        default:
            throw new IllegalStateException();
        }
        return value;
    }

    /**
     * Set the format used to encode the video data.
     *
     * @param format
     *            the format used encode the video, either VideoFormat.H263,
     *            VideoFormat.SCREEN, VideoFormat.VP6 or VideoFormat.VP6ALPHA.
     */
    public void setCodec(final VideoFormat format) {
        switch (format) {
        case H263:
            codec = Coder.BIT1;
            break;
        case SCREEN:
            codec = Coder.BIT0 | Coder.BIT1;
            break;
        case VP6:
            codec = Coder.BIT2;
            break;
        case VP6ALPHA:
            codec = Coder.BIT2 | Coder.BIT0;
        default:
            throw new IllegalArgumentException();
        }
    }

    /** {@inheritDoc} */
    public DefineVideo copy() {
        return new DefineVideo(this);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, identifier, frameCount, width, height,
                deblocking, smoothed, codec);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 1 LINES
        length = 10;
        return Coder.SHORT_HEADER + length;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {

        if (length > Coder.HEADER_LIMIT) {
            coder.writeShort((MovieTypes.DEFINE_VIDEO
                    << Coder.LENGTH_FIELD_SIZE) | Coder.IS_EXTENDED);
            coder.writeInt(length);
        } else {
            coder.writeShort((MovieTypes.DEFINE_VIDEO
                    << Coder.LENGTH_FIELD_SIZE) | length);
        }
        if (Constants.DEBUG) {
            coder.mark();
        }
        coder.writeShort(identifier);
        coder.writeShort(frameCount);
        coder.writeShort(width);
        coder.writeShort(height);
        int bits = deblocking << 1;
        bits |= smoothed ? Coder.BIT0 : 0;
        coder.writeByte(bits);
        coder.writeByte(codec);
        if (Constants.DEBUG) {
            coder.check(length);
            coder.unmark();
        }
    }
}
