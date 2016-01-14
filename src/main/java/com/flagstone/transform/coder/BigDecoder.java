/*
 * BigDecoder.java
 * Transform
 *
 * Copyright (c) 2010 Flagstone Software Ltd. All rights reserved.
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

package com.flagstone.transform.coder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

/**
 * BigDecoder wraps an InputStream with a buffer to reduce the amount of
 * memory required to decode an image or sound and to improve efficiency by
 * reading data from a file or external source in blocks. Word values are
 * read in Big-Endian format with the most significant byte decoded first.
 */
@SuppressWarnings("PMD.TooManyMethods")
public class BigDecoder {
    /** The default size, in bytes, for the internal buffer. */
    public static final int BUFFER_SIZE = 4096;

    /** Bit mask applied to bytes when converting to unsigned integers. */
    private static final int BYTE_MASK = 255;
    /** Number of bits in an int. */
    private static final int BITS_PER_INT = 32;
    /** Number of bits in a byte. */
    private static final int BITS_PER_BYTE = 8;
    /** Left shift to convert number of bits to number of bytes. */
    private static final int BITS_TO_BYTES = 3;
    /** Right shift to convert number of bits to number of bytes. */
    private static final int BYTES_TO_BITS = 3;
    /** Number of bits to shift when aligning a value to the second byte. */
    private static final int TO_BYTE1 = 8;
    /** Number of bits to shift when aligning a value to the third byte. */
    private static final int TO_BYTE2 = 16;
    /** Number of bits to shift when aligning a value to the fourth byte. */
    private static final int TO_BYTE3 = 24;

    /** The underlying input stream. */
    private final transient InputStream stream;
    /** The buffer for data read from the stream. */
    private final transient byte[] buffer;
    /** Stack for storing file locations. */
    private final transient Stack<Integer>locations;
    /** The position of the buffer relative to the start of the stream. */
    private transient int pos;
    /** The position from the start of the buffer. */
    private transient int index;
    /** The offset in bits in the current buffer location. */
    private transient int offset;
    /** The number of bytes available in the current buffer. */
    private transient int size;

    /**
     * Create a new BigDecoder for the underlying InputStream with the
     * specified buffer size.
     *
     * @param streamIn the stream from which data will be read.
     * @param length the size in bytes of the buffer.
     */
    public BigDecoder(final InputStream streamIn, final int length) {
        stream = streamIn;
        buffer = new byte[length];
        locations = new Stack<Integer>();
    }

    /**
     * Create a new BigDecoder for the underlying InputStream using the
     * default buffer size.
     *
     * @param streamIn the stream from which data will be read.
     */
    public BigDecoder(final InputStream streamIn) {
        stream = streamIn;
        buffer = new byte[BUFFER_SIZE];
        locations = new Stack<Integer>();
    }

    /**
     * Fill the internal buffer. Any unread bytes are copied to the start of
     * the buffer and the remaining space is filled with data from the
     * underlying stream.
     *
     * @throws IOException if an error occurs reading from the underlying
     * input stream.
     */
    public void fill() throws IOException {
        final int diff = size - index;
        pos += index;

        if (index < size) {
            for (int i = 0; i < diff; i++) {
                buffer[i] = buffer[index++];
            }
        }

        int bytesRead = 0;
        int bytesToRead = buffer.length - diff;

        index = diff;
        size = diff;

        do {
            bytesRead = stream.read(buffer, index, bytesToRead);
            if (bytesRead == -1) {
                bytesToRead = 0;
            } else {
                index += bytesRead;
                size += bytesRead;
                bytesToRead -= bytesRead;
            }
        } while (bytesToRead > 0);

        index = 0;
    }

    /**
     * Mark the current position.
     * @return the current position.
     */
    public int mark() {
        return locations.push(pos + index);
    }

    /**
     * Discard the last saved position.
     */
    public void unmark() {
        locations.pop();
    }

