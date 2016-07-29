package com.charter.aesd.aws.snsclient;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.SubscribeResult;
import com.charter.aesd.aws.enums.AWSAuthType;
import com.charter.aesd.aws.util.AbstractAWSClientBuilder;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p/>
 * Implementation of the ISNSClient that is nnected to AWS as the topic
 * provider.
 * 
 * @see <a
 *      href="http://aws.amazon.com/sns/faqs/">http://aws.amazon.com/sns/faqs/</a>
 * @see <a
 *      href="http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/sns/AmazonSNS.html">http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/sns/AmazonSNS.html</a>
 * 
 *      <p/>
 *      User: matthewsmith Date: 7/10/14 Time: 10:23 AM
 * 
 * @author $Author: $
 * @version $Rev: $
 * @since ${date}
 */
public class SNSClient implements ISNSClient {

    /**
     *
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SNSClient.class);

    /**
     * Protocol indicator used by SNS to target AWS SQS queue instances as topic
     * consumers
     */
    private final static String AWS_SQS_SNS_PROTOCOL = "SQS";
    private final static String RAW_MESSAGE_ATRR_NAME = "RawMessageDelivery";
    private final static String RAW_MESSAGE_INDICATOR_ON = "true";
    private final static String RAW_MESSAGE_INDICATOR_OFF = "false";

    /**
     * local ref to the AWS API
     */
    private AmazonSNS awsSNSClient = null;

    /**
     * @param client {@link AmazonSNS} the AWS API reference. Used to connect
     *        the implementation to the specified AWS account.
     */
    protected SNSClient(final AmazonSNS client) {

        this.awsSNSClient = client;
    }

    /**
     * @return {@link AmazonSNS} local and derived class handle to the AWS API
     */
    protected AmazonSNS getClient() {

        return this.awsSNSClient;
    }

    /**
     * Create a new SNS Topic in the AWS space.
     * 
     * @param topicName {@code String} the name to assign to the created Topic.
     *        Should follow AWS naming conventions
     * 
     * @return {@code String} the ARN to use to reference the new Topic in
     *         subsequent calls
     * 
     * @throws java.io.IOException
     */
    public String createTopic(final String topicName) throws IOException {

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("createTopic(" + topicName + ")");
        }

