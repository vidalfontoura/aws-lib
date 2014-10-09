package com.charter.aesd.aws.sqsclient;

import com.google.common.base.Optional;

import java.io.IOException;
import java.util.List;

/**
 * <p/>
 *
 * Provide an abstraction to a message queue.  Right now Amazon SQS is
 *   the focus, but the interface can easily be utilized as the description
 *   of most message queues.
 *
 * <p/>
 * User: matthewsmith Date: 7/10/14 Time: 10:23 AM
 *
 * @author $Author: $
 * @version $Rev: $
 * @since ${date}
 */
public interface ISQSClient
  extends ISNSTopicListener {

    /**
     * @param queueName {@code String} the name used by the Queue creation
     *                                 that resolves to the Queue instance in
     *                                 the Service Provider space.
     *
     * @return (@code Boolean} true - If there is an existing Queue with the specified name
     *                         false - No Queue by that name exists in the Service
     *                                 Provider Space
     */
    boolean isQueueExists(String queueName);

    /**
     * Resolve the URL to use for an existing Queue
     *
     * @param queueName {@code String} the name of the Queue to lookup.
     *                                 Should follow Service Provider naming conventions
     *
     * @return {@code String} the URL to use to reference the Queue in
     *                        subsequent calls
     *
     * @throws IOException
     */
    String resolveQueueUrl(String queueName);

    /**
     * Create a new Message Queue in the service provider space.
     *
     * @param queueName {@code String} the name to assign to the
     *                                 created Queue.  Should follow
     *                                 Service Provider naming conventions
     *
     * @return {@code String} the URL to use to reference the new Queue in
     *                        subsequent calls
     *
     * @throws IOException
     */
    String createQueue(String queueName) throws IOException;

    /**
     * @param queueUrl {@code String} the url returned by the Queue creation
     *                                that resolves to the Queue instance in
     *                                the Service Provider space.
     *
     * @throws IOException
     */
    void deleteQueue(String queueUrl)throws IOException;

    /**
     * @param queueUrl {@code String} the url returned by the Queue creation
     *                                that resolves to the Queue instance in
     *                                the Service Provider space.
     *
     * @return (@code Boolean} true - If there are messages waiting on the Queue
     *                         false - No messages have been sent to the Queue that
     *                                 are waiting to be processed
     */
    boolean hasPendingMessages(String queueUrl);

    /**
     * @param queueUrl {@code String} the url returned by the Queue creation
     *                                that resolves to the Queue instance in
     *                                the Service Provider space.
     *
     * @return (@code int} the current Queue depth
     */
    int getPendingMessageCount(String queueUrl);

    /**
     * @param queueUrl {@code String} the url returned by the Queue creation
     *                                that resolves to the Queue instance in
     *                                the Service Provider space.
     * @param content {@code String} The content to be included as the body in
     *                               the message sent to the Queue.  The content
     *                               may be enveloped by the Service Provider, but
     *                               the content returned via a receiveMessage
     *                               call should match this explicitly.
     *
     * @throws IOException
     */
    void sendMessage(String queueUrl,
                     String content) throws IOException;

    /**
     * @param queueUrl {@code String} the url returned by the Queue creation
     *                                that resolves to the Queue instance in
     *                                the Service Provider space.
     *
     * @return {@code String} The content that was submitted to the Queue via a
     *                        sendMessage call.  This content is the user space content
     *                        and includes nothing from the Service Provider envelope.
     *                        This value should match what was submitted exactly.  This call
     *                        returns the next message on the Queue.  NOTE:  order is
     *                        NOT implied.  It is up to Service Provider implementation
     *                        whether the Message Queue implementation is actually a
     *                        FIFO.  This method returns at most the content of 1 message.
     *
     * @throws IOException
     */
    Optional<String> receiveMessage(String queueUrl) throws IOException;

    /**
     * @param queueUrl {@code String} the url returned by the Queue creation
     *                                that resolves to the Queue instance in
     *                                the Service Provider space.
     *
     * @return {@code String} The content(s) that were submitted to the Queue via
     *                        sendMessage calls.  This content is the user space content
     *                        and includes nothing from the Service Provider envelope.
     *                        This value should match what was submitted exactly.  This call
     *                        empties the Queue.  NOTE:  order is NOT implied.  It is up
     *                        to Service Provider implementation whether the Message Queue
     *                        implementation is actually a FIFO.  This method returns all
     *                        of the messages on the Queue at the time of the call.
     *
     * @throws IOException
     */
    List<String> receiveMessages(String queueUrl) throws IOException;
} // ISQSClient