    /**
     * Reposition the decoder to the point recorded by the last call to the
     * mark() method.
     *
     * @throws IOException if the internal buffer was filled after mark() was
     * called.
     */
    public void reset() throws IOException {
        int location;

        if (locations.isEmpty()) {
            location = 0;
        } else {
            location = locations.peek();
        }
        if (location - pos < 0) {
            throw new IOException();
        }
        index = location - pos;
    }

    /**
     * Change the position of the decoder within the internal buffer.
     *
     * @param location
     *            the offset in bytes from the start of the internal buffer.
     *
     * @throws IOException if the internal buffer was filled after mark() was
     * called.
     */
    public void move(final int location) throws IOException {
        if (size - index == 0) {
            fill();
        }
        if (location < 0 || location > size) {
            throw new ArrayIndexOutOfBoundsException();
        }
        index = location;
        offset = 0;
    }

    /**
     * Get the number of bytes read from the last saved position.
     *
     * @return the number of bytes read since the mark() method was last called.
     */
    public int bytesRead() {
        return (pos + index) - locations.peek();
    }

    /**
     * Changes the location to the next byte boundary.
     */
    public void alignToByte() {
        if (offset > 0) {
            index += 1;
            offset = 0;
        }
    }

    /**
     * Skips over and discards n bytes of data.
     *
     * @param count the number of bytes to skip.
     *
     * @throws IOException if an error occurs reading from the underlying
     * input stream.
     */
    public void skip(final int count) throws IOException {
        if (size - index == 0) {
            fill();
        }
        if (count < size - index) {
            index += count;
        } else {
            int toSkip = count;
            int diff;
            while (toSkip > 0) {
                diff = size - index;
                if (toSkip <= diff) {
                    index += toSkip;
                    toSkip = 0;
                } else {
                    index += diff;
                    toSkip -= diff;
                    fill();
                    if (size - index == 0) {
                        throw new ArrayIndexOutOfBoundsException();
                    }
                }
            }
        }
    }

    /**
     * Is there any more data to read.
     *
     * @return true there is no more data to read from the stream.
     *
     * @throws IOException if an error from the underlying input stream.
     */
    public boolean eof() throws IOException {
        if (size - index == 0) {
            fill();
        }
        return size - index == 0;
    }

    /**
     * Read a bit field.
     *
     * @param numberOfBits
     *            the number of bits to read.
     *
     * @param signed
     *            indicates whether the integer value read is signed.
     *
     * @return the value read.
     *
     * @throws IOException if an error occurs reading from the underlying
     * input stream.
     */
    public int readBits(final int numberOfBits, final boolean signed)
            throws IOException {

        int pointer = (index << BYTES_TO_BITS) + offset;

        if (((size << BYTES_TO_BITS) - pointer) < numberOfBits) {
            fill();
            pointer = (index << BYTES_TO_BITS) + offset;
        }

        int value = 0;

        if (numberOfBits > 0) {

            if (pointer + numberOfBits > (size << BYTES_TO_BITS)) {
                throw new ArrayIndexOutOfBoundsException();
            }

            for (int i = BITS_PER_INT; (i > 0)
                    && (index < buffer.length); i -= BITS_PER_BYTE) {
                value |= (buffer[index++] & BYTE_MASK) << (i - BITS_PER_BYTE);
            }

            value <<= offset;

            if (signed) {
                value >>= BITS_PER_INT - numberOfBits;
            } else {
                value >>>= BITS_PER_INT - numberOfBits;
            }

            pointer += numberOfBits;
            index = pointer >>> BITS_TO_BYTES;
            offset = pointer & Coder.LOWEST3;
        }

        return value;
    }

    /**
     * Read an unsigned byte.
     *
     * @return an 8-bit unsigned value.
     *
     * @throws IOException if an error occurs reading from the underlying
     * input stream.
     */
    public int readByte() throws IOException {
        if (size - index < 1) {
            fill();
        }
        if (index + 1 > size) {
            throw new ArrayIndexOutOfBoundsException();
        }
        return buffer[index++] & BYTE_MASK;
    }

