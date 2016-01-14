/*
 * SWFDecoder.java
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

package com.flagstone.transform.coder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Stack;

import com.flagstone.transform.CharacterEncoding;


/**
 * SWFDecoder wraps an InputStream with a buffer to reduce the amount of
 * memory required to decode a movie and to improve efficiency by reading
 * data from a file or external source in blocks.
 */
@SuppressWarnings("PMD.TooManyMethods")
public class SWFDecoder {
    /** The default size, in bytes, for the internal buffer. */
    public static final int BUFFER_SIZE = 4096;

    /** The default size, in bytes, for the reading strings. */
    private static final int STR_BUFFER_SIZE = 1024;
    /** Bit mask applied to bytes when converting to unsigned integers. */
    private static final int BYTE_MASK = 255;
    /** Number of bits to shift when aligning a value to the second byte. */
    private static final int TO_BYTE1 = 8;
    /** Number of bits to shift when aligning a value to the third byte. */
    private static final int TO_BYTE2 = 16;
    /** Number of bits to shift when aligning a value to the fourth byte. */
    private static final int TO_BYTE3 = 24;
    /** Number of bits in an int. */
    private static final int BITS_PER_INT = 32;
    /** Number of bits in a byte. */
    private static final int BITS_PER_BYTE = 8;
    /** Right shift to convert number of bits to number of bytes. */
    private static final int BITS_TO_BYTES = 3;
    /** Left shift to convert number of bytes to number of bits. */
    private static final int BYTES_TO_BITS = 3;

    /** The underlying input stream. */
    private final transient InputStream stream;
    /** The buffer for data read from the stream. */
    private final transient byte[] buffer;
    /** A buffer used for reading null terminated strings. */
    private transient byte[] stringBuffer;
    /** The character encoding used for strings. */
    private transient String encoding;
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
    /** The starting location from the last check-point. */
    private transient int location;
    /** The expected number number of bytes to be decoded. */
    private transient int expected;
    /** The difference from the expected number. */
    private transient int delta;

    /**
     * Create a new SWFDecoder for the underlying InputStream with the
     * specified buffer size.
     *
     * @param streamIn the stream from which data will be read.
     * @param length the size in bytes of the buffer.
     */
    public SWFDecoder(final InputStream streamIn, final int length) {
        stream = streamIn;
        buffer = new byte[length];
        stringBuffer = new byte[STR_BUFFER_SIZE];
        encoding = CharacterEncoding.UTF8.getEncoding();
        locations = new Stack<Integer>();
    }

    /**
     * Create a new SWFDecoder for the underlying InputStream using the
     * default buffer size.
     *
     * @param streamIn the stream from which data will be read.
     */
    public SWFDecoder(final InputStream streamIn) {
        stream = streamIn;
        buffer = new byte[BUFFER_SIZE];
        stringBuffer = new byte[BUFFER_SIZE];
        encoding = CharacterEncoding.UTF8.getEncoding();
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
     * Remember the current position.
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
        int last;

        if (locations.isEmpty()) {
            last = 0;
        } else {
            last = locations.peek();
        }
        if (last - pos < 0) {
            throw new IOException();
        }
        index = last - pos;
    }

    /**
     * Compare the number of bytes read since the last saved position and
     * record any difference.
     *
     * @param count the expected number of bytes read.
     */
    public void check(final int count) {
        expected = count;
        location = locations.peek();
        delta = count - ((pos + index) - location);
    }

    /**
     * Get the location recorded for the last call to check().
     * @return the position in the buffer of the call to mark() used by
     * check().
     */
    public int getLocation() {
        return location;
    }

    /**
     * Get the expected number of bytes from the last call to check().
     * @return the difference from the expected number of bytes decoded.
     */
    public int getExpected() {
        return expected;
    }

    /**
     * Get the difference from the expected number of bytes from the last call
     * to check().
     * @return the difference from the expected number of bytes decoded.
     */
    public int getDelta() {
        return delta;
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
     * Read-ahead a bit field.
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
    public int scanBits(final int numberOfBits, final boolean signed)
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

            index = pointer >>> BITS_TO_BYTES;
            offset = pointer & Coder.LOWEST3;
        }

