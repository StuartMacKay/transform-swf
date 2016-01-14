/*
 * LimitScript.java
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

package com.flagstone.transform;

import java.io.IOException;

import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.exception.IllegalArgumentRangeException;

/**
 * The LimitScript is used to define the execution environment of the Flash
 * Player, limiting the resources available when executing actions and improving
 * performance.
 *
 * <p>
 * LimitScript can be used to limit the maximum recursion depth and limit the
 * time a sequence of actions can execute for. This provides a rudimentary
 * mechanism for people viewing a movie to regain control of the Flash Player
 * should a script fail.
 * </p>
 */
public class LimitScript implements MovieTag {

    /** Maximum stack depth for recursive functions. */
    private static final int MAX_DEPTH = 65535;
    /** Maximum timeout in seconds for function execution. */
    private static final int MAX_TIMEOUT = 65535;

    /** Format string used in toString() method. */
    private static final String FORMAT = "LimitScript: { depth=%d;"
    		+ " timeout=%d}";

    /** The maximum stack depth for nested functions. */
    private int depth;
    /** The maximum execution time of a script. */
    private int timeout;

    /**
     * Creates and initialises a LimitScript object using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public LimitScript(final SWFDecoder coder) throws IOException {
        if ((coder.readUnsignedShort() & Coder.LENGTH_FIELD)
        		== Coder.IS_EXTENDED) {
            coder.readInt();
        }
        depth = coder.readUnsignedShort();
        timeout = coder.readUnsignedShort();
    }

    /**
     * Creates a LimitScript object that limits the recursion depth to
     * <em>depth</em> levels and specifies that any sequence of actions will
     * timeout after <em>timeout</em> seconds.
     *
     * @param stackDepth
     *            the maximum depth a sequence of actions can recurse to. Must
     *            be in the range 0..65535.
     * @param timeLimit
     *            the time in seconds that a sequence of actions is allowed to
     *            execute before the Flash Player displays a dialog box asking
     *            whether the script should be terminated. Must be in the range
     *            0..65535.
     */
    public LimitScript(final int stackDepth, final int timeLimit) {
        setDepth(stackDepth);
        setTimeout(timeLimit);
    }

    /**
     * Creates and initialises a LimitScript object using the values copied
     * from another LimitScript object.
     *
     * @param object
     *            a LimitScript object from which the values will be
     *            copied.
     */
    public LimitScript(final LimitScript object) {
        depth = object.depth;
        timeout = object.timeout;
    }

    /**
     * Get the maximum stack depth for function execution.
     *
     * @return the maximum number of stack frames for recursive functions.
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Sets the maximum recursion level.
     *
     * @param stackDepth
     *            the maximum depth a sequence of actions can recurse to. Must
     *            be in the range 0..65535.
     */
    public void setDepth(final int stackDepth) {
        if ((stackDepth < 0) || (stackDepth > MAX_DEPTH)) {
            throw new IllegalArgumentRangeException(0, MAX_DEPTH, stackDepth);
        }
        depth = stackDepth;
    }

    /**
     * Get the maximum time a sequence of actions will execute before the
     * Flash Player present a dialog box asking whether the script should be
     * terminated.
     *
     * @return the maximum execution time of a script.
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Sets the maximum time a sequence of actions will execute before the Flash
     * Player present a dialog box asking whether the script should be
     * terminated.
     *
     * @param time
     *            the time in seconds that a sequence of actions is allowed to
     *            execute. Must be in the range 0..65535.
     */
    public void setTimeout(final int time) {
        if ((time < 0) || (time > MAX_TIMEOUT)) {
            throw new IllegalArgumentRangeException(0, MAX_TIMEOUT, time);
        }
        timeout = time;
    }

    /** {@inheritDoc} */
    @Override
	public LimitScript copy() {
        return new LimitScript(this);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, depth, timeout);
    }

    /** {@inheritDoc} */
    @Override
	public int prepareToEncode(final Context context) {
        // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 1 LINES
        return 6;
    }

    /** {@inheritDoc} */
    @Override
	public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 2 LINES
        coder.writeShort((MovieTypes.LIMIT_SCRIPT
                << Coder.LENGTH_FIELD_SIZE) | 4);
        coder.writeShort(depth);
        coder.writeShort(timeout);
    }
}
