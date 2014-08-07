package com.charter.aesd.aws.snsclient.demo;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This class is meant to be a very slimmed down implementation
 *   that will read from a set of SQS Queues to demo message retrieval
 *   when messages are injected to the topic externally.
 * <p/>
 */
public class SNSReader {
    /**
     *
     */
    private final static String AWS_ACCESS_KEY = "AKIAICIJW4N4A5MZUHVA";
    private final static String AWS_SECRET_KEY = "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";
    private final static String[] SQS_CONSUMER_NAMES = {
      "QueuePOC-Consumer-1",
      "QueuePOC-Consumer-2"
    };

    /**
     * No command line arguments
     */
    public static void main(String[] args) {
        AmazonSQSClient sqsClient = new AmazonSQSClient(new AWSCredentials() {

            /**
             *
             * @return
             */
            @Override
            public String getAWSAccessKeyId() {

                return AWS_ACCESS_KEY;
            }

            /**
             *
             * @return
             */
            @Override
            public String getAWSSecretKey() {

                return AWS_SECRET_KEY;
            }
        });

        // Start the Queues
        List<SQSSNSConsumer> sqsConsumers = new ArrayList<SQSSNSConsumer>();

        System.out.println("Starting AWS SQS Consumers");
        for (int i=0; i<SQS_CONSUMER_NAMES.length; i++) {
            try {
                SQSSNSConsumer sqsConsumer = allocateConsumer(sqsClient,
                                                              SQS_CONSUMER_NAMES[i]);

                // Attach the Consumer to the Topic
                sqsConsumers.add(sqsConsumer);
                sqsConsumer.start();

                System.out.println("Consumer " + SQS_CONSUMER_NAMES[i] + " STARTED");
            } catch(Exception e) {
                e.printStackTrace(System.err);
            }
        }

        // Wait on consumers
        try {
            sqsConsumers.get(0).join();
        } catch(Exception e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     *
     */
    static SQSSNSConsumer allocateConsumer(final AmazonSQSClient qClient,
                                           final String consumerName)
        throws IOException {
        SQSSNSConsumer consumer = new SQSSNSConsumer(qClient,
                                                     consumerName);

        consumer.init();

        return consumer;
    }

    /**
     *
     */
    static class SQSSNSConsumer
        extends Thread {
        /**
         *
         */
        private AmazonSQSClient sqsClient;
        private String _queueUrl = null;

        /**
         *
         */
        SQSSNSConsumer(final AmazonSQSClient qClient,
                       final String name) {
            super(name);

            sqsClient = qClient;
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
            _queueUrl = sqsClient.getQueueUrl(getName()).getQueueUrl();
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
        public void run() {
            System.out.println("Consumer " + getName() + " WAITING...");

            List<Message> msgs = null;
            while(true) {
                try {
                    msgs = sqsClient.receiveMessage(getQueueUrl()).getMessages();
                    if (msgs != null) {
                        for (Message msg : msgs) {
                            process(msg.getBody());
                        }
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
} // SNSReader
