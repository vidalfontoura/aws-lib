package com.charter.aesd.aws.snsclient.demo;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.sns.AmazonSNSClient;

import java.util.UUID;

/**
 * This class is meant to be self contained to demonstrate a slimmed
 * down class using the AWS SNS Client directly and limiting
 * dependencies.
 * <p/>
 * Very simple and straigtforward to possibly use as Stored Proc in Oracle
 */
public class RawSNS {
    /**
     *
     */
    private final static String AWS_ACCESS_KEY = "AKIAICIJW4N4A5MZUHVA";
    private final static String AWS_SECRET_KEY = "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";
    private final static String SNS_TOPIC_NAME = "QueuePOC-Demo-Topic";
    private final static String ENTITLEMENT_MESSAGE = "{" +
                    "  \"MessageId\": \"{messageId}\"," +
                    "  \"MessageName\": \"VideoEntitlements\"," +
                    "  \"AccountNumber\":  \"80092320357266\"," +
                    "  \"LastModified\": {lastModified}" +
                    "}";

    /**
     *
     */
    /**
     * No command line arguments
     */
    public static void main(String[] args) {
        // Raw Amazon SNS Interface
        AmazonSNSClient snsClient = new AmazonSNSClient(new AWSCredentials() {

            /**
             *
             * @return
             */
            @Override
            public String getAWSAccessKeyId() {

                return AWS_ACCESS_KEY;
            }

            @Override public String getAWSSecretKey() {

                return AWS_SECRET_KEY;
            }
        });

        // Publish messages
        int rc = 0;
        try {
            // The topic exists, but resolve the ARN
            String topicArn = snsClient.createTopic(SNS_TOPIC_NAME).getTopicArn();

            while(true) {
                String msg = ENTITLEMENT_MESSAGE.replaceAll("\\{messageId\\}",
                                                            UUID.randomUUID().toString())
                                                .replaceAll("\\{lastModified\\}",
                                                            Long.toString(System.currentTimeMillis()));

                snsClient.publish(topicArn,
                                  msg);
                System.out.println("SENT MESSAGE " + msg);

                Thread.sleep(1000);
            }
        } catch(InterruptedException e) {
            // No-op Ctrl-C
        } catch(Exception e) {
            e.printStackTrace(System.err);
            rc = 1;
        }

        System.exit(rc);
    }
} // RawSNS
