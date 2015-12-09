package com.charter.aesd.aws.sqsclient;

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchResult;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.google.common.base.Optional;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * <p/>
 *
 * Provide an abstraction to a message queue. Right now Amazon SQS is the focus,
 * but the interface can easily be utilized as the description of most message
 * queues.
 *
 * <p/>
 * User: matthewsmith Date: 7/10/14 Time: 10:23 AM
 *
 * @author $Author: $
 * @version $Rev: $
 * @since ${date}
 */
public interface ISQSClient extends ISNSTopicListener {

    /**
     * @param queueName {@code String} the name used by the Queue creation that
     *        resolves to the Queue instance in the Service Provider space.
     *
     * @return (@code Boolean} true - If there is an existing Queue with the
     *         specified name false - No Queue by that name exists in the
     *         Service Provider Space
     */
    boolean isQueueExists(String queueName);

    /**
     * Resolve the URL to use for an existing Queue
     *
     * @param queueName {@code String} the name of the Queue to lookup. Should
     *        follow Service Provider naming conventions
     *
     * @return {@code String} the URL to use to reference the Queue in
     *         subsequent calls
     *
     * @throws IOException
     */
    String resolveQueueUrl(String queueName);

    /**
     * Create a new Message Queue in the service provider space.
     *
     * @param queueName {@code String} the name to assign to the created Queue.
     *        Should follow Service Provider naming conventions
     *
     * @return {@code String} the URL to use to reference the new Queue in
     *         subsequent calls
     *
     * @throws IOException
     */
    String createQueue(String queueName) throws IOException;

    /**
     * @param queueUrl {@code String} the url returned by the Queue creation
     *        that resolves to the Queue instance in the Service Provider space.
     *
     * @throws IOException
     */
    void deleteQueue(String queueUrl) throws IOException;

    /**
     * @param queueUrl {@code String} the url returned by the Queue creation
     *        that resolves to the Queue instance in the Service Provider space.
     *
     * @return (@code Boolean} true - If there are messages waiting on the Queue
     *         false - No messages have been sent to the Queue that are waiting
     *         to be processed
     */
    boolean hasPendingMessages(String queueUrl);

    /**
     * @param queueUrl {@code String} the url returned by the Queue creation
     *        that resolves to the Queue instance in the Service Provider space.
     *
     * @return (@code int} the current Queue depth
     */
    int getPendingMessageCount(String queueUrl);

    /**
     * @param queueUrl {@code String} the url returned by the Queue creation
     *        that resolves to the Queue instance in the Service Provider space.
     * @param content {@code String} The content to be included as the body in
     *        the message sent to the Queue. The content may be enveloped by the
     *        Service Provider, but the content returned via a receiveMessage
     *        call should match this explicitly.
     *
     * @throws IOException
     */
    SendMessageResult sendMessage(String queueUrl, String content) throws IOException;

    SendMessageBatchResult sendMessages(String queueUrl, List<String> content);

    /**
     * @param queueUrl {@code String} the url returned by the Queue creation
     *        that resolves to the Queue instance in the Service Provider space.
     *
     * @return {@code Message} The message that was submitted to the Queue via a
     *         sendMessage call. This call returns the next message on the
     *         Queue. NOTE: order is NOT implied. It is up to Service Provider
     *         implementation whether the Message Queue implementation is
     *         actually a FIFO. This method returns at most the content of 1
     *         message.
     *
     * @throws IOException
     */
    Optional<Message> receiveMessage(String queueUrl) throws IOException;

    /**
     * @param request {@code ReceiveMessageRequest} the container for the
     *        parameters to the ReceiveMessage operation.
     *
     * @return {@code Message} The messages that were submitted to the Queue via
     *         sendMessage calls. This call empties the Queue. NOTE: order is
     *         NOT implied. It is up to Service Provider implementation whether
     *         the Message Queue implementation is actually a FIFO. This method
     *         returns all of the messages on the Queue according to parameters
     *         for the operation.
     *
     * @throws IOException
     */
    List<Message> receiveMessage(ReceiveMessageRequest request) throws IOException;

    /**
     * @param queueUrl {@code String} the url returned by the Queue creation
     *        that resolves to the Queue instance in the Service Provider space.
     *
     * @return {@code Message} The messages that were submitted to the Queue via
     *         sendMessage calls. This call empties the Queue. NOTE: order is
     *         NOT implied. It is up to Service Provider implementation whether
     *         the Message Queue implementation is actually a FIFO. This method
     *         returns all of the messages on the Queue at the time of the call.
     *
     * @throws IOException
     */
    List<Message> receiveMessages(String queueUrl) throws IOException;

    /**
     * @param queueUrl {@code String} the url returned by the Queue creation
     *        that resolves to the Queue instance in the Service Provider space.
     *
     * @param receiptHandle {@code String} the identifier associated with the
     *        act of receiving the message.
     *
     */
    void deleteMessage(final String queueUrl, final String receiptHandle);

    /**
     * @param queueUrl {@code String} the url returned by the Queue creation
     *        that resolves to the Queue instance in the Service Provider space.
     *
     * @param content {@code Map<String, String>} the identifiers composed by Id
     *        and receipt Handle associated with the act of receiving the
     *        messages.
     *
     */
    void deleteMessages(final String queueUrl, final Map<String, String> content);

} // ISQSClient
