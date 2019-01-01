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
package org.mini2Dx.minibus;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.mini2Dx.minibus.exchange.ConcurrentMessageExchange;
import org.mini2Dx.minibus.exchange.ImmediateMessageExchange;
import org.mini2Dx.minibus.exchange.IntervalMessageExchange;
import org.mini2Dx.minibus.exchange.OnUpdateMessageExchange;
import org.mini2Dx.minibus.exchange.query.QueryMessageExchange;
import org.mini2Dx.minibus.exchange.query.QueryMessageExchangePool;
import org.mini2Dx.minibus.transmission.MessageTransmission;
import org.mini2Dx.minibus.transmission.MessageTransmissionPool;

/**
 * A message bus to publishing {@link MessageData}s
 */
public class MessageBus {
    final List<MessageExchange> exchangers = new CopyOnWriteArrayList<>();
    final MessageTransmissionPool transmissionPool = new MessageTransmissionPool();
    private final MessageExchange anonymousExchange;
    private final QueryMessageExchangePool queryMessageExchangePool;

    /**
     * Constructor
     */
    public MessageBus() {
	this.anonymousExchange = new AnonymousMessageExchange(this);
	this.queryMessageExchangePool = new QueryMessageExchangePool(this, this.exchangers);
    }

    /**
     * Updates all {@link MessageExchange}s
     *
     * @param delta (in seconds) The timestep or amount of time that has elapsed
     *              since the last frame
     */
    public void update(final float delta) {
	for (final MessageExchange exchanger : this.exchangers) {
	    exchanger.update(delta);
	}
	this.anonymousExchange.flush();
    }

    /**
     * Creates a {@link ImmediateMessageExchange} that processes messages
     * immediately when they are received
     *
     * @param messageHandlers The {@link MessageHandler} instances for processing
     *                        messages received by the {@link MessageExchange}
     * @return A new {@link ImmediateMessageExchange}
     */
    public MessageExchange createImmediateExchange(final MessageHandler... messageHandlers) {
	final ImmediateMessageExchange result = new ImmediateMessageExchange(this, messageHandlers);
	this.exchangers.add(result);
	return result;
    }

    /**
     * Creates a {@link IntervalMessageExchange} that processes messages after a
     * certain amount of time has elapsed.
     *
     * @param interval        The interval between processing {@link MessageData}s
     *                        (in seconds)
     * @param messageHandlers The {@link MessageHandler} instances for processing
     *                        messages received by the {@link MessageExchange}
     * @return A new {@link IntervalMessageExchange}
     */
    public MessageExchange createIntervalExchange(final float interval, final MessageHandler messageHandlers) {
	final IntervalMessageExchange result = new IntervalMessageExchange(interval, this, messageHandlers);
	this.exchangers.add(result);
	return result;
    }

    /**
     * Creates a {@link OnUpdateMessageExchange} that processes messages when
     * {@link MessageBus#update(float)} is called
     *
     * @param messageHandlers The {@link MessageHandler} instances for processing
     *                        messages received by the {@link MessageExchange}
     * @return A new {@link OnUpdateMessageExchange}
     */
    public MessageExchange createOnUpdateExchange(final MessageHandler... messageHandlers) {
	final OnUpdateMessageExchange result = new OnUpdateMessageExchange(this, messageHandlers);
	this.exchangers.add(result);
	return result;
    }

    /**
     * Creates a {@link ConcurrentMessageExchange} that processes messages on its
     * own {@link Thread}. The exchanger/thread can be stopped by calling
     * {@link MessageExchange#dispose()}
     *
     * @param messageHandlers The {@link MessageHandler} instances for processing
     *                        messages received by the {@link MessageExchange}
     * @return A new {@link ConcurrentMessageExchange} running on its own thread
     */
    public MessageExchange createConcurrentExchange(final MessageHandler... messageHandlers) {
	final ConcurrentMessageExchange result = new ConcurrentMessageExchange(this, messageHandlers);
	this.exchangers.add(result);
	return result;
    }

    /**
     * Broadcasts a message to all {@link MessageExchange}s from an anonymous source
     *
     * @param messageType The message type
     */
    public void broadcast(final String messageType) {
	this.broadcast(this.anonymousExchange, messageType, null);
    }

