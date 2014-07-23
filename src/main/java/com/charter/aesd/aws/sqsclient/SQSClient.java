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
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.charter.aesd.aws.util.AbstractAWSClientBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p/>
 * Implementation of the ISQSClient that is connected to AWS
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
    private final static String QUEUE_DEPTH_ATTR_NAME = "ApproximateNumberOfMessages";
    private final static String QUEUE_ARN_ATTR_NAME = "QueueArn";
    private final static String QUEUE_SNS_ATTR_NAME = "Policy";
    private final int MAX_NUM_MESSAGES_CHUNK = 10;  // Max Allowed by Amazon SQS

    /**
     * local ref to the AWS API
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
        boolean bFound = true;

        try {
            getClient().getQueueUrl(queueName);
        } catch(QueueDoesNotExistException e) {
            bFound = false;
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
        String qUrl = null;
        try {
            GetQueueUrlResult qResult = getClient().getQueueUrl(queueName);
            qUrl = (qResult == null) ? null : qResult.getQueueUrl();
        } catch(QueueDoesNotExistException e) {

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
        String qArn = null;

        List<String> attrs = new ArrayList<String>();
        attrs.add(QUEUE_ARN_ATTR_NAME);

        GetQueueAttributesResult result = getClient().getQueueAttributes(queueUrl,
                                                                         attrs);

        java.util.Map<String, String> attrMap = null;
        if ((result != null) &&
            ((attrMap = result.getAttributes()) != null)) {
            qArn = attrMap.get(QUEUE_ARN_ATTR_NAME);
        }

        return qArn;
    }

    /**
     *
     */
    @Override
    public void allowTopic(final String queueUrl,
                           final String topicArn) {
        Map<String, String> attrs = new HashMap<String, String>();

        // I think the Java SDK has an issue with the Policy generation...
        //  it generates actions as sqs:*, whereas the AWS Management
        //  console generates them as SQS:*.  When the Actions are sqs:*,
        //  no messages are passed, however, when they are SQS:*, all messages
        //  flow as expected
        attrs.put(QUEUE_SNS_ATTR_NAME,
                  generateSqsPolicyForTopic(topicArn).toJson());

        getClient().setQueueAttributes(queueUrl,
                        attrs);
    }

    /**
     *
     */
    protected Policy generateSqsPolicyForTopic(final String topicArn) {
        Policy policy = new Policy().withStatements(
            new Statement(Statement.Effect.Allow)
                .withPrincipals(Principal.AllUsers)
                .withActions(SQSActions.SendMessage)
                .withConditions(new ArnCondition(ArnCondition.ArnComparisonType.ArnEquals,
                                                 ConditionFactory.SOURCE_ARN_CONDITION_KEY,
                                                 topicArn)));

        return policy;
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

        return getClient().createQueue(new CreateQueueRequest().withQueueName(queueName)).getQueueUrl();
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

        getClient().deleteQueue(queueUrl);
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

        List<String> attrs = new ArrayList<String>();
        attrs.add(QUEUE_DEPTH_ATTR_NAME);

        GetQueueAttributesResult result = getClient().getQueueAttributes(queueUrl,
                                                                         attrs);

        int msgCnt = 0;
        try {
            msgCnt = Integer.parseInt(result.getAttributes().get(QUEUE_DEPTH_ATTR_NAME));
        } catch(Exception e) {

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

        getClient().sendMessage(new SendMessageRequest(queueUrl,
                        content));
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
    public String receiveMessage(final String queueUrl) throws IOException {

        ReceiveMessageResult result = getClient().receiveMessage(queueUrl);

        java.util.List<Message> msgs = (result == null)
                        ? new ArrayList<Message>(0)
                        : result.getMessages();
        if (msgs.isEmpty()) {
            return null;
        }

        return msgs.get(0).getBody();
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

        // Drain the queue...
        // ToDo :: implement a threshold here
        List<String> contentMsgs = new ArrayList<String>();
        while(getPendingMessageCount(queueUrl) > 0) {
            ReceiveMessageRequest request = new ReceiveMessageRequest(queueUrl);
            request.setMaxNumberOfMessages(MAX_NUM_MESSAGES_CHUNK);

            ReceiveMessageResult result = getClient().receiveMessage(request);

            java.util.List<Message> msgs = (result == null)
                            ? new ArrayList<Message>(0)
                            : result.getMessages();
            if (msgs.isEmpty()) {
                continue;
            }

            for (Message msg : msgs) {
                if (msg == null) {
                    continue;
                }

                contentMsgs.add(msg.getBody());
            }
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
        protected SQSClient allocateClient(final ProfileCredentialsProvider provider,
                                           final ClientConfiguration config) {

            return (provider == null)
                   ? new SQSClient(new AmazonSQSClient(getConfig()))
                   : new SQSClient(new AmazonSQSClient(provider,
                                                       getConfig()));
        }
    }
} // SQSClient
