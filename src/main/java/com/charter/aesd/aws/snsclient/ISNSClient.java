package com.charter.aesd.aws.snsclient;

import java.io.IOException;
import java.util.List;

/**
 * <p/>
 *
 * Provide an abstraction to a notification topic.  Right now Amazon SNS is
 *   the focus, but the interface can easily be utilized as the description
 *   of most topics.
 *
 * <p/>
 * User: matthewsmith Date: 7/22/14 Time: 12:15 PM
 *
 * @author $Author: $
 * @version $Rev: $
 * @since ${date}
 */
public interface ISNSClient {

    /**
     * Create a new Topic in the service provider space.
     *
     * @param topicName {@code String} the name to assign to the
     *                                 created Topic.  Should follow
     *                                 Service Provider naming conventions
     *
     * @return {@code String} the ARN to use to reference the new Topic in
     *                        subsequent calls
     *
     * @throws java.io.IOException
     */
    String createTopic(String topicName) throws IOException;

    /**
     * Connects to a Topic in the service provider space.
     *
     * @param topicName {@code String} the name of the Topic to connect to.
     *                                 Should follow Service Provider naming
     *                                 conventions
     *
     * @return {@code String} the ARN to use to reference the new Topic in
     *                        subsequent calls
     *
     * @throws java.io.IOException
     */
    String resolveTopic(String topicName) throws IOException;

    /**
     * @param topicArn {@code String} the arn returned by the Topic creation
     *                                that resolves to the Topic instance in
     *                                the Service Provider space.
     *
     * @throws IOException
     */
    void deleteTopic(String topicArn)throws IOException;

    /**
     * @param topicArn {@code String} the arn returned by the Topic creation
     *                                that resolves to the Topic instance in
     *                                the Service Provider space.
     * @param content {@code String} The content to be included as the body in
     *                               the message sent to the Topic.  The content
     *                               may be enveloped by the Service Provider, but
     *                               the content received by a subscriber should
     *                               match this explicitly.
     *
     * @throws IOException
     */
    void publishMessage(String topicArn,
                        String content) throws IOException;

    /**
     * @param topicArn {@code String} the arn returned by the Topic creation
     *                                that resolves to the Topic instance in
     *                                the Service Provider space.
     *
     * @throws IOException
     */
    void publishMessages(String topicArn,
                         List<String> content) throws IOException;

    /**
     * @param topicArn {@code String} the arn returned by the Topic creation
     *                                that resolves to the Topic instance in
     *                                the Service Provider space.
     * @param queueArn {@code String} the arn that resolves to the a Consumer
     *                                Queue instance in the Service Provider space.
     *
     * @return {@code String} the ARN to use to reference the subscription mapping
     *                        between the topic and the queue
     */
    String subscribeToTopic(String topicArn,
                            String queueArn);

    /**
     * @param subscriptionArn {@code String} the ARN to use to reference the subscription mapping
     *                                       between the topic and the queue
     */
    void unsubscribeFromTopic(String subscriptionArn);
} // ISNSClient