        return ((topicName == null) || (topicName.length() == 0)) ? null : getClient().createTopic(topicName)
            .getTopicArn();
    }

    /**
     * Connects to a SNS Topic in the AWS space.
     * 
     * @param topicName {@code String} the name of the Topic to connect to.
     *        Should follow AWS naming conventions
     * 
     * @return {@code String} the ARN to use to reference the new Topic in
     *         subsequent calls
     * 
     * @throws java.io.IOException
     */
    public String resolveTopic(String topicName) throws IOException {

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("resolveTopic(" + topicName + ")");
        }

        String topicArn =
            ((topicName == null) || (topicName.length() == 0)) ? null : getClient().createTopic(topicName)
                .getTopicArn();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Topic " + topicName + " resolves to ARN " + topicArn);
        }

        return topicArn;
    }

    /**
     * @param topicArn {@code String} the arn returned by the Topic creation
     *        that resolves to the Topic instance in the AWS space. This is the
     *        topic that is to be removed from AWS SNS.
     * 
     * @throws IOException
     */
    public void deleteTopic(final String topicArn) throws IOException {

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("deleteTopic(" + topicArn + ")");
        }

        if ((topicArn == null) || (topicArn.length() == 0)) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.trace("Invalid / empty Topic ARN specified");
            }

            return;
        }

        getClient().deleteTopic(topicArn);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("SNS Topic, arn=" + topicArn + " DELETED");
        }
    }

    /**
     * @param topicArn {@code String} the arn returned by the Topic creation
     *        that resolves to the Topic instance in the AWS space.
     * @param content {@code String} The content to be included as the body in
     *        the message sent to the Topic. The content is enveloped by AWS,
     *        but the content received by a subscriber should match this
     *        explicitly.
     * @return {@code String} The ID of the published message
     * 
     * @throws IOException
     */
    public String publishMessage(final String topicArn, final String content) throws IOException {

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("publishMessage(" + topicArn + ", " + content + ")");
        }

        if ((topicArn == null) || (topicArn.length() == 0)) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.trace("Invalid / empty Topic ARN specified");
            }

            return null;
        }

        PublishResult result = getClient().publish(topicArn, content);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Published message, id=" + result.getMessageId() + " to " + topicArn);
        }

        return result.getMessageId();
    }

    /**
     * @param topicArn {@code String} the arn returned by the Topic creation
     *        that resolves to the Topic instance in the Service Provider space.
     * @param content {@code List<String><} The content to be included as the
     *        body in the messages sent to the Topic. The content may be
     *        enveloped by the Service Provider, but the content received by a
     *        subscriber should match this explicitly.
     * 
     * @throws IOException
     */
    public void publishMessages(final String topicArn, final List<String> content) throws IOException {

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("publishMessage(" + topicArn + ", " + content + ")");
        }

        if ((topicArn == null) || (topicArn.length() == 0)) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.trace("Invalid / empty Topic ARN specified");
            }

            return;
        }

        if ((content == null) || (content.size() == 0)) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.trace("No content specified ... Nothing to publish.");
            }

            return;
        }

        for (String msg : content) {
            try {
                getClient().publish(topicArn, msg);
            } catch (Exception e) {
                if (LOGGER.isInfoEnabled()) {
                    StringWriter errDetailsWriter = new StringWriter();
                    e.printStackTrace(new PrintWriter(errDetailsWriter));
                    LOGGER.info("Error sending message:  error=" + e.getMessage() + ", content=" + msg
                        + ", error details=" + errDetailsWriter.toString());
                }

                // Do not fail the remaining messages ... keep processing
            }
        }
    }

    /**
     * @param topicArn {@code String} the arn returned by the Topic creation
     *        that resolves to the Topic instance in the Service Provider space.
     * @param queueArn {@code String} the arn that resolves to the a Consumer
     *        Queue instance in the Service Provider space.
     * 
     * @return {@code String} the ARN to use to reference the subscription
     *         mapping between the topic and the queue
     */
    public String subscribeToTopic(final String topicArn, final String queueArn) {

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("subscribeToTopic(" + topicArn + ", " + queueArn + ")");
        }

        if ((topicArn == null) || (topicArn.length() == 0)) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.trace("Invalid / empty Topic ARN specified");
            }

            return null;
        }

        SubscribeResult result = getClient().subscribe(topicArn, AWS_SQS_SNS_PROTOCOL, queueArn);
        String subArn = (result == null) ? null : result.getSubscriptionArn();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Queue[arn=" + queueArn + "] SUBSCRIBED to Topic[arn=" + topicArn + "] AS Subscription[arn="
                + subArn + "]");
        }

        return subArn;
    }

    /**
     * @param subscriptionArn {@code String} the ARN to use to reference the
     *        subscription mapping between the topic and the queue
     */
    public void unsubscribeFromTopic(final String subscriptionArn) {

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("unsubscribeFromTopic(" + subscriptionArn + ")");
        }

        if ((subscriptionArn == null) || (subscriptionArn.length() == 0)) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.trace("Invalid / empty Subscription ARN specified");
            }

            return;
        }

        getClient().unsubscribe(subscriptionArn);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("UNSUBSCRIBE request COMPLETE for Subscription[arn=" + subscriptionArn + "]");
        }
    }

    /**
     * @param subscriptionArn {@code String} the ARN to use to reference the
     *        subscription mapping between the topic and the queue
     * 
     *        When the SNS Notification if forwarded to the listeners, the SNS
     *        JSON envelope will be used
     */
    @Override
    public void enableEnvelope(final String subscriptionArn) {

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("enableEnvelope(" + subscriptionArn + ")");
        }

        getClient().setSubscriptionAttributes(subscriptionArn, RAW_MESSAGE_ATRR_NAME, RAW_MESSAGE_INDICATOR_OFF);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("RawMessageDelivery is ENABLED for Subscription[arn=" + subscriptionArn + "]");
        }
    }

    /**
     * @param subscriptionArn {@code String} the ARN to use to reference the
     *        subscription mapping between the topic and the queue
     * 
     *        When the SNS Notification if forwarded to the listeners, the SNS
     *        JSON envelope will NOT be used
     */
    @Override
    public void disableEnvelope(final String subscriptionArn) {

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("disableEnvelope(" + subscriptionArn + ")");
        }

        getClient().setSubscriptionAttributes(subscriptionArn, RAW_MESSAGE_ATRR_NAME, RAW_MESSAGE_INDICATOR_ON);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("RawMessageDelivery is DISABLED for Subscription[arn=" + subscriptionArn + "]");
        }
    }

    /**
     * Builder class for constructing an instance of {@link SNSClient}
     */
    public static class Builder extends AbstractAWSClientBuilder<SNSClient> {

        /**
         * @param authType
         */
        public Builder(AWSAuthType authType) {

            super(authType);
        }

        /**
         * 
         * @param provider
         * @param config
         * @return
         */
        @Override
        protected SNSClient allocateClient(final AWSCredentialsProvider provider, final ClientConfiguration config) {

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("allocateClient()");
            }

            return (provider == null) ? new SNSClient(new AmazonSNSClient(getConfig())) : new SNSClient(
                new AmazonSNSClient(provider, getConfig()));
        }
    }
} // SNSClient
