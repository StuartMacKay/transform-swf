/*
 * GetUrl2.java
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
import java.util.LinkedHashMap;
import java.util.Map;

import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;

/**
 * The GetUrl2 action is used to either load a web page or movie clip or load or
 * submit variable values to/from a server.
 *
 * <p>
 * It extends the functionality provided by the GetUrl action by allowing the
 * variables defined in a movie to be submitted as form values to a server.
 * Variables defined in a movie can also be initialised by loading a file
 * containing variable name / value assignments.
 * </p>
 *
 * <p>
 * GetUrl2 gets the URL and the target from the Flash Player stack. The
 * <i>url</i> is the first argument popped from the stack and is a fully
 * qualified uniform resource location where the movie clip or web page will be
 * retrieved from. The second argument <i>target</i> - is either the name of a
 * specific movie clip, e.g. _root.movieClip or the name of a level in the main
 * movie into which a movie clip has been loaded, e.g. _level1.
 * </p>
 *
 * <p>
 * The <i>target</i> can either be the name of the frame can be one of the
 * following reserved words:
 * </p>
 *
 * <table class="datasheet">
 * <tr>
 * <td valign="top"><code>"name"</code></td>
 * <td>opens the new page in the frame with the name defined in the HTML
 * &lt;frame&gt; tag.</td>
 * </tr>
 * <tr>
 * <td valign="top"><code>_blank</code></td>
 * <td>opens the new page in a new window.</td>
 * </tr>
 * <tr>
 * <td valign="top"><code>_self</code></td>
 * <td>opens the new page in the current window.</td>
 * </tr>
 * <tr>
 * <td valign="top"><code>_top</code></td>
 * <td>opens the new page in the top level frame of the current window.</td>
 * </tr>
 * <tr>
 * <td valign="top"><code>_parent</code></td>
 * <td>opens the new page in the parent frame of the frame where the Flash
 * Player id displayed.</td>
 * </tr>
 * <tr>
 * <td valign="top"><code>""</code></td>
 * <td>(blank string) opens the new page in the current frame or window.</td>
 * </tr>
 * </table>
 *
 * <p>
 * Levels are virtual layers (analogous to the layers in the Display List).
 * Higher levels are displayed in front of lower levels. The background of each
 * level is transparent allowing movie clips on lower levels to be visible in
 * areas not filled by the movie clip on a given level. The main movie is loaded
 * into level 0. Movie clips are loaded into any level above this (1, 2, 124,
 * etc.). If a movie clip is loaded into a level that already contains a movie
 * clip then the existing clip is replaced by the new one. The level follows the
 * general form: "_level<i>n</i>" loads a movie clip into the current movie at
 * level <i>n</i>.
 * </p>
 *
 */
public class GetUrl2 implements Action {

    /** Format string used in toString() method. */
    private static final String FORMAT = "GetUrl2: { requestType=%s}";

    /** The encoded value for a MOVIE_TO_LEVEL request. */
    private static final int MOVIE_LEVEL = 0;
    /** The encoded value for a MOVIE_TO_LEVEL_WITH_GET request. */
    private static final int MOVIE_LEVEL_GET = 1;
    /** The encoded value for a MOVIE_TO_LEVEL_WITH_POST request. */
    private static final int MOVIE_LEVEL_POST = 2;
    /** The encoded value for a MOVIE_TO_TARGET request. */
    private static final int MOVIE_TARGET = 64;
    /** The encoded value for a MOVIE_TO_TARGET_WITH_GET request. */
    private static final int MOVIE_TARGET_GET = 65;
    /** The encoded value for a MOVIE_TO_TARGET_WITH_POST request. */
    private static final int MOVIE_TARGET_POST = 66;
    /** The encoded value for a VARIABLES_TO_LEVEL request. */
    private static final int VAR_LEVEL = 128;
    /** The encoded value for a VARIABLES_TO_LEVEL_WITH_GET request. */
    private static final int VAR_LEVEL_GET = 129;
    /** The encoded value for a VARIABLES_TO_LEVEL_WITH_POST request. */
    private static final int VAR_LEVEL_POST = 130;
    /** The encoded value for a VARIABLES_TO_TARGET request. */
    private static final int VAR_TARGET = 192;
    /** The encoded value for a VARIABLES_TO_TARGET_WITH_GET request. */
    private static final int VAR_TARGET_GET = 193;
    /** The encoded value for a VARIABLES_TO_TARGET_WITH_POST request. */
    private static final int VAR_TARGET_POST = 194;

