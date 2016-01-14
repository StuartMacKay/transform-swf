/*
 * ScreenVideoPacket.java
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Copyable;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;

/**
 * The ScreenVideoPacket class is used to encode or decode a frame of video data
 * using Macromedia's ScreenVideo format.
 */
public class ScreenPacket implements Copyable<ScreenPacket> {

    /** Multiplier for the encoded value representing the block width. */
    private static final int PIXELS_PER_BLOCK = 16;

    /** Is this frame a key frame with blocks for the entire image. */
    private boolean keyFrame;
    /** The width of each block. */
    private int blockWidth;
    /** The height of each block. */
    private int blockHeight;
    /** The width of the image. */
    private int imageWidth;
    /** The height of the image. */
    private int imageHeight;
    /** List of blocks that make up the image. */
    private List<ImageBlock> imageBlocks;

    /**
     * Decode a screen packet from a block of data.
     * @param data the encoded screen packet data.
     * @throws IOException if the data cannot be decoded.
     */
    public ScreenPacket(final byte[] data) throws IOException {
        final ByteArrayInputStream stream = new ByteArrayInputStream(data);
        final SWFDecoder coder = new SWFDecoder(stream);

        int info = coder.readByte();
        keyFrame = (info & Coder.NIB1) != 0;

        info = (coder.readByte() << Coder.TO_UPPER_BYTE) + coder.readByte();
        blockWidth = (((info & Coder.NIB3) >> Coder.ALIGN_NIB3) + 1)
                    * PIXELS_PER_BLOCK;
        imageWidth = info & Coder.LOWEST12;

        info = (coder.readByte() << Coder.TO_UPPER_BYTE) + coder.readByte();
        blockHeight = (((info & Coder.NIB3) >> Coder.ALIGN_NIB3) + 1)
                    * PIXELS_PER_BLOCK;
        imageHeight = info & Coder.LOWEST12;

        final int columns = imageWidth / blockWidth
                + ((imageWidth % blockWidth > 0) ? 1 : 0);
        final int rows = imageHeight / blockHeight
                + ((imageHeight % blockHeight > 0) ? 1 : 0);

        int height = imageHeight;
        int width = imageWidth;

        imageBlocks = new ArrayList<ImageBlock>();
        ImageBlock block;

        int length;

        for (int i = 0; i < rows; i++, height -= blockHeight) {
            for (int j = 0; j < columns; j++, width -= blockWidth) {
                length = (coder.readByte() << Coder.TO_UPPER_BYTE)
                        + coder.readByte();

                if (length == 0) {
                    block = new ImageBlock(0, 0, new byte[]{});
                } else {
                    final int dataHeight = (height < blockHeight) ? height
                            : blockHeight;
                    final int dataWidth = (width < blockWidth) ? width
                            : blockWidth;

                    block = new ImageBlock(dataWidth, dataHeight, coder
                            .readBytes(new byte[length]));
                }

                imageBlocks.add(block);
            }
        }
    }

    /**
     * Create a ScreenPacket with no image blocks.
     */
    public ScreenPacket() {
        imageBlocks = new ArrayList<ImageBlock>();
    }

    /**
     * Creates a ScreenVideoPacket.
     *
     * @param key
     *            indicates whether the packet contains a key frame.
     * @param imgWidth
     *            the width of the frame.
     * @param imgHeight
     *            the height of the frame.
     * @param blkWidth
     *            the width of the blocks that make up the frame.
     * @param blkHeight
     *            the height of the blocks that make up the frame.
     * @param blocks
     *            the array of ImageBlocks that make up the frame.
     */
    public ScreenPacket(final boolean key, final int imgWidth,
            final int imgHeight, final int blkWidth, final int blkHeight,
            final List<ImageBlock> blocks) {
        setKeyFrame(key);
        setImageWidth(imgWidth);
        setImageHeight(imgHeight);
        setBlockWidth(blkWidth);
        setBlockHeight(blkHeight);
        setImageBlocks(blocks);
    }

    /**
     * Creates and initialises a ScreenPacket object using the values copied
     * from another ScreenPacket object.
     *
     * @param object
     *            a ScreenPacket object from which the values will be
     *            copied.
     */
    public ScreenPacket(final ScreenPacket object) {
        keyFrame = object.keyFrame;
        blockWidth = object.blockWidth;
        blockHeight = object.blockHeight;
        imageWidth = object.imageWidth;
        imageHeight = object.imageHeight;

        imageBlocks = new ArrayList<ImageBlock>(object.imageBlocks.size());

        for (final ImageBlock block : object.imageBlocks) {
            imageBlocks.add(block.copy());
        }
    }

