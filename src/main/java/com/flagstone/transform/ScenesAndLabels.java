/*
 * ScenesAndLabels.java
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

package com.flagstone.transform;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.exception.IllegalArgumentRangeException;

/**
 * ScenesAndLabels is used to list the scenes (main timeline only) and labelled
 * frames for movies and movie clips.
 */
public class ScenesAndLabels implements MovieTag {

    /** Format string used in toString() method. */
    private static final String FORMAT = "ScenesAndLabels: { scenes=%s;"
            + " labels=%s}";

    /** The table of scenes. */
    private Map<Integer, String> scenes;
    /** The table of labelled frames. */
    private Map<Integer, String> labels;

    /** The length of the object, minus the header, when it is encoded. */
    private transient int length;

    /**
     * Creates and initialises a ScenesAndLabels object using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public ScenesAndLabels(final SWFDecoder coder) throws IOException {
        length = coder.readUnsignedShort() & Coder.LENGTH_FIELD;
        if (length == Coder.IS_EXTENDED) {
            length = coder.readInt();
        }
        coder.mark();

        int count = coder.readVarInt();
        scenes = new LinkedHashMap<Integer, String>(count);
        for (int i = 0; i < count; i++) {
            scenes.put(coder.readVarInt(), coder.readString());
        }

        count = coder.readVarInt();
        labels = new LinkedHashMap<Integer, String>(count);
        for (int i = 0; i < count; i++) {
            labels.put(coder.readVarInt(), coder.readString());
        }
        coder.check(length);
        coder.unmark();
    }

    /**
     * Creates a ScenesAndLabels object with empty tables for the scenes and
     * labels.
     */
    public ScenesAndLabels() {
        scenes = new LinkedHashMap<Integer, String>();
        labels = new LinkedHashMap<Integer, String>();
    }

    /**
     * Create a new ScenesAndLabels object with the specified list of scenes
     * and labelled frames.
     * @param sceneMap a table of frame numbers and the associated name for
     * the scenes on the main timeline of a movie.
     * @param labelMap a table of frame numbers and the associated name for
     * the labelled frames in a movie or movie clip.
     */
    public ScenesAndLabels(final Map<Integer, String> sceneMap,
            final Map<Integer, String> labelMap) {
        scenes = sceneMap;
        labels = labelMap;
    }

    /**
     * Creates and initialises a ScenesAndLabels object using the values copied
     * from another ScenesAndLabels object.
     *
     * @param object
     *            a ScenesAndLabels object from which the values will be
     *            copied.
     */
    public ScenesAndLabels(final ScenesAndLabels object) {
        scenes = new LinkedHashMap<Integer, String>(object.scenes);
        labels = new LinkedHashMap<Integer, String>(object.labels);
    }

    /**
     * Add an entry to the list of scenes with the frame number and scene
     * name.
     * @param offset the frame number.
     * @param name the scene name.
     * @return this object.
     */
    public ScenesAndLabels addScene(final int offset, final String name) {
        if ((offset < 0) || (offset > Coder.USHORT_MAX)) {
            throw new IllegalArgumentRangeException(
                    0, Coder.USHORT_MAX, offset);
        }
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException();
        }
        scenes.put(offset, name);
        return this;
    }

    /**
     * Get the table of frame numbers and associated names.
     * @return a map associating frame numbers to scene names.
     */
    public Map<Integer, String> getScenes() {
        return scenes;
    }

    /**
     * Set the table of frame numbers and associated names.
     * @param map a table associating frame numbers to scene names.
     */
    public void setScenes(final Map<Integer, String> map) {
        if (map == null) {
            throw new IllegalArgumentException();
        }
        scenes = map;
    }

    /**
     * Add an entry to the list of labelled frames with the frame number and
     * frame label.
     * @param offset the frame number.
     * @param name the frame label.
     * @return this object.
     */
    public ScenesAndLabels addLabel(final int offset, final String name) {
        if ((offset < 0) || (offset > Coder.USHORT_MAX)) {
            throw new IllegalArgumentRangeException(
                    0, Coder.USHORT_MAX, offset);
        }
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException();
        }
        labels.put(offset, name);
        return this;
    }

    /**
     * Get the table of frame numbers and frame labels.
     * @return a map associating frame numbers to frame labels.
     */
    public Map<Integer, String> getLabels() {
        return labels;
    }

    /**
     * Set the table of frame numbers and associated names.
     * @param map a table associating frame numbers to frame labels.
     */
    public void setLabels(final Map<Integer, String> map) {
        if (map == null) {
            throw new IllegalArgumentException();
        }
        labels = map;
    }

    /** {@inheritDoc} */
    public ScenesAndLabels copy() {
        return new ScenesAndLabels(this);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, scenes, labels);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {

        length = Coder.sizeVariableU32(scenes.size());

        for (final Integer offset : scenes.keySet()) {
            length += Coder.sizeVariableU32(offset)
                    + context.strlen(scenes.get(offset));
        }

        length += Coder.sizeVariableU32(labels.size());

        for (final Integer offset : labels.keySet()) {
            length += Coder.sizeVariableU32(offset)
                    + context.strlen(labels.get(offset));
        }

        return (length > Coder.HEADER_LIMIT ? Coder.LONG_HEADER
                : Coder.SHORT_HEADER) + length;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {

        if (length > Coder.HEADER_LIMIT) {
            coder.writeShort((MovieTypes.SCENES_AND_LABELS
                    << Coder.LENGTH_FIELD_SIZE) | Coder.IS_EXTENDED);
            coder.writeInt(length);
        } else {
            coder.writeShort((MovieTypes.SCENES_AND_LABELS
                    << Coder.LENGTH_FIELD_SIZE) | length);
        }
        if (Constants.DEBUG) {
            coder.mark();
        }
        coder.writeVarInt(scenes.size());

        for (final Integer identifier : scenes.keySet()) {
            coder.writeVarInt(identifier.intValue());
            coder.writeString(scenes.get(identifier));
        }

        coder.writeVarInt(labels.size());

        for (final Integer identifier : labels.keySet()) {
            coder.writeVarInt(identifier.intValue());
            coder.writeString(labels.get(identifier));
        }
        if (Constants.DEBUG) {
            coder.check(length);
            coder.unmark();
        }
    }
}
