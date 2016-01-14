/*
 * JPEGInfo.java
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

package com.flagstone.transform.image;

import com.flagstone.transform.coder.Coder;


/**
 * JPEGInfo is used to extract the width and height from a JPEG encoded image.
 */
public class JPEGInfo {

    /** Bit mask for the least significant byte. */
    private static final int BYTE_MASK = 255;

    /** Marks the start of an image. */
    public static final int SOI = 0xFFD8;
    /** Marks the end of an image. */
    public static final int EOI = 0xFFD9;
    /** Marks the start of a frame - baseline DCT. */
    public static final int SOF0 = 0xFFC0;
    /** Marks the start of a frame - progressive DCT. */
    public static final int SOF2 = 0xFFC2;
    /** Marks the start of a JPG block. */
    public static final int JPG = 0xFFC8;
    /** Marks the start of a JPG block. */
    public static final int SOFF = 0xFFCF;
    /** Marks the Huffman table. */
    public static final int DHT = 0xFFC4;
    /** Marks the quantization table. */
    public static final int DQT = 0xFFDB;
    /** Marks the restart interval. */
    public static final int DRI = 0xFFDD;
    /** Marks the start of scan. */
    public static final int SOS = 0xFFDA;
    /** Marks a restart. */
    public static final int RST = 0xFFD0;
    /** Marks the start of an application specific block. */
    public static final int APP = 0xFFE0;
    /** Marks the start of an comment block. */
    public static final int COM = 0xFFFE;

    /** The width of the image. */
    private transient int width;
    /** The height of the image. */
    private transient int height;

    /**
     * Get the width of the image in pixels, not twips.
     *
     * @return the width of the image
     */
    public int getWidth() {
        return width;
    }

    /**
     * Return the height of the image in pixels, not twips.
     *
     * @return the height of the image
     */
    public int getHeight() {
        return height;
    }

    /**
     * Decode a JPEG encoded image.
     *
     * @param image the image data.
     */
    public void decode(final byte[] image) {
        final int limit = image.length - 2;
        int marker;
        int length;
        int index = 0;

        while (index < limit) {
            marker = ((image[index++] & BYTE_MASK) << Coder.TO_UPPER_BYTE)
                | (image[index++] & BYTE_MASK);

            if (marker == SOI || marker == EOI) {
                continue;
            }

            length = ((image[index++] & BYTE_MASK) << Coder.TO_UPPER_BYTE)
                | (image[index++] & BYTE_MASK);

            if (marker >= SOF0 && marker <= SOFF
                    && marker != DHT && marker != JPG) {
                index++;
                height = ((image[index++] & BYTE_MASK) << Coder.TO_UPPER_BYTE)
                    | (image[index++] & BYTE_MASK);
                width = ((image[index++] & BYTE_MASK) << Coder.TO_UPPER_BYTE)
                    | (image[index++] & BYTE_MASK);
                break;
            } else {
                index += length - 2;
            }
        }
    }
}
