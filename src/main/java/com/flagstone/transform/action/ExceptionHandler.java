/*
 * ExceptionHandler.java
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
import java.util.ArrayList;
import java.util.List;

import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.coder.SWFFactory;
import com.flagstone.transform.exception.IllegalArgumentRangeException;

/**
 * The ExceptionHandler class is used to represent try..catch blocks in
 * Actionscript.
 *
 * <p>
 * When an exception is thrown, the object can be assigned to either one of the
 * Flash Player's 256 internal registers or to a variable in memory.
 * </p>
 *
 * <p>
 * The ExceptionHandler class contains three lists of actions supporting the
 * standard syntax for an exception with try, catch and finally blocks. Both the
 * catch and finally blocks are optional when defining an exception, the
 * corresponding arguments in constructors and methods may be set to empty.
 * </p>
 */
public class ExceptionHandler implements Action {

    /**
     * The Builder class is used to generate a new ExceptionHandler object
     * using a small set of convenience methods.
     */
    public static final class Builder {
        /** The register where the thrown object will be stored. */
        private transient int register;
        /** The name of the variable where the thrown object will be stored. */
        private transient String variable;
        /** The list of actions that make up the try block. */
        private final transient List<Action> tryActions =
            new ArrayList<Action>();
        /** The list of actions that make up the catch block. */
        private final transient List<Action> catchActions =
            new ArrayList<Action>();
        /** The list of actions that make up the finally block. */
        private final transient List<Action> finalActions =
            new ArrayList<Action>();

        /**
         * Set the register where the thrown object will be stored.
         *
         * @param index the register number. Must be in the range 0..255.
         * @return this object.
         */
        public Builder setRegister(final int index) {
            if ((index < 0) || (index > HIGHEST_REGISTER)) {
                throw new IllegalArgumentRangeException(0,
                        HIGHEST_REGISTER, index);
            }
            variable = "";
            register = index;
            return this;
        }

        /**
         * Set the name of the variable where thrown object will be assigned.
         * @param name the name of the actionsctipt variable.
         * @return this object.
         */
        public Builder setVariable(final String name) {
            if (name == null || name.length() == 0) {
                throw new IllegalArgumentException();
            }
            variable = name;
            register = 0;
            return this;
        }

        /**
         * Add an action to the try block of the exception handler.
         * @param action the action to the executed in the try block.
         * @return this object.
         */
        public Builder addToTry(final Action action) {
            if (action == null) {
                throw new IllegalArgumentException();
            }
            tryActions.add(action);
            return this;
        }

        /**
         * Add an action to the catch block of the exception handler.
         * @param action the action to the executed in the catch block.
         * @return this object.
         */
        public Builder addToCatch(final Action action) {
            if (action == null) {
                throw new IllegalArgumentException();
            }
            catchActions.add(action);
            return this;
        }

        /**
         * Add an action to the final block of the exception handler.
         * @param action the action to the executed in the final block.
         * @return this object.
         */
        public Builder addToFinal(final Action action) {
            if (action == null) {
                throw new IllegalArgumentException();
            }
            finalActions.add(action);
            return this;
        }

        /**
         * Generate an ExceptionHandler using the parameters defined in the
         * Builder.
         * @return an initialized ExceptionHandler object.
         */
        public ExceptionHandler build() {
            return new ExceptionHandler(this);
        }
    }

    /** Format string used in toString() method. */
    private static final String FORMAT = "ExceptionHandler: { variable=%s;"
            + " register=%d try=%s; catch=%s; final=%s}";

    /** Bit mask used to read the containsVariable field. */
    private static final int VARIABLE_MASK = 0x04;
    /** Bit mask used to read the containsVariable field. */
    private static final int FINAL_MASK = 0x02;
    /** Bit mask used to read the containsVariable field. */
    private static final int CATCH_MASK = 0x01;
    /** Length of an empty exception handler with no actions. */
    private static final int EMPTY_LENGTH = 8;
    /** Number of registers in the FLash Player. */
    private static final int HIGHEST_REGISTER = 255;

    /** The number of the register that contains the thrown object. */
    private final transient int register;
    /** The name of the variable that the thrown object will be assigned to. */
    private final transient String variable;
    /** Set of actions where the exception might be thrown. */
    private final transient List<Action> tryActions;
    /** Set of actions used to process the exception. */
    private final transient List<Action> catchActions;
    /** Final set of actions executed, whether or not an exception occurred. */
    private final transient List<Action> finalActions;

