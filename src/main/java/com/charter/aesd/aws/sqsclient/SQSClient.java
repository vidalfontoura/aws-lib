package com.charter.aesd.aws.sqsclient;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.policy.Policy;
import com.amazonaws.auth.policy.Principal;
import com.amazonaws.auth.policy.Statement;
import com.amazonaws.auth.policy.actions.SQSActions;
import com.amazonaws.auth.policy.conditions.ArnCondition;
import com.amazonaws.auth.policy.conditions.ConditionFactory;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.charter.aesd.aws.sqsclient.util.DefaultSNSSQSPolicy;
import com.charter.aesd.aws.util.AbstractAWSClientBuilder;

import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p/>
 * Implementation of the ISQSClient that is connected to AWS SQS
 *   as the message queue provider.
 *
 *  @see <a href="http://aws.amazon.com/sqs/faqs/">http://aws.amazon.com/sqs/faqs/</a>
 *  @see <a href="http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/sqs/AmazonSQS.html">http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/sqs/AmazonSQS.html</a>
 *
 * <p/>
 * User: matthewsmith Date: 7/10/14 Time: 10:23 AM
 *
 * @author $Author: $
 * @version $Rev: $
 * @since ${date}
 */
public class SQSClient implements ISQSClient {
    /**
     *
     */
    private static final Logger _LOGGER = LoggerFactory.getLogger(SQSClient.class);

    /**
     *
     */
    private final static String QUEUE_DEPTH_ATTR_NAME = "ApproximateNumberOfMessages";
    private final static String QUEUE_ARN_ATTR_NAME = "QueueArn";
    private final static String QUEUE_SNS_ATTR_NAME = "Policy";
    private final static int MAX_NUM_MESSAGES_CHUNK = 10;  // Max Allowed by Amazon SQS
    private final static String DEFAULT_SNS_PUBLISH_POLICY_NAME = "DefaultSNSPolicy";

    /**
     * local ref to the AWS SQS API
     */
    private AmazonSQS _awsSQSClient = null;

    /**
     * @param client {@link AmazonSQS} the AWS API reference.  Used to
     *                                 connect the implementation to the
     *                                 specified AWS account.
     */
    protected SQSClient(final AmazonSQS client) {

        _awsSQSClient = client;
    }

    /**
     *
     */
    protected ISQSPolicy allocateSQSTopicPolicy(final String name,
                                                final String queueArn,
                                                final String topicArn) {
        return new DefaultSNSSQSPolicy(name,
                                       queueArn,
                                       topicArn);
    }

    /**
     * @return {@link AmazonSQS} local and derived class handle to the
     *                           AWS API
     */
    protected AmazonSQS getClient() {

        return _awsSQSClient;
    }

    /**
     * @param queueName {@code String} the name used by the Queue creation
     *                                 that resolves to the Queue instance in
     *                                 the Service Provider space.
     *
     * @return (@code Boolean} true - If there is an existing Queue with the specified name
     *                         false - No Queue by that name exists in the Service
     *                                 Provider Space
     */
    @Override
    public boolean isQueueExists(final String queueName) {
        if (_LOGGER.isTraceEnabled()) {
            _LOGGER.trace("isQueueExists(" + queueName + ")");
        }

        boolean bFound = true;

        try {
            GetQueueUrlResult result = getClient().getQueueUrl(queueName);

            if (_LOGGER.isDebugEnabled()) {
                _LOGGER.debug("Queue " + queueName + " EXISTS, url=" + result.getQueueUrl());
            }
        } catch(QueueDoesNotExistException e) {
            bFound = false;

            if (_LOGGER.isDebugEnabled()) {
                _LOGGER.debug("Queue " + queueName + " DOES NOT EXIST");
            }
        }

        return bFound;
    }

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
    @Override
    public String resolveQueueUrl(final String queueName) {
        if (_LOGGER.isTraceEnabled()) {
            _LOGGER.trace("resolveQueueUrl(" + queueName + ")");
        }

        String qUrl = null;
        try {
            GetQueueUrlResult qResult = getClient().getQueueUrl(queueName);
            qUrl = (qResult == null) ? null : qResult.getQueueUrl();

            if (_LOGGER.isDebugEnabled()) {
                _LOGGER.debug("Queue " + queueName + " EXISTS, url=" + qUrl);
            }
        } catch(QueueDoesNotExistException e) {
            if (_LOGGER.isDebugEnabled()) {
                _LOGGER.debug("Queue " + queueName + " DOES NOT EXIST");
            }
        }

        return qUrl;
    }

