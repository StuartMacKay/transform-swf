/*
 * Shape.java
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

package com.flagstone.transform.shape;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.Copyable;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncodeable;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.coder.SWFFactory;

/**
 * Shape is a container class for the shape objects (Line, Curve, ShapeStyle
 * and ShapeStyle2 objects) that describe how a particular shape is drawn.
 *
 * <p>
 * Shapes are used in shape and font definitions. The Shape class is used to
 * simplify the design of these classes and provides no added functionality
 * other than acting as a container class.
 * </p>
 */
public class Shape implements SWFEncodeable, Copyable<Shape> {

    /**
     * The minimum coordinate in along the x or y axes.
     */
    public static final int MIN_COORD = -65536;
    /**
     * The maximum coordinate in along the x or y axes.
     */
    public static final int MAX_COORD = 65535;

    /** Format string used in toString() method. */
    private static final String FORMAT = "Shape: { records=%s}";

    /**
     * Decode a ShapeData object into the set of ShapeRecord objects that
     * describe how a shape is drawn.
     * @param shapeData a ShapeData object containing the encoded shape.
     * @return the decoded Shape.
     * @throws IOException if there is an error decoding the shape.
     */
    public static Shape shapeFromData(final ShapeData shapeData)
                throws IOException {
        final byte[] data = shapeData.getData();
        final ByteArrayInputStream stream = new ByteArrayInputStream(data);
        final SWFDecoder coder = new SWFDecoder(stream);
        final Context context = new Context();
        return new Shape(coder, context);
    }

    /** List of ShapeRecords that draws the shape. */
    private List<ShapeRecord> objects;
    /** Indicates whether the ShapeRecords are already encoded. */
    private transient boolean isEncoded;

    /**
     * Creates and initialises a Shape object using values encoded in the Flash
     * binary format.
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
    @SuppressWarnings("PMD.AssignmentInOperand")
    public Shape(final SWFDecoder coder, final Context context)
            throws IOException {
        objects = new ArrayList<ShapeRecord>();

        final int sizes = coder.readByte();
        context.put(Context.FILL_SIZE, (sizes & Coder.NIB1)
                >> Coder.TO_LOWER_NIB);
        context.put(Context.LINE_SIZE, sizes & Coder.NIB0);

        final SWFFactory<ShapeRecord> decoder = context.getRegistry()
            .getShapeDecoder();

        while (coder.scanBits(6, false) != 0) {
            decoder.getObject(objects, coder, context);
        }
        coder.readBits(6, false);
        coder.alignToByte();
    }

    /**
     * Constructs an empty Shape.
     */
    public Shape() {
        objects = new ArrayList<ShapeRecord>();
    }

    /**
     * Creates a Shape object, specifying the Objects that describe how the
     * shape is drawn.
     *
     * @param list
     *            the list of shape records. Must not be null.
     */
    public Shape(final List<ShapeRecord> list) {
        setObjects(list);
    }

    /**
     * Creates and initialises a Shape object using the values copied from
     * another Shape object.
     *
     * @param object
     *            a Shape object from which the values will be copied.
     */
    public Shape(final Shape object) {
        objects = new ArrayList<ShapeRecord>(object.objects.size());

        for (final ShapeRecord record : object.objects) {
            objects.add(record.copy());
        }
    }

    /**
     * Adds the object to the list of shape records.
     *
     * @param anObject
     *            an instance of ShapeStyle, Line or Curve. Must not be null.
     * @return this object.
     */
    public Shape add(final ShapeRecord anObject) {
        if (anObject == null) {
            throw new IllegalArgumentException();
        }
        objects.add(anObject);
        return this;
    }

    /**
     * Get the list of shape records that define the shape.
     *
     * @return the list of shape records.
     */
    public List<ShapeRecord> getObjects() {
        return objects;
    }

    /**
     * Sets the list of shape records.
     *
     * @param list
     *            the list of shape records. Must not be null.
     */
    public void setObjects(final List<ShapeRecord> list) {
        if (list == null) {
            throw new IllegalArgumentException();
        }
        objects = list;
    }

    /** {@inheritDoc} */
    public Shape copy() {
        return new Shape(this);
    }

    @Override
    public String toString() {
        return String.format(FORMAT, objects);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {

        int length = 0;

        isEncoded = objects.size() == 1 && objects.get(0) instanceof ShapeData;

        if (isEncoded) {
            length += objects.get(0).prepareToEncode(context);
        } else {
            context.put(Context.SHAPE_SIZE, 0);

            // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 6 LINES
            int numberOfBits = 21; // Includes end of shape and align to byte

            for (final ShapeRecord record : objects) {
                numberOfBits += record.prepareToEncode(context);
            }
            length += (numberOfBits >>> 3);
        }
        return length;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {

        if (isEncoded) {
            objects.get(0).encode(coder, context);
        } else {
            int bits = context.get(Context.FILL_SIZE) << Coder.TO_UPPER_NIB;
            bits |= context.get(Context.LINE_SIZE);
            coder.writeByte(bits);

            for (final ShapeRecord record : objects) {
                record.encode(coder, context);
            }
            // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 1 LINES
            coder.writeBits(0, 6); // End of shape
            coder.alignToByte();
      }
    }
}
