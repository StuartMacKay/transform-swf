/*
 * FilterTypes.java
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
package com.flagstone.transform.filter;

/**
 * FilterTypes defines the values that are used to identify the different types
 * of filter when they are encoded in a Flash file.
 */
public class FilterTypes {
    /** A DropShadowFilter. */
    public static final int DROP_SHADOW = 0;
    /** A BlueFilter. */
    public static final int BLUR = 1;
    /** A GlowFilter. */
    public static final int GLOW = 2;
    /** A BevelFilter. */
    public static final int BEVEL = 3;
    /** A GradientGlowFilter. */
    public static final int GRADIENT_GLOW = 4;
    /** A ConvolutionFilter. */
    public static final int CONVOLUTION = 5;
    /** A ColorMatrixFilter. */
    public static final int COLOR_MATRIX = 6;
    /** A GradientBevelFilter. */
    public static final int GRADIENT_BEVEL = 7;

    /** Private constructor. */
    private FilterTypes() {
        // Class contains only constants
    }
}
