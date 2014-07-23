package com.charter.aesd.aws.snsclient;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.charter.aesd.aws.util.AbstractAWSClientBuilder;

import java.io.IOException;
import java.util.List;

/**
 * <p/>
 * Implementation of the ISNSClient that is connected to AWS
 *   as the topic provider.
 *
 *  @see <a href="http://aws.amazon.com/sns/faqs/">http://aws.amazon.com/sns/faqs/</a>
 *  @see <a href="http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/sns/AmazonSNS.html">http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/sns/AmazonSNS.html</a>
 *
 * <p/>
 * User: matthewsmith Date: 7/10/14 Time: 10:23 AM
 *
 * @author $Author: $
 * @version $Rev: $
 * @since ${date}
 */
public class SNSClient
    implements ISNSClient {
    /**
     *
     */
    private final static String AWS_SQS_SNS_PROTOCOL = "SQS";

    /**
     * local ref to the AWS API
     */
    private AmazonSNS _awsSNSClient = null;

    /**
     * @param client {@link AmazonSNS} the AWS API reference.  Used to
     *                                 connect the implementation to the
     *                                 specified AWS account.
     */
    protected SNSClient(final AmazonSNS client) {

        _awsSNSClient = client;
    }

    /**
     * @return {@link AmazonSNS} local and derived class handle to the
     *                           AWS API
     */
    protected AmazonSNS getClient() {

        return _awsSNSClient;
    }

    /**
     * Create a new Topic in the service provider space.
     *
     * @param topicName {@code String} the name to assign to the
     *                                 created Topic.  Should follow
     *                                 AWSnaming conventions
     *
     * @return {@code String} the ARN to use to reference the new Topic in
     *                        subsequent calls
     *
     * @throws java.io.IOException
     */
    public String createTopic(final String topicName) throws IOException {
        return ((topicName == null) ||
                (topicName.length() == 0))
               ? null
               : getClient().createTopic(topicName).getTopicArn();
    }

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
    public String resolveTopic(String topicName) throws IOException {
        return ((topicName == null) ||
                (topicName.length() == 0))
               ? null
               : getClient().createTopic(topicName).getTopicArn();
    }

    /**
     * @param topicArn {@code String} the arn returned by the Topic creation
     *                                that resolves to the Topic instance in
     *                                the AWS space.
     *
     * @throws IOException
     */
    public void deleteTopic(final String topicArn) throws IOException {

        if ((topicArn == null) ||
            (topicArn.length() == 0)) {
            return;
        }

        getClient().deleteTopic(topicArn);
    }

    /**
     * @param topicArn {@code String} the arn returned by the Topic creation
     *                                that resolves to the Topic instance in
     *                                the AWS space.
     * @param content {@code String} The content to be included as the body in
     *                               the message sent to the Topic.  The content
     *                               is enveloped by AWS, but the content
     *                               received by a subscriber should match this
     *                               explicitly.
     *
     * @throws IOException
     */
    public void publishMessage(final String topicArn,
                               final String content) throws IOException {

        if ((topicArn == null) ||
            (topicArn.length() == 0)) {
            return;
        }

        getClient().publish(topicArn,
                        content);
    }

    /**
     * @param topicArn {@code String} the arn returned by the Topic creation
     *                                that resolves to the Topic instance in
     *                                the Service Provider space.
     * @param content {@code List<String><} The content to be included as the body in
     *                                      the messages sent to the Topic.  The content
     *                                      may be enveloped by the Service Provider, but
     *                                      the content received by a subscriber should
     *                                      match this explicitly.
     *
     * @throws IOException
     */
    public void publishMessages(final String topicArn,
                                final List<String> content) throws IOException {

        if ((topicArn == null) ||
            (topicArn.length() == 0) ||
            (content == null)) {
            return;
        }

        for (String msg : content) {
            getClient().publish(topicArn,
                            msg);
        }
    }

    /**
     *
     */
    public String subscribeToTopic(final String topicArn,
                                   final String queueArn) {

        if ((topicArn == null) ||
            (topicArn.length() == 0)) {
            return null;
        }

        return getClient().subscribe(topicArn,
                                     AWS_SQS_SNS_PROTOCOL,
                                     queueArn).getSubscriptionArn();
    }

    /**
     *
     */
    public void unsubscribeFromTopic(final String subscriptionArn) {

        if ((subscriptionArn == null) ||
            (subscriptionArn.length() == 0)) {
            return;
        }

        getClient().unsubscribe(subscriptionArn);
    }

    /**
     * Builder class for constructing an instance of {@link SNSClient}
     */
    public static class Builder
        extends AbstractAWSClientBuilder<SNSClient> {

        /**
         *
         */
        public Builder() {
            super();
        }

        /**
         *
         * @param provider
         * @param config
         * @return
         */
        @Override
        protected SNSClient allocateClient(final ProfileCredentialsProvider provider,
                                           final ClientConfiguration config) {

            return (provider == null)
                   ? new SNSClient(new AmazonSNSClient(getConfig()))
                   : new SNSClient(new AmazonSNSClient(provider,
                                                       getConfig()));
        }
    }
} // SQSClient
