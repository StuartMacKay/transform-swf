/*
 * ActionDecoder.java
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

package com.flagstone.transform.action;

import java.io.IOException;
import java.util.List;

import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFFactory;

/**
 * ActionDecoder decodes the actions in the Flash movie.
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
public class ActionDecoder implements SWFFactory<Action> {

    /** {@inheritDoc} */
    public void getObject(final List<Action> list, final SWFDecoder coder,
            final Context context) throws IOException {

        Action action;

        final int type = coder.readByte();

        if (type <= ActionTypes.HIGHEST_BYTE_CODE) {
            action = BasicAction.fromInt(type);
        } else {
            switch (type) {
            case ActionTypes.GET_URL:
                action = new GetUrl(coder);
                break;
            case ActionTypes.GOTO_FRAME:
                action = new GotoFrame(coder);
                break;
            case ActionTypes.GOTO_LABEL:
                action = new GotoLabel(coder);
                break;
            case ActionTypes.SET_TARGET:
                action = new SetTarget(coder);
                break;
            case ActionTypes.WAIT_FOR_FRAME:
                action = new WaitForFrame(coder);
                break;
            case ActionTypes.CALL:
                action = Call.getInstance();
                coder.readByte();
                coder.readByte();
                break;
            case ActionTypes.PUSH:
                action = new Push(coder, context);
                break;
            case ActionTypes.WAIT_FOR_FRAME_2:
                action = new WaitForFrame2(coder);
                break;
            case ActionTypes.JUMP:
                action = new Jump(coder);
                break;
            case ActionTypes.IF:
                action = new If(coder);
                break;
            case ActionTypes.GET_URL_2:
                action = new GetUrl2(coder);
                break;
            case ActionTypes.GOTO_FRAME_2:
                action = new GotoFrame2(coder);
                break;
            case ActionTypes.TABLE:
                action = new Table(coder);
                break;
            case ActionTypes.REGISTER_COPY:
                action = new RegisterCopy(coder);
                break;
            case ActionTypes.NEW_FUNCTION:
                action = new NewFunction(coder, context);
                break;
            case ActionTypes.WITH:
                action = new With(coder, context);
                break;
            case ActionTypes.EXCEPTION_HANDLER:
                action = new ExceptionHandler(coder, context);
                break;
            case ActionTypes.NEW_FUNCTION_2:
                action = new NewFunction2(coder, context);
                break;
            default:
                action = new ActionObject(type, coder);
                break;
            }
        }
        list.add(action);
    }

}