        return value;
    }

    /**
     * Read an unsigned byte but do not advance the internal pointer.
     *
     * @return an 8-bit unsigned value.
     *
     * @throws IOException if an error occurs reading from the underlying
     * input stream.
     */
    public int scanByte() throws IOException {
        if (size - index < 1) {
            fill();
        }
        if (index + 1 > size) {
            throw new ArrayIndexOutOfBoundsException();
        }
        return buffer[index] & BYTE_MASK;
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

            if (index == size) {
                fill();
            }
        }
        return bytes;
    }

    /**
     * Sets the character encoding scheme used when encoding or decoding
     * strings.
     *
     * @param enc
     *            the CharacterEncoding that identifies how strings are encoded.
     */
    public void setEncoding(final CharacterEncoding enc) {
        encoding = enc.getEncoding();
    }

    /**
     * Read a string using the default character set defined in the decoder.
     *
     * @param length
     *            the number of bytes to read.
     *
     * @return the decoded string.
     *
     * @throws IOException if an error occurs reading from the underlying
     * input stream.
     */
    public String readString(final int length) throws IOException {
        final byte[] bytes = new byte[length];
        readBytes(bytes);
        int len;
        if (bytes[length - 1] == 0) {
            len = length - 1;
        } else {
            len = length;
        }
        return new String(bytes, 0, len, encoding);
    }

    /**
     * Read a null-terminated string using the default character set defined in
     * the decoder.
     *
     * @return the decoded string.
     *
     * @throws IOException if an error occurs reading from the underlying
     * input stream.
     */
    public String readString() throws IOException {
        int start = index;
        int length = 0;
        int available;
        int dest = 0;
        boolean finished = false;
        int count;

        while (!finished) {
            available = size - index;
            if (available == 0) {
                fill();
                available = size - index;
            }
            start = index;
            count = 0;
            for (int i = 0; i < available; i++) {
                if (buffer[index++] == 0) {
                    finished = true;
                    break;
                } else {
                    length++;
                    count++;
                }
            }
            if (stringBuffer.length < length) {
                stringBuffer = Arrays.copyOf(stringBuffer, length << 2);
            }
            System.arraycopy(buffer, start, stringBuffer, dest, count);
            dest += length;
        }
        return new String(stringBuffer, 0, length, encoding);
    }

    /**
     * Read an unsigned 16-bit integer.
     *
     * @return the value read.
     *
     * @throws IOException if an error occurs reading from the underlying
     * input stream.
     */
    public int scanUnsignedShort() throws IOException {
        if (size - index < 2) {
            fill();
        }
        if (index + 2 > size) {
            throw new ArrayIndexOutOfBoundsException();
        }
        int value = buffer[index] & BYTE_MASK;
        value |= (buffer[index + 1] & BYTE_MASK) << TO_BYTE1;
        return value;
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
        int value = buffer[index++] & BYTE_MASK;
        value |= (buffer[index++] & BYTE_MASK) << TO_BYTE1;
        return value;
    }

    /**
     * Read an unsigned 16-bit integer.
     *
     * @return the value read.
     *
     * @throws IOException if an error occurs reading from the underlying
     * input stream.
     */
    public int readSignedShort() throws IOException {
        if (size - index < 2) {
            fill();
        }
        if (index + 2 > size) {
            throw new ArrayIndexOutOfBoundsException();
        }
        int value = buffer[index++] & BYTE_MASK;
        value |= buffer[index++] << TO_BYTE1;
        return value;
    }

    /**
     * Read an unsigned 32-bit integer.
     *
     * @return the value read.
     *
     * @throws IOException if an error occurs reading from the underlying
     * input stream.
     */
    public int readInt() throws IOException {
        if (size - index < 4) {
            fill();
        }
        if (index + 4 > size) {
            throw new ArrayIndexOutOfBoundsException();
        }
        int value = buffer[index++] & BYTE_MASK;
        value |= (buffer[index++] & BYTE_MASK) << TO_BYTE1;
        value |= (buffer[index++] & BYTE_MASK) << TO_BYTE2;
        value |= (buffer[index++] & BYTE_MASK) << TO_BYTE3;
        return value;
    }

    /**
     * Read a 32-bit unsigned integer, encoded using a variable number of bytes.
     *
     * @return the value read.
     *
     * @throws IOException if an error occurs reading from the underlying
     * input stream.
     */
    public int readVarInt() throws IOException {

        if (size - index < 5) {
            fill();
        }

        int value = buffer[index++] & BYTE_MASK;
        final int mask = -1;
        int test = Coder.BIT7;
        int step = Coder.VAR_INT_SHIFT;

        while ((value & test) != 0) {
            value = ((buffer[index++] & BYTE_MASK) << step)
                + (value & mask >>> (32 - step));
            test <<= Coder.VAR_INT_SHIFT;
            step += Coder.VAR_INT_SHIFT;
        }
        return value;
    }

    /**
     * Number of bits to shift to obtain the sign in a half-precision
     * float-point value.
     */
    private static final int HALF_SIGN_SHIFT = 15;
    /**
     * Number of bits to shift to obtain the exponent in a half-precision
     * floating-point value.
     */
    private static final int HALF_EXP_SHIFT = 10;
    /**
     * The offset to apply to the exponent in a half-precision
     * floating-point value.
     */
    private static final int HALF_EXP_OFFSET = 15;
    /**
     * The maximum value of the exponent in a half-precision
     * floating-point value.
     */
    private static final int HALF_EXP_MAX = 31;

    /**
     * Number of bits to shift to obtain the sign in a single-precision
     * float-point value.
     */
    private static final int SIGN_SHIFT = 31;
    /**
     * Number of bits to shift to obtain the exponent in a singlr-precision
     * floating-point value.
     */
    private static final int EXP_SHIFT = 23;
    /**
     * The maximum value of the exponent in a single-precision
     * floating-point value.
     */
    private static final int EXP_MAX = 127;
    /**
     * Number of bits to shift to obtain the mantissa in a single-precision
     * float-point value.
     */
    private static final int MANT_SHIFT = 13;
    /**
     * The bit pattern used to represent Infinity in a single-precision
     * floating-point value.
     */
    private static final int INFINITY = 0x7f800000;

    /**
     * Read a single-precision floating point number.
     *
     * @return the value.
     *
     * @throws IOException if an error occurs reading from the underlying
     * input stream.
     */
    public float readHalf() throws IOException {
        final int bits = readUnsignedShort();
        final int sign = (bits >> HALF_SIGN_SHIFT) & Coder.BIT0;
        int exp = (bits >> HALF_EXP_SHIFT) & Coder.LOWEST5;
        int mantissa = bits & Coder.LOWEST10;
        float value;

        if (exp == 0) {
            if (mantissa == 0) { // Plus or minus zero
                value = Float.intBitsToFloat(sign << SIGN_SHIFT);
            } else { // Denormalized number -- renormalize it
                while ((mantissa & Coder.BIT10) == 0) {
                    mantissa <<= 1;
                    exp -=  1;
                }
                exp += 1;
                exp = exp + (EXP_MAX - HALF_EXP_OFFSET);
                mantissa &= ~Coder.BIT10;
                mantissa = mantissa << MANT_SHIFT;
                value = Float.intBitsToFloat((sign << SIGN_SHIFT)
                        | (exp << EXP_SHIFT) | mantissa);
            }
        } else if (exp == HALF_EXP_MAX) {
            if (mantissa == 0) { // Inf
                value = Float.intBitsToFloat((sign << SIGN_SHIFT) | INFINITY);
            } else { // NaN
                value = Float.intBitsToFloat((sign << SIGN_SHIFT)
                        | INFINITY | (mantissa << MANT_SHIFT));
            }
        } else {
            exp = exp + (EXP_MAX - HALF_EXP_OFFSET);
            mantissa = mantissa << MANT_SHIFT;
            value = Float.intBitsToFloat((sign << SIGN_SHIFT)
                    | (exp << EXP_SHIFT) | mantissa);
        }
        return value;
    }
}
