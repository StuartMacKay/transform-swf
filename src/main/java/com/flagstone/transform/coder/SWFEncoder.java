/*
 * SWFEncoder.java
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
import java.io.OutputStream;
import java.util.Stack;

import com.flagstone.transform.CharacterEncoding;

/**
 * SWFEncoder wraps an OutputStream with a buffer to reduce the amount of
 * memory required to encode a movie and to improve efficiency by writing
 * data to a file or external source in blocks.
 */
@SuppressWarnings("PMD.TooManyMethods")
public class SWFEncoder {
    /** The default size, in bytes, for the internal buffer. */
    public static final int BUFFER_SIZE = 4096;

    /** Bit mask applied to bytes when converting to unsigned integers. */
    private static final int BYTE_MASK = 255;
    /** Number of bits in an int. */
    private static final int BITS_PER_INT = 32;
    /** Number of bits in a byte. */
    private static final int BITS_PER_BYTE = 8;
    /** Offset to add to number of bits when calculating number of bytes. */
    private static final int ROUND_TO_BYTES = 7;
    /** Right shift to convert number of bits to number of bytes. */
    private static final int BITS_TO_BYTES = 3;
    /** Left shift to convert number of bytes to number of bits. */
    private static final int BYTES_TO_BITS = 3;
    /** Number of bits to shift when aligning a value to the second byte. */
    private static final int TO_BYTE1 = 8;
    /** Number of bits to shift when aligning a value to the third byte. */
    private static final int TO_BYTE2 = 16;
    /** Number of bits to shift when aligning a value to the fourth byte. */
    private static final int TO_BYTE3 = 24;


    /** The underlying input stream. */
    private final transient OutputStream stream;
    /** The buffer for data read from the stream. */
    private final transient byte[] buffer;
    /** The index in bytes to the current location in the buffer. */
    private transient int index;
    /** The offset in bits to the location in the current byte. */
    private transient int offset;
    /** The character encoding used for strings. */
    private transient String encoding;
    /** Stack for storing file locations. */
    private final transient Stack<Integer>locations;
    /** The position of the buffer relative to the start of the stream. */
    private transient int pos;

    /**
     * Create a new SWFEncoder for the underlying InputStream with the
     * specified buffer size.
     *
     * @param streamOut the stream from which data will be written.
     * @param length the size in bytes of the buffer.
     */
    public SWFEncoder(final OutputStream streamOut, final int length) {
        stream = streamOut;
        buffer = new byte[length];
        encoding = CharacterEncoding.UTF8.getEncoding();
        locations = new Stack<Integer>();
    }

