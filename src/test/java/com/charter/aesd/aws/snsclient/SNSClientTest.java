package com.charter.aesd.aws.snsclient;

import com.charter.aesd.aws.enums.AWSAuthType;
import com.charter.aesd.aws.sqsclient.SQSClient;
import com.google.common.base.Optional;

import java.io.IOException;

import junit.framework.Assert;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class SNSClientTest {

    private final static String TEST_PROFILE_NAME = "snsclient-test";
    private final static String TEST_TOPIC_NAME = "SNSTestTopic";
    private final static String TEST_TOPIC_NOEXIST_NAME = "SNSTestNotExist";
    private final static String TEST_CONSUMER_QUEUE_NAME = "SNSTestConsumer";
    private final static String TEST_MSG_CONTENT = "SNS Test Message";

    private SNSClient _client = null;
    private String _topicArn = null;
    private String _topicName = null;

    protected ISNSClient getClient() {

        return _client;
    }

    protected String getTopicARN() {

        return _topicArn;
    }

    protected void setTopicARN(final String topicArn) {

        _topicArn = topicArn;
    }

    protected String getTopicName() {

        return _topicName;
    }

    protected void setTopicName(final String topicName) {

        _topicName = topicName;
    }

    @Before
    public void setUp() {

        _client = new SNSClient.Builder(AWSAuthType.PROFILE).setProfileName(TEST_PROFILE_NAME).build();

        String topicArn = null;
        try {
            setTopicName(TEST_TOPIC_NAME + "-" + System.currentTimeMillis());

            topicArn = _client.createTopic(getTopicName());
            setTopicARN(topicArn);
        } catch (Exception e) {

        }

        if ((topicArn == null) || (topicArn.length() == 0)) {
            // Illegal Queue URL
            Assert.fail("Illegal Topic ARN");
        }

        System.out.println("TESTING with Topic " + getTopicARN());
    }

    @After
    public void tearDown() {

        ISNSClient client = getClient();
        if (client != null) {

            // Wait for the API ...
            try {
                client.deleteTopic(getTopicARN());
            } catch (Exception e) {
                System.out.println("Topic " + getTopicARN() + " DELETED");
            }
        }
    }

    @Test
    public void testCreate() throws IOException {

        ISNSClient client = getClient();

        // Try sending a message
        try {
            client.publishMessage(getTopicARN(), TEST_MSG_CONTENT);
        } catch (Exception e) {
            Assert.fail("Error during test ... " + e.getMessage());
        }

        Assert.assertTrue(true);
    }

    @Test
    public void testDestroy() throws IOException {

        ISNSClient client = getClient();
        String topicArn = getTopicARN();

        // Queue should not exist ...
        try {
            client.deleteTopic(topicArn);
        } catch (Exception e) {
            Assert.fail("Delete of Topic FAILED::" + e.getMessage());
        }

        Assert.assertTrue(true);
    }

    @Test
    public void testSend() throws IOException {

        ISNSClient client = getClient();
        String topicArn = getTopicARN();

        try {
            client.publishMessage(topicArn, TEST_MSG_CONTENT);
        } catch (Exception e) {
            Assert.fail("SNS Publish FAILED, " + e.getMessage());
        }

        Assert.assertTrue(true);
    }

    @Test
    public void testReceive() throws IOException {

        ISNSClient client = getClient();
        String topicARN = getTopicARN();

        try {
            Thread.sleep(1000);
        } catch (Exception e) {

        }

        // Use an SQS Queue to receive and verify the content
        SQSClient sqsClient = new SQSClient.Builder(AWSAuthType.PROFILE).build();
        String url = sqsClient.createQueue(TEST_CONSUMER_QUEUE_NAME);

        sqsClient.allowTopic(url, topicARN);
        client.subscribeToTopic(topicARN, sqsClient.resolveQueueARN(url));

        // Send a notification
        client.publishMessage(topicARN, TEST_MSG_CONTENT);

        Optional<String> msg = sqsClient.receiveMessage(url);

        if (msg.isPresent()) {
            // Msg is JSON encoded, pull out the Message property
            ObjectMapper mapper = new ObjectMapper();
            JsonNode actualObj = mapper.readTree(msg.get());
            String msgBody = actualObj.get("Message").asText();

            Assert.assertEquals(TEST_MSG_CONTENT, msgBody);
        } else {
            Assert.fail("FAILED to receive original message on SQS Queue");
        }
    }

    @Test
    public void testRawReceive() throws IOException {

        ISNSClient client = getClient();
        String topicARN = getTopicARN();

        try {
            Thread.sleep(1000);
        } catch (Exception e) {

        }

        // Use an SQS Queue to receive and verify the content
        SQSClient sqsClient = new SQSClient.Builder(AWSAuthType.PROFILE).build();
        String url = sqsClient.createQueue(TEST_CONSUMER_QUEUE_NAME);

        sqsClient.allowTopic(url, topicARN);
        String subscriptionArn = client.subscribeToTopic(topicARN, sqsClient.resolveQueueARN(url));

        // Enable RAW delivery
        client.disableEnvelope(subscriptionArn);

        // Send a notification
        client.publishMessage(topicARN, TEST_MSG_CONTENT);

        Optional<String> msg = sqsClient.receiveMessage(url);

        if (msg.isPresent()) {
            Assert.assertEquals(TEST_MSG_CONTENT, msg.get());
        } else {
            Assert.fail("FAILED to receive original message on SQS Queue");
        }
    }
}
