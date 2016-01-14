/*
 * DefineImage.java
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

package com.flagstone.transform.image;

import java.io.IOException;
import java.util.Arrays;

import com.flagstone.transform.Constants;
import com.flagstone.transform.MovieTypes;
import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.exception.IllegalArgumentRangeException;

/**
 * DefineImage is used to define an image compressed using the lossless zlib
 * compression algorithm.
 *
 * <p>
 * The class supports colour-mapped images where the image data contains an
 * index into a colour table or direct-mapped images where the colour is
 * specified directly.
 * </p>
 *
 * <p>
 * For colour-mapped images the colour table contains up to 256, 24-bit colours.
 * The image contains one byte for each pixel which is an index into the table
 * to specify the colour for that pixel. The colour table and the image data are
 * compressed as a single block, with the colour table placed before the image.
 * </p>
 *
 * <p>
 * For images where the colour is specified directly, the image data contains
 * either 16 or 24 bit colour values. For 16-bit colour values the most
 * significant bit is zero followed by three, 5-bit fields for the red, green
 * and blue channels:
 * </p>
 *
 * <pre>
 *  +-+--------+--------+--------+
 *  |0|   Red  |  Green |  Blue  |
 *  +-+--------+--------+--------+
 *  15                            0
 * </pre>
 *
 * <p>
 * Four bytes are used to represent 24-bit colours. The first byte is always set
 * to zero and the following bytes contain the colour values for the red, green
 * and blue colour channels.
 * </p>
 *
 * <p>
 * The number of bytes in each row of an image must be aligned to a 32-bit word
 * boundary. For example if an image if an icon is 25 pixels wide, then for an
 * 8-bit colour mapped image an additional three bytes (0x00) must be used to
 * pad each row; for a 16-bit direct mapped colour image an additional two bytes
 * must be used as padding.
 * </p>
 *
 * <p>
 * The image data is stored in zlib compressed form within the object. For
 * colour-mapped images the compressed data contains the colour table followed
 * by the image data. The colour table is omitted for direct-mapped images.
 * </p>
 *
 * @see DefineImage2
 */
public class DefineImage implements ImageTag {

    /** Format string used in toString() method. */
    private static final String FORMAT = "DefineImage: { identifier=%d;"
            + " pixelSize=%d; width=%d; height=%d; tableSize=%d;"
            + " image=byte<%d> ...}";

    /** Identifies an indexed image. */
    private static final int IDX_FORMAT = 3;
    /** Identifies a true-color image with 16-bit pixels. */
    private static final int RGB5_FORMAT = 4;
    /** Identifies a true-color image with 24-bit pixels. */
    private static final int RGB8_FORMAT = 5;

    /** Size of a pixel in an indexed image. */
    private static final int IDX_SIZE = 8;
    /** Size of a pixel in an RGB5 image. */
    private static final int RGB5_SIZE = 16;
    /** Size of a pixel in an RGB image. */
    private static final int RGB8_SIZE = 24;
    /** Number of entries in the colour table of an indexed image. */
    private static final int TABLE_SIZE = 256;

    /** The unique identifier for this object. */
    private int identifier;
    /** The width of the image in pixels. */
    private int width;
    /** The height of the image in pixels. */
    private int height;
    /** The size, in bits, of each pixel. */
    private int pixelSize;
    /** The length, in bytes, of the colour table. */
    private int tableSize;
    /** The compressed colour table and image data. */
    private byte[] image;

    /** The length of the object, minus the header, when it is encoded. */
    private transient int length;

    /**
     * Creates and initialises a DefineImage object using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public DefineImage(final SWFDecoder coder) throws IOException {
        length = coder.readUnsignedShort() & Coder.LENGTH_FIELD;
        if (length == Coder.IS_EXTENDED) {
            length = coder.readInt();
        }
        coder.mark();
        identifier = coder.readUnsignedShort();

        switch (coder.readByte()) {
        case IDX_FORMAT:
            pixelSize = IDX_SIZE;
            break;
        case RGB5_FORMAT:
            pixelSize = RGB5_SIZE;
            break;
        case RGB8_FORMAT:
            pixelSize = RGB8_SIZE;
            break;
        default:
            pixelSize = 0;
            break;
        }

        width = coder.readUnsignedShort();
        height = coder.readUnsignedShort();

        if (pixelSize == IDX_SIZE) {
            tableSize = coder.readByte() + 1;
            // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 1 LINES
            image = coder.readBytes(new byte[length - 8]);
        } else {
            // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 1 LINES
            image = coder.readBytes(new byte[length - 7]);
        }
        coder.check(length);
        coder.unmark();
    }

    /**
     * Creates a DefineImage object defining a colour-mapped image.
     *
     * @param uid
     *            the unique identifier for this object. Must be in the range
     *            1..65535.
     * @param imgWidth
     *            the width of the image. Must be in the range 0..65535.
     * @param imgHeight
     *            the height of the image. Must be in the range 0..65535.
     * @param size
     *            the number of entries in the colour table in the compressed
     *            data. Each entry is 24 bits. Must be in the range 1..256.
     * @param data
     *            the zlib compressed colour table and image data.
     */
    public DefineImage(final int uid, final int imgWidth, final int imgHeight,
            final int size, final byte[] data) {
        setIdentifier(uid);
        setWidth(imgWidth);
        setHeight(imgHeight);
        setPixelSize(IDX_SIZE);
        setTableSize(size);
        setImage(data);
    }

