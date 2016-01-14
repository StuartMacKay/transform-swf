/*
 * NewFunction2.java
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
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.coder.SWFFactory;
import com.flagstone.transform.exception.IllegalArgumentRangeException;

/**
 * The NewFunction2 action is used to create a user-defined function with
 * optimisations to improve performance.
 *
 * <p>
 * NewFunction2 was added in Flash 7 to improve the performance of function
 * calls by allowing pre-defined variables such as <em>_root</em>,
 * <em>_parent</em>, <em>_global</em>, <em>super</em>, <em>this</em> and the
 * <em>arguments</em> passed to the function to be pre-loaded to a set of up to
 * 256 internal registers.
 * </p>
 *
 * <p>
 * The optimisation attribute is a compound code, containing a number of flags
 * that control which variables are pre-loaded:
 * </p>
 *
 * <table class="datasheet">
 * <tr>
 * <td valign="top">CreateSuper</td>
 * <td>Create and initialise the <em>super</em> variable with the parent class
 * of the function.</td>
 * </tr>
 * <tr>
 * <td valign="top">CreateArguments</td>
 * <td>Create the <em>arguments</em> variable which contains the arguments
 * passed to the function.</td>
 * </tr>
 * <tr>
 * <td valign="top">CreateThis</td>
 * <td>Create and initialise the <em>this</em> variable with the object.</td>
 * </tr>
 * <tr>
 * <td valign="top">LoadThis</td>
 * <td>Pre-load the <em>this</em> variable into register number 1.</td>
 * </tr>
 * <tr>
 * <td valign="top">LoadArguments</td>
 * <td>Pre-load the <em>parent</em> variable into register number 2.</td>
 * </tr>
 * <tr>
 * <td valign="top">LoadSuper</td>
 * <td>Pre-load the <em>super</em> variable into register number 3.</td>
 * </tr>
 * <tr>
 * <td valign="top">LoadRoot</td>
 * <td>Pre-load the <em>_root</em> variable into register number 4.</td>
 * </tr>
 * <tr>
 * <td valign="top">LoadParent</td>
 * <td>Pre-load the <em>_parent</em> variable into register number 5.</td>
 * </tr>
 * <tr>
 * <td valign="top">LoadGlobal</td>
 * <td>Pre-load the <em>_global</em> variable into register number 5.</td>
 * </tr>
 * </table>
 *
 * <p>
 * The register numbers that the predefined variables are assigned to are fixed.
 * When specifying which of the functions arguments are also assigned to
 * registers it is important avoid these locations otherwise the variables will
 * be overwritten.
 * </p>
 *
 * <p>
 * User-defined functions are also used to create methods for user-defined
 * objects. The name of the function is omitted and the function definition is
 * assigned to a variable which allows it to be referenced at a alter time. See
 * the example below.
 * </p>
 *
 * <p>
 * The arguments supplied to the function can be referenced by the name supplied
 * in the arguments list.
 * </p>
 *
 * <p>
 * All the action objects added are owned by the function. They will be deleted
 * when the function definition is deleted.
 * </p>
 *
 * @see NewFunction
 */
public class NewFunction2 implements Action {

    /** Number of last internal register in the Flash Player. */
    private static final int LAST_REGISTER = 255;

    /**
     * The Builder class is used to generate a new NewFunction2 object
     * using a small set of convenience methods.
     */
    public static final class Builder {
        /** The name, if any, for the function. */
        private transient String name = "";
        /** The number of registers to allocate for use by the function. */
        private transient int registerCount;
        /** The set of optimizations to speed the function. */
        private transient int optimizations;
        /** The set of arguments, with optional register assignments. */
        private final transient Map<String, Integer> arguments =
            new LinkedHashMap<String, Integer>();
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
         * Set the number of registers to allocate for the function.
         * @param count the number of registers. Must be in the range 0..255.
         * @return this object.
         */
        public Builder allocate(final int count) {
            if ((count < 0) || (count > LAST_REGISTER)) {
                throw new IllegalArgumentRangeException(0,
                        LAST_REGISTER, count);
            }
            registerCount = count;
            return this;
        }

        /**
         * Add an Optimization to be used by the function.
         * @param opt an Optimization used to speed up the function execution.
         * @return this object.
         */
        public Builder optimize(final Optimization opt) {
            optimizations |= opt.getValue();
            return this;
        }

        /**
         * Add the name of an argument to the list of arguments that will be
         * passed to the function. Must not be null or an empty string.
         * @param argName the name of the argument.
         * @return this object.
         */
        public Builder addArgument(final String argName) {
            return addArgument(argName, 0);
        }

