package com.charter.aesd.aws.sqsclient.demo;

import com.charter.aesd.aws.sqsclient.SQSClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * <p/>
 * Very simple SQS test / demo class.
 * <p/>
 * Connect to the Queue specified on the command line and flood it with the specified number of messages, wait for X
 * seconds, then read the messages off the Queue and compare them.
 * <p/>
 * The program does not validate its arguments.
 * <p/>
 * An example command line (omitting class path for clarity) to produce-consume with 1 producer and 1 consumer:
 * <p/>
 * {@code java om.charter.aesd.aws.sqsclient.SQSDemo <queueName> <numMessages> }
 * <p/>
 *
 * @author $Author: $
 * @version $Rev: $
 * @since ${date}
 */
public class SQSDemo {

    /**
     *
     */
    private final static int DEFAULT_NUM_MESSAGES = 1;
    private final static int DEFAULT_SLEEP_INTERVAL_MS = 2000;
    private final static String ENTITLEMENT_MESSAGE = "{\n" +
                    "  \"MessageId\": \"{messageId}\",\n" +
                    "  \"MessageName\": \"VideoEntitlements\",\n" +
                    "  \"AccountNumber\":  [\"80092320357266\"],\n" +
                    "  \"LastModified\": \"2014-11-05T08:15:30-05:00\n" +
                    "}";

    /**
     * @param args
     * The command line parameters are, in order:
     *
     * queueName - The name of the queue. The program assumes that the queue already exists.
     * numMessages - The number of messages to test
     *
     * @throws InterruptedException
     */
    public static void main(String[] args)
                    throws InterruptedException {

        if (args.length != 2) {
            System.err.println("USAGE:  java com.charter.aesd.aws.sqsclient.SQSDemo <queueName> <numMessages>");

            System.exit(1);
        }

        String queueName = args[0];
        int numMessages = -1;
        try {
            numMessages = Integer.parseInt(args[1]);
        } catch (Exception e) {
        }

        if (numMessages < 1) {
            numMessages = DEFAULT_NUM_MESSAGES;
        }

        final List<String> msgQueue = new ArrayList<String>();
        SQSClient sqsClient = new SQSClient.Builder().build();
        String queueUrl = null;
        int rc = 0;

        try {
            // configure the SQS client with 1 connection (1 producer THEN 1 consumer)
            queueName += Long.toString(System.currentTimeMillis());
            System.out.println("Creating Queue " + queueName);

            queueUrl = sqsClient.createQueue(queueName);

            // Build up the messages
            if (numMessages < 1) {
                numMessages = DEFAULT_NUM_MESSAGES;
            }

            for (int msgNum = 0; msgNum < numMessages; msgNum++) {
                msgQueue.add(ENTITLEMENT_MESSAGE.replaceAll("\\{messageId\\}",
                                UUID.randomUUID().toString()));
            }

            System.out.println("Sending " +
                            numMessages +
                            " to Queue, endpoint=" +
                            queueUrl);

            // Send the messages
            for (String msg : msgQueue) {
                sqsClient.sendMessage(queueUrl,
                                msg);

                System.out.println("SENT Message " +
                                msg);
            }

            System.out.println("Send COMPLETE, sleeping for " +
                            DEFAULT_SLEEP_INTERVAL_MS +
                            "ms");

            // Typically the producer would move onto other work here ....
            Thread.sleep(DEFAULT_SLEEP_INTERVAL_MS);

            // We are just going to read the messages back in from the Queue
            System.out.println("INITIATING Message Consumption");
            int numRecvd = 0;
            while (numRecvd < numMessages) {
                String msg = sqsClient.receiveMessage(queueUrl);
                process(msg);

                numRecvd++;
            }

            System.out.println("COMPLETED SUCCESSFULLY.");
        } catch (Exception e) {
            System.err.print("Error Processing Messages on Queue " + queueName + ", msg=" + e.getMessage());
            e.printStackTrace(System.err);

            rc = 1;
        } finally {
            if (sqsClient != null) {
                try {
                    sqsClient.deleteQueue(queueUrl);
                } catch (IOException e) {

                }
            }
        }

        System.exit(rc);
    }

    /**
     * @param content {@code String} derived processing of the message
     *                               body
     */
    private static void process(final String content) {

        System.out.println("RECV Message " + content);
    }
}
