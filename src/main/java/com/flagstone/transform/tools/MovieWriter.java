/*
 * MovieWriter.java
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

package com.flagstone.transform.tools;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import com.flagstone.transform.Movie;
import com.flagstone.transform.MovieTag;

/**
 * MovieWriter can be used to pretty print the output from the toString()
 * method of an object or even an entire Movie.
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
public class MovieWriter {
    /**
     * Pretty print an entire Movie and write it to a file.
     * @param movie the Movie to get the string representation of.
     * @param file the file where the formatted output will be written.
     * @throws IOException if there is an error writing to the file.
     */
    public void write(final Movie movie, final File file) throws IOException {
        final PrintWriter writer = new PrintWriter(file);
        write(movie, writer);
        writer.close();
    }
    /**
     * Pretty print an entire Movie.
     * @param movie the Movie to get the string representation of.
     * @param writer the Writer formatted output will be written.
     * @throws IOException if there is an error writing to the file.
     */
    public void write(final Movie movie, final Writer writer)
            throws IOException {
        for (final MovieTag tag : movie.getObjects()) {
            write(tag, writer);
        }
    }
    /**
     * Pretty print an object from a Movie.
     * @param obj the object to get the string representation of.
     * @param writer the Writer formatted output will be written.
     * @throws IOException if there is an error writing to the file.
     */
    @SuppressWarnings("PMD.CyclomaticComplexity")
    public void write(final Object obj, final Writer writer)
            throws IOException {

        int level = 0;
        boolean start = false;
        boolean coord = false;

        final String str = obj.toString();

        for (final char c : str.toCharArray()) {

            if (c == '{') {
                writer.append(c).append('\n');
                indent(writer, ++level);
                start = true;
            } else if (c == '}') {
                writer.append(';').append('\n');
                indent(writer, --level);
                writer.append(c);
            } else if (c == '[') {
                writer.append(c).append('\n');
                indent(writer, ++level);
            } else if (c == ']') {
                writer.append('\n');
                indent(writer, --level);
                writer.append(c);
            } else if (c == ';') {
                writer.append(c).append('\n');
                indent(writer, level);
                start = true;
            } else if (c == ',') {
                writer.append(c);
                if (!coord) {
                    writer.append('\n');
                    indent(writer, level);
                    start = true;
                }
            } else if (c == '<') {
                writer.append('[');
            } else if (c == '>') {
                writer.append(']');
            } else if (c == '(') {
                writer.append(c);
                coord = true;
            } else if (c == ')') {
                writer.append(c);
                coord = false;
            } else if (c == '=') {
                writer.append(' ').append('=').append(' ');
            } else if (c == ' ') {
                if (!start) {
                    writer.append(c);
                }
            } else {
                writer.append(c);
                start = false;
            }
        }
        writer.append(',').append('\n');
        writer.flush();
    }

    /**
     * Indent the text by adding tabs.
     * @param writer the Writer where the indents will be sent.
     * @param level the number of tabs to indent.
     * @throws IOException if an error occurs while writing.
     */
    private void indent(final Writer writer, final int level)
            throws IOException {
        for (int i = 0; i < level; i++) {
            writer.append('\t');
        }
    }
}