        /**
         * Add the name of an argument and the number of the register where it
         * will be stored to the list of arguments that will be
         * passed to the function. The name must not be null or an empty string
         * and the register number must be in the range 0..255. If the number
         * is set to zero then the argument will not be stored in a register.
         *
         * @param argName the name of the argument.
         * @param index the register number.
         * @return this object.
         */
        public Builder addArgument(final String argName, final int index) {
            if (argName == null || argName.length() == 0) {
                throw new IllegalArgumentException();
            }
            if ((index < 0) || (index > LAST_REGISTER)) {
                throw new IllegalArgumentRangeException(0,
                        LAST_REGISTER, index);
            }
            arguments.put(argName, index);
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
         * Generate an NewFunction2 using the parameters defined in the
         * Builder.
         * @return an initialized NewFunction2 object.
         */
        public NewFunction2 build() {
            return new NewFunction2(this);
        }
    }

    /** Format string used in toString() method. */
    private static final String FORMAT = "NewFunction2: { name=%s; "
            + "registerCount=%d; optimizations=%s; arguments=%s; actions=%s}";

    /**
     * The set of optimisations that can be used to speed up the execution of
     * functions.
     */
    public enum Optimization {
        /** Create the predefined variable, <em>super</em>. */
        CREATE_SUPER(4),
        /** Create the predefined variable, <em>arguments</em>. */
        CREATE_ARGUMENTS(16),
        /** Create and initialised the predefined variable, <em>this</em>. */
        CREATE_THIS(64),
        /** Load the predefine variable, <em>this</em>, into register 1. */
        LOAD_THIS(128),
        /** Load the predefine variable, <em>arguments</em>, into register 2. */
        LOAD_ARGUMENTS(32),
        /** Load the predefine variable, <em>super</em>, into register 3. */
        LOAD_SUPER(8),
        /** Load the predefine variable, <em>_root</em>, into register 4. */
        LOAD_ROOT(2),
        /** Load the predefine variable, <em>_parent</em>, into register 5. */
        LOAD_PARENT(1),
        /** Load the predefine variable, <em>_global</em>, into register 6. */
        LOAD_GLOBAL(32768);

        /** Table used to convert flags into Optimization values. */
        private static final Map<Integer, Optimization> TABLE;

        static {
            TABLE = new LinkedHashMap<Integer, Optimization>();

            for (final Optimization opt : values()) {
                TABLE.put(opt.value, opt);
            }
        }

        /** The encoded value for the Optimization. */
        private final int value;

        /**
         * Creates and initializes an Optimization for an encoded value.
         *
         * @param val the encoded value for an Optimization.
         */
        private Optimization(final int val) {
            value = val;
        }

        /**
         * Get the value used to represent the Optimization when encoded.
         * @return the value used to encode the Optimization.
         */
        public int getValue() {
            return value;
        }
    }

    /** Initial number of bytes when encoding. */
    private static final int INITIAL_LENGTH = 5;

    /** The name of the function or an empty string for methods. */
    private final transient String name;
    /** The number of registers to allocate for variables. */
    private final transient int registerCount;
    /** The set of flags identifying optimizations to be applied. */
    private final transient int optimizations;
    /** The set of arguments with optional assignment to registers. */
    private final transient Map<String, Integer> arguments;
    /** The set of actions that make up the function body. */
    private final transient List<Action> actions;

    /** The length of the action, minus the header, when it is encoded. */
    private transient int length;
    /** The length of the encoded function body. */
    private transient int actionsLength;

    /**
     * Creates and initialises a NewFunction2 object using parameters defined
     * in the Builder.
     *
     * @param builder a Builder object containing the parameters to generate
     * the function definition.
     */
    public NewFunction2(final Builder builder) {
        name = builder.name;
        registerCount = builder.registerCount;
        optimizations = builder.optimizations;
        arguments = new LinkedHashMap<String, Integer>(builder.arguments);
        actions = new ArrayList<Action>(builder.actions);
     }

