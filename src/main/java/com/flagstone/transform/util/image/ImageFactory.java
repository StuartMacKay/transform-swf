/*
 * ImageFactory.java
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

package com.flagstone.transform.util.image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.DataFormatException;

import com.flagstone.transform.image.ImageTag;

/**
 * <p>
 * ImageFactory is used to generate an image definition object from an image
 * stored in a file, references by a URL or read from an stream. An plug-in
 * architecture allows decoders to be registered to handle different image
 * formats. The ImageFactory provides a standard interface for using the
 * decoders.
 * </p>
 *
 * <p>
 * Currently PNG, BMP and JPEG encoded images are supported by dedicated
 * decoders. The BufferedImageDecoder can be used to decode any format supported
 * using Java's ImageIO, including PNG, BMP and JPG format images. New decoders
 * can be added by implementing the ImageDecoder interface and registering them
 * in the ImageRegistry.
 * </p>
 *
 * <P>
 * The defineImage() methods return an Definition (the abstract base class for
 * all objects used to define shapes etc. in a Flash file. The exact class of
 * the object generated depends of the format of the image loaded.
 * </P>
 *
 * <table>
 * <tr>
 * <th>Class</th>
 * <th>Generated when...</th>
 * </tr>
 *
 * <tr>
 * <td valign="top">DefineJPEGImage2</td>
 * <td>A JPEG encoded image is loaded. The getFormat() method returns the class
 * constant JPEG.</td>
 * </tr>
 *
 * <tr>
 * <td valign="top">DefineImage</td>
 * <td>An indexed BMP or PNG image contains a colour table without transparent
 * colours or when a true colour image contains 16-bit or 24-bit colours is
 * loaded. The getFormat() method returns the class constants IDX8, RGB5 or
 * RGB8.</td>
 * </tr>
 *
 * <tr>
 * <td valign="top">DefineImage2</td>
 * <td>A BMP or PNG indexed image contains a colour table with transparent
 * colours is loaded or when a true colour image contains 32-bit bit colours.
 * The getFormat() method returns the class constants IDXA or RGBA.</td>
 * </tr>
 *
 * </table>
 *
 * <P>
 * Images are displayed in Flash by filling a shape with the image bitmap. The
 * defineEnclosingShape() method generates a rectangular shape object which
 * wraps the image:
 *
 * <pre>
 *     int imageId = movie.newIdentifier();
 *     int shapeId = movie.newIdentifier();
 *
 *     Definition image = Image(defineImage(imageId, ...);
 *
 *     int x = image.getWidth()/2;
 *     int y = image.getHeight()/2;
 *
 *     LineStyle style = new LineStyle(20, ColorTable.black());
 *
 *     movie.add(image);
 *     movie.add(Image.defineEnclosingShape(shapeId, image, x, y, style);
 * </pre>
 *
 * <P>
 * Here the origin, used when placing the shape on the screen, is defined as the
 * centre of the shape. Other points may be defined to suit the alignment of the
 * shape when it is placed on the display list.
 * </P>
 */
public final class ImageFactory {

    /** The object used to decode the image. */
    private transient ImageDecoder decoder;

    /**
     * Create an image definition for the image located in the specified file.
     *
     * @param file
     *            a file containing the abstract path to the image.
     *
     * @throws IOException
     *             if there is an error reading the file.
     *
     * @throws DataFormatException
     *             if there is a problem decoding the image, either it is in an
     *             unsupported format or an error occurred while decoding the
     *             image data.
     */
    public void read(final File file) throws IOException, DataFormatException {

        final ImageInfo info = new ImageInfo();
        info.setInput(new RandomAccessFile(file, "r"));
//        info.setDetermineImageNumber(true);

        if (!info.check()) {
            throw new DataFormatException("Unsupported format");
        }

        decoder = ImageRegistry.getImageProvider(
                info.getImageFormat().getMimeType());
        decoder.read(new FileInputStream(file));
    }

    /**
     * Create an image definition for the image referenced by a URL.
     *
     * @param url
     *            the Uniform Resource Locator referencing the file.
     *
     * @throws IOException
     *             if there is an error reading the file.
     *
     * @throws DataFormatException
     *             if there is a problem decoding the image, either it is in an
     *             unsupported format or an error occurred while decoding the
     *             image data.
     */
    public void read(final URL url) throws IOException, DataFormatException {

        final URLConnection connection = url.openConnection();
        final int fileSize = connection.getContentLength();

        if (fileSize < 0) {
            throw new FileNotFoundException(url.getFile());
        }

        final String mimeType = connection.getContentType();
        decoder = ImageRegistry.getImageProvider(mimeType);

        if (decoder == null) {
            throw new DataFormatException("Unsupported format");
        }

        decoder.read(url.openStream());
    }

    /**
     * Create an image definition for an image read from a stream.
     *
     * @param stream
     *            the InputStream containing the image data.
     *
     * @throws IOException
     *             if there is an error reading the stream.
     *
     * @throws DataFormatException
     *             if there is a problem decoding the image, either it is in an
     *             unsupported format or an error occurred while decoding the
     *             image data.
     */
    public void read(final InputStream stream)
            throws IOException, DataFormatException {
        decoder.read(stream);
    }
    
    /**
     * Create an image definition for an image read from a stream.
     *
     * @param stream
     *            the InputStream containing the image data.
     *
     * @throws IOException
     *             if there is an error reading the stream.
     *
     * @throws DataFormatException
     *             if there is a problem decoding the image, either it is in an
     *             unsupported format or an error occurred while decoding the
     *             image data.
     */
    public void read(final InputStream stream, String mimeType)
            throws IOException, DataFormatException {
        if(null == mimeType || "".equals(mimeType)){
            throw new DataFormatException("MIMEType Can't be null");
        }
        decoder = ImageRegistry.getImageProvider(mimeType);

        if (decoder == null) {
            throw new DataFormatException("Unsupported format");
        }
        decoder.read(stream);
    }

    /**
     * Create a definition for the image so it can be added to a Flash movie.
     * @param identifier the unique identifier for the image.
     * @return an ImageTag representing one of the image definitions supported
     * in Flash.
     */
    public ImageTag defineImage(final int identifier) {
        return decoder.defineImage(identifier);
    }

    /**
     * Get the ImageDecoder used to decode the image.
     *
     * @return the ImageDecoder instance that the factory created to decode the
     * image.
     */
    public ImageDecoder getDecoder() {
        return decoder;
    }
}