    /**
     * Request defines the different types of request that can be submitted
     * to a server using a GetUrl action.
      */
    public enum Request {
        /** Load a movie without submitting the movie variables. */
        MOVIE_TO_LEVEL(MOVIE_LEVEL),
        /** Load a movie submitting the movie variables using HTTP GET. */
        MOVIE_TO_LEVEL_WITH_GET(MOVIE_LEVEL_GET),
        /** Load a movie submitting the movie variables using HTTP POST. */
        MOVIE_TO_LEVEL_WITH_POST(MOVIE_LEVEL_POST),
        /** Load a movie or web page without submitting the movie variables. */
        MOVIE_TO_TARGET(MOVIE_TARGET),
        /** Load a movie or web page sending variables using HTTP GET. */
        MOVIE_TO_TARGET_WITH_GET(MOVIE_TARGET_GET),
        /** Load a movie or web page sending the variables using HTTP POST. */
        MOVIE_TO_TARGET_WITH_POST(MOVIE_TARGET_POST),
        /** Load variables without submitting the movie variables. */
        VARIABLES_TO_LEVEL(VAR_LEVEL),
        /** Load variables submitting the movie variables using HTTP GET. */
        VARIABLES_TO_LEVEL_WITH_GET(VAR_LEVEL_GET),
        /** Load variables submitting the movie variables using HTTP POST. */
        VARIABLES_TO_LEVEL_WITH_POST(VAR_LEVEL_POST),
        /** Load variables without submitting the movie variables. */
        VARIABLES_TO_TARGET(VAR_TARGET),
        /** Load variables submitting the movie variables using HTTP GET. */
        VARIABLES_TO_TARGET_WITH_GET(VAR_TARGET_GET),
        /** Load variables submitting the movie variables using HTTP POST. */
        VARIABLES_TO_TARGET_WITH_POST(VAR_TARGET_POST);

        /** Table used to decode encoded values into enum values. */
        private static final Map<Integer, Request> TABLE;

        static {
            TABLE = new LinkedHashMap<Integer, Request>();

            for (final Request property : values()) {
                TABLE.put(property.value, property);
            }
        }

        /**
         * Used to generate the Request from its encoded value.
         *
         * @param requestType the value for an encoded Request.
         *
         * @return the decoded Request type.
         */
        private static Request fromInt(final int requestType) {
            return TABLE.get(requestType);
        }

        /** The value represented the request when encoded. */
        private final int value;

        /**
         * Creates a Request representing an encoded value.
         *
         * @param encodedValue the value representing the encoded Request.
         */
        private Request(final int encodedValue) {
            value = encodedValue;
        }

        /**
         * Get the value used to represent the request when it is encoded.
         *
         * @return the value for the encoded request.
         */
        public int getValue() {
            return value;
        }
    }

    /** The value represented the request when encoded. */
    private final transient int request;

    /**
     * Creates and initialises a GetUrl2 action using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public GetUrl2(final SWFDecoder coder) throws IOException {
        coder.readUnsignedShort();
        request = coder.readByte();
    }

    /**
     * Creates a GetUrl2 using the specified request type.
     *
     * @param req
     *            the type of request to be performed.
     */
    public GetUrl2(final Request req) {
        request = req.value;
    }

    /**
     * Creates and initialises a GetUrl2 action using the values
     * copied from another GetUrl2 action.
     *
     * @param object
     *            a GetUrl2 action from which the values will be
     *            copied.
     */
    public GetUrl2(final GetUrl2 object) {
        request = object.request;
    }

    /**
     * Gets the request that will be executed by this action.
     *
     * @return the type of Request that will be performed.
     */
    public Request getRequest() {
        return Request.fromInt(request);
    }

    /** {@inheritDoc} */
    public GetUrl2 copy() {
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, request);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        return Coder.ACTION_HEADER + 1;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        coder.writeByte(ActionTypes.GET_URL_2);
        coder.writeShort(1);
        coder.writeByte(request);
    }
}