    /**
     * Create a new SWFEncoder for the underlying InputStream using the
     * default buffer size.
     *
     * @param streamOut the stream from which data will be written.
     */
    public SWFEncoder(final OutputStream streamOut) {
        stream = streamOut;
        buffer = new byte[BUFFER_SIZE];
        encoding = CharacterEncoding.UTF8.getEncoding();
        locations = new Stack<Integer>();
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
     * Compare the number of bytes read with the expected number and throw an
     * exception if there is a difference.
     *
     * @param expected the expected number of bytes read.
     *
     * @throws CoderException if the number of bytes read is different from the
     * expected number.
     */
    public void check(final int expected) throws CoderException {
        final int actual = (pos + index) - locations.peek();
        if (actual != expected) {
            throw new CoderException(locations.peek(), expected,
                    actual - expected);
        }
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
     * Write the data currently stored in the buffer to the underlying stream.
     * @throws IOException if an error occurs while writing the data to the
     * stream.
     */
    public void flush() throws IOException {
        stream.write(buffer, 0, index);
        stream.flush();

        int diff;
        if (offset == 0) {
            diff = 0;
        } else {
            diff = 1;
            buffer[0] = buffer[index];
        }

        for (int i = diff; i < buffer.length; i++) {
            buffer[i] = 0;
        }

        pos += index;
        index = 0;
    }

    /**
     * Write a value to bit field.
     *
     * @param value
     *            the value.
     * @param numberOfBits
     *            the (least significant) number of bits that will be written.
     * @throws IOException if there is an error writing data to the underlying
     * stream.
     */
    public void writeBits(final int value, final int numberOfBits)
                throws IOException {

        final int ptr = (index << BYTES_TO_BITS) + offset + numberOfBits;

        if (ptr >= (buffer.length << BYTES_TO_BITS)) {
            flush();
        }

        final int val = ((value << (BITS_PER_INT - numberOfBits)) >>> offset)
                | (buffer[index] << TO_BYTE3);
        int base = BITS_PER_INT - (((offset + numberOfBits
                + ROUND_TO_BYTES) >>> BITS_TO_BYTES) << BYTES_TO_BITS);
        base = base < 0 ? 0 : base;

        int pointer = (index << BYTES_TO_BITS) + offset;

        for (int i = 24; i >= base; i -= BITS_PER_BYTE) {
            buffer[index++] = (byte) (val >>> i);
        }

        if (offset + numberOfBits > BITS_PER_INT) {
            buffer[index] = (byte) (value << (BITS_PER_BYTE
                    - (offset + numberOfBits - BITS_PER_INT)));
        }

        pointer += numberOfBits;
        index = pointer >>> BITS_TO_BYTES;
        offset = pointer & Coder.LOWEST3;
    }

    /**
     * Write a byte.
     *
     * @param value
     *            the value to be written - only the least significant byte will
     *            be written.
     * @throws IOException if there is an error writing data to the underlying
     * stream.
     */
    public void writeByte(final int value) throws IOException {
        if (index == buffer.length) {
            flush();
        }
        buffer[index++] = (byte) value;
    }

    /**
     * Write an array of bytes.
     *
     * @param bytes
     *            the array to be written.
     *
     * @return the number of bytes written.
     * @throws IOException if there is an error reading data from the underlying
     * stream.
     */
    public int writeBytes(final byte[] bytes) throws IOException {
        if (index + bytes.length < buffer.length) {
            System.arraycopy(bytes, 0, buffer, index, bytes.length);
            index += bytes.length;
        } else {
            flush();
            stream.write(bytes, 0, bytes.length);
            pos += bytes.length;
        }
        return bytes.length;
    }

    /**
     * Write a string using the default character set defined in the encoder.
     *
     * @param str
     *            the string.
     * @throws IOException if there is an error reading data from the underlying
     * stream.
     */
    public void writeString(final String str) throws IOException {
        try {
            writeBytes(str.getBytes(encoding));
            buffer[index++] = 0;
        } catch (final java.io.UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Write a 16-bit integer.
     *
     * @param value
     *            an integer containing the value to be written.
     * @throws IOException if there is an error reading data from the underlying
     * stream.
     */
    public void writeShort(final int value) throws IOException {
        if (index + 2 > buffer.length) {
            flush();
        }
        buffer[index++] = (byte) value;
        buffer[index++] = (byte) (value >>> TO_BYTE1);
    }

    /**
     * Write a 32-bit integer.
     *
     * @param value
     *            an integer containing the value to be written.
     * @throws IOException if there is an error reading data from the underlying
     * stream.
     */
    public void writeInt(final int value) throws IOException {
        if (index + 4 > buffer.length) {
            flush();
        }
        buffer[index++] = (byte) value;
        buffer[index++] = (byte) (value >>> TO_BYTE1);
        buffer[index++] = (byte) (value >>> TO_BYTE2);
        buffer[index++] = (byte) (value >>> TO_BYTE3);
    }

    /**
     * Write a 32-bit unsigned integer, encoded in a variable number of bytes.
     *
     * @param value
     *            an integer containing the value to be written.
     * @throws IOException if there is an error reading data from the underlying
     * stream.
     */
    public void writeVarInt(final int value) throws IOException {
        if (index + 5 > buffer.length) {
            flush();
        }

        int val = value;
        while (val > Coder.VAR_INT_MAX) {
            buffer[index++] = (byte) ((val & Coder.LOWEST7) | Coder.BIT7);
            val = val >>> Coder.VAR_INT_SHIFT;
        }
        buffer[index++] = (byte) (val & Coder.LOWEST7);
    }

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
     * The bit pattern used to represent Infinity in a half-precision
     * floating-point value.
     */
    private static final int HALF_INF = 0x7C00;

    /**
     * Number of bits to shift to obtain the exponent in a single-precision
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
     * Mask to obtain the mantissa in a single-precision floating-point value.
     */
    private static final int LOWEST23 = 0x007fffff;
    /**
     * Mask to obtain the most significant bit of the mantissa in a
     * single-precision floating-point value.
     */
    private static final int BIT23 = 0x00800000;

    /**
     * Write a single-precision floating point number.
     *
     * @param value
     *            the value to be written.
     * @throws IOException if there is an error reading data from the underlying
     * stream.
     */
    public void writeHalf(final float value) throws IOException {
        final int intValue = Float.floatToIntBits(value);
        final int sign = (intValue >> Coder.ALIGN_BYTE2) & Coder.BIT15;
        final int exponent = ((intValue >> EXP_SHIFT) & BYTE_MASK)
                        - (EXP_MAX - HALF_EXP_OFFSET);
        int mantissa = intValue & LOWEST23;

        if (exponent <= 0) {
            if (exponent < -10) {
                writeShort(0);
            } else {
                mantissa = (mantissa | BIT23) >> (1 - exponent);
                writeShort((sign | (mantissa >> MANT_SHIFT)));
            }
        } else if (exponent == 0xff - (EXP_MAX - HALF_EXP_OFFSET)) {
            if (mantissa == 0) { // Inf
                writeShort(sign | HALF_INF);
            } else { // NAN
                mantissa >>= MANT_SHIFT;
                writeShort((sign | HALF_INF | mantissa
                        | ((mantissa == 0) ? 1 : 0)));
            }
        } else {
            if (exponent >= HALF_EXP_MAX) { // Overflow
                writeShort((sign | HALF_INF));
            } else {
                writeShort((sign
                        | (exponent << HALF_EXP_SHIFT)
                        | (mantissa >> MANT_SHIFT)));
            }
        }
    }
}
