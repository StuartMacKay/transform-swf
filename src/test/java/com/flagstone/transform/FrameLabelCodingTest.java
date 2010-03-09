/*
 * FrameLabelCodingTest.java
 * Transform
 *
 * Copyright (c) 2010 Flagstone Software Ltd. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  * Neither the name of Flagstone Software Ltd. nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.flagstone.transform;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.yaml.snakeyaml.Yaml;

import com.flagstone.transform.coder.CoderException;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;

public final class FrameLabelCodingTest {

    private static final String RESOURCE = "com/flagstone/transform/FrameLabel.yaml";

    private static final String LABEL = "label";
    private static final String ANCHOR = "anchor";
    private static final String DATA = "data";

    @Parameters
    public static Collection<Object[]>  patterns() {

        ClassLoader loader = DoActionCodingTest.class.getClassLoader();
        InputStream other = loader.getResourceAsStream(RESOURCE);
        Yaml yaml = new Yaml();
        
        Collection<Object[]> list = new ArrayList<Object[]>();
         
        for (Object data : yaml.loadAll(other)) {
            list.add(new Object[] { data });
        }

        return list;
    }

    private transient final String label;
    private transient final boolean anchor;
    private transient final byte[] data;
    private transient final Context context;
    
    public FrameLabelCodingTest(Map<String,Object>values) {
        label = (String)values.get(LABEL);
        anchor = (Boolean)values.get(ANCHOR);
        data = (byte[])values.get(DATA);
        context = new Context();
    }

    @Test
    public void checkSizeMatchesEncodedSize() throws CoderException {     
        final FrameLabel object = new FrameLabel(label, anchor);       
        final SWFEncoder encoder = new SWFEncoder(data.length);        
         
        assertEquals(data.length, object.prepareToEncode(encoder, context));
    }

    @Test
    public void checkObjectIsEncoded() throws CoderException {
        final FrameLabel object = new FrameLabel(label, anchor);       
        final SWFEncoder encoder = new SWFEncoder(data.length);        
        
        object.prepareToEncode(encoder, context);
        object.encode(encoder, context);

        assertTrue(encoder.eof());
        assertArrayEquals(data, encoder.getData());
    }

    @Test
    public void checkObjectIsDecoded() throws CoderException {
        final SWFDecoder decoder = new SWFDecoder(data);
        final FrameLabel object = new FrameLabel(decoder);

        assertTrue(decoder.eof());
        assertEquals(anchor, object.isAnchor());
        assertEquals(label, object.getLabel());
    }
}