    /**
     * Broadcasts a message with {@link MessageData} to all {@link MessageExchange}s
     * from an anonymous source
     *
     * @param messageType The message type
     * @param messageData The {@link MessageData} to be published
     */
    public void broadcast(final String messageType, final MessageData messageData) {
	this.broadcast(this.anonymousExchange, messageType, messageData);
    }

    /**
     * Broadcasts a message to all {@link MessageExchange}s from a specified
     * {@link MessageExchange}
     *
     * @param source      The {@link MessageExchange} to broadcast the
     *                    {@link MessageData} from
     * @param messageType The message type
     */
    public void broadcast(final MessageExchange source, final String messageType) {
	this.broadcast(source, messageType, null);
    }

    /**
     * Broadcasts a message with {@link MessageData} to all {@link MessageExchange}s
     * from a specified {@link MessageExchange}
     *
     * @param source      The {@link MessageExchange} to broadcast the
     *                    {@link MessageData} from
     * @param messageType The message type
     * @param messageData The {@link MessageData} to broadcast
     */
    public void broadcast(final MessageExchange source, final String messageType, final MessageData messageData) {
	if (this.exchangers.size() == 0) {
	    return;
	}
	final MessageTransmission messageTransmission = this.transmissionPool.allocate();
	messageTransmission.setMessageType(messageType);
	messageTransmission.setMessageData(messageData);
	messageTransmission.setSource(source);
	messageTransmission.setBroadcastMessage(true);
	for (int i = this.exchangers.size() - 1; i >= 0; i--) {
	    final MessageExchange exchange = this.exchangers.get(i);
	    if (exchange.getId() == source.getId()) {
		continue;
	    }
	    messageTransmission.allocate();
	    exchange.queue(messageTransmission);
	}
    }

    /**
     * Sends a message from one {@link MessageExchange} to another
     *
     * @param source      The {@link MessageExchange} the {@link MessageData} is
     *                    sent from
     * @param destination The {@link MessageExchange} the {@link MessageData} is
     *                    sent to
     * @param messageType The message type
     */
    public void send(final MessageExchange source, final MessageExchange destination, final String messageType) {
	this.send(source, destination, messageType, null);
    }

    /**
     * Sends a message with {@link MessageData} from one {@link MessageExchange} to
     * another
     *
     * @param source      The {@link MessageExchange} the {@link MessageData} is
     *                    sent from
     * @param destination The {@link MessageExchange} the {@link MessageData} is
     *                    sent to
     * @param messageType The message type
     * @param messageData The {@link MessageData} that is sent
     */
    public void send(final MessageExchange source, final MessageExchange destination, final String messageType,
	    final MessageData messageData) {
	if (source == null) {
	    throw new RuntimeException("source cannot be null, use sendTo() instead");
	}
	final MessageTransmission messageTransmission = this.transmissionPool.allocate();
	messageTransmission.allocate();
	messageTransmission.setMessageType(messageType);
	messageTransmission.setMessageData(messageData);
	messageTransmission.setSource(source);
	messageTransmission.setBroadcastMessage(false);
	destination.queue(messageTransmission);
    }

    /**
     * Sends a message to a {@link MessageExchange} from an anonymous source
     *
     * @param destination The {@link MessageExchange} the {@link MessageData} is
     *                    sent to
     * @param messageType The message type
     */
    public void sendTo(final MessageExchange destination, final String messageType) {
	this.sendTo(destination, messageType, null);
    }

    /**
     * Sends a message with {@link MessageData} to a {@link MessageExchange} from an
     * anonymous source
     *
     * @param destination The {@link MessageExchange} the {@link MessageData} is
     *                    sent to
     * @param messageType The message type
     * @param messageData The {@link MessageData} that is sent
     */
    public void sendTo(final MessageExchange destination, final String messageType, final MessageData messageData) {
	final MessageTransmission messageTransmission = this.transmissionPool.allocate();
	messageTransmission.allocate();
	messageTransmission.setMessageType(messageType);
	messageTransmission.setMessageData(messageData);
	messageTransmission.setSource(this.anonymousExchange);
	messageTransmission.setBroadcastMessage(false);
	destination.queue(messageTransmission);
    }

