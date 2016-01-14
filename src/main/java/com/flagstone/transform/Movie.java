/*
 * Movie.java
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

package com.flagstone.transform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.Copyable;
import com.flagstone.transform.coder.DecoderRegistry;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.coder.SWFFactory;

/**
 * Movie is a container class for the objects that represents the data
 * structures in a Flash file.
 *
 * <p>
 * Movie is the core class of the Transform package. It is used to parse and
 * generate Flash files, translating the binary format of the Flash file into an
 * list objects that can be inspected and updated.
 * </p>
 *
 * <p>
 * A Movie object also contains the attributes that make up the header
 * information of the Flash file, identifying the version support, size of the
 * Flash Player screen, etc.
 * </p>
 *
 * <p>
 * Movie is also used to generate the unique identifiers that are used to
 * reference objects. Each call to newIdentifier() returns a unique number for
 * the current. The identifiers are generated using a simple counter. When a
 * movie is decoded this counter is updated each time an object definition is
 * decoded. This allows new objects to be added and ensures that the identifier
 * does not conflict with an existing object.
 * </p>
 */
public class Movie implements Copyable<Movie> {

    /** The version of Flash supported. */
    public static final int VERSION = 10;

    /** Length in bytes of the magic number used to identify the file type. */
    private static final int SIGNATURE_LENGTH = 3;
    /** Length in bytes of the signature and length fields. */
    private static final int HEADER_LENGTH = 8;
    /** Signature identifying Flash (SWF) files. */
    public static final byte[] FWS = new byte[] {0x46, 0x57, 0x53 };
    /** Signature identifying Compressed Flash (SWF) files. */
    public static final byte[] CWS = new byte[] {0x43, 0x57, 0x53 };

    /** Format string used in toString() method. */
    private static final String FORMAT = "Movie: { objects=%s}";
    /** The registry for the different types of decoder. */
    private transient DecoderRegistry registry;
    /** The character encoding used for strings. */
    private transient CharacterEncoding encoding;
    /** The list of objects that make up the movie. */
    private List<MovieTag> objects;

    /**
     * Creates a new Movie.
     */
    public Movie() {
        registry = DecoderRegistry.getDefault();
        encoding = CharacterEncoding.UTF8;
        objects = new ArrayList<MovieTag>();
    }

    /**
     * Creates a complete copy of this movie.
     *
     * @param movie the Movie to copy.
     */
    public Movie(final Movie movie) {
        if (movie.registry != null) {
            registry = movie.registry.copy();
        }
        encoding = movie.encoding;

        objects = new ArrayList<MovieTag>(movie.objects.size());

        for (final MovieTag tag : movie.objects) {
            objects.add(tag.copy());
        }
    }

    /**
     * Sets the registry containing the object used to decode the different
     * types of object found in a movie.
     *
     * @param decoderRegistry a central registry to decoders of different types
     * of object.
     */
    public void setRegistry(final DecoderRegistry decoderRegistry) {
        registry = decoderRegistry;
    }

    /**
     * Sets the encoding scheme for strings encoded and decoded from Flash
     * files.
     *
     * @param enc the character encoding used for strings.
     */
    public void setEncoding(final CharacterEncoding enc) {
        encoding = enc;
    }

    /**
     * Get the list of objects contained in the Movie.
     *
     * @return the list of objects that make up the movie.
     */
    public List<MovieTag> getObjects() {
        return objects;
    }

    /**
     * Sets the list of objects contained in the Movie.
     *
     * @param list
     *            the list of objects that describe a coder. Must not be null.
     */
    public void setObjects(final List<MovieTag> list) {
        if (list == null) {
            throw new IllegalArgumentException();
        }
        objects = list;
    }

    /**
     * Adds the object to the Movie.
     *
     * @param anObject
     *            the object to be added to the movie. Must not be null.
     * @return this object.
     */
    public Movie add(final MovieTag anObject) {
        if (anObject == null) {
            throw new IllegalArgumentException();
        }
        objects.add(anObject);
        return this;
    }

