/*
 * Copyright 2015, Charter Communications, All rights reserved.
 */
package com.charter.aesd.aws.sqsclient;

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchResult;
import com.amazonaws.services.sqs.model.SendMessageBatchResultEntry;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.google.common.base.Optional;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stub implementation of ISQSClient
 *
 * @author mpaliari
 */
public class SQSClientStub implements ISQSClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(SQSClientStub.class);

    private final List<Message> stubQueue = new ArrayList<>();

    private String QUEUE_NAME = "stubQueue";

    @Override
    public void allowTopic(String queueUrl, String topicArn) {

        // TODO Implement when necessary
    }

    @Override
    public boolean isQueueExists(String queueName) {

        return StringUtils.isNotBlank(queueName) && QUEUE_NAME.equals(queueName);
    }

    @Override
    public String resolveQueueUrl(String queueName) {

        return StringUtils.isNotBlank(queueName) && QUEUE_NAME.equals(queueName) ? queueName : null;
    }

    @Override
    public String createQueue(String queueName) throws IOException {

        QUEUE_NAME = queueName;
        return queueName;
    }

    @Override
    public void deleteQueue(String queueUrl) throws IOException {

        stubQueue.clear();
    }

    @Override
    public boolean hasPendingMessages(String queueUrl) {

        return !stubQueue.isEmpty();
    }

    @Override
    public int getPendingMessageCount(String queueUrl) {

        return stubQueue.size();
    }

    @Override
    public SendMessageResult sendMessage(String queueUrl, String content) throws IOException {

        BigInteger hash = null;
        try {

            MessageDigest digest = MessageDigest.getInstance("MD5");

            byte[] hashMd5 = digest.digest(content.getBytes());
            hash = new BigInteger(1, hashMd5);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Error to encript the message content in SQSClientStub");
        }

        String messageId = UUID.randomUUID().toString();
        String receiptHandle = UUID.randomUUID().toString();
        String md5Content = hash != null ? hash.toString(16) : content;

        Message message = new Message();
        message.setBody(content);
        message.setMD5OfBody(md5Content);
        message.setMessageId(messageId);
        message.setReceiptHandle(receiptHandle);

        stubQueue.add(message);

        SendMessageResult result = new SendMessageResult();
        result.setMessageId(messageId);
        result.setMD5OfMessageBody(md5Content);
        return result;
    }

    @Override
    public SendMessageBatchResult sendMessages(String queueUrl, List<String> content) {

        SendMessageBatchResult result = new SendMessageBatchResult();
        List<SendMessageBatchResultEntry> entries = new ArrayList<>();

        for (String messageContent : content) {
            SendMessageBatchResultEntry entry = new SendMessageBatchResultEntry();

            try {
                SendMessageResult sendMessageResult = sendMessage(queueUrl, messageContent);

                entry.setMessageId(sendMessageResult.getMessageId());
                entry.setMD5OfMessageBody(sendMessageResult.getMD5OfMessageBody());
            } catch (IOException e) {
                LOGGER.error("Error to add a message in SQSClientStub");
            }

            entries.add(entry);
        }

        result.setSuccessful(entries);
        return result;
    }

    @Override
    public Optional<Message> receiveMessage(String queueUrl) throws IOException {

        return !stubQueue.isEmpty() ? Optional.of(stubQueue.get(0)) : Optional.absent();
    }

    @Override
    public List<Message> receiveMessage(ReceiveMessageRequest request) throws IOException {

        return stubQueue;
    }

    @Override
    public List<Message> receiveMessages(String queueUrl) throws IOException {

        return stubQueue;
    }

    @Override
    public void deleteMessage(String queueUrl, String receiptHandle) {

        Message deletedMessage = null;
        for (Message message : stubQueue) {
            if (message.getReceiptHandle().equals(receiptHandle)) {
                deletedMessage = message;
                break;
            }
        }

        if (deletedMessage != null) {
            stubQueue.remove(deletedMessage);
        }
    }
}
