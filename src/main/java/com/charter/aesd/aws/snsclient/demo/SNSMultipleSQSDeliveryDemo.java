package com.charter.aesd.aws.snsclient.demo;

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
 *   delivered to multiple SQS Queue endpoints and read
 * <p/>
 * The program does not validate its arguments.
 * <p/>
 * An example command line (omitting class path for clarity) to produce-consume
 *   with 1 SNS producer and X SQS consumers:
 * <p/>
 * {@code java om.charter.aesd.aws.snsclient.demo.SNSMultipleSQSDeliveryDemo <topicName> <numMessages> <numConsumers>}
 * <p/>
 * User: matthewsmith Date: 7/22/14 Time: 12:12 PM
 *
 * @author $Author: $
 * @version $Rev: $
 * @since ${date}
 */
public class SNSMultipleSQSDeliveryDemo {
    /* @@_BEGIN: STATICS ----------------------------------------------------- */
    /**
     *
     */
    private final static int DEFAULT_NUM_SQS_CONSUMERS = 5;
    private final static int DEFAULT_NUM_MESSAGES = 2;
    private final static String SQS_CONSUMER_NAME = "SQS-SNS-Consumer";
    private final static String ENTITLEMENT_MESSAGE = "{\n" +
                    "  \"MessageId\": \"{messageId}\",\n" +
                    "  \"MessageName\": \"VideoEntitlements\",\n" +
                    "  \"AccountNumber\":  [\"80092320357266\"],\n" +
                    "  \"LastModified\": \"2014-11-05T08:15:30-05:00\n" +
                    "}";


    /* @@_END: STATICS ------------------------------------------------------- */

    /* @@_BEGIN: MEMBERS ----------------------------------------------------- */
    /* @@_END: MEMBERS ------------------------------------------------------- */

    /* @@_BEGIN: CONSTRUCTION ------------------------------------------------ */
    /* @@_END: CONSTRUCTION -------------------------------------------------- */

    /* @@_BEGIN: PROPERTIES -------------------------------------------------- */
    /* @@_END: PROPERTIES ---------------------------------------------------- */

    /* @@_BEGIN: METHODS ----------------------------------------------------- */
    /**
     * @param args
     * The command line parameters are, in order:
     *
     * topicName - The name of the topic to publish.  The topic will be created if it does nto exist
     * numMessages - The number of messages to publish
     * numConsumers - The number of SQS Queues to create / attach as topic listeners
     */
    public static void main(String[] args) {

        if (args.length != 3) {
            System.err.println("USAGE:  java com.charter.aesd.aws.snsclient.SNSMultipleSQSDeliveryDemo <topicName> <numMessages> <numConsumers>");

            System.exit(1);
        }

        String topicName = args[0];
        if ((topicName == null) ||
            (topicName.length() == 0)) {
            System.err.println("ERROR:  Invalid Topic Name");

            System.exit(1);
        }

        int numMessages = -1;
        try {
            numMessages = Integer.parseInt(args[1]);
        } catch (Exception e) {
        }

        if (numMessages < 1) {
            numMessages = DEFAULT_NUM_MESSAGES;
        }

        int numSQSConsumers = -1;
        try {
            numSQSConsumers = Integer.parseInt(args[2]);
        } catch (Exception e) {
        }

        if (numSQSConsumers < 1) {
            numSQSConsumers = DEFAULT_NUM_SQS_CONSUMERS;
        }

        // Build the SNS Topic
        ISNSClient snsClient = new SNSClient.Builder().build();
        String topicArn = null;
        try {
            topicArn = snsClient.createTopic(topicName);
        } catch(Exception e) {
            System.err.println("ERROR Creating Topic :: msg=" + e.getMessage());
            System.exit(1);
        }
        System.out.println("AWS SNS Client AVAILABLE");

        SQSClient sqsClient = new SQSClient.Builder().build();

        // Start the Queues
        List<SQSSNSConsumer> sqsConsumers = new ArrayList<SQSSNSConsumer>();

        System.out.println("Starting " + numSQSConsumers + " AWS SQS Consumers");
        for (int qIdx=0; qIdx<numSQSConsumers; qIdx++) {
            try {
                String consumerName = SQS_CONSUMER_NAME + "-" + System.currentTimeMillis();
                SQSSNSConsumer sqsConsumer = allocateConsumer(sqsClient,
                                                              consumerName,
                                                              numMessages);

                // Update the Queue's Policy to allow SNS sends from the topic
                sqsClient.allowTopic(sqsConsumer.getQueueUrl(),
                                topicArn);

                // Attach the Consumer to the Topic
                snsClient.subscribeToTopic(topicArn,
                                           sqsConsumer.getQueueArn());
                System.out.println("Consumer " + consumerName + " SUBSCRIBED TO TOPIC ARN=" + topicArn);

                sqsConsumers.add(sqsConsumer);

                sqsConsumer.start();

                System.out.println("Consumer " + consumerName + " STARTED");
            } catch(Exception e) {
                e.printStackTrace(System.err);
            }
        }

        // Publish messages
        try {
            while(true) {
                String msg = ENTITLEMENT_MESSAGE.replaceAll("\\{messageId\\}",
                                                            UUID.randomUUID().toString());
                snsClient.publishMessage(topicArn,
                                         msg);
                System.out.println("SENT MESSAGE " + msg);

                Thread.sleep(1000);
            }

        } catch(Exception e) {
            e.printStackTrace(System.err);
        }

        // Wait for processing...
        for (SQSSNSConsumer sqsConsumer : sqsConsumers) {
            try {
                sqsConsumer.join();
            } catch(Exception e) {

            }
        }

        // Clean up the topic and consumers
        try {
            snsClient.deleteTopic(topicArn);
        } catch(Exception e) {
            e.printStackTrace(System.err);
        }

        for (SQSSNSConsumer consumer : sqsConsumers) {
            try {
                consumer.cleanup();
            } catch(Exception e) {

            }
        }
    }

