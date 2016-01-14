/*
 * Place3.java
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
import java.util.ArrayList;
import java.util.List;

import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.coder.SWFFactory;
import com.flagstone.transform.datatype.Blend;
import com.flagstone.transform.datatype.ColorTransform;
import com.flagstone.transform.datatype.CoordTransform;
import com.flagstone.transform.exception.IllegalArgumentRangeException;
import com.flagstone.transform.filter.Filter;

/**
 * Place3 is used to update the display list. It extends Place2 by specifying
 * a Blend which controls how an object is composited with its background and
 * also Filters which can be used to create special effects such as drop shadows
 * etc.
 *
 * @see Place2
 */
@SuppressWarnings({"PMD.TooManyFields", "PMD.TooManyMethods",
       "PMD.CyclomaticComplexity" })
public class Place3 implements MovieTag {

    /**
     * Place a new object on the display list.
     * @param identifier the unique identifier for the object.
     * @param layer the layer where it will be displayed.
     * @param xCoord the x-coordinate where the object's origin will be.
     * @param yCoord the y-coordinate where the object's origin will be.
     * @return the Place3 object to update the display list.
     */
    public static Place3 show(final int identifier, final int layer,
            final int xCoord, final int yCoord) {
        final Place3 object = new Place3();
        object.setType(PlaceType.NEW);
        object.setLayer(layer);
        object.setIdentifier(identifier);
        object.setTransform(CoordTransform.translate(xCoord, yCoord));
        return object;
    }

    /**
     * Change the position of a displayed object.
     *
     * @param layer the display list layer where the object is displayed.
     * @param xCoord the x-coordinate where the object's origin will be moved.
     * @param yCoord the y-coordinate where the object's origin will be moved.
     * @return the Place3 object to change the position of the object.
     */
    public static Place3 move(final int layer, final int xCoord,
            final int yCoord) {
        final Place3 object = new Place3();
        object.setType(PlaceType.MODIFY);
        object.setLayer(layer);
        object.setTransform(CoordTransform.translate(xCoord, yCoord));
        return object;
    }

    /**
     * Replace an existing object with another.
     *
     * @param identifier the unique identifier of the new object.
     * @param layer the display list layer of the existing object.
     * @return the Place3 object to update the display list.
     */
    public static Place3 replace(final int identifier, final int layer) {
        final Place3 object = new Place3();
        object.setType(PlaceType.REPLACE);
        object.setLayer(layer);
        object.setIdentifier(identifier);
        return object;
    }

    /**
     * Replace an existing object with another.
     *
     * @param identifier the unique identifier of the new object.
     * @param layer the display list layer of the existing object.
     * @param xCoord the x-coordinate where the new object's origin will be.
     * @param yCoord the y-coordinate where the new object's origin will be.
     * @return the Place3 object to update the display list.
     */
    public static Place3 replace(final int identifier, final int layer,
            final int xCoord, final int yCoord) {
        final Place3 object = new Place3();
        object.setType(PlaceType.REPLACE);
        object.setLayer(layer);
        object.setIdentifier(identifier);
        object.setTransform(CoordTransform.translate(xCoord, yCoord));
        return object;
    }

    /** Format string used in toString() method. */
    private static final String FORMAT = "PlaceObject3: { type=%s; layer=%d;"
            + " bitmapCache=%d; identifier=%d; transform=%s;"
            + " colorTransform=%s; ratio=%d; clippingDepth=%d;"
            + " name=%s; className=%s;"
            + " filters=%s; blend=%s; clipEvents=%s}";

    /** How the display list will be updated. */
    private PlaceType type;
    /** The display list layer number. */
    private int layer;
    /** The actionscript 3 class that will render the object to be displayed. */
    private String className;
    /** Whether the displayed object will be cached as a bitmap. */
    private Integer bitmapCache;
    /** The unique identifier of the object that will be displayed. */
    private int identifier;
    /** The coordinate transform applied to the displayed object. */
    private CoordTransform transform;
    /** The color transform applied to the displayed object. */
    private ColorTransform colorTransform;
    /** The progression of the morphing process. */
    private Integer ratio;
    /** The name assigned to an object. */
    private String name;
    /** The number of layers to clip. */
    private Integer depth;
    /** The set of effects filters applied to the object. */
    private List<Filter> filters;
    /** How the object is blended with its background. */
    private Integer blend;
    /** The set of event handlers for movie clips. */
    private List<EventHandler> events;

