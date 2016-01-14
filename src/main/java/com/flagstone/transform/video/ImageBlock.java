/*
 * ImageBlock.java
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

import java.util.Arrays;

import com.flagstone.transform.coder.Copyable;

/**
 * ImageBlock is used to sub-divide an image into a set of blocks so they can be
 * streamed using Screen Video. Image blocks are compared so only pixel
 * information for the portions of the image that change are sent.
 *
 * <p>
 * An image is divided by tiling the blocks across the image from top-left to
 * bottom right. If the image is not covered an integer number of blocks then
 * the size of the blocks along the right and bottom edges of the image are
 * reduced in size.
 * </p>
 *
 * @see DefineVideo
 */
public class ImageBlock implements Copyable<ImageBlock> {
    /** Width of the block in pixels. */
    private final transient int width;
    /** Height of the block in pixels. */
    private final transient int height;
    /** The block pixels. */
    private final transient byte[] block;

    /**
     * Create a new image block with the specified width and height and image
     * data. The image is compressed using the zip format.
     *
     * @param blockWidth
     *            the width of the block in pixels.
     * @param blockHeight
     *            the height of the block in pixels
     * @param pixels
     *            the pixels covered by the block, compressed using the zip
     *            format.
     */
    public ImageBlock(final int blockWidth, final int blockHeight,
            final byte[] pixels) {
        width = blockWidth;
        height = blockHeight;
        block = Arrays.copyOf(pixels, pixels.length);
    }

    /**
     * Creates and initialises a ImageBlock object using the values copied
     * from another ImageBlock object.
     *
     * @param object
     *            a ImageBlock object from which the values will be
     *            copied.
     */
    public ImageBlock(final ImageBlock object) {
        width = object.width;
        height = object.height;
        block = object.block;
    }

    /**
     * Get the width of the block. although the block size is specified in
     * parent ScreenVideoPacket object the actual block size used may vary if
     * the tiled array of blocks overlaps the edge of the image.
     *
     * @return the width of the block.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get the height of the block. although the block size is specified in
     * parent ScreenVideoPacket object the actual block size used may vary if
     * the tiled array of blocks overlaps the edge of the image.
     *
     * @return the height of the block.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Get the zipped image data for the block.
     *
     * @return a copy of the block data.
     */
    public byte[] getBlock() {
        return Arrays.copyOf(block, block.length);
    }

    /**
     * When a ScreenVideo stream is created only the image blocks that change
     * are included. The blocks that do not change are encoded as empty blocks
     * which have width and height of zero and do not contain any image data.
     * This convenience method is used to determine when an image block contains
     * any valid image data.
     *
     * @return true if the block covers an area of the image that changed or
     *         false if no image data is included.
     */
    public boolean isEmpty() {
        return (width == 0) || (height == 0) || (block == null)
                || (block.length == 0);
    }

    /** {@inheritDoc} */
    public ImageBlock copy() {
        return new ImageBlock(this);
    }
}