    /**
     * Add an image block to the array that make up the frame.
     *
     * @param block
     *            an ImageBlock. Must not be null.
     * @return this object.
     */
    public ScreenPacket add(final ImageBlock block) {
        imageBlocks.add(block);
        return this;
    }

    /**
     * Does the packet contains a key frame.
     *
     * @return true if the packet is a key frame.
     */
    public boolean isKeyFrame() {
        return keyFrame;
    }

    /**
     * Sets whether the frame is a key frame (true) or normal one (false).
     *
     * @param key
     *            a boolean value indicating whether the frame is key (true) or
     *            normal (false.
     */
    public void setKeyFrame(final boolean key) {
        keyFrame = key;
    }

    /**
     * Get the width of the frame in pixels.
     *
     * @return the frame width.
     */
    public int getImageWidth() {
        return imageWidth;
    }

    /**
     * Sets the width of the frame.
     *
     * @param width
     *            the width of the frame in pixels.
     */
    public void setImageWidth(final int width) {
        imageWidth = width;
    }

    /**
     * Get the height of the frame in pixels.
     *
     * @return the image height.
     */
    public int getImageHeight() {
        return imageHeight;
    }

    /**
     * Set the height of the frame in pixels.
     *
     * @param height the image height.
     */
    public void setImageHeight(final int height) {
        imageHeight = height;
    }

    /**
     * Get the width of the blocks in pixels.
     *
     * @return the block width.
     */
    public int getBlockWidth() {
        return blockWidth;
    }

    /**
     * Sets the width of the image blocks.
     *
     * @param width
     *            the width of the blocks in pixels.
     */
    public void setBlockWidth(final int width) {
        blockWidth = width;
    }

    /**
     * Get the height of the blocks in pixels.
     *
     * @return the block width.
     */
    public int getBlockHeight() {
        return blockHeight;
    }

    /**
     * Sets the height of the image blocks.
     *
     * @param height
     *            the height of the blocks in pixels.
     */
    public void setBlockHeight(final int height) {
        blockHeight = height;
    }

    /**
     * Get the image blocks that have changed in this frame.
     *
     * @return the list of image blocks that make up the frame.
     */
    public List<ImageBlock> getImageBlocks() {
        return imageBlocks;
    }

    /**
     * Set the image blocks that have changed in this frame. If this is a key
     * frame then all image blocks are displayed.
     *
     * @param blocks
     *            the array of image blocks. Must not be null.
     */
    public void setImageBlocks(final List<ImageBlock> blocks) {
        imageBlocks = new ArrayList<ImageBlock>(blocks);
    }

    /** {@inheritDoc} */
    @Override
	public ScreenPacket copy() {
        return new ScreenPacket(this);
    }

    /**
     * Encode this ScreenPacket.
     * @return the data representing the encoded image blocks.
     * @throws IOException if there is an error encoding the blocks.
     */
    public byte[] encode() throws IOException {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final SWFEncoder coder = new SWFEncoder(stream);

        int bits = keyFrame ? Coder.BIT4 : Coder.BIT5;
        bits |= Coder.BIT0 | Coder.BIT1;
        coder.writeByte(bits);

        int word = ((blockWidth / PIXELS_PER_BLOCK) - 1) << Coder.TO_UPPER_NIB;
        word |= imageWidth & Coder.LOWEST12;
        coder.writeByte(word >> Coder.TO_LOWER_BYTE);
        coder.writeByte(word);

        word = ((blockHeight / PIXELS_PER_BLOCK) - 1) << Coder.TO_UPPER_NIB;
        word |= imageHeight & Coder.LOWEST12;
        coder.writeByte(word >> Coder.TO_LOWER_BYTE);
        coder.writeByte(word);

        byte[] blockData;

        for (final ImageBlock block : imageBlocks) {
            if (block.isEmpty()) {
                coder.writeShort(0);
            } else {
                blockData = block.getBlock();
                coder.writeByte(blockData.length >> Coder.TO_LOWER_BYTE);
                coder.writeByte(blockData.length);
                coder.writeBytes(blockData);
            }
        }

        return stream.toByteArray();
    }
}