    /** The length of the object, minus the header, when it is encoded. */
    private transient int length;
    /** Indicates whether the encoded object contains a blend. */
    private transient boolean hasBlend;
    /** Indicates whether the encoded object contains filters. */
    private transient boolean hasFilters;
    /** Indicates whether the encoded object contains an image. */
    private transient boolean hasImage;

    /**
     * Creates and initialises a Place3 object using values encoded
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
    @SuppressWarnings({"PMD.AssignmentInOperand", "PMD.ExcessiveMethodLength" })
    public Place3(final SWFDecoder coder, final Context context)
            throws IOException {
        context.put(Context.TRANSPARENT, 1);
        length = coder.readUnsignedShort() & Coder.LENGTH_FIELD;
        if (length == Coder.IS_EXTENDED) {
            length = coder.readInt();
        }
        coder.mark();
        int bits = coder.readByte();
        final boolean hasEvents = (bits & Coder.BIT7) != 0;
        final boolean hasDepth = (bits & Coder.BIT6) != 0;
        final boolean hasName = (bits & Coder.BIT5) != 0;
        final boolean hasRatio = (bits & Coder.BIT4) != 0;
        final boolean hasColorTransform = (bits & Coder.BIT3) != 0;
        final boolean hasTransform = (bits & Coder.BIT2) != 0;

        switch (bits & Coder.PAIR0) {
        case 0:
            type = PlaceType.MODIFY;
            break;
        case 1:
            type = PlaceType.MODIFY;
            break;
        case 2:
            type = PlaceType.NEW;
            break;
        default:
            type = PlaceType.REPLACE;
            break;
        }

        bits = coder.readByte();
        hasImage = (bits & Coder.BIT4) != 0;
        final boolean hasClassName = (bits & Coder.BIT3) != 0;
        final boolean hasBitmapCache = (bits & Coder.BIT2) != 0;
        hasBlend = (bits & Coder.BIT1) != 0;
        hasFilters = (bits & Coder.BIT0) != 0;

        layer = coder.readUnsignedShort();

        /* The following line implements the logic as described in the SWF 9
         * specification but it appears to be incorrect. The class name is not
         * given when hasImage is set.
         *
         * if (hasClassName || ((type == PlaceType.NEW
         * || type == PlaceType.REPLACE) && hasImage)) {
         */
        if (hasClassName) {
            className = coder.readString();
        }

        if ((type == PlaceType.NEW) || (type == PlaceType.REPLACE)) {
            identifier = coder.readUnsignedShort();
        }

        if (hasTransform) {
            transform = new CoordTransform(coder);
        }

        if (hasColorTransform) {
            colorTransform = new ColorTransform(coder, context);
        }

        if (hasRatio) {
            ratio = coder.readUnsignedShort();
        }

        if (hasName) {
            name = coder.readString();
        }

        if (hasDepth) {
            depth = coder.readUnsignedShort();
        }

        filters = new ArrayList<Filter>();

        if (hasFilters) {
            final SWFFactory<Filter> decoder = context.getRegistry()
                    .getFilterDecoder();

            final int count = coder.readByte();

            for (int i = 0; i < count; i++) {
                decoder.getObject(filters, coder, context);
            }
        }

        if (hasBlend) {
            blend = coder.readByte();
        }

        if (hasBitmapCache) {
            bitmapCache = coder.readByte();
        }

        events = new ArrayList<EventHandler>();

