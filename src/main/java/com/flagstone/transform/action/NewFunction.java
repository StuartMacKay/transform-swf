/*
 * NewFunction.java
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

/**
 * The NewFunction action is used to create a user-defined function.
 *
 * <p>
 * User-defined functions are also used to create methods for user-defined
 * objects. The name of the function is omitted and the function definition is
 * assigned to a variable which allows it to be referenced at a later time.
 * </p>
 *
 * <p>
 * In the actions which form the function body all the arguments passed to the
 * function can be referenced by the name supplied in the arguments list.
 * </p>
 *
 * <p>
 * All the action objects created are owned by the function. They will be
 * deleted when the function definition is deleted.
 * </p>
 *
 * @see NewFunction2
 */
public class NewFunction implements Action {

    /**
     * The Builder class is used to generate a new NewFunction object
     * using a small set of convenience methods.
     */
    public static final class Builder {
        /** The name, if any, for the function. */
        private transient String name = "";
        /** The function arguments. */
        private final transient List<String>arguments = new ArrayList<String>();
        /** The list of actions that make up the function body. */
        private final transient List<Action>actions = new ArrayList<Action>();

        /**
         * Set the name of the function. Must not be null or an empty string.
         * The name defaults to an empty string so this method is not needed to
         * define methods.
         *
         * @param aString the name of the function.
         * @return this object.
         */
        public Builder setName(final String aString) {
            if (aString == null || aString.length() == 0) {
                throw new IllegalArgumentException();
            }
            name = aString;
            return this;
        }

        /**
         * Add the name of an argument to the list of arguments that will be
         * passed to the function. Must not be null of an empty string.
         * @param argName the name of the argument.
         * @return this object.
         */
        public Builder setArgument(final String argName) {
            if (argName == null || argName.length() == 0) {
                throw new IllegalArgumentException();
            }
            arguments.add(argName);
            return this;
        }

        /**
         * Add an action to the list of actions that will make up the body of
         * the function. Must not be null.
         * @param action the action to add to the function body.
         * @return this object.
         */
        public Builder addAction(final Action action) {
            if (action == null) {
                throw new IllegalArgumentException();
            }
            actions.add(action);
            return this;
        }

        /**
         * Generate an NewFunction using the parameters defined in the
         * Builder.
         * @return an initialized NewFunction object.
         */
        public NewFunction build() {
            return new NewFunction(this);
        }
    }

    /** Format string used in toString() method. */
    private static final String FORMAT = "NewFunction: { name=%s;"
            + " arguments=%s; actions=%s}";

    /** The name of the function or an empty string if a method. */
    private final transient String name;
    /** The names of the arguments. */
    private final transient List<String> arguments;
    /** The actions that make up the function body. */
    private final transient List<Action> actions;

    /** The length of the action, minus the header, when it is encoded. */
    private transient int length;
    /** The length of the actions when encoded. */
    private transient int actionsLength;

    /**
     * Creates and initialises a NewFunction object using parameters defined
     * in the Builder.
     *
     * @param builder a Builder object containing the parameters to generate
     * the function definition.
     */
    public NewFunction(final Builder builder) {
        name = builder.name;
        arguments = new ArrayList<String>(builder.arguments);
        actions = new ArrayList<Action>(builder.actions);
    }

    /**
     * Creates and initialises a NewFunction definition using values encoded
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
    public NewFunction(final SWFDecoder coder, final Context context)
            throws IOException {
        length = coder.readUnsignedShort();

        name = coder.readString();

        final int argumentCount = coder.readUnsignedShort();

        arguments = new ArrayList<String>(argumentCount);

        if (argumentCount > 0) {
            for (int i = argumentCount; i > 0; i--) {
                arguments.add(coder.readString());
            }
        }

        final SWFFactory<Action> decoder = context.getRegistry()
        .getActionDecoder();
        actions = new ArrayList<Action>();

        actionsLength = coder.readUnsignedShort();
        coder.mark();

        while (coder.bytesRead() < actionsLength) {
            decoder.getObject(actions, coder, context);
        }

        coder.unmark();
    }

    /**
     * Creates a NewFunction with the specified name, argument names and actions
     * to be executed. The order of the Strings in the argument list indicate
     * the order in which the values will be popped off the stack when the
     * function is executed. The fist argument is popped from the stack first.
     *
     * @param aString
     *            the name of the function. May not be null.
     * @param argumentArray
     *            the list of Strings giving the names of the arguments.
     * @param actionArray
     *            the list of actions that define the operation performed by
     *            the function.
     */
    public NewFunction(final String aString, final List<String> argumentArray,
            final List<Action> actionArray) {
        if (aString == null) {
            throw new IllegalArgumentException();
        }
        name = aString;
        if (argumentArray == null) {
            throw new IllegalArgumentException();
        }
        arguments = argumentArray;

        if (actionArray == null) {
            throw new IllegalArgumentException();
        }
        actions = actionArray;
    }

    /**
     * Creates a anonymous NewFunction with the specified argument names and
     * actions to be executed. Use this constructor when defining functions that
     * will be assigned to object variables and used as methods.
     *
     * @param argumentArray
     *            a list of Strings giving the names of the arguments.
     * @param actionArray
     *            a list of actions that define the operation performed by
     *            the function.
     */
    public NewFunction(final List<String> argumentArray,
            final List<Action> actionArray) {
        this("", argumentArray, actionArray);
    }

    /**
     * Creates and initialises a NewFunction action using the values
     * copied from another NewFunction action.
     *
     * @param object
     *            a NewFunction action from which the values will be
     *            copied. References to immutable objects will be shared.
     */
    public NewFunction(final NewFunction object) {
        name = object.name;

        arguments = new ArrayList<String>(object.arguments);
        actions = new ArrayList<Action>(object.actions);
    }

    /**
     * Get the name of the function. If the function will be used as an object
     * method then the name is an empty string.
     *
     * @return the name of the function or an empty string.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the names of the function arguments.
     *
     * @return a list of argument names in the order they appear in the
     * function definition.
     */
    public List<String> getArguments() {
        return new ArrayList<String>(arguments);
    }

    /**
     * Get the actions that will be executed.
     *
     * @return the actions that define the function behaviour.
     */
    public List<Action> getActions() {
        return new ArrayList<Action>(actions);
    }

    /** {@inheritDoc} */
    public NewFunction copy() {
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, name, arguments, actions);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        length = 2 + context.strlen(name);

        for (final String argument : arguments) {
            length += context.strlen(argument);
        }

        length += 2;
        actionsLength = 0;

        for (final Action action : actions) {
            actionsLength += action.prepareToEncode(context);
        }

        return Coder.ACTION_HEADER + length + actionsLength;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        coder.writeByte(ActionTypes.NEW_FUNCTION);
        coder.writeShort(length);

        coder.writeString(name);

        coder.writeShort(arguments.size());

        for (final String argument : arguments) {
            coder.writeString(argument);
        }

        coder.writeShort(actionsLength);

        for (final Action action : actions) {
            action.encode(coder, context);
        }
    }
}
