/*
 * TabOrder.java
 * Transform
 * 
 * Copyright (c) 2001-2009 Flagstone Software Ltd. All rights reserved.
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

import com.flagstone.transform.coder.CoderException;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;

/**
 * The TabOrder class is used to set the tabbing order of text fields, movie
 * clips and buttons visible on the display list.
 * 
 * <p>
 * The objects are referenced by the number of the layer on which they displayed
 * rather than the unique identifier. This differs from the other classes in the
 * framework but it does allow objects creating at run-time by ActionScript
 * statements to be referenced.
 * </p>
 */
public final class TabOrder implements MovieTag {
	
	private static final String FORMAT = "TabOrder: { layer=%d; index=%d }";

	private int layer;
	private int index;

	//TODO(doc)
	public TabOrder(final SWFDecoder coder, final Context context) throws CoderException {

		if ((coder.readWord(2, false) & 0x3F) == 0x3F) {
			coder.readWord(4, false);
		}

		layer = coder.readWord(2, false);
		index = coder.readWord(2, false);
	}

	/**
	 * Construct a TabOrder object that set the tab order for the object on the
	 * display list at the specified layer.
	 * 
	 * @param layer
	 *            the layer number which contains the object assigned to the
	 *            tabbing order. Must be in the range 1..65535.
	 * @param index
	 *            the index of the object in the tabbing order. Must be in the
	 *            range 0..65535.
	 */
	public TabOrder(int layer, int index) {
		setLayer(layer);
		setIndex(index);
	}

	//TODO(doc)
	public TabOrder(TabOrder object) {
		layer = object.layer;
		index = object.index;
	}

	/**
	 * Returns the layer number which contains the object assigned to the
	 * tabbing order.
	 */
	public int getLayer() {
		return layer;
	}

	/**
	 * Sets the layer number which contains the object assigned to the tabbing
	 * order.
	 * 
	 * @param layer
	 *            the layer number. Must be in the range 1..65535.
	 */
	public void setLayer(int layer) {
		if (layer < 1 || layer > 65535) {
			throw new IllegalArgumentException(Strings.LAYER_OUT_OF_RANGE);
		}
		this.layer = layer;
	}

	/**
	 * Returns the index of the object in the tabbing order.
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Sets the index of the object in the tabbing order.
	 * 
	 * @param index
	 *            the index in the tabbing order. Must be in the range 0..65535.
	 */
	public void setIndex(int index) {
		if (index < 0 || index > 65535) {
			throw new IllegalArgumentException(
					Strings.UNSIGNED_VALUE_OUT_OF_RANGE);
		}
		this.index = index;
	}

	public TabOrder copy() {
		return new TabOrder(this);
	}

	@Override
	public String toString() {
		return String.format(FORMAT, layer, index);
	}

	public int prepareToEncode(final SWFEncoder coder, final Context context) {
		return 6;
	}

	public void encode(final SWFEncoder coder, final Context context) throws CoderException {
		coder.writeWord((MovieTypes.TAB_ORDER << 6) | 4, 2);
		coder.writeWord(layer, 2);
		coder.writeWord(index, 2);
	}
}