    /**
     * Broadcasts a message and calls a {@link MessageHandler} when a response
     * message is received.
     *
     * @param messageType         The message type to send
     * @param responseMessageType The required message type of the response
     * @param queryHandler        The {@link MessageHandler} to call when the
     *                            response is received
     */
    public void broadcastQuery(final String messageType, final String responseMessageType,
	    final MessageHandler queryHandler) {
	this.broadcastQuery(messageType, null, responseMessageType, false, queryHandler);
    }

    /**
     * Broadcasts a message and calls a {@link MessageHandler} when a response
     * message is received.
     *
     * @param messageType            The message type to send
     * @param responseMessageType    The required message type of the response
     * @param requiresDirectResponse True if a direct response is required. A direct
     *                               response is a message sent explicitly from a
     *                               {@link MessageExchange} to the
     *                               {@link MessageExchange} used to send the query.
     * @param queryHandler           The {@link MessageHandler} to call when the
     *                               response is received
     */
    public void broadcastQuery(final String messageType, final String responseMessageType,
	    final boolean requiresDirectResponse, final MessageHandler queryHandler) {
	this.broadcastQuery(messageType, null, responseMessageType, requiresDirectResponse, queryHandler);
    }

    /**
     * Broadcasts a message and calls a {@link MessageHandler} when a response
     * message is received.
     *
     * @param messageType         The message type to send
     * @param messageData         The {@link MessageData} to send
     * @param responseMessageType The required message type of the response
     * @param queryHandler        The {@link MessageHandler} to call when the
     *                            response is received
     */
    public void broadcastQuery(final String messageType, final MessageData messageData,
	    final String responseMessageType, final MessageHandler queryHandler) {
	this.broadcastQuery(messageType, messageData, responseMessageType, false, queryHandler);
    }

    /**
     * Broadcasts a message and calls a {@link MessageHandler} when a response
     * message is received.
     *
     * @param messageType            The message type to send
     * @param messageData            The {@link MessageData} to send
     * @param responseMessageType    The required message type of the response
     * @param requiresDirectResponse True if a direct response is required. A direct
     *                               response is a message sent explicitly from a
     *                               {@link MessageExchange} to the
     *                               {@link MessageExchange} used to send the query.
     * @param queryHandler           The {@link MessageHandler} to call when the
     *                               response is received
     */
    public void broadcastQuery(final String messageType, final MessageData messageData,
	    final String responseMessageType, final boolean requiresDirectResponse, final MessageHandler queryHandler) {
	final QueryMessageExchange queryMessageExchange = this.queryMessageExchangePool.allocate(queryHandler,
		responseMessageType, requiresDirectResponse);
	this.exchangers.add(queryMessageExchange);
	this.broadcast(queryMessageExchange, messageType, messageData);
    }

    void dispose(final MessageExchange messageExchange) {
	this.exchangers.remove(messageExchange);
    }

    /**
     * Returns the amount of {@link QueryMessageExchange} instances available
     *
     * @return 0 if no queries have ever completed
     */
    public int getQueryMessagePoolSize() {
	return this.queryMessageExchangePool.getSize();
    }

    /**
     * Returns the id of the {@link MessageExchange} used for anonymous sending
     *
     * @return
     */
    public int getAnonymousExchangeId() {
	return this.anonymousExchange.getId();
    }

    /**
     * Returns the total amount of active {@link MessageExchange}s (including
     * {@link QueryMessageExchange}s
     *
     * @return
     */
    public int getTotalActiveExchanges() {
	return this.exchangers.size();
    }

    /**
     * An internal {@link MessageExchange} for anonymous message sending
     */
    private class AnonymousMessageExchange extends MessageExchange {
	public AnonymousMessageExchange(final MessageBus messageBus) {
	    super(messageBus, new MessageHandler() {
		@Override
		public void onMessageReceived(final String messageType, final MessageExchange source,
			final MessageExchange receiver, final MessageData messageData) {
		}
	    });
	}

	@Override
	public void update(final float delta) {
	}

	@Override
	public boolean isImmediate() {
	    return true;
	}

	@Override
	public boolean isAnonymous() {
	    return true;
	}
    }
}