    /**
     * Resolve the ARN to use for an existing Queue
     *
     * @param queueUrl {@code String} the url returned by the Queue creation
     *                                that resolves to the Queue instance in
     *                                the AWS space.
     *
     * @return {@code String} the ARN to use to reference the Queue in
     *                        subsequent calls
     *
     * @throws IOException
     */
    public String resolveQueueARN(final String queueUrl) {
        if (_LOGGER.isTraceEnabled()) {
            _LOGGER.trace("resolveQueueARN(" + queueUrl + ")");
        }

        String qArn = null;

        List<String> attrs = new ArrayList<String>();
        attrs.add(QUEUE_ARN_ATTR_NAME);

        GetQueueAttributesResult result = getClient().getQueueAttributes(queueUrl,
                                                                         attrs);

        java.util.Map<String, String> attrMap = null;
        if ((result != null) &&
            ((attrMap = result.getAttributes()) != null)) {
            qArn = attrMap.get(QUEUE_ARN_ATTR_NAME);

            if (_LOGGER.isDebugEnabled()) {
                _LOGGER.debug("Queue[url=" + queueUrl + ", arn=" + qArn + "]");
            }
        } else {
            if (_LOGGER.isDebugEnabled()) {
                _LOGGER.debug("No ARN Found for Queue[url=" + queueUrl + "]");
            }
        }

        return qArn;
    }

    /**
     * @param queueUrl {@code String} the AWS url returned by the Queue creation
     *                                that resolves to the Queue instance in
     *                                the proper AWS space.
     * @param topicArn {@code String} the arn returned by the Topic creation/attachment
     *                                that resolves to the Topic instance in
     *                                the AWS space.
     *
     * Add a permission to the SQS instance in AWS that allows the specified SNS Topic
     *  to publish to the Queue.
     */
    @Override
    public void allowTopic(final String queueUrl,
                           final String topicArn) {
        if (_LOGGER.isTraceEnabled()) {
            _LOGGER.trace("allowTopic(" + queueUrl + ", " + topicArn + ")");
        }

        Map<String, String> attrs = new HashMap<String, String>();

        // I think the Java SDK has an issue with the Policy generation...
        //  it generates actions as sqs:*, whereas the AWS Management
        //  console generates them as SQS:*.  When the Actions are sqs:*,
        //  no messages are passed, however, when they are SQS:*, all messages
        //  flow as expected
        // When the same permission is added to the SQS queue via AWS, the
        //   messages are published without issue
//        attrs.put(QUEUE_SNS_ATTR_NAME,
//                  generateSqsPolicyForTopic(topicArn).toJson());

        // Using this for now... JSON policy is working fine
        attrs.put(QUEUE_SNS_ATTR_NAME,
                  allocateSQSTopicPolicy(DEFAULT_SNS_PUBLISH_POLICY_NAME,
                                         resolveQueueARN(queueUrl),
                                         topicArn).toJson());
        getClient().setQueueAttributes(queueUrl,
                                       attrs);

        if (_LOGGER.isDebugEnabled()) {
            _LOGGER.debug("Topic[arn=" +
                                          topicArn +
                                          "] ALLOWED to publish to Queue[url=" +
                                          queueUrl +
                                          "]");
        }
    }

