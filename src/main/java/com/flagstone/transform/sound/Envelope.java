/*
 * Envelope.java
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

package com.flagstone.transform.sound;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.flagstone.transform.Constants;
import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.Copyable;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncodeable;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.exception.IllegalArgumentRangeException;

/**
 * Envelope is a container class for Level objects that describe how the volume
 * of a sound changes over time.
 */
public class Envelope implements SWFEncodeable, Copyable<Envelope> {
    /**
     * Level describes the sound levels for stereo sound.
     *
     * <p>
     * Each Level object contains a sample number in the audio <b>when it is
     * played</b> where the envelope will be applied along with the sound levels
     * for the left and right channels.
     * </p>
     *
     * <p>
     * The Flash Player plays sounds at a fixed rate of 44.1KHz, therefore
     * sounds sampled at a lower frequency are interpolated with each sample
     * repeated to generated the 44.1Khz playback rate. For example each sample
     * in a sound sampled at 22KHz is played twice to generated the 44.1Khz
     * playback rate.
     * </p>
     *
     * <p>
     * The Level defines the sample number (and hence the time) in the
     * playback data stream where the level information applies and <b>not</b>
     * the sample number in the original sound data. For example to set the
     * level 0.1 seconds into a sound that plays for 1 second the value for the
     * mark attribute in the envelope object would be 44100 * 0.1/1.0 = 4410.
     * </p>
     *
     * @see SoundInfo
     */
    public static final class Level implements SWFEncodeable {

        /** Format string used in toString() method. */
        private static final String FORMAT = "Envelope: { mark=%d; left=%d;"
        		+ " right=%d}";

        /** The sample number (at 44KHz) where the levels apply. */
        private final transient int mark;
        /** The sound level for the left channel. */
        private final transient int left;
        /** The sound level for the right channel. */
        private final transient int right;

        /**
         * Creates and initialises a sound Level object using values encoded
         * in the Flash binary format.
         *
         * @param coder
         *            an SWFDecoder object that contains the encoded Flash data.
        *
         * @throws IOException
         *             if an error occurs while decoding the data.
         */
       public Level(final SWFDecoder coder) throws IOException {
            mark = coder.readInt();
            left = coder.readUnsignedShort();
            right = coder.readUnsignedShort();
        }

        /**
         * Creates a envelope specifying the mark, left and right sound levels.
         *
         * @param markValue
         *            the sample number in the 44.1KHz playback data stream
         *            where the levels for the channels is applied.
         * @param leftValue
         *            the level for the left sound channel, in the range
         *            0..65535.
         * @param rightValue
         *            the level for the right sound channel, in the range
         *            0..65535.
         */
        public Level(final int markValue, final int leftValue,
                final int rightValue) {

            mark = markValue;

            if ((leftValue < 0) || (leftValue > Coder.USHORT_MAX)) {
                throw new IllegalArgumentRangeException(0,
                        Coder.USHORT_MAX, leftValue);
            }
            left = leftValue;

            if ((rightValue < 0) || (rightValue > Coder.USHORT_MAX)) {
                throw new IllegalArgumentRangeException(0,
                        Coder.USHORT_MAX, rightValue);
            }
            right = rightValue;
        }

        /**
         * Get the sample number in the 44.1KHz playback data stream where
         * the level information is applied.
         *
         * @return the sample number where the sound levels will be applied.
         */
        public int getMark() {
            return mark;
        }

        /**
         * Get the level of the sound played in the left channel.
         *
         * @return the sound level for the left channel.
         */
        public int getLeft() {
            return left;
        }

        /**
         * Get the level of the sound played in the right channel.
         *
         * @return the sound level for the right channel.
         */
        public int getRight() {
            return right;
        }

        @Override
        public String toString() {
            return String.format(FORMAT, mark, left, right);
        }

        @Override
        public boolean equals(final Object object) {
            boolean result;
            Level level;

            if (object == null) {
                result = false;
            } else if (object == this) {
                result = true;
            } else if (object instanceof Level) {
                level = (Level) object;
                result = (mark == level.mark) && (left == level.left)
                        && (right == level.right);
            } else {
                result = false;
            }
            return result;
        }

        @Override
        public int hashCode() {
            return ((mark * Constants.PRIME) + left) * Constants.PRIME + right;
        }

        /** {@inheritDoc} */
        @Override
		public int prepareToEncode(final Context context) {
            // CHECKSTYLE IGNORE MagciNumberCheck FOR NEXT 1 LINES
            return 8;
        }

        /** {@inheritDoc} */
        @Override
		public void encode(final SWFEncoder coder, final Context context)
                    throws IOException {
            coder.writeInt(mark);
            coder.writeShort(left);
            coder.writeShort(right);
        }
    }

    /** List of sound levels that define the envelope. */
    private List<Level> levels;
    /** Number of levels in list. */
    private transient int count;

    /**
     * Creates and initialises an Envelope object using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public Envelope(final SWFDecoder coder) throws IOException {
        count = coder.readByte();
        levels = new ArrayList<Level>(count);

        for (int i = 0; i < count; i++) {
            levels.add(new Level(coder));
        }
    }

    /**
     * Creates and initialises an Envelope object using the values copied
     * from another Envelope object.
     *
     * @param object
     *            an Envelope object from which the values will be
     *            copied.
     */
    public Envelope(final Envelope object) {
        levels = new ArrayList<Level>(object.levels);
    }

    /**
     * Add a Envelope object to the list of envelope objects.
     *
     * @param level
     *            a SoundLevel object. Must not be null.
     * @return this object.
     */
    public Envelope add(final Level level) {
        if (level == null) {
            throw new IllegalArgumentException();
        }
        levels.add(level);
        return this;
    }

    /**
     * Get the list of Levels that control the volume of the sound.
     *
     * @return the Levels that define the envelope.
     */
    public List<Level> getLevels() {
        return levels;
    }

    /**
     * Sets the list of Levels that define the levels at which a
     * sound is played over the duration of the sound. May be set to null if no
     * envelope is defined.
     *
     * @param list
     *            a list of Envelope objects. Must not be null.
     */
    public void setLevels(final List<Level> list) {
        if (list == null) {
            throw new IllegalArgumentException();
        }
        levels = list;
    }

    /** {@inheritDoc} */
    @Override
	public Envelope copy() {
        return new Envelope(this);
    }

    @Override
    public String toString() {
        return levels.toString();
    }

    /** {@inheritDoc} */
    @Override
	public int prepareToEncode(final Context context) {
        // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 2 LINES
        count = levels.size();
        return 1 + (count << 3);
    }

    /** {@inheritDoc} */
    @Override
	public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        coder.writeByte(count);

        for (final Level level : levels) {
            level.encode(coder, context);
        }
    }
}