    /** The length of the action, minus the header, when it is encoded. */
    private transient int length;
    /** Holds the length of the try block when it is encoded. */
    private transient int tryLength;
    /** Holds the length of the catch block when it is encoded. */
    private transient int catchLength;
    /** Holds the length of the final block when it is encoded. */
    private transient int finalLength;

    /**
     * Creates and initialises an ExceptionHandler using parameters defined
     * in the Builder.
     *
     * @param builder a Builder object containing the parameters to generate
     * the ExceptionHandler.
     */
    public ExceptionHandler(final Builder builder) {
        register = builder.register;
        variable = builder.variable;
        tryActions = new ArrayList<Action>(builder.tryActions);
        catchActions = new ArrayList<Action>(builder.catchActions);
        finalActions = new ArrayList<Action>(builder.finalActions);
    }

    /**
     * Creates and initialises an ExceptionHandler action using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @param context
     *            a Context object used to manage the decoders for different
     *            type of object and to pass information on how objects are
     *            decoded.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public ExceptionHandler(final SWFDecoder coder, final Context context)
            throws IOException {
        length = coder.readUnsignedShort();

        final int flags = coder.readByte();
        final boolean containsVariable = (flags & VARIABLE_MASK) >> 2 == 1;
        final boolean containsFinal = (flags & FINAL_MASK) >> 1 == 1;
        final boolean containsCatch = (flags & CATCH_MASK) == 1;

        tryLength = coder.readUnsignedShort();
        catchLength = coder.readUnsignedShort();
        finalLength = coder.readUnsignedShort();

        if (length == EMPTY_LENGTH) {
            length += tryLength;
            length += catchLength;
            length += finalLength;
        }

        if (containsVariable) {
            variable = coder.readString();
            register = 0;
        } else {
            variable = "";
            register = coder.readByte();
        }

        tryActions = new ArrayList<Action>();
        catchActions = new ArrayList<Action>();
        finalActions = new ArrayList<Action>();

        final SWFFactory<Action> decoder = context.getRegistry()
                .getActionDecoder();

        coder.mark();
        while (coder.bytesRead() < tryLength) {
            decoder.getObject(tryActions, coder, context);
        }
        coder.unmark();

        if (containsCatch) {
            coder.mark();
            while (coder.bytesRead() < catchLength) {
                decoder.getObject(catchActions, coder, context);
            }
            coder.unmark();
        }

        if (containsFinal) {
            coder.mark();
            while (coder.bytesRead() < finalLength) {
                decoder.getObject(finalActions, coder, context);
            }
            coder.unmark();
        }
    }

    /**
     * Creates a new exception handler with the thrown object assigned to a
     * local variable.
     *
     * @param name
     *            the name of the variable that the thrown object will be
     *            assigned to. Must not be null.
     * @param tryArray
     *            actions that will be executed in the try block of the
     *            exception. Must not be null.
     * @param catchArray
     *            actions that will be executed in the catch block of the
     *            exception, if one is defined. This may be empty if no
     *            catch block is required - the exception will be handled by
     *            another catch block higher in the exception tree.
     * @param finallyArray
     *            actions that will be executed in the finally block of the
     *            exception, if one is defined. This may be empty if no
     *            finally block is required.
     */

    public ExceptionHandler(final String name, final List<Action> tryArray,
            final List<Action> catchArray, final List<Action> finallyArray) {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException();
        }
        variable = name;
        register = 0;

        if (tryArray == null) {
            throw new IllegalArgumentException();
        }
        tryActions = tryArray;

        if (catchArray == null) {
            throw new IllegalArgumentException();
        }
        catchActions = catchArray;

