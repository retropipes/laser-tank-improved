/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 See AUTHORS file
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.mini2Dx.minibus.handler;

import java.util.ArrayList;
import java.util.List;

import org.mini2Dx.minibus.MessageData;
import org.mini2Dx.minibus.MessageExchange;
import org.mini2Dx.minibus.MessageHandler;

/**
 * A {@link MessageHandler} that allows multiple {@link MessageHandler}s to be
 * chained and receive the same {@link MessageData}s
 */
public class MessageHandlerChain implements MessageHandler {
    private final List<MessageHandler> messageHandlers = new ArrayList<>(1);

    @Override
    public void onMessageReceived(final String messageType, final MessageExchange source,
	    final MessageExchange receiver, final MessageData messageData) {
	for (int i = this.messageHandlers.size() - 1; i >= 0; i--) {
	    this.messageHandlers.get(i).onMessageReceived(messageType, source, receiver, messageData);
	}
    }

    /**
     * Adds a {@link MessageHandler} to the chain
     *
     * @param messageHandler
     */
    public void add(final MessageHandler messageHandler) {
	this.messageHandlers.add(messageHandler);
    }

    /**
     * Removes a {@link MessageHandler} from the chain
     *
     * @param messageHandler
     */
    public void remove(final MessageHandler messageHandler) {
	this.messageHandlers.remove(messageHandler);
    }

    /**
     * Removes all {@link MessageHandler}s from the chain
     */
    public void clear() {
	this.messageHandlers.clear();
    }
}