    /**
     * Reads an array of bytes.
     *
     * @param bytes
     *            the array that will contain the bytes read.
     *
     * @return the array of bytes.
     *
     * @throws IOException if an error occurs reading from the underlying
     * input stream.
     */
    public byte[] readBytes(final byte[] bytes) throws IOException {
        final int wanted = bytes.length;
        int dest = 0;
        int read = 0;

        int available;
        int remaining;

        while (read < wanted) {
            available = size - index;
            remaining = wanted - read;
            if (available > remaining) {
                available = remaining;
            }
            System.arraycopy(buffer, index, bytes, dest, available);
            read += available;
            index += available;
            dest += available;

            if (read < wanted && index == size) {
                fill();
            }
        }
        return bytes;
    }

    /**
     * Reads an array of bytes.
     *
     * @param bytes the array that will contain the bytes read.
     * @param start the offset from the start of the array of bytes where
     * the data will be written.
     * @param length the number of bytes to write to the array.
     * @return the array of bytes.
     *
     * @throws IOException if an error occurs reading from the underlying
     * input stream.
     */
    public byte[] readBytes(final byte[] bytes, final int start,
             final int length) throws IOException {
        final int wanted = length;
        int dest = start;
        int read = 0;

        int available;
        int remaining;

        while (read < wanted) {
            available = size - index;
            remaining = wanted - read;
            if (available > remaining) {
                available = remaining;
            }
            System.arraycopy(buffer, index, bytes, dest, available);
            read += available;
            index += available;
            dest += available;

            if (index == size) {
                fill();
            }
        }
        return bytes;
    }

    /**
     * Read an unsigned 16-bit integer.
     *
     * @return the value read.
     *
     * @throws IOException if an error occurs reading from the underlying
     * input stream.
     */
    public int readUnsignedShort() throws IOException {
        if (size - index < 2) {
            fill();
        }
        if (index + 2 > size) {
            throw new ArrayIndexOutOfBoundsException();
        }
        int value = (buffer[index++] & BYTE_MASK) << TO_BYTE1;
        value |= buffer[index++] & BYTE_MASK;
        return value;
    }

    /**
     * Read a signed 16-bit integer.
     *
     * @return the value read.
     *
     * @throws IOException if an error occurs reading from the underlying
     * input stream.
     */
    public int readShort() throws IOException {
        if (size - index < 2) {
            fill();
        }
        if (index + 2 > size) {
            throw new ArrayIndexOutOfBoundsException();
        }
        int value = buffer[index++] << TO_BYTE1;
        value |= buffer[index++] & BYTE_MASK;
        return value;
    }

    /**
     * Read an unsigned 32-bit integer.
     *
     *
     * @throws IOException if an error occurs reading from the underlying
     * input stream.
     * @return the value read.
     */
    public int scanInt() throws IOException {
        if (size - index < 4) {
            fill();
        }
        if (index + 4 > size) {
            throw new ArrayIndexOutOfBoundsException();
        }
        int addr = index;
        int value = (buffer[addr++] & BYTE_MASK) << TO_BYTE3;
        value |= (buffer[addr++] & BYTE_MASK) << TO_BYTE2;
        value |= (buffer[addr++] & BYTE_MASK) << TO_BYTE1;
        value |= buffer[addr] & BYTE_MASK;
        return value;
    }

    /**
     * Read an unsigned 32-bit integer.
     *
     *
     * @throws IOException if an error occurs reading from the underlying
     * input stream.
     * @return the value read.
     */
    public int readInt() throws IOException {
        if (size - index < 2) {
            fill();
        }
        if (index + 4 > size) {
            throw new ArrayIndexOutOfBoundsException();
        }
        int value = (buffer[index++] & BYTE_MASK) << TO_BYTE3;
        value |= (buffer[index++] & BYTE_MASK) << TO_BYTE2;
        value |= (buffer[index++] & BYTE_MASK) << TO_BYTE1;
        value |= buffer[index++] & BYTE_MASK;
        return value;
    }
}
