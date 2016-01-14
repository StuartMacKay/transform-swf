package com.flagstone.transform.coder;

/**
 * Coder contains constants and utility functions used by the various classes
 * for encoding and decoding.
 */
public class Coder {
    /** Mask for getting and setting bit 0 of a word. */
    public static final int BIT0 = 0x00000001;
    /** Mask for getting and setting bit 1 of a word. */
    public static final int BIT1 = 0x00000002;
    /** Mask for getting and setting bit 2 of a word. */
    public static final int BIT2 = 0x00000004;
    /** Mask for getting and setting bit 3 of a word. */
    public static final int BIT3 = 0x00000008;
    /** Mask for getting and setting bit 4 of a word. */
    public static final int BIT4 = 0x00000010;
    /** Mask for getting and setting bit 5 of a word. */
    public static final int BIT5 = 0x00000020;
    /** Mask for getting and setting bit 6 of a word. */
    public static final int BIT6 = 0x00000040;
    /** Mask for getting and setting bit 7 of a word. */
    public static final int BIT7 = 0x00000080;
    /** Mask for getting and setting bit 10 of a word. */
    public static final int BIT10 = 0x00000400;
    /** Mask for getting and setting bit 15 of a word. */
    public static final int BIT15 = 0x00008000;

    /** Mask for accessing bits 0-3 of a word. */
    public static final int NIB0 = 0x0000000F;
    /** Mask for accessing bits 4-7 of a word. */
    public static final int NIB1 = 0x000000F0;
    /** Mask for accessing bits 8-11 of a word. */
    public static final int NIB2 = 0x00000F00;
    /** Mask for accessing bits 12-15 of a word. */
    public static final int NIB3 = 0x0000F000;

    /** Mask for accessing bits 0 & 1 of a byte. */
    public static final int PAIR0 = 0x0003;
    /** Mask for accessing bits 2 & 3 of a byte. */
    public static final int PAIR1 = 0x000C;
    /** Mask for accessing bits 0 & 1 of a byte. */
    public static final int PAIR2 = 0x0030;
    /** Mask for accessing bits 2 & 3 of a byte. */
    public static final int PAIR3 = 0x00C0;

    /** Bit mask for the lowest 5 bits in a word. */
    public static final int LOWEST3 = 0x0007;
    /** Bit mask for the lowest 5 bits in a word. */
    public static final int LOWEST5 = 0x001F;
    /** Bit mask for the lowest 5 bits in a word. */
    public static final int LOWEST7 = 0x007F;
    /** Bit mask for the lowest 5 bits in a word. */
    public static final int LOWEST10 = 0x03FF;
    /** Bit mask for the lowest 5 bits in a word. */
    public static final int LOWEST12 = 0x0FFF;
    /** Bit mask for the lowest 5 bits in a word. */
    public static final int LOWEST15 = 0x7FFF;

    /** Right shift to move upper byte of 16-bit word to lower. */
    public static final int TO_LOWER_BYTE = 8;
    /** Left shift to move lower byte of 16-bit word to upper. */
    public static final int TO_UPPER_BYTE = 8;

    /** Right shift to move upper byte of 16-bit word to lower. */
    public static final int TO_LOWER_NIB = 4;
    /** Left shift to move lower byte of 16-bit word to upper. */
    public static final int TO_UPPER_NIB = 4;

    /** Maximum value that can be stored in a 16-bit unsigned field. */
    public static final int USHORT_MAX = 65535;
    /** Minimum value that can be stored in a 16-bit signed field. */
    public static final int SHORT_MIN = -32768;
    /** Maximum value that can be stored in a 16-bit signed field. */
    public static final int SHORT_MAX = 32767;

    /** Number of bits to shift when aligning a value to the second byte. */
    public static final int ALIGN_BYTE1 = 8;
    /** Number of bits to shift when aligning a value to the third byte. */
    public static final int ALIGN_BYTE2 = 16;
    /** Number of bits to shift when aligning a value to the fourth byte. */
    public static final int ALIGN_BYTE3 = 24;