        if (finallyArray == null) {
            throw new IllegalArgumentException();
        }
        finalActions = finallyArray;
    }

    /**
     * Constructs a new exception handler with the thrown object assigned to one
     * of the Flash Player's internal registers.
     *
     * @param index
     *            the number of the register that the thrown object will be
     *            assigned to. Must be in the range 0..255.
     * @param tryArray
     *            actions that will be executed in the try block of the
     *            exception. Must not be null.
     * @param catchArray
     *            actions that will be executed in the catch block of the
     *            exception, if one is defined. This may be empty if no
     *            catch block is required - the exception will be handled by
     *            another catch block higher in the exception tree.
     * @param finallyArray
     *            actions that will be executed in the finally block of the
     *            exception, if one is defined. This may be empty is no
     *            finally block is required.
     */
    public ExceptionHandler(final int index, final List<Action> tryArray,
            final List<Action> catchArray, final List<Action> finallyArray) {
        if ((index < 0) || (index > HIGHEST_REGISTER)) {
            throw new IllegalArgumentRangeException(0, HIGHEST_REGISTER, index);
        }
        variable = "";
        register = index;

        if (tryArray == null) {
            throw new IllegalArgumentException();
        }
        tryActions = tryArray;

        if (catchArray == null) {
            throw new IllegalArgumentException();
        }
        catchActions = catchArray;

        if (finallyArray == null) {
            throw new IllegalArgumentException();
        }
        finalActions = finallyArray;
    }

    /**
     * Creates and initialises an ExceptionHandler action using the values
     * copied from another ExceptionHandler.
     *
     * @param object
     *            an ExceptionHandler object from which the values will be
     *            copied. References to immutable objects will be shared.
     */
    public ExceptionHandler(final ExceptionHandler object) {
        variable = object.variable;
        register = object.register;
        tryActions = new ArrayList<Action>(object.tryActions);
        catchActions = new ArrayList<Action>(object.catchActions);
        finalActions = new ArrayList<Action>(object.finalActions);
    }

    /**
     * Returns the name of the variable which the exception object is assigned
     * to.
     *
     * @return the name of the function. Returns null if the exception object
     *         will be assigned to a register.
     */
    public String getVariable() {
        return variable;
    }

    /**
     * Returns the index of the register that the exception object is assigned
     * to.
     *
     * @return the number of register. Returns 0 if the exception object will be
     *         assigned to a local variable.
     */
    public int getRegister() {
        return register;
    }

    /**
     * Returns the list of actions executed in the try block.
     *
     * @return the list of actions for the try block.
     */
    public List<Action> getTryActions() {
        return new ArrayList<Action>(tryActions);
    }

    /**
     * Returns the list of actions executed in the catch block.
     *
     * @return the list of actions for the catch block.
     */
    public List<Action> getCatchActions() {
        return new ArrayList<Action>(catchActions);
    }

    /**
     * Returns the list of actions executed in the finally block.
     *
     * @return the list of actions for the finally block.
     */
    public List<Action> getFinalActions() {
        return new ArrayList<Action>(finalActions);
    }

    /** {@inheritDoc} */
    public ExceptionHandler copy() {
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, variable, register, tryActions,
                catchActions, finalActions);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        length = EMPTY_LENGTH; // assume thrown object is stored in register.

        if (register == 0) {
            length += context.strlen(variable) - 1;
        }

        tryLength = 0;
        catchLength = 0;
        finalLength = 0;

        for (final Action action : tryActions) {
            tryLength += action.prepareToEncode(context);
        }

        for (final Action action : catchActions) {
            catchLength += action.prepareToEncode(context);
        }

        for (final Action action : finalActions) {
            finalLength += action.prepareToEncode(context);
        }

        length += tryLength;
        length += catchLength;
        length += finalLength;

        return Coder.ACTION_HEADER + length;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {

        coder.writeByte(ActionTypes.EXCEPTION_HANDLER);
        coder.writeShort(length);

        int flags = 0;

        if (register == 0) {
            flags |= VARIABLE_MASK;
        }
        if (finalLength > 0) {
            flags |= FINAL_MASK;
        }
        if (catchLength > 0) {
            flags |= CATCH_MASK;
        }
        coder.writeByte(flags);

        coder.writeShort(tryLength);
        coder.writeShort(catchLength);
        coder.writeShort(finalLength);

        if (register == 0) {
            coder.writeString(variable);
        } else {
            coder.writeByte(register);
        }

        for (final Action action : tryActions) {
            action.encode(coder, context);
        }

        for (final Action action : catchActions) {
            action.encode(coder, context);
        }

        for (final Action action : finalActions) {
            action.encode(coder, context);
        }
    }
}