        if (hasEvents) {
            int event;

            coder.readUnsignedShort();
            coder.readInt();

            while ((event = coder.readInt()) != 0) {
                events.add(new EventHandler(event,
                        coder, context));
            }
        }
        context.remove(Context.TRANSPARENT);
        coder.check(length);
        coder.unmark();
    }

    /**
     * Creates an uninitialised Place3 object.
     */
    public Place3() {
        filters = new ArrayList<Filter>();
        events = new ArrayList<EventHandler>();
    }

    /**
     * Creates and initialises a Place3 object using the values copied
     * from another Place3 object.
     *
     * @param object
     *            a Place3 object from which the values will be
     *            copied.
     */
    public Place3(final Place3 object) {
        type = object.type;
        layer = object.layer;
        bitmapCache = object.bitmapCache;
        className = object.className;
        identifier = object.identifier;
        if (object.transform != null) {
            transform = object.transform;
        }
        if (object.colorTransform != null) {
            colorTransform = object.colorTransform;
        }
        ratio = object.ratio;
        depth = object.depth;
        name = object.name;

        filters = new ArrayList<Filter>(object.filters);
        blend = object.blend;
        events = new ArrayList<EventHandler>(object.events.size());

        for (final EventHandler event : object.events) {
            events.add(event.copy());
        }

    }

    /**
     * Get the type of place operation being performed, either adding a new
     * object, replacing an existing one with another or modifying an existing
     * object.
     *
     * @return the way the object will be placed.
     */
    public PlaceType getType() {
        return type;
    }

    /**
     * Sets the type of placement.
     *
     * @param aType
     *            the type of operation to be performed, either New, Modify or
     *            Replace.
     *
     * @return this object.
     */
    public Place3 setType(final PlaceType aType) {
        type = aType;
        return this;
    }

    /**
     * Get the Layer on which the object will be displayed in the display
     * list.
     *
     * @return the layer where the object will be displayed.
     */
    public int getLayer() {
        return layer;
    }

    /**
     * Sets the layer at which the object will be placed.
     *
     * @param aLayer
     *            the layer number on which the object is being displayed. Must
     *            be in the range 1..65535.
     *
     * @return this object.
     */
    public Place3 setLayer(final int aLayer) {
        if ((aLayer < 1) || (aLayer > Coder.USHORT_MAX)) {
            throw new IllegalArgumentRangeException(1,
                    Coder.USHORT_MAX, aLayer);
        }
        layer = aLayer;
        return this;
    }

    /**
     * Get the identifier of the object to be placed. This is only required
     * when placing an object for the first time. Subsequent references to the
     * object on this layer can simply use the layer number.
     *
     * @return the unique identifier of the object to be displayed.
     */
    public int getIdentifier() {
        return identifier;
    }

    /**
     * Sets the identifier of the object.
     *
     * @param uid
     *            the identifier of a new object to be displayed. Must be in the
     *            range 1..65535.
     *
     * @return this object.
     */
    public Place3 setIdentifier(final int uid) {
        if ((uid < 1) || (uid > Coder.USHORT_MAX)) {
            throw new IllegalArgumentRangeException(
                    1, Coder.USHORT_MAX, uid);
        }
        identifier = uid;
        return this;
    }

    /**
     * Get the coordinate transform. May be null if no coordinate transform
     * was defined.
     *
     * @return the coordinate transform that will be applied to the displayed
     * object.
     */
    public CoordTransform getTransform() {
        return transform;
    }

    /**
     * Sets the coordinate transform that defines the position where the object
     * will be displayed. The argument may be null if the location of the object
     * is not being changed.
     *
     * @param matrix
     *            an CoordTransform object that will be applied to the object
     *            displayed.
     *
     * @return this object.
     */
    public Place3 setTransform(final CoordTransform matrix) {
        transform = matrix;
        return this;
    }

    /**
     * Get the colour transform. May be null if no colour transform was
     * defined.
     *
     * @return the colour transform that will be applied to the displayed
     * object.
     */
    public ColorTransform getColorTransform() {
        return colorTransform;
    }

    /**
     * Sets the colour transform that defines the colour effects applied to the
     * object. The argument may be null if the color of the object is not being
     * changed.
     *
     * @param cxform
     *            an ColorTransform object that will be applied to the object
     *            displayed.
     *
     * @return this object.
     */
    public Place3 setColorTransform(final ColorTransform cxform) {
        colorTransform = cxform;
        return this;
    }

    /**
     * Get the morph ratio, in the range 0..65535 that defines the progress
     * in the morphing process performed by the Flash Player from the defined
     * start and end shapes. A value of 0 indicates the start of the process and
     * 65535 the end. Returns null if no ratio was specified.
     *
     * @return the morphing ratio.
     */
    public Integer getRatio() {
        return ratio;
    }

    /**
     * Sets point of the morphing process for a morph shape in the range
     * 0..65535. May be set to null if the shape being placed is not being
     * morphed.
     *
     * @param aNumber
     *            the progress in the morphing process.
     *
     * @return this object.
     */
    public Place3 setRatio(final Integer aNumber) {
        if ((aNumber != null) && ((aNumber < 0)
                || (aNumber > Coder.USHORT_MAX))) {
            throw new IllegalArgumentRangeException(
                    1, Coder.USHORT_MAX, aNumber);
        }
        ratio = aNumber;
        return this;
    }

    /**
     * Get the number of layers that will be clipped by the object placed on
     * the layer specified in this object.
     *
     * @return the number of layers to be clipped.
     */
    public Integer getDepth() {
        return depth;
    }

    /**
     * Sets the number of layers that this object will mask. May be set to zero
     * if the shape being placed does not define a clipping area.
     *
     * @param aNumber
     *            the number of layers clipped.
     *
     * @return this object.
     */
    public Place3 setDepth(final Integer aNumber) {
        if ((aNumber != null) && ((aNumber < 1)
                || (aNumber > Coder.USHORT_MAX))) {
             throw new IllegalArgumentRangeException(
                     1, Coder.USHORT_MAX, aNumber);
        }
        depth = aNumber;
        return this;
    }

    /**
     * Get the name of the object. May be null if a name was not assigned to
     * the object.
     *
     * @return the name of the object.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of an object to be displayed. If a shape is not being
     * assigned a name then setting the argument to null will omit the attribute
     * when the object is encoded.
     *
     * @param aString
     *            the name assigned to the object.
     *
     * @return this object.
     */
    public Place3 setName(final String aString) {
        name = aString;
        return this;
    }

    /**
     * Get the value indicating whether the display object will be cached as a
     * bitmap (non-zero) or not cached (zero).
     *
     * @return a non-zero value if the object is cached or zer oif not cached.
     */
    public Integer getBitmapCache() {
        return bitmapCache;
    }

    /**
     * Set whether the displayed object should be cached as a bitmap.
     *
     * @param cache set to a non-zero value if the object should be cached as
     * a bitmap or to zero to disable caching.
     *
     * @return this object.
     */
    public Place3 setBitmapCache(final Integer cache) {
        bitmapCache = cache;
        return this;
    }

    /**
     * Get the name of the Actionscript 3 class which will be used to render
     * the object to be displayed.
     *
     * @return the name of the Actionscript class.
     */
    public String getClassName() {
        return className;
    }

    /**
     * Set the name of the Actionscript 3 class which will be used to render
     * the object to be displayed.
     *
     * @param aName the name of the Actionscript class.
     * @return this object.
     */
    public Place3 setClassName(final String aName) {
        className = aName;
        return this;
    }

    /**
     * Get the list of filters that will be applied to the object when it is
     * displayed as a bitmap.
     * @return the list of bitmap filters.
     */
    public List<Filter> getFilters() {
        return filters;
    }

    /**
     * Set the list of filters that will be applied to the object when it is
     * displayed as a bitmap.
     * @param list the list of bitmap filters.
     */
    public void setFilters(final List<Filter> list) {
        if (list == null) {
            throw new IllegalArgumentException();
        }
        filters = list;
    }

    /**
     * Get the blend that describes how the object will be rendered in relation
     * to the background.
     * @return the Blend that describes how the object is composited.
     */
    public Blend getBlend() {
        return Blend.fromInt(blend);
    }

    /**
     * Set the blend that describes how the object will be rendered in relation
     * to the background.
     * @param mode the Blend that describes how the object is composited.
     * @return this object.
     */
    public Place3 setBlend(final Blend mode) {
        blend = mode.getValue();
        return this;
    }

    /**
     * Adds a clip event to the list of clip events.
     *
     * @param aClipEvent
     *            a clip event object.
     *
     * @return this object.
     */
    public Place3 add(final EventHandler aClipEvent) {
        if (aClipEvent == null) {
            throw new IllegalArgumentException();
        }
        events.add(aClipEvent);
        return this;
    }

    /**
     * Get the list of event handlers that define the actions that will
     * be executed in response to events that occur in the movie clip being
     * placed.
     *
     * @return the set of event handlers for the movie clip.
     */
    public List<EventHandler> getEvents() {
        return events;
    }

    /**
     * Set the list of Clip events. Clip Events are only valid for movie clips
     * and the argument should be set to null when placing other types of
     * object.
     *
     * If the object already contains a set of encoded clip event objects they
     * will be deleted.
     *
     * @param list
     *            a list of ClipEvent objects.
     */
    public void setEvents(final List<EventHandler> list) {
        if (list == null) {
            throw new IllegalArgumentException();
        }
        events = list;
    }

    /**
     * Adds a Filter to the list of filters.
     *
     * @param filter
     *            a Filter object.
     *
     * @return this object.
     */
    public Place3 add(final Filter filter) {
        if (filter == null) {
            throw new IllegalArgumentException();
        }
        filters.add(filter);
        return this;
    }

    /** {@inheritDoc} */
    public Place3 copy() {
        return new Place3(this);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, type, layer, bitmapCache, identifier,
                transform, colorTransform, ratio, depth, name, className,
                filters, blend, events);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("PMD.NPathComplexity")
    public int prepareToEncode(final Context context) {
        // CHECKSTYLE:OFF
        context.put(Context.TRANSPARENT, 1);

        hasBlend = blend != null;
        hasFilters = true ^ filters.isEmpty();

        length = 4;
        length += ((type == PlaceType.NEW) || (type == PlaceType.REPLACE)) ? 2
                : 0;
        length += transform == null ? 0 : transform.prepareToEncode(context);
        length += colorTransform == null ? 0 : colorTransform.prepareToEncode(
                context);
        length += ratio == null ? 0 : 2;
        length += depth == null ? 0 : 2;
        length += name == null ? 0 : context.strlen(name);
        length += className == null ? 0 : context.strlen(className);

        if (hasFilters) {
            length += 1;
            for (final Filter filter : filters) {
                length += filter.prepareToEncode(context);
            }
        }

        if (hasBlend) {
            length += 1;
        }

        if (bitmapCache != null) {
            length += 1;
        }

        if (!events.isEmpty()) {
            length += 6;

            for (final EventHandler handler : events) {
                length += handler.prepareToEncode(context);
            }

            length += 4;
        }

        context.remove(Context.TRANSPARENT);

        return (length > Coder.HEADER_LIMIT ? Coder.LONG_HEADER
                : Coder.SHORT_HEADER) + length;
        // CHECKSTYLE:ON
    }

    /** {@inheritDoc} */
    @SuppressWarnings({"PMD.NPathComplexity", "PMD.ExcessiveMethodLength" })
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        if (length > Coder.HEADER_LIMIT) {
            coder.writeShort((MovieTypes.PLACE_3
                    << Coder.LENGTH_FIELD_SIZE) | Coder.IS_EXTENDED);
            coder.writeInt(length);
        } else {
            coder.writeShort((MovieTypes.PLACE_3
                    << Coder.LENGTH_FIELD_SIZE) | length);
        }
        if (Constants.DEBUG) {
            coder.mark();
        }

        context.put(Context.TRANSPARENT, 1);
        int bits = 0;
        bits |= events.isEmpty() ? 0 : Coder.BIT7;
        bits |= depth == null ? 0 : Coder.BIT6;
        bits |= name == null ? 0 : Coder.BIT5;
        bits |= ratio == null ? 0 : Coder.BIT4;
        bits |= colorTransform == null ? 0 : Coder.BIT3;
        bits |= transform == null ? 0 : Coder.BIT2;

        switch (type) {
        case MODIFY:
            bits |= Coder.BIT0;
            break;
        case NEW:
            bits |= Coder.BIT1;
            break;
        default:
            bits |= Coder.BIT0;
            bits |= Coder.BIT1;
            break;
        }
        coder.writeByte(bits);

        bits = 0;
        bits |= hasImage ? Coder.BIT4 : 0;
        bits |= className == null ? 0 : Coder.BIT3;
        bits |= bitmapCache == null ? 0 : Coder.BIT2;
        bits |= hasBlend ? Coder.BIT1 : 0;
        bits |= hasFilters ? Coder.BIT0 : 0;
        coder.writeByte(bits);
        coder.writeShort(layer);

        if (className != null) {
            coder.writeString(className);
        }
        if ((type == PlaceType.NEW) || (type == PlaceType.REPLACE)) {
            coder.writeShort(identifier);
        }
        if (transform != null) {
            transform.encode(coder, context);
        }
        if (colorTransform != null) {
            colorTransform.encode(coder, context);
        }
        if (ratio != null) {
            coder.writeShort(ratio);
        }
        if (name != null) {
            coder.writeString(name);
        }

        if (depth != null) {
            coder.writeShort(depth);
        }

        if (hasFilters) {
            coder.writeByte(filters.size());
            for (Filter filter : filters) {
                filter.encode(coder, context);
            }
        }

        if (hasBlend) {
            coder.writeByte(blend);
        }

        if (bitmapCache != null) {
            coder.writeByte(bitmapCache);
        }

        if (!events.isEmpty()) {
            int eventMask = 0;

            coder.writeShort(0);

            for (final EventHandler handler : events) {
                eventMask |= handler.getEventCode();
            }

            coder.writeInt(eventMask);

            for (final EventHandler handler : events) {
                handler.encode(coder, context);
            }

            coder.writeInt(0);
        }
        context.remove(Context.TRANSPARENT);
        if (Constants.DEBUG) {
            coder.check(length);
            coder.unmark();
        }
    }
}
