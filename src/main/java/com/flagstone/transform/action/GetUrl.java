/*
 * GetUrl.java
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

/**
 * GetUrl is used to display a web page or load a movie clip into the Flash
 * Player.
 *
 * <p>
 * In addition to the URL to be loaded, GetUrl also contains a target which is
 * either a level in the Flash Player where the movie clip will be loaded or
 * frame or window in the browser where the web page will be displayed. The
 * following reserved words may be used to identify a specific frame or window
 * in a web browser:
 * </p>
 *
 * <table class="datasheet">
 *
 * <tr>
 * <td valign="top"><code>_blank</code></td>
 * <td>opens the new page in a new window.</td>
 * </tr>
 *
 * <tr>
 * <td valign="top"><code>_self</code></td>
 * <td>opens the new page in the current window.</td>
 * </tr>
 *
 * <tr>
 * <td valign="top"><code>_top</code></td>
 * <td>opens the new page in the top level frame of the current window.</td>
 * </tr>
 *
 * <tr>
 * <td valign="top"><code>_parent</code></td>
 * <td>opens the new page in the parent frame of the frame where the Flash
 * Player id displayed.</td>
 * </tr>
 *
 * <tr>
 * <td valign="top"><code>""</code></td>
 * <td>(blank string) opens the new page in the current frame or window.</td>
 * </tr>
 *
 * </table>
 *
 * <p>
 * To load a movie clip into the currently playing movie then the target is a
 * string literal of the form "_level<i>n</i>". The Flash Player supports the
 * concept of virtual layers (analogous to the layers in the Display List).
 * Higher levels are displayed in front of lower levels. The background of each
 * level is transparent allowing movie clips on lower levels to be visible in
 * areas not filled by the movie clip on a given level. The main movie is loaded
 * into _level0. Movie clips are loaded into any level above this (1, 2, 3,
 * ...). If a movie clip is loaded into a level that already contains a movie
 * clip then the existing clip is replaced by the new one.
 * </p>
 *
 * @see GetUrl2
 */
public class GetUrl implements Action {

    /** Format string used in toString() method. */
    private static final String FORMAT = "GetUrl: { url=%s; target=%s}";

    /** The URL to be loaded. */
    private final transient String url;
    /** Where the document returned by the URL will be displayed. */
    private final transient String target;

    /** The length of the action, minus the header, when it is encoded. */
    private transient int length;

    /**
     * Creates and initialises a GetUrl action using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public GetUrl(final SWFDecoder coder) throws IOException {
        length = coder.readUnsignedShort();
        url = coder.readString();
        target = coder.readString();
    }

    /**
     * Creates a GetUrl with the specified url and target frame.
     *
     * @param urlString
     *            a fully qualified URL. Must not be null.
     * @param targetString
     *            the location (in the Flash Player or web browser) where the
     *            contents of file retrieved via the url will be displayed. Must
     *            not be null.
     */
    public GetUrl(final String urlString, final String targetString) {
        if (urlString == null) {
            throw new IllegalArgumentException();
        }
        url = urlString;

        if (targetString == null) {
            throw new IllegalArgumentException();
        }
        target = targetString;
    }

    /**
     * Creates a GetUrl with the specified url. The target defaults to the
     * current window.
     *
     * @param urlString
     *            a fully qualified URL. Must not be null or an empty string.
     */
    public GetUrl(final String urlString) {
        this(urlString, "");
    }

    /**
     * Creates and initialises a GetUrl action using the values
     * copied from another GetUrl action.
     *
     * @param object
     *            a GetUrl action from which the values will be
     *            copied.
     */
    public GetUrl(final GetUrl object) {
        url = object.url;
        target = object.target;
    }

    /**
     * Get the URL from where the web page or movie clip will be loaded.
     *
     * @return the URL.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Get the name of the target frame where the web page or movie clip will
     * be displayed.
     *
     * @return the name of the target frame.
     */
    public String getTarget() {
        return target;
    }

    /** {@inheritDoc} */
    public GetUrl copy() {
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, url, target);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        length = context.strlen(url);
        length += context.strlen(target);

        return Coder.ACTION_HEADER + length;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        coder.writeByte(ActionTypes.GET_URL);
        coder.writeShort(length);
        coder.writeString(url);
        coder.writeString(target);
    }
}