    /**
     * Creates and initialises a NewFunction2 definition using values encoded
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

    public NewFunction2(final SWFDecoder coder, final Context context)
            throws IOException {
        final SWFFactory<Action> decoder = context.getRegistry()
                .getActionDecoder();

        length = coder.readUnsignedShort();
        name = coder.readString();
        final int argumentCount = coder.readUnsignedShort();
        registerCount = coder.readByte();
        optimizations = (coder.readByte() << Coder.TO_UPPER_BYTE)
            + coder.readByte();

        int index;

        arguments = new LinkedHashMap<String, Integer>(argumentCount);

        for (int i = 0; i < argumentCount; i++) {
            index = coder.readByte();
            arguments.put(coder.readString(), index);
        }

        actionsLength = coder.readUnsignedShort();
        coder.mark();
        length += actionsLength;
        actions = new ArrayList<Action>();

        while (coder.bytesRead() < actionsLength) {
            decoder.getObject(actions, coder, context);
        }
        coder.check(actionsLength);
        coder.unmark();
    }

    /**
     * Creates a NewFunction2 with the specified name, argument names and
     * actions to be executed. The order of the Strings in the argument list
     * indicate the order in which the values will be popped off the stack when
     * the function is executed. The first argument is popped from the stack
     * first.
     *
     * @param aString
     *            the name of the function. Can be an empty string if the
     *            function is anonymous.
     * @param count
     *            the number of registers to allocate for variables.
     * @param opts
     *            the set of optimizations that will be applied to boost
     *            function performance.
     * @param map
     *            the arguments and any register numbers they will be
     *            assigned to (zero for no assignment).
     * @param list
     *            the list of actions that define the operation performed by
     *            the function.
     */
    public NewFunction2(final String aString,
            final int count,
            final Set<Optimization>opts,
            final Map<String, Integer> map,
            final List<Action> list) {

        if (aString == null || aString.length() == 0) {
            throw new IllegalArgumentException();
        }
        name = aString;

        if ((count < 0) || (count > LAST_REGISTER)) {
            throw new IllegalArgumentRangeException(0,
                    LAST_REGISTER, count);
        }
        registerCount = count;

        int value = 0;
        for (Optimization opt : opts) {
            value |= opt.getValue();
        }
        optimizations = value;

        if (map == null) {
            throw new IllegalArgumentException();
        }
        arguments = map;

        if (list == null) {
            throw new IllegalArgumentException();
        }
        actions = list;
    }

    /**
     * Creates and initialises a NewFunction2 action using the values
     * copied from another NewFunction2 action.
     *
     * @param object
     *            a NewFunction2 action from which the values will be
     *            copied. References to immutable objects will be shared.
     */
    public NewFunction2(final NewFunction2 object) {
        name = object.name;
        registerCount = object.registerCount;
        optimizations = object.optimizations;
        arguments = new LinkedHashMap<String, Integer>(object.arguments);
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
     * Get the number of registers to allocate for function variables.
     *
     * @return the number of registers to allocate.
     */
    public int getRegisterCount() {
        return registerCount;
    }

    /**
     * Get the list of Optimizations that will be used.
     *
     * @return the set of optimizations to increase performance.
     */
    public Set<Optimization> getOptimizations() {
        final Set<Optimization> set = EnumSet.noneOf(Optimization.class);
        for (final Optimization opt : EnumSet.allOf(Optimization.class)) {
            if ((optimizations & opt.getValue()) != 0) {
                set.add(opt);
            }
        }
        return set;
    }

    /**
     * Get the list of RegisterVariables that define the function arguments
     * and whether they are assigned to internal registers or to local variables
     * in memory.
     *
     * @return a copy of the function arguments with optional register
     * assignments.
     */
    public Map<String, Integer> getArguments() {
        return new LinkedHashMap<String, Integer>(arguments);
    }

    /**
     * Get the actions executed by the function.
     *
     * @return a copy of the list of actions that make up the function body.
     */
    public List<Action> getActions() {
        return new ArrayList<Action>(actions);
    }

    /** {@inheritDoc} */
    public NewFunction2 copy() {
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, name, registerCount, optimizations,
                arguments, actions);
    }


    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        length = INITIAL_LENGTH + context.strlen(name);

        for (final String arg : arguments.keySet()) {
            length += arg.getBytes().length + 2;
        }

        length += 2;

        if (actions.isEmpty()) {
            actionsLength = 1;
        } else {
            actionsLength = 0;
        }

        for (final Action action : actions) {
            actionsLength += action.prepareToEncode(context);
        }

        length += actionsLength;

        return Coder.ACTION_HEADER + length;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        coder.writeByte(ActionTypes.NEW_FUNCTION_2);
        coder.writeShort(length - actionsLength);

        coder.writeString(name);
        coder.writeShort(arguments.size());
        coder.writeByte(registerCount);
        coder.writeByte(optimizations >>> Coder.TO_LOWER_BYTE);
        coder.writeByte(optimizations);

        for (final String arg : arguments.keySet()) {
            coder.writeByte(arguments.get(arg));
            coder.writeString(arg);
        }

        coder.writeShort(actionsLength);

        for (final Action action : actions) {
            action.encode(coder, context);
        }

        if (actions.isEmpty()) {
            coder.writeByte(0);
        }
    }
}
