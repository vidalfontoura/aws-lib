package com.charter.aesd.aws.snsclient.demo;

import com.amazonaws.services.sqs.model.Message;
import com.charter.aesd.aws.enums.AWSAuthType;
import com.charter.aesd.aws.snsclient.ISNSClient;
import com.charter.aesd.aws.snsclient.SNSClient;
import com.charter.aesd.aws.sqsclient.SQSClient;
import com.google.common.base.Optional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * <p/>
 * Simple Demo of publishing to an SNS topic and that notification being
 * delivered to multiple SQS Queue endpoints and read
 * <p/>
 * User: matthewsmith Date: 7/22/14 Time: 12:12 PM
 * 
 * @author $Author: $
 * @version $Rev: $
 * @since ${date}
 */
public class QueuePOC {

    /* @@_BEGIN: STATICS ----------------------------------------------------- */
    /**
     *
     */
    private final static String[] SQS_CONSUMER_NAMES = { "QueuePOC-Consumer-1", "QueuePOC-Consumer-2" };
    private final static String SNS_TOPIC_NAME = "QueuePOC-Demo-Topic";
    private final static String ENTITLEMENT_MESSAGE = "{\n" + "  \"MessageId\": \"{messageId}\",\n"
        + "  \"MessageName\": \"VideoEntitlements\",\n" + "  \"AccountNumber\":  \"80092320357266\",\n"
        + "  \"LastModified\": " + System.currentTimeMillis() + "\n" + "}";

    /* @@_END: STATICS ------------------------------------------------------- */

    /* @@_BEGIN: MEMBERS ----------------------------------------------------- */
    /* @@_END: MEMBERS ------------------------------------------------------- */

    /* @@_BEGIN: CONSTRUCTION ------------------------------------------------ */
    /* @@_END: CONSTRUCTION -------------------------------------------------- */

    /* @@_BEGIN: PROPERTIES -------------------------------------------------- */
    /* @@_END: PROPERTIES ---------------------------------------------------- */

    /* @@_BEGIN: METHODS ----------------------------------------------------- */
    /**
     * No command line arguments
     */
    public static void main(String[] args) {

        // Build the SNS Topic
        ISNSClient snsClient = new SNSClient.Builder(AWSAuthType.PROFILE).build();
        String topicArn = null;
        try {
            topicArn = snsClient.createTopic(SNS_TOPIC_NAME);
        } catch (Exception e) {
            System.err.println("ERROR Could Not Connect to Topic :: msg=" + e.getMessage());
            System.exit(1);
        }
        System.out.println("AWS SNS Topic AVAILABLE");

        SQSClient sqsClient = new SQSClient.Builder(AWSAuthType.PROFILE).build();

        // Start the Queues
        List<SQSSNSConsumer> sqsConsumers = new ArrayList<SQSSNSConsumer>();

        System.out.println("Starting AWS SQS Consumers");
        for (int i = 0; i < SQS_CONSUMER_NAMES.length; i++) {
            try {
                SQSSNSConsumer sqsConsumer = allocateConsumer(sqsClient, SQS_CONSUMER_NAMES[i]);

                // Attach the Consumer to the Topic
                sqsConsumers.add(sqsConsumer);
                sqsConsumer.start();

                System.out.println("Consumer " + SQS_CONSUMER_NAMES[i] + " STARTED");
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }

        // Publish messages
        int rc = 0;
        try {
            while (true) {
                String msg = ENTITLEMENT_MESSAGE.replaceAll("\\{messageId\\}", UUID.randomUUID().toString());
                snsClient.publishMessage(topicArn, msg);
                System.out.println("SENT MESSAGE " + msg);

                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            // No-op Ctrl-C
        } catch (Exception e) {
            e.printStackTrace(System.err);
            rc = 1;
        }

        System.exit(rc);
    }

    /**
     *
     */
    static SQSSNSConsumer allocateConsumer(final SQSClient qClient, final String consumerName) throws IOException {

        SQSSNSConsumer consumer = new SQSSNSConsumer(qClient, consumerName);

        consumer.init();

        return consumer;
    }

    /* @@_END: METHODS ------------------------------------------------------- */

    /**
     *
     */
    static class SQSSNSConsumer extends Thread {

        /**
         *
         */
        private SQSClient _sqsClient = null;
        private String _queueUrl = null;

        /**
         *
         */
        SQSSNSConsumer(final SQSClient qClient, final String name) {

            super(name);

            _sqsClient = qClient;
        }

        /**
         *
         */
        public String getQueueUrl() {

            return _queueUrl;
        }

        /**
         *
         */
        void init() throws IOException {

            _queueUrl = _sqsClient.resolveQueueUrl(getName());
            System.out.println("Queue " + getName() + " is available at URL " + _queueUrl);
        }

        /**
         *
         */
        void process(final String msg) {

            System.out.println("Consumer " + getName() + " Received " + msg);
        }

        /**
         *
         */
        @Override
        public void run() {

            System.out.println("Consumer " + getName() + " WAITING...");

            while (true) {
                try {
                    if (!_sqsClient.hasPendingMessages(_queueUrl)) {
                        System.out.println("Q " + _queueUrl + " has NO messages");
                        Thread.sleep(1000);

                        continue;
                    }

                    Optional<Message> msg = _sqsClient.receiveMessage(_queueUrl);
                    if (!msg.isPresent()) {
                        continue;
                    }

                    process(msg.get().getBody());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
} // SNSMultipleSQSDeliveryDemo
