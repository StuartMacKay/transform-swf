/*
 * Jump.java
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

package com.flagstone.transform.action;


import java.io.IOException;

import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.exception.IllegalArgumentRangeException;

/**
 * The Jump action performs an unconditional branch to control the actions
 * executed by the Flash Player.
 *
 * <p>
 * When executed the Jump action adds an offset to the instruction pointer and
 * execution of the stream of actions continues from that address.
 * </p>
 *
 * <p>
 * Although the Flash Player contains an instruction pointer it does not support
 * an explicit address space. The instruction pointer is used to reference
 * actions within the current stream of actions being executed whether they are
 * associated with a given frame, button or movie clip. The value contained in
 * the instruction pointer is the address relative to the start of the current
 * stream.
 * </p>
 *
 * <p>
 * The offset is a signed number allowing branches up to -32768 to 32767 bytes.
 * The instruction pointer points to the next instruction so specifying an
 * offset of zero will have no effect on the sequence of instructions executed.
 * </p>
 *
 * @see If
 */
public class Jump implements Action {

    /** Format string used in toString() method. */
    private static final String FORMAT = "Jump: { offset=%d}";
    /** Minimum coder pointer offset. */
    private static final int MIN_CODE_JUMP = -32768;
    /** Maximum coder pointer offset. */
    private static final int MAX_CODE_JUMP = 32767;

    /** The offset to the next action. */
    private final transient int offset;

    /**
     * Creates and initialises a Jump action using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public Jump(final SWFDecoder coder) throws IOException {
        coder.readUnsignedShort();
        offset = coder.readSignedShort();
    }

    /**
     * Creates a Jump action with the specified offset.
     *
     * @param anOffset
     *            the number of bytes to add to the instruction pointer. The
     *            offset must be in the range -32768..32767.
     */
    public Jump(final int anOffset) {
        if ((anOffset < MIN_CODE_JUMP)
                || (anOffset > MAX_CODE_JUMP)) {
            throw new IllegalArgumentRangeException(MIN_CODE_JUMP,
                    MAX_CODE_JUMP, anOffset);
        }
        offset = anOffset;
    }

    /**
     * Creates and initialises a Jump action using the values
     * copied from another Jump action.
     *
     * @param object
     *            a Jump action from which the values will be
     *            copied.
     */
    public Jump(final Jump object) {
        offset = object.offset;
    }

    /**
     * Get the offset that will be added to the instruction pointer.
     *
     * @return the offset to the next action.
     */
    public int getOffset() {
        return offset;
    }

    /** {@inheritDoc} */
    public Jump copy() {
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, offset);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        return Coder.ACTION_HEADER + 2;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        coder.writeByte(ActionTypes.JUMP);
        coder.writeShort(2);
        coder.writeShort(offset);
    }
}