    /** Number of bits to shift when aligning bits 4-7 to positions 0-3. */
    public static final int ALIGN_NIB1 = 4;
    /** Number of bits to shift when aligning bits 8-11 to positions 0-3. */
    public static final int ALIGN_NIB2 = 8;
    /** Number of bits to shift when aligning bits 12-15 to positions 0-3. */
    public static final int ALIGN_NIB3 = 12;

    /** Factor for converting floats to/from 8.8 fixed-point values. */
    public static final float SCALE_8 = 256.0f;
    /** Factor for converting floats to/from 15.15 fixed-point values. */
    public static final float SCALE_14 = 16384.0f;
    /** Factor for converting floats to/from 16.16 fixed-point values. */
    public static final float SCALE_16 = 65536.0f;

    /** The maximum value for each byte in a variable length integer. */
    public static final int VAR_INT_MAX = 127;
    /** Shift when converting to a variable length integer. */
    public static final int VAR_INT_SHIFT = 7;

    /**
     * Bit mask for extracting the length field from the header word.
     */
    public static final int LENGTH_FIELD = 0x3F;
    /**
     * The number of bits used to encode the length field when the length is
     * less than the maximum length of 62.
     */
    public static final int LENGTH_FIELD_SIZE = 6;
    /**
     * Value used to indicate that the length of an object has been encoded
     * as a 32-bit integer following the header for the MovieTag.
     */
    public static final int IS_EXTENDED = 63;
    /**
     * Number of bytes occupied by the header when the size of the encoded
     * object is 62 bytes or less.
     */
    public static final int SHORT_HEADER = 2;
    /**
     * The maximum length in bytes of an encoded object before the length must
     * be encoded using a 32-bit integer.
     */
    public static final int HEADER_LIMIT = 62;
    /**
     * Number of bytes occupied by the header when the size of the encoded
     * object is greater than 62 bytes.
     */
    public static final int LONG_HEADER = 6;
    /**
     * Length, in bytes, of type and length fields of an encoded action.
     */
    public static final int ACTION_HEADER = 3;


    /** Number of bits in an int. */
    private static final int BITS_PER_INT = 32;
    /** Bit mask with most significant bit of a 32-bit integer set. */
    private static final int MSB_MASK = 0x80000000;

    /**
     * Calculates the minimum number of bits required to encoded an unsigned
     * integer in a bit field.
     *
     * @param value
     *            the unsigned value to be encoded.
     *
     * @return the number of bits required to encode the value.
     */
    public static int unsignedSize(final int value) {

        final int val = (value < 0) ? -value - 1 : value;
        int counter = BITS_PER_INT;
        int mask = MSB_MASK;

        while (((val & mask) == 0) && (counter > 0)) {
            mask >>>= 1;
            counter -= 1;
        }
        return counter;
    }

    /**
     * Calculates the minimum number of bits required to encoded a signed
     * integer in a bit field.
     *
     * @param value
     *            the signed value to be encoded.
     *
     * @return the number of bits required to encode the value.
     */
    public static int size(final int value) {
        int counter = BITS_PER_INT;
        int mask = MSB_MASK;
        final int val = (value < 0) ? -value - 1 : value;

        while (((val & mask) == 0) && (counter > 0)) {
            mask >>>= 1;
            counter -= 1;
        }
        return counter + 1;
    }

    /**
     * Returns the minimum number of bits required to encode all the signed
     * values in an array as a set of bit fields with the same size.
     *
     * @param values
     *            an array of signed integers.
     *
     * @return the minimum number of bits required to encode each of the values.
     */
    public static int maxSize(final int... values) {

        int max = 0;
        int size;

        for (final int value : values) {
            size = size(value);
            max = (max > size) ? max : size;
        }
        return max;
    }

    /**
     * Calculate minimum number of bytes a 32-bit unsigned integer can be
     * encoded in.
     *
     * @param value
     *            an integer containing the value to be written.
     * @return the number of bytes required to encode the integer.
     */
    public static int sizeVariableU32(final int value) {

        int val = value;
        int size = 1;

        while (val > VAR_INT_MAX) {
            size += 1;
            val = val >>> VAR_INT_SHIFT;
        }
        return size;
    }

    /** Private constructor. */
    private Coder() {
        // Private Constructor
    }
}