    /** {@inheritDoc} */
    @Override
	public Movie copy() {
        return new Movie(this);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, objects);
    }

    /**
     * Decodes the contents of the specified file.
     *
     * @param file
     *            the Flash file that will be parsed.
     * @throws DataFormatException
     *             - if the file does not contain Flash data.
     * @throws IOException
     *             - if an I/O error occurs while reading the file.
     */
    public void decodeFromFile(final File file) throws DataFormatException,
            IOException {
        decodeFromStream(new FileInputStream(file));
    }

    /**
     * Decodes a Flash file referenced by a URL.
     *
     * @param url
     *            the Uniform Resource Locator referencing the file.
     *
     * @throws IOException
     *             if there is an error reading the file.
     *
     * @throws DataFormatException
     *             if there is a problem decoding the font, either it is in an
     *             unsupported format or an error occurred while decoding the
     *             font data.
     */
    public void decodeFromUrl(final URL url) throws DataFormatException,
            IOException {
        final URLConnection connection = url.openConnection();
        if (connection.getContentLength() < 0) {
            throw new FileNotFoundException(url.getFile());
        }
        decodeFromStream(connection.getInputStream());
    }

    /**
     * Decodes the binary Flash data from an input stream. If an error occurs
     * while the data is being decoded an exception is thrown. The list of
     * objects in the Movie will contain the last tag successfully decoded.
     *
     * @param stream
     *            an InputStream from which the objects will be decoded.
     *
     * @throws DataFormatException
     *             if the file does not contain Flash data.
     * @throws IOException
     *             if an I/O error occurs while reading the file.
     */
    public void decodeFromStream(final InputStream stream)
            throws DataFormatException, IOException {

        InputStream streamIn = null;

        try {
            final Context context = new Context();
            context.setRegistry(registry);
            context.setEncoding(encoding.getEncoding());

            final byte[] signature = new byte[SIGNATURE_LENGTH];
            if (stream.read(signature) != signature.length) {
                throw new DataFormatException("Could not read file signature");
            }

            if (Arrays.equals(CWS, signature)) {
                streamIn = new InflaterInputStream(stream);
                context.put(Context.COMPRESSED, 1);
            } else if (Arrays.equals(FWS, signature)) {
                streamIn = stream;
                context.put(Context.COMPRESSED, 0);
            } else {
                throw new DataFormatException();
            }

            context.put(Context.VERSION, stream.read());

            int length = stream.read();
            length |= stream.read() << Coder.ALIGN_BYTE1;
            length |= stream.read() << Coder.ALIGN_BYTE2;
            length |= stream.read() << Coder.ALIGN_BYTE3;

            /*
             * If the file is shorter than the default buffer size then set the
             * buffer size to be the file size - this gets around a bug in Java
             * where the end of ZLIB streams are not detected correctly.
             */
            SWFDecoder decoder;

            if (length < SWFDecoder.BUFFER_SIZE) {
                decoder = new SWFDecoder(streamIn, length - HEADER_LENGTH);
            } else {
                decoder = new SWFDecoder(streamIn);
            }

            decoder.setEncoding(encoding);

            objects.clear();

            final SWFFactory<MovieTag> factory = registry.getMovieDecoder();

            final MovieHeader header = new MovieHeader(decoder, context);
            objects.add(header);

            while (decoder.scanUnsignedShort() >>> Coder.LENGTH_FIELD_SIZE
                    != MovieTypes.END) {
                factory.getObject(objects, decoder, context);
            }

            decoder.readUnsignedShort();

            header.setVersion(context.get(Context.VERSION));
            header.setCompressed(context.get(Context.COMPRESSED) == 1);

        } finally {
            if (streamIn != null) {
                streamIn.close();
            }
        }
    }

    /**
     * Encodes the list of objects and writes the data to the specified file.
     * If an error occurs while encoding the file then an exception is thrown.
     *
     * @param file
     *            the Flash file that the movie will be encoded to.
     *
     * @throws IOException
     *             - if an I/O error occurs while writing the file.
     * @throws DataFormatException
     *             if an error occurs when compressing the flash file.
     */
    public void encodeToFile(final File file) throws IOException,
            DataFormatException {
        encodeToStream(new FileOutputStream(file));
    }

    /**
     * Returns the encoded representation of the list of objects that this
     * Movie contains. If an error occurs while encoding the file then an
     * exception is thrown.
     *
     * @param stream
     *            the output stream that the video will be encoded to.
     * @throws IOException
     *             - if an I/O error occurs while encoding the file.
     * @throws DataFormatException
     *             if an error occurs when compressing the flash file.
     */
    public void encodeToStream(final OutputStream stream)
            throws DataFormatException, IOException {

        OutputStream streamOut = null;

        try {
            final MovieHeader header = (MovieHeader) objects.get(0);

            final Context context = new Context();
            context.setEncoding(encoding.getEncoding());
            context.put(Context.VERSION, header.getVersion());

            // length of signature, version, length and end
            // CHECKSTYLE IGNORE MagicNumberCheck FOR NEXT 1 LINES
            int length = 10;
            int frameCount = 0;

            for (final MovieTag tag : objects) {
                length += tag.prepareToEncode(context);

                if (tag instanceof ShowFrame) {
                    frameCount++;
                }
            }

            header.setFrameCount(frameCount);

            if (header.isCompressed()) {
                stream.write(CWS);
            } else {
                stream.write(FWS);
            }

            stream.write(header.getVersion());
            stream.write(length);
            stream.write(length >>> Coder.ALIGN_BYTE1);
            stream.write(length >>> Coder.ALIGN_BYTE2);
            stream.write(length >>> Coder.ALIGN_BYTE3);

            if (header.isCompressed()) {
                streamOut = new DeflaterOutputStream(stream);
            } else {
                streamOut = stream;
            }

            final SWFEncoder coder = new SWFEncoder(streamOut);
            coder.setEncoding(encoding);

            for (final MovieTag tag : objects) {
                tag.encode(coder, context);
            }
            coder.writeShort(0);
            coder.flush();
        } finally {
            if (streamOut != null) {
                streamOut.close();
            }
        }
    }
}
