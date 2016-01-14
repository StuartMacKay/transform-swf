/*
 *  DecoderRegistry.java
 *  Transform Utilities
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

package com.flagstone.transform.coder;

import com.flagstone.transform.MovieDecoder;
import com.flagstone.transform.MovieTag;
import com.flagstone.transform.action.Action;
import com.flagstone.transform.action.ActionDecoder;
import com.flagstone.transform.fillstyle.FillStyle;
import com.flagstone.transform.fillstyle.FillStyleDecoder;
import com.flagstone.transform.fillstyle.MorphFillStyleDecoder;
import com.flagstone.transform.filter.Filter;
import com.flagstone.transform.filter.FilterDecoder;
import com.flagstone.transform.shape.ShapeDecoder;
import com.flagstone.transform.shape.ShapeRecord;


/**
 * The DecoderRegistry is used to maintain a table of objects that can be used
 * to decode the different types of object encountered in a Flash file.
 */
public class DecoderRegistry implements Copyable<DecoderRegistry> {

    /** Registry containing a set of default decoders for different objects. */
    private static DecoderRegistry defaultRegistry;

    static {
        defaultRegistry = new DecoderRegistry();
        defaultRegistry.setFilterDecoder(new FilterDecoder());
        defaultRegistry.setFillStyleDecoder(new FillStyleDecoder());
        defaultRegistry.setMorphFillStyleDecoder(new MorphFillStyleDecoder());
        defaultRegistry.setShapeDecoder(new ShapeDecoder());
        defaultRegistry.setActionDecoder(new ActionDecoder());
        defaultRegistry.setMovieDecoder(new MovieDecoder());
    }

    /**
     * Get the default registry.
     *
     * @return a registry with a default set of decoders.
     */
    public static DecoderRegistry getDefault() {
        return new DecoderRegistry(defaultRegistry);
    }

    /**
     * Set the registry that will be used as the default.
     *
     * @param registry the DecoderRegistry that will replace the existing
     * default.
     */
    public static void setDefault(final DecoderRegistry registry) {
        defaultRegistry = new DecoderRegistry(registry);
    }

    /** The decoder for filters. */
    private transient SWFFactory<Filter> filterDecoder;
    /** The decoder for fill styles. */
    private transient SWFFactory<FillStyle> fillStyleDecoder;
    /** The decoder for morphing fill styles. */
    private transient SWFFactory<FillStyle> morphStyleDecoder;
    /** The decoder for shape records. */
    private transient SWFFactory<ShapeRecord> shapeDecoder;
    /** The decoder for actions. */
    private transient SWFFactory<Action> actionDecoder;
    /** The decoder for movie objects. */
    private transient SWFFactory<MovieTag> movieDecoder;

    /**
     * Creates a DecoderRegistry with no decoders yet registered.
     */
    public DecoderRegistry() {
        // All decoders default to null
    }

    /**
     * Create a new registry and initialize it with the decoders from an
     * existing registry.
     *
     * @param registry the DeocderRegistry to copy.
     */
    public DecoderRegistry(final DecoderRegistry registry) {
        filterDecoder = registry.filterDecoder;
        fillStyleDecoder = registry.fillStyleDecoder;
        morphStyleDecoder = registry.morphStyleDecoder;
        shapeDecoder = registry.shapeDecoder;
        actionDecoder = registry.actionDecoder;
        movieDecoder = registry.movieDecoder;
    }

    /** {@inheritDoc} */
    public DecoderRegistry copy() {
        return new DecoderRegistry(this);
    }

    /**
     * Get the decoder that will be used for Filter objects.
     * @return the decoder for filters.
     */
    public SWFFactory<Filter> getFilterDecoder() {
        return filterDecoder;
    }

    /**
     * Set the decoder that will be used for filters.
     * @param factory an instance of the class that will be used to decode
     * the filters defined in Place3 or ButtonShape objects.
     */
    public void setFilterDecoder(final SWFFactory<Filter> factory) {
        filterDecoder = factory;
    }

    /**
     * Get the decoder that will be used for FillStyle objects.
     * @return the decoder for fill styles.
     */
    public SWFFactory<FillStyle> getFillStyleDecoder() {
        return fillStyleDecoder;
    }

    /**
     * Set the decoder that will be used for fill styles in a shape.
     * @param factory an instance of the class that will be used to decode
     * the fill styles in a shape.
     */
    public void setFillStyleDecoder(final SWFFactory<FillStyle> factory) {
        fillStyleDecoder = factory;
    }

    /**
     * Get the decoder that will be used for FillStyle objects used in morphing
     * shapes.
     * @return the decoder for morphing fill styles.
     */
    public SWFFactory<FillStyle> getMorphFillStyleDecoder() {
        return morphStyleDecoder;
    }

    /**
     * Set the decoder that will be used for fill styles in a morphing shape.
     * @param factory an instance of the class that will be used to decode
     * the fill styles in a morphing shape.
     */
    public void setMorphFillStyleDecoder(final SWFFactory<FillStyle> factory) {
        morphStyleDecoder = factory;
    }

    /**
     * Get the decoder that will be used for ShapeRecords.
     * @return the decoder for the objects in Shapes.
     */
    public SWFFactory<ShapeRecord> getShapeDecoder() {
        return shapeDecoder;
    }

    /**
     * Set the decoder that will be used for shapes.
     * @param factory an instance of the class that will be used to decode
     * the shape records in a movie.
     */
   public void setShapeDecoder(final SWFFactory<ShapeRecord> factory) {
        shapeDecoder = factory;
    }

   /**
    * Get the decoder that will be used for actions.
    * @return the decoder for actions.
    */
    public SWFFactory<Action> getActionDecoder() {
        return actionDecoder;
    }

    /**
     * Set the decoder that will be used for actions.
     * @param factory an instance of the class that will be used to decode
     * the actions in a movie.
     */
    public void setActionDecoder(final SWFFactory<Action> factory) {
        actionDecoder = factory;
    }

    /**
     * Get the decoder that will be used for movie objects.
     * @return the decoder for the main objects decoded in a Flash file.
     */

    public SWFFactory<MovieTag> getMovieDecoder() {
        return movieDecoder;
    }

    /**
     * Set the decoder that will be used for movie objects.
     * @param factory an instance of the class that will be used to decode
     * the main objects in a movie.
     */
    public void setMovieDecoder(final SWFFactory<MovieTag> factory) {
        movieDecoder = factory;
    }
}