    /**
     *
     */
    static SQSSNSConsumer allocateConsumer(final SQSClient qClient,
                                           final String consumerName,
                                           final int numMessages) throws IOException {
        SQSSNSConsumer consumer = new SQSSNSConsumer(qClient,
                                                     consumerName,
                                                     numMessages);

        consumer.init();

        return consumer;
    }
    /* @@_END: METHODS ------------------------------------------------------- */

    /**
     *
     */
    static class SQSSNSConsumer
        extends Thread {
        /**
         *
         */
        private SQSClient _sqsClient = null;
        private String _queueUrl = null;
        private int _expectedMessages = -1;

        /**
         *
         */
        SQSSNSConsumer(final SQSClient qClient,
                       final String name,
                       final int numMessagesToProcess) {
            super(name);

            _expectedMessages = numMessagesToProcess;
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
        public String getQueueArn() {
            return _sqsClient.resolveQueueARN(_queueUrl);
        }

        /**
         *
         */
        void init() throws IOException {
            _queueUrl = _sqsClient.createQueue(getName());
            System.out.println("Queue " + getName() + " is available at URL " + _queueUrl);
        }

        /**
         *
         */
        void cleanup() throws IOException {
            _sqsClient.deleteQueue(_queueUrl);
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
        public void run() {
            System.out.println("Consumer " + getName() + " WAITING...");

            int numMsgsRecvd = 0;
            while(numMsgsRecvd < _expectedMessages) {
                try {
                    System.out.println("Checking for Messages on Q " + _queueUrl);
                    if (!_sqsClient.hasPendingMessages(_queueUrl)) {
                        System.out.println("Q " + _queueUrl + " has NO messages");
                        Thread.sleep(1000);

                        continue;
                    }

                    System.out.println("Processing Messages on Q " + _queueUrl);
                    Optional<String> msg = _sqsClient.receiveMessage(_queueUrl);

                    if (msg.isPresent()) {
                        process(msg.get());
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }

                numMsgsRecvd++;
            }

            System.out.println("Consumer " + getName() + "FINISHED");
        }
    }
} // SNSMultipleSQSDeliveryDemo
