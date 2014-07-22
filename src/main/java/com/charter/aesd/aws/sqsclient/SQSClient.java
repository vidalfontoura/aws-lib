package com.charter.aesd.aws.sqsclient;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
public class SQSClient
                implements ISQSClient {

    /**
     * local ref to the AWS API
     */
    private AmazonSQS _awsSQSClient = null;

    /**
     * @param client {@link AmazonSQS} the AWS API reference.  Used to
     *                                 connect the implementation to the
     *                                 specified AWS account.
     */
    private SQSClient(final AmazonSQS client) {

        _awsSQSClient = client;
    }

    /**
     * @return {@link AmazonSQS} local and dervied class handle to the
     *                           AWS API
     */
    protected AmazonSQS getClient() {

        return _awsSQSClient;
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
    public String createQueue(final String queueName)
                    throws IOException {

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
    public void deleteQueue(final String queueUrl)
                    throws IOException {

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

        ReceiveMessageResult result = getClient().receiveMessage(queueUrl);

        java.util.List<Message> msgs = (result == null)
                        ? new ArrayList<Message>(0)
                        : result.getMessages();

        return !msgs.isEmpty();
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
                            final String content)
                    throws IOException {

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
    public String receiveMessage(final String queueUrl)
                    throws IOException {

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
    public List<String> receiveMessages(final String queueUrl)
                    throws IOException {

        ReceiveMessageResult result = getClient().receiveMessage(queueUrl);

        java.util.List<Message> msgs = (result == null)
                        ? new ArrayList<Message>(0)
                        : result.getMessages();
        if (msgs.isEmpty()) {
            return null;
        }

        List<String> contentMsgs = new ArrayList<String>(msgs.size());
        for (Message msg : msgs) {
            if (msg == null) {
                continue;
            }

            contentMsgs.add(msg.getBody());
        }

        return contentMsgs;
    }

    /**
     * Builder class for constructing an instance of {@link SQSClient}
     */
    public static class Builder {

        /**
         *
         */
        private ClientConfiguration _config = null;

        /**
         *
         */
        public Builder() {

            _config = new ClientConfiguration();
        }

        /**
         * Sets the {@link ClientConfiguration} used to configure
         *
         * @param config
         *                 {@link ClientConfiguration}
         *
         * @return {@link Builder}
         */
        public Builder setConfig(ClientConfiguration config) {

            _config = config;

            return this;
        }

        public SQSClient build() {

            return new SQSClient(new AmazonSQSClient(getConfiguration()));
        }

        /**
         * Creates a {@code ClientConfiguration} object using the System properties for {@code http.proxyHost} and
         * {@code http.proxyPort}. To leverage this both host and port must be set using the -D args (i.e., {@code
         * -Dhttp.proxyHost=my.proxy.host.com -Dhttp.proxyPort=3128} and if auth is required {@code
         * -Dhttp.proxyUser=username -Dhttp.proxyPassword=password1234}.
         *
         * @return A {@ClientConfiguration}. Never {@code null}.
         */
        public ClientConfiguration getConfiguration() {

            String proxyHost = System.getProperty("http.proxyHost");
            String proxyPort = System.getProperty("http.proxyPort");
            String proxyUserName = System.getProperty("http.proxyUser");
            String proxyUserPasswd = System.getProperty("http.proxyPassword");

            if (proxyHost != null) {
                _config.setProxyHost(proxyHost);
            }

            if (proxyPort != null) {
                _config.setProxyPort(Integer.parseInt(proxyPort));
            }

            if (proxyUserName != null) {
                _config.setProxyUsername(proxyUserName);
            }

            if (proxyUserPasswd != null) {
                _config.setProxyPassword(proxyUserPasswd);
            }

            return _config;
        }
    }
} // SQSClient
