/*
 * SoundInfo.java
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

import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.Copyable;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncodeable;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.exception.IllegalArgumentRangeException;

/**
 * SoundInfo identifies a sound (previously defined using The DefineSound class)
 * and controls how it is played.
 *
 * <p>
 * SoundInfo defines how the sound fades in and out, whether it is repeated as
 * well as specifying an envelope that provides a finer degree of control over
 * the levels at which the sound is played.
 * </p>
 *
 * <p>
 * The in and out point specify the sample number which marks the point in time
 * at which the sound stops increasing or starts decreasing in volume
 * respectively. Sounds are played by the Flash player at 44.1KHz so the sample
 * number also indicates the time when the total number of samples in the sound
 * is taken into account.
 * </p>
 *
 * <p>
 * Not all the attributes are required to play a sound. Only the identifier and
 * the mode is required. The other attributes are optional and may be added as a
 * greater degree of control is required. The inPoint and outPoint attributes
 * may be set to zero if the sound does not fade in or out respectively. The
 * loopCount may be set to zero if a sound is being stopped. The envelopes
 * may be left empty if no envelope is defined for the sound. The class provides
 * different constructors to specify different sets of attributes.
 * </p>
 *
 * @see DefineSound
 */
public class SoundInfo implements SWFEncodeable, Copyable<SoundInfo> {

    /** Format string used in toString() method. */
    private static final String FORMAT = "SoundInfo: { identifier=%d; mode=%s;"
            + " inPoint=%d; outPoint=%d; loopCount=%d; envelopes=%s}";

    /** Mode describes how the sound is controlled. */
    public enum Mode {
        /** Start playing the sound. */
        START,
        /** Start playing the sound or continues if it is already playing. */
        CONTINUE,
        /** Stop playing the sound. */
        STOP;
    }

    /** The unique identifier of the sound that this info applies to. */
    private int identifier;
    /** Controls whether the sound starts or stops. */
    private int mode;
    /** The number of samples to fade the sound in. */
    private Integer inPoint;
    /** The number of samples to fade the sound out. */
    private Integer outPoint;
    /** The number of time the sound will be repeated. */
    private Integer loopCount;
    /** The envelope that controls how the sound is played. */
    private Envelope envelope;

    /**
     * Creates and initialises a SoundInfo object using values encoded
     * in the Flash binary format.
     *
     * @param uid the unique identifier for the sound definition - decoded by
     * the parent object.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public SoundInfo(final int uid, final SWFDecoder coder)
            throws IOException {
        identifier = uid;

        final int info = coder.readByte();
        mode = info & Coder.NIB1;

        if ((info & Coder.BIT0) != 0) {
            inPoint = coder.readInt();
        }

        if ((info & Coder.BIT1) != 0) {
            outPoint = coder.readInt();
        }

        if ((info & Coder.BIT2) != 0) {
            loopCount = coder.readUnsignedShort();
        }

        if ((info & Coder.BIT3) != 0) {
            envelope = new Envelope(coder);
        }
    }

    /**
     * Creates a Sound object specifying how the sound is played and the number
     * of times the sound is repeated.
     *
     * @param uid
     *            the unique identifier of the object that contains the sound
     *            data.
     * @param aMode
     *            how the sound is synchronised when the frames are displayed:
     *            Play - do not play the sound if it is already playing and Stop
     *            - stop playing the sound.
     * @param aCount
     *            the number of times the sound is repeated. May be set to zero
     *            if the sound will not be repeated.
     * @param anEnvelope
     *            the Envelope that control the levels the sound is played.
     */
    public SoundInfo(final int uid, final Mode aMode, final int aCount,
            final Envelope anEnvelope) {
        setIdentifier(uid);
        setMode(aMode);
        setLoopCount(aCount);
        setEnvelope(anEnvelope);
    }

    /**
     * Creates and initialises a SoundInfo object using the values copied
     * from another SoundInfo object.
     *
     * @param object
     *            a SoundInfo object from which the values will be
     *            copied.
     */
    public SoundInfo(final SoundInfo object) {
        identifier = object.identifier;
        mode = object.mode;
        loopCount = object.loopCount;
        inPoint = object.inPoint;
        outPoint = object.outPoint;

        if (object.envelope != null) {
            envelope = object.envelope.copy();
        }
    }

    /**
     * Get the identifier of the sound to the played.
     *
     * @return the unique identifier of the sound.
     */
    public int getIdentifier() {
        return identifier;
    }

    /**
     * Get the synchronisation mode: START - start playing the sound,
     * CONTINUE - do not play the sound if it is already playing and STOP - stop
     * playing the sound.
     *
     * @return the sound synchronisation mode.
     */
    public Mode getMode() {
        Mode value;
        switch (mode) {
        case 0:
            value = Mode.START;
            break;
        case Coder.BIT4:
            value = Mode.CONTINUE;
            break;
        case Coder.BIT5:
            value = Mode.STOP;
            break;
        default:
            throw new IllegalStateException();
        }
        return value;
    }