    /**
     * Creates a DefineImage object defining an true-colour image.
     *
     * @param uid
     *            the unique identifier for this object. Must be in the range
     *            1..65535.
     * @param imgWidth
     *            the width of the image. Must be in the range 0..65535.
     * @param imgHeight
     *            the height of the image. Must be in the range 0..65535.
     * @param data
     *            the zlib compressed image data.
     * @param size
     *            the size of each pixel, either 16 or 24 bits.
     */
    public DefineImage(final int uid, final int imgWidth, final int imgHeight,
            final byte[] data, final int size) {
        setIdentifier(uid);
        setWidth(imgWidth);
        setHeight(imgHeight);
        setPixelSize(size);
        tableSize = 0;
        setImage(data);
    }

    /**
     * Creates and initialises a DefineImage object using the values copied
     * from another DefineImage object.
     *
     * @param object
     *            a DefineImage object from which the values will be
     *            copied.
     */
    public DefineImage(final DefineImage object) {
        identifier = object.identifier;
        width = object.width;
        height = object.height;
        pixelSize = object.pixelSize;
        tableSize = object.tableSize;
        image = object.image;
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
     * Get the width of the image in pixels (not twips).
     *
     * @return the width of the image.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get the height of the image in pixels (not twips).
     *
     * @return the height of the image.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the number of bits used to represent each pixel. Either 8, 16 or
     * 24 bits. The pixel size is 8-bits for colour-mapped images and 16 or 24
     * bits for images where the colour is specified directly.
     *
     * @return the number of bits per pixel: 8, 16 or 24.
     */
    public int getPixelSize() {
        return pixelSize;
    }

    /**
     * Get the number of entries in the colour table encoded the compressed
     * image. For images where the colour is specified directly in the image
     * then the table size is zero.
     *
     * @return the number of entries in the colour table.
     */
    public int getTableSize() {
        return tableSize;
    }

    /**
     * Get a copy of the compressed colour table and image.
     *
     * @return  a copy of the data.
     */
    public byte[] getImage() {
        return Arrays.copyOf(image, image.length);
    }

    /**
     * Sets the width of the image in pixels.
     *
     * @param aNumber
     *            the width of the image. Must be in the range 0..65535.
     */
    public void setWidth(final int aNumber) {
        if ((aNumber < 0) || (aNumber > Coder.USHORT_MAX)) {
            throw new IllegalArgumentRangeException(
                    0, Coder.USHORT_MAX, aNumber);
        }
        width = aNumber;
    }

    /**
     * Sets the height of the image in pixels.
     *
     * @param aNumber
     *            the height of the image in pixels. Must be in the range
     *            0..65535.
     */
    public void setHeight(final int aNumber) {
        if ((aNumber < 0) || (aNumber > Coder.USHORT_MAX)) {
            throw new IllegalArgumentRangeException(
                    0, Coder.USHORT_MAX, aNumber);
        }
        height = aNumber;
    }

    /**
     * Sets the size of the pixel in bits: 8, 16 or 32. The pixel size is 8-bits
     * for colour-mapped images and 16 or 24 bits for images where the colour is
     * specified directly.
     *
     * @param size
     *            the size of each pixel in bits. Must be either 8, 16 or 24.
     */
    public void setPixelSize(final int size) {
        if ((size != IDX_SIZE) && (size != RGB5_SIZE) && (size != RGB8_SIZE)) {
            throw new IllegalArgumentException(
                    "Pixel size must be either 8, 16 or 24.");
        }
        pixelSize = size;
    }

    /**
     * Sets the number of entries in the colour table in the compressed image.
     * For images where the colour is specified directly in the image then the
     * table size should be zero.
     *
     * @param size
     *            the number of entries in the colour table in the compressed
     *            image, in the range 1..256.
     */
    public void setTableSize(final int size) {
        if ((size < 1) || (size > TABLE_SIZE)) {
            throw new IllegalArgumentException(
                    "Colour table size must be in the range 1..256.");
        }
        tableSize = size;
    }

    /**
     * Sets the data containing the compressed image and colour table.
     *
     * @param bytes
     *            byte array containing zlib compressed colour table and image.
     *            Must not be null.
     */
    public void setImage(final byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException();
        }
        image = Arrays.copyOf(bytes, bytes.length);
    }

    /** {@inheritDoc} */
    public DefineImage copy() {
        return new DefineImage(this);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, identifier, pixelSize, width, height,
                tableSize, image.length);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 1 LINES
        length = 7;
        length += (pixelSize == IDX_SIZE) ? 1 : 0;
        length += image.length;

        return Coder.LONG_HEADER + length;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {

        coder.writeShort((MovieTypes.DEFINE_IMAGE << Coder.LENGTH_FIELD_SIZE)
                | Coder.IS_EXTENDED);
        coder.writeInt(length);
        if (Constants.DEBUG) {
            coder.mark();
        }
        coder.writeShort(identifier);

        switch (pixelSize) {
        case IDX_SIZE:
            coder.writeByte(IDX_FORMAT);
            break;
        case RGB5_SIZE:
            coder.writeByte(RGB5_FORMAT);
            break;
        case RGB8_SIZE:
            coder.writeByte(RGB8_FORMAT);
            break;
        default:
            break;
        }

        coder.writeShort(width);
        coder.writeShort(height);

        if (pixelSize == IDX_SIZE) {
            coder.writeByte(tableSize - 1);
        }

        coder.writeBytes(image);
        if (Constants.DEBUG) {
            coder.check(length);
            coder.unmark();
        }
    }
}