    /**
     * Create a new Message Queue in the attached AWS Account.
     *
     * @param queueName {@code String} the name to assign to the
     *                                 created Queue.  Should follow
     *                                 AWS SQS naming conventions
     *
     * @return {@code String} the AWS region URL to use to reference
     *                        the new Queue in subsequent calls
     *
     * @throws IOException
     */
    @Override
    public String createQueue(final String queueName) throws IOException {
        if (_LOGGER.isTraceEnabled()) {
            _LOGGER.trace("createQueue(" + queueName + ")");
        }

        CreateQueueResult result = getClient().createQueue(new CreateQueueRequest().withQueueName(queueName));
        String qUrl = result.getQueueUrl();
        if (_LOGGER.isDebugEnabled()) {
            _LOGGER.debug("Queue[name=" + queueName +
                                          ", url=" + qUrl +
                                          "] CREATED");
        }

        return qUrl;
    }

    /**
     * @param queueUrl {@code String} the AWS url returned by the Queue creation
     *                                that resolves to the Queue instance in
     *                                the proper AWS region.
     *
     * @throws IOException
     */
    @Override
    public void deleteQueue(final String queueUrl) throws IOException {
        if (_LOGGER.isTraceEnabled()) {
            _LOGGER.trace("deleteQueue(" + queueUrl + ")");
        }

        getClient().deleteQueue(queueUrl);

        if (_LOGGER.isDebugEnabled()) {
            _LOGGER.debug("Queue[url=" + queueUrl +
                          "] DELETED");
        }
    }

    /**
     * @param queueUrl {@code String} the url returned by the Queue creation
     *                                that resolves to the Queue instance in
     *                                the AWS space.
     *
     * @return (@code Boolean} true - If there are messages waiting on the Queue
     *                         false - No messages have been sent to the Queue that
     *                                 are waiting to be processed
     */
    @Override
    public boolean hasPendingMessages(final String queueUrl) {
        return getPendingMessageCount(queueUrl) > 0;
    }

    /**
     * @param queueUrl {@code String} the url returned by the Queue creation
     *                                that resolves to the Queue instance in
     *                                the AWS space.
     *
     * @return (@code int} the current Queue depth
     *
     */
    @Override
    public int getPendingMessageCount(final String queueUrl) {
        if (_LOGGER.isTraceEnabled()) {
            _LOGGER.trace("getPendingMessageCount(" + queueUrl + ")");
        }

        List<String> attrs = new ArrayList<String>();
        attrs.add(QUEUE_DEPTH_ATTR_NAME);

        GetQueueAttributesResult result = getClient().getQueueAttributes(queueUrl,
                                                                         attrs);

        int msgCnt = 0;
        String val = result.getAttributes().get(QUEUE_DEPTH_ATTR_NAME);
        try {
            msgCnt = Integer.parseInt(val);
        } catch(Exception e) {
            if (_LOGGER.isWarnEnabled()) {
                StringWriter errDetailsWriter = new StringWriter();
                e.printStackTrace(new PrintWriter(errDetailsWriter));
                _LOGGER.info("Invalid Message Count Attribute Received from AWS, val=" + val);
            }
        }

        if (_LOGGER.isDebugEnabled()) {
            _LOGGER.debug("Queue[url=" + queueUrl + "] has " + msgCnt + " messages pending");
        }

        return msgCnt;
    }

    /**
     * @param queueUrl {@code String} the url returned by the Queue creation
     *                                that resolves to the Queue instance in
     *                                the AWS space.
     * @param content {@code String} The content to be included as the body in
     *                               the message sent to the Queue.  The content
     *                               may be enveloped by Amazon SQS, but
     *                               the content returned via a receiveMessage
     *                               call should match this explicitly.
     *
     * @throws IOException
     */
    @Override
    public void sendMessage(final String queueUrl,
                            final String content) throws IOException {
        if (_LOGGER.isTraceEnabled()) {
            _LOGGER.trace("sendMessage(" + queueUrl + ", content=" + content + ")");
        }

        SendMessageResult result = getClient().sendMessage(new SendMessageRequest(queueUrl,
                                                                                  content));

        if (_LOGGER.isDebugEnabled()) {
            _LOGGER.debug("Message " + result.getMessageId() + " SENT");
        }
    }