    /**
     * Get the sample number at which the sound reaches full volume when
     * fading in.
     *
     * @return the fade in point.
     */
    public Integer getInPoint() {
        return inPoint;
    }

    /**
     * Get the sample number at which the sound starts to fade.
     *
     * @return the fade out point.
     */
    public Integer getOutPoint() {
        return outPoint;
    }

    /**
     * Get the number of times the sound will be repeated.
     *
     * @return the number of loops.
     */
    public Integer getLoopCount() {
        return loopCount;
    }

    /**
     * Get the Envelope that control the levels the sound is played.
     *
     * @return the sound envelope.
     */
    public Envelope getEnvelope() {
        return envelope;
    }

    /**
     * Sets the identifier of the sound to the played.
     *
     * @param uid
     *            the identifier for the sound to be played. Must be in the
     *            range 1..65535.
     */
    public void setIdentifier(final int uid) {
        if ((uid < 1) || (uid > Coder.USHORT_MAX)) {
            throw new IllegalArgumentRangeException(
                    1, Coder.USHORT_MAX, uid);
        }
        identifier = uid;
    }

    /**
     * Sets how the sound is synchronised when the frames are displayed: START -
     * start playing the sound, CONTINUE - do not play the sound if it is
     * already playing and STOP - stop playing the sound.
     *
     * @param soundMode
     *            how the sound is played.
     */
    public void setMode(final Mode soundMode) {
        switch (soundMode) {
        case START:
            mode = 0;
            break;
        case CONTINUE:
            mode = Coder.BIT4;
            break;
        case STOP:
            mode = Coder.BIT5;
            break;
        default:
            throw new IllegalArgumentException();
        }
    }

    /**
     * Sets the sample number at which the sound reaches full volume when fading
     * in. May be set to zero if the sound does not fade in.
     *
     * @param aNumber
     *            the sample number which the sound fades in to.
     */
    public void setInPoint(final Integer aNumber) {
        if ((aNumber != null)
                && ((aNumber < 0) || (aNumber > Coder.USHORT_MAX))) {
            throw new IllegalArgumentRangeException(0,
                    Coder.USHORT_MAX, aNumber);
        }
        inPoint = aNumber;
    }

    /**
     * Sets the sample number at which the sound starts to fade. May be set to
     * zero if the sound does not fade out.
     *
     * @param aNumber
     *            the sample number at which the sound starts to fade.
     */
    public void setOutPoint(final Integer aNumber) {
        if ((aNumber != null)
                && ((aNumber < 0) || (aNumber > Coder.USHORT_MAX))) {
            throw new IllegalArgumentRangeException(0,
                    Coder.USHORT_MAX, aNumber);
        }
        outPoint = aNumber;
    }

    /**
     * Sets the number of times the sound is repeated. May be set to zero if the
     * sound will not be repeated.
     *
     * @param aNumber
     *            the number of times the sound is repeated.
     */
    public void setLoopCount(final Integer aNumber) {
        if ((aNumber != null)
                && ((aNumber < 0) || (aNumber > Coder.USHORT_MAX))) {
            throw new IllegalArgumentRangeException(0,
                    Coder.USHORT_MAX, aNumber);
       }
        loopCount = aNumber;
    }

    /**
     * Sets the Envelope that define the levels at which a sound is played over
     * the duration of the sound. May be set to null if no envelope is defined.
     *
     * @param anEnvelope
     *            an Envelope object.
     */
    public void setEnvelope(final Envelope anEnvelope) {
        envelope = anEnvelope;
    }

    /** {@inheritDoc} */
    public SoundInfo copy() {
        return new SoundInfo(this);
    }

    @Override
    public String toString() {
        return String.format(FORMAT, identifier, mode, inPoint, outPoint,
                loopCount, envelope);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        // CHECKSTYLE:OFF
        int length = 3;
        if (inPoint != null) {
            length += 4;
        }
        if (outPoint != null) {
            length += 4;
        }
        if (loopCount != null) {
            length += 2;
        }
        if (envelope != null) {
            length += envelope.prepareToEncode(context);
        }
        return length;
        // CHECKSTYLE:ON
    }

    /** {@inheritDoc} */
    @SuppressWarnings("PMD.NPathComplexity")
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        coder.writeShort(identifier);

        int bits = mode;
        bits |= envelope == null ? 0 : Coder.BIT3;
        bits |= loopCount == null ? 0 : Coder.BIT2;
        bits |= outPoint == null ? 0 : Coder.BIT1;
        bits |= inPoint == null ? 0 : Coder.BIT0;
        coder.writeByte(bits);

        if (inPoint != null) {
            coder.writeInt(inPoint);
        }
        if (outPoint != null) {
            coder.writeInt(outPoint);
        }
        if (loopCount != null) {
            coder.writeShort(loopCount);
        }
        if (envelope != null) {
            envelope.encode(coder, context);
        }
    }
}
