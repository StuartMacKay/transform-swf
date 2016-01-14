/*
 * ActionTypes.java
 * Transform
 *
 * Copyright (c) 2009-2010 Flagstone Software Ltd. All rights reserved.
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

/**
 * ActionTypes defines the constants that identify the different types of action
 * when encoded according to the Flash file format specification.
 * <p>
 * IMPORTANT: Only Actions for Actionscript 1.0 and 2.0 are included, actions
 * defined for Actionscript 3.0 are not included.
 * </p>
 */
public class ActionTypes {
    /** The type for creating the end of a sequence of actions. */
    public static final int END = 0;
    /** The type used to identify NextFrame action when encoded. */
    public static final int NEXT_FRAME = 4;
    /** The type used to identify PrevFrame action when encoded. */
    public static final int PREV_FRAME = 5;
    /** The type used to identify Play action when encoded. */
    public static final int PLAY = 6;
    /** The type used to identify Stop action when encoded. */
    public static final int STOP = 7;
    /** The type used to identify ToggleQuality action when encoded. */
    public static final int TOGGLE_QUALITY = 8;
    /** The type used to identify StopSounds action when encoded. */
    public static final int STOP_SOUNDS = 9;
    /** The type used to identify IntegerAdd action when encoded. */
    public static final int INTEGER_ADD = 10;
    /** The type used to identify Subtract action when encoded. */
    public static final int SUBTRACT = 11;
    /** The type used to identify Multiply action when encoded. */
    public static final int MULTIPLY = 12;
    /** The type used to identify Divide action when encoded. */
    public static final int DIVIDE = 13;
    /** The type used to identify IntegerEquals action when encoded. */
    public static final int INTEGER_EQUALS = 14;
    /** The type used to identify IntegerLess action when encoded. */
    public static final int INTEGER_LESS = 15;
    /** The type used to identify And action when encoded. */
    public static final int LOGICAL_AND = 16;
    /** The type used to identify Not action when encoded. */
    public static final int LOGICAL_NOT = 18;
    /** The type used to identify Or action when encoded. */
    public static final int LOGICAL_OR = 17;
    /** The type used to identify StringEquals action when encoded. */
    public static final int STRING_EQUALS = 19;
    /** The type used to identify StringLength action when encoded. */
    public static final int STRING_LENGTH = 20;
    /** The type used to identify StringExtract action when encoded. */
    public static final int STRING_EXTRACT = 21;
    /** The type used to identify Pop action when encoded. */
    public static final int POP = 23;
    /** The type used to identify ToInteger action when encoded. */
    public static final int TO_INTEGER = 24;
    /** The type used to identify GetVariable action when encoded. */
    public static final int GET_VARIABLE = 28;
    /** The type used to identify SetVariable action when encoded. */
    public static final int SET_VARIABLE = 29;
    /** The type used to identify SetTarget2 action when encoded. */
    public static final int SET_TARGET_2 = 32;
    /** The type used to identify StringAdd action when encoded. */
    public static final int STRING_ADD = 33;
    /** The type used to identify GetProperty action when encoded. */
    public static final int GET_PROPERTY = 34;
    /** The type used to identify SetProperty action when encoded. */
    public static final int SET_PROPERTY = 35;
    /** The type used to identify CloneSprite action when encoded. */
    public static final int CLONE_SPRITE = 36;
    /** The type used to identify RemoveSprite action when encoded. */
    public static final int REMOVE_SPRITE = 37;
    /** The type used to identify Trace action when encoded. */
    public static final int TRACE = 38;
    /** The type used to identify StartDrag action when encoded. */
    public static final int START_DRAG = 39;
    /** The type used to identify EndDrag action when encoded. */
    public static final int END_DRAG = 40;
    /** The type used to identify StringLess action when encoded. */
    public static final int STRING_LESS = 41;
    /** The type used to identify Throw action when encoded. */
    public static final int THROW = 42;
    /** The type used to identify Cast action when encoded. */
    public static final int CAST = 43;
    /** The type used to identify Implements action when encoded. */
    public static final int IMPLEMENTS = 44;
    /** The type used to identify FSCommand2 action when encoded. */
    public static final int FS_COMMAND2 = 45;
    /** The type used to identify RandomNumber action when encoded. */
    public static final int RANDOM_NUMBER = 48;
    /** The type used to identify MBStringLength action when encoded. */
    public static final int MB_STRING_LENGTH = 49;
    /** The type used to identify CharToAscii action when encoded. */
    public static final int CHAR_TO_ASCII = 50;
    /** The type used to identify AsciiToChar action when encoded. */
    public static final int ASCII_TO_CHAR = 51;
    /** The type used to identify GetTime action when encoded. */
    public static final int GET_TIME = 52;
    /** The type used to identify MBStringExtract action when encoded. */
    public static final int MB_STRING_EXTRACT = 53;
    /** The type used to identify MBCharToAscii action when encoded. */
    public static final int MB_CHAR_TO_ASCII = 54;
    /** The type used to identify MBAsciiToChar action when encoded. */
    public static final int MB_ASCII_TO_CHAR = 55;
    /** The type used to identify DeleteVariable action when encoded. */
    public static final int DELETE_VARIABLE = 58;
    /** The type used to identify Delete action when encoded. */
    public static final int DELETE = 59;
    /** The type used to identify InitVariable action when encoded. */
    public static final int INIT_VARIABLE = 60;
    /** The type used to identify ExecuteFunction action when encoded. */
    public static final int EXECUTE_FUNCTION = 61;
    /** The type used to identify Return action when encoded. */
    public static final int RETURN = 62;
    /** The type used to identify Modulo action when encoded. */
    public static final int MODULO = 63;
    /** The type used to identify NamedObject action when encoded. */
    public static final int NAMED_OBJECT = 64;
    /** The type used to identify NewVariable action when encoded. */
    public static final int NEW_VARIABLE = 65;
    /** The type used to identify NewArray action when encoded. */
    public static final int NEW_ARRAY = 66;
    /** The type used to identify NewObject action when encoded. */
    public static final int NEW_OBJECT = 67;
    /** The type used to identify GetType action when encoded. */
    public static final int GET_TYPE = 68;
    /** The type used to identify GetTarget action when encoded. */
    public static final int GET_TARGET = 69;
    /** The type used to identify Enumerate action when encoded. */
    public static final int ENUMERATE = 70;
    /** The type used to identify Add action when encoded. */
    public static final int ADD = 71;
    /** The type used to identify Less action when encoded. */
    public static final int LESS = 72;
    /** The type used to identify Equals action when encoded. */
    public static final int EQUALS = 73;
    /** The type used to identify ToNumber action when encoded. */
    public static final int TO_NUMBER = 74;
    /** The type used to identify ToString action when encoded. */
    public static final int TO_STRING = 75;
    /** The type used to identify Duplicate action when encoded. */
    public static final int DUPLICATE = 76;
    /** The type used to identify Swap action when encoded. */
    public static final int SWAP = 77;
    /** The type used to identify GetAttribute action when encoded. */
    public static final int GET_ATTRIBUTE = 78;
    /** The type used to identify SetAttribute action when encoded. */
    public static final int SET_ATTRIBUTE = 79;
    /** The type used to identify Increment action when encoded. */
    public static final int INCREMENT = 80;
    /** The type used to identify Decrement action when encoded. */
    public static final int DECREMENT = 81;
    /** The type used to identify ExecuteMethod action when encoded. */
    public static final int EXECUTE_METHOD = 82;
    /** The type used to identify NewMethod action when encoded. */
    public static final int NEW_METHOD = 83;
    /** The type used to identify InstanceOf action when encoded. */
    public static final int INSTANCEOF = 84;
    /** The type used to identify EnumerateObject action when encoded. */
    public static final int ENUMERATE_OBJECT = 85;
    /** The type used to identify BitwiseAnd action when encoded. */
    public static final int BITWISE_AND = 96;
    /** The type used to identify BitwiseOr action when encoded. */
    public static final int BITWISE_OR = 97;
    /** The type used to identify BitwiseXOr action when encoded. */
    public static final int BITWISE_XOR = 98;
    /** The type used to identify LogicalShiftLeft action when encoded. */
    public static final int SHIFT_LEFT = 99;
    /** The type used to identify ArithmeticShiftRight action when encoded. */
    public static final int ARITH_SHIFT_RIGHT = 100;
    /** The type used to identify LogicalShiftRight action when encoded. */
    public static final int SHIFT_RIGHT = 101;
    /** The type used to identify StrictEquals action when encoded. */
    public static final int STRICT_EQUALS = 102;
    /** The type used to identify Greater action when encoded. */
    public static final int GREATER = 103;
    /** The type used to identify StringGreater action when encoded. */
    public static final int STRING_GREATER = 104;
    /** The type used to identify Extends action when encoded. */
    public static final int EXTENDS = 105;
    /** The type used to identify GotoFrame action when encoded. */
    public static final int GOTO_FRAME = 129;
    /** The type used to identify GetUrl action when encoded. */
    public static final int GET_URL = 131;
    /** The type used to identify RegisterCopy action when encoded. */
    public static final int REGISTER_COPY = 135;
    /** The type used to identify Table action when encoded. */
    public static final int TABLE = 136;
    /** The type used to identify WaitForFrame action when encoded. */
    public static final int WAIT_FOR_FRAME = 138;
    /** The type used to identify SetTarget action when encoded. */
    public static final int SET_TARGET = 139;
    /** The type used to identify GotoLabel action when encoded. */
    public static final int GOTO_LABEL = 140;
    /** The type used to identify WaitForFrame2 action when encoded. */
    public static final int WAIT_FOR_FRAME_2 = 141;
    /** The type used to identify NewFunction2 action when encoded. */
    public static final int NEW_FUNCTION_2 = 142;
    /** The type used to identify ExceptionHandler action when encoded. */
    public static final int EXCEPTION_HANDLER = 143;
    /** The type used to identify With action when encoded. */
    public static final int WITH = 148;
    /** The type used to identify Push action when encoded. */
    public static final int PUSH = 150;
    /** The type used to identify Jump action when encoded. */
    public static final int JUMP = 153;
    /** The type used to identify GetUrl2 action when encoded. */
    public static final int GET_URL_2 = 154;
    /** The type for creating an If action. */
    public static final int IF = 157; //NOPMD
    /** The type used to identify Call action when encoded. */
    public static final int CALL = 158;
    /** The type used to identify GotoFrame2 action when encoded. */
    public static final int GOTO_FRAME_2 = 159;
    /** The type used to identify NewFunction action when encoded. */
    public static final int NEW_FUNCTION = 155;

    /**
     * The highest value used to encode an action that only operates on values
     * on the Flash Player's stack.
     */
    public static final int HIGHEST_BYTE_CODE = 127;

    /** Private constructor for a class that contains only constants. */
    private ActionTypes() {
        // Class only contains constants
    }
}