    /**
     * @param queueUrl {@code String} the url returned by the Queue creation
     *                                that resolves to the Queue instance in
     *                                the AWS space.
     *
     * @return {@code String} The content that was submitted to the Queue via a
     *                        sendMessage call.  This content is the user space content
     *                        and includes nothing from the AWS SQS envelope.
     *                        This value should match what was submitted exactly.  This call
     *                        returns the next message on the Queue.  NOTE:  order is
     *                        NOT maintained.  This method returns at most the content
     *                        of 1 message.
     *
     * @throws IOException
     */
    @Override
    public Optional<String> receiveMessage(final String queueUrl) throws IOException {
        if (_LOGGER.isTraceEnabled()) {
            _LOGGER.trace("receiveMessage(" + queueUrl + ")");
        }

        ReceiveMessageResult result = getClient().receiveMessage(queueUrl);

        java.util.List<Message> msgs = null;
        if ((result == null) ||
            ((msgs = result.getMessages()) == null) ||
            (msgs.size() == 0)) {
            if (_LOGGER.isDebugEnabled()) {
                _LOGGER.debug("No Message Available");
            }

            return Optional.absent();
        }

        Message msg = msgs.get(0);
        if (_LOGGER.isDebugEnabled()) {
            // ToDo :: Add message details here
            _LOGGER.debug("RECEIVED message[id=" + msg.getMessageId() + "]");
        }

        return Optional.of(msg.getBody());
    }

    /**
     * @param queueUrl {@code String} the url returned by the Queue creation
     *                                that resolves to the Queue instance in
     *                                the AWS space.
     *
     * @return {@code String} The content(s) that were submitted to the Queue via
     *                        sendMessage calls.  This content is the user space content
     *                        and includes nothing from the Service Provider envelope.
     *                        This value should match what was submitted exactly.  This call
     *                        empties the Queue.  NOTE:  order is NOT maintained.
     *                        This method returns all of the messages on the
     *                        Queue at the time of the call.
     *
     * @throws IOException
     */
    @Override
    public List<String> receiveMessages(final String queueUrl) throws IOException {
        if (_LOGGER.isTraceEnabled()) {
            _LOGGER.trace("receiveMessages(" + queueUrl + ")");
        }

        // Drain the queue...
        // ToDo :: implement a threshold here
        List<String> contentMsgs = new ArrayList<String>();
        while(getPendingMessageCount(queueUrl) > 0) {
            ReceiveMessageRequest request = new ReceiveMessageRequest(queueUrl);
            request.setMaxNumberOfMessages(MAX_NUM_MESSAGES_CHUNK);

            ReceiveMessageResult result = getClient().receiveMessage(request);

            java.util.List<Message> msgs = null;
            if ((result == null) ||
                ((msgs = result.getMessages()) == null) ||
                (msgs.size() == 0)) {
                if (_LOGGER.isDebugEnabled()) {
                    _LOGGER.debug("No Message Available");
                }

                continue;
            }

            for (Message msg : msgs) {
                if (msg == null) {
                    continue;
                }

                contentMsgs.add(msg.getBody());
            }
        }

        if (_LOGGER.isDebugEnabled()) {
            _LOGGER.debug("Received " + contentMsgs.size() +
                          " messages");
        }

        return contentMsgs;
    }

    /**
     * Builder class for constructing an instance of {@link SQSClient}
     */
    public static class Builder
        extends AbstractAWSClientBuilder<SQSClient> {
        /**
         *
         * @param provider AWS credentials provider
         * @param config
         *
         * @return the AWS SQS client implementation
         */
        @Override
        protected SQSClient allocateClient(final ProfileCredentialsProvider provider,
                                           final ClientConfiguration config) {
            if (_LOGGER.isTraceEnabled()) {
                _LOGGER.trace("allocateClient()");
            }

            return (provider == null)
                   ? new SQSClient(new AmazonSQSClient(getConfig()))
                   : new SQSClient(new AmazonSQSClient(provider,
                                                       getConfig()));
        }
    }
} // SQSClient
