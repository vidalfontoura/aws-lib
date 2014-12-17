package com.charter.aesd.aws.sqsclient;

import com.amazonaws.services.sqs.model.Message;
import com.charter.aesd.aws.enums.AWSAuthType;
import com.google.common.base.Optional;

import java.io.IOException;
import java.util.ArrayList;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class SQSClientTest {

    private final static String TEST_PROFILE_NAME = "sqsclient-test";
    private final static String TEST_QUEUE_NAME = "SQSTest";
    private final static String TEST_QUEUE_NOEXIST_NAME = "SQSTestNotExist";
    private final static String TEST_MSG_CONTENT = "SQS Test Message";

    private SQSClient _client = null;
    private String _queueUrl = null;
    private String _queueName = null;

    protected ISQSClient getClient() {

        return _client;
    }

    protected String getQueueUrl() {

        return _queueUrl;
    }

    protected void setQueueUrl(final String queueUrl) {

        _queueUrl = queueUrl;
    }

    protected String getQueueName() {

        return _queueName;
    }

    protected void setQueueName(final String queueName) {

        _queueName = queueName;
    }

    @Before
    public void setUp() {

        _client = new SQSClient.Builder(AWSAuthType.PROFILE).setProfileName(TEST_PROFILE_NAME).build();

        String qUrl = null;
        try {
            setQueueName(TEST_QUEUE_NAME + "-" + System.currentTimeMillis());

            qUrl = _client.createQueue(getQueueName());
            setQueueUrl(qUrl);
        } catch (Exception e) {

        }

        if ((qUrl == null) || (qUrl.length() == 0)) {
            // Illegal Queue URL
            Assert.fail("Illegal Queue URL");
        }

        System.out.println("TESTING with Queue " + getQueueUrl());
    }

    @After
    public void tearDown() {

        ISQSClient client = getClient();
        if (client != null) {

            // Wait for the API ...
            try {
                client.deleteQueue(getQueueUrl());
            } catch (Exception e) {
                System.out.println("Queue " + getQueueUrl() + " DELETED");
            }
        }
    }

    @Test
    public void testCreate() throws IOException {

        ISQSClient client = getClient();

        Assert.assertFalse(client.hasPendingMessages(getQueueUrl()));
    }

    @Test
    public void testDestroy() throws IOException {

        ISQSClient client = getClient();
        String qUrl = getQueueUrl();

        // Queue should not exist ...
        try {
            client.deleteQueue(qUrl);
        } catch (Exception e) {
            Assert.fail("Delete of Queue FAILED::" + e.getMessage());
        }
    }

    @Test
    public void testSend() throws IOException {

        ISQSClient client = getClient();
        String qUrl = getQueueUrl();

        client.sendMessage(qUrl, TEST_MSG_CONTENT);

        Assert.assertEquals(1, client.getPendingMessageCount(qUrl));
    }

    @Test
    public void testNoPending() throws IOException {

        ISQSClient client = getClient();
        String qUrl = getQueueUrl();

        // Send a message, receive a message and then verify no pending
        client.sendMessage(qUrl, TEST_MSG_CONTENT);
        client.receiveMessage(qUrl);

        Assert.assertFalse(client.hasPendingMessages(qUrl));
    }

    @Test
    public void testPending() throws IOException {

        ISQSClient client = getClient();
        String qUrl = getQueueUrl();

        // Send a message and then verify pending
        client.sendMessage(qUrl, TEST_MSG_CONTENT);

        Assert.assertTrue(client.hasPendingMessages(qUrl));
    }

    @Test
    public void testReceive() throws IOException {

        ISQSClient client = getClient();
        String qUrl = getQueueUrl();

        // Send a message, receive a message and then verify no pending
        client.sendMessage(qUrl, TEST_MSG_CONTENT);
        Optional<Message> recvdMsg = client.receiveMessage(qUrl);

        Assert.assertEquals(TEST_MSG_CONTENT, recvdMsg.get().getBody());
    }

    @Test
    public void testEmptyReceive() throws IOException {

        ISQSClient client = getClient();
        String qUrl = getQueueUrl();

        Optional<Message> recvdMsg = client.receiveMessage(qUrl);

        Assert.assertFalse(recvdMsg.isPresent());
    }

    @Test
    public void testQueueDepth() throws IOException {

        ISQSClient client = getClient();
        String qUrl = getQueueUrl();

        int msgCnt = 0;
        for (msgCnt = 0; msgCnt < 5; msgCnt++) {
            client.sendMessage(qUrl, TEST_MSG_CONTENT + "-" + System.currentTimeMillis());
        }

        Assert.assertEquals(msgCnt, client.getPendingMessageCount(qUrl));
    }

    @Test
    public void testExistingQueueURL() throws IOException {

        ISQSClient client = getClient();
        String qUrl = getQueueUrl();

        Assert.assertEquals(qUrl, client.resolveQueueUrl(getQueueName()));
    }

    @Test
    public void testQueueExists() throws IOException {

        ISQSClient client = getClient();

        Assert.assertTrue(client.isQueueExists(getQueueName()));
    }

    @Test
    public void testQueueNotExists() throws IOException {

        ISQSClient client = getClient();

        Assert.assertFalse(client.isQueueExists(TEST_QUEUE_NOEXIST_NAME));
    }

    @Test
    public void testMultipleSendAndReceive() throws IOException {

        ISQSClient client = getClient();
        String qUrl = getQueueUrl();

        // Send in 10 messages and track the content
        java.util.List<String> msgs = new ArrayList<String>(10);
        for (int i = 0; i < 10; i++) {
            String msgContent = TEST_MSG_CONTENT + "-" + i;
            client.sendMessage(qUrl, msgContent);

            msgs.add(msgContent);
        }

        // Now, drain the Q and verify all were received
        java.util.List<Message> recvdMsgs = client.receiveMessages(qUrl);

        if ((recvdMsgs == null) || (recvdMsgs.size() != 10)) {
            Assert.fail("Invalid Number of Messages Received");
        }

        for (Message recvdMsg : recvdMsgs) {
        	for (String msg : msgs) {
        		if (recvdMsg.getBody().equals(msg)) {
        			msgs.remove(msg);        			
        		}
        	}
        }

        Assert.assertEquals(0, msgs.size());
    }
}
