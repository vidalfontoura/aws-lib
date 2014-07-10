package com.charter.aesd.aws.sqsclient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.amazonaws.AmazonClientException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.*;
import com.google.inject.Guice;

/**
 * Very simple SQS test / demo class.
 *
 * Connect to the Queue specified on the command line and flood
 *   it with the specified number of messages, wait for X seconds,
 *   then read the messages off the Queue and compare them.
 * <p>
 * The program does not validate its arguments.
 * <p>
 * An example command line (omitting class path for clarity) to produce-consume
 * with 1 producer and 2 consumers, batches of 10, for 20min is as follows:
 * <p>
 * {@code java com.charter.aesd.edgecache.test.SQSTester <accessKey> <secretKey> <endpointUrl> <queueName> <numMessages> [numProducers] }
 * <p>
 * The command line parameters are, in order:
 *
 * @param accessKey
 *          The AWS access key
 * @param secretKey
 *          The AWS secret key
 * @param endpointUrl
 *          The SQS region endpoint, i.e. https://sqs.us-east-1.amazonaws.com
 * @param queueName
 *          The name of the queue. The program assumes that the queue already
 *          exists.
 * @param numMessages
 *          The number of messages to test
 */
public class SQSTester {
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
   *
   * @param args
   * @throws InterruptedException
   */
  public static void main(String[] args)
    throws InterruptedException {
    if (args.length != 5) {
      System.err.println("USAGE:  java com.charter.aesd.edgecache.test.SQSTester " +
                         "<accessKey> " +
                         "<secretKey> " +
                         "<endpointUrl> " +
                         "<queueName> " +
                         "<numMessages> " +
                         "[numProducers]");

      System.exit(1);
    }

    int argIndex = 0;

    final String accessKey = args[argIndex++];
    final String secretKey = args[argIndex++];
    final String endpointUrl = args[argIndex++];
    String queueName = args[argIndex++];
    int numMessages = Integer.parseInt(args[argIndex++]);

    final AWSCredentials credentials = new BasicAWSCredentials(accessKey,
                                                               secretKey);
    final List<String> msgQueue = new ArrayList<String>();

    AmazonSQS sqsClient=null;
    String queueUrl=null;
    int rc=0;

    try {
      // configure the SQS client with 1 connection (1 producer THEN 1 consumer)
      queueName += Long.toString(System.currentTimeMillis());
      System.out.println("Creating Queue " + queueName + " at " + endpointUrl);

      sqsClient=new AmazonSQSClient(credentials,
                                    new ClientConfiguration().withMaxConnections(1));
      sqsClient.setEndpoint(endpointUrl);
      queueUrl=sqsClient.createQueue(new CreateQueueRequest().withQueueName(queueName)).getQueueUrl();

      // Build up the messages
      if (numMessages < 1) {
        numMessages=DEFAULT_NUM_MESSAGES;
      }

      for (int msgNum=0; msgNum < numMessages; msgNum++) {
        msgQueue.add(ENTITLEMENT_MESSAGE.replaceAll("\\{messageId\\}", 
        		UUID.randomUUID().toString()));
      }

      System.out.println("Sending " +
                         numMessages +
                         " to Queue, endpoint=" +
                         queueUrl);

      // Send the messages
      for (String msg : msgQueue) {
        sqsClient.sendMessage(new SendMessageRequest(queueUrl, msg));
        System.out.println("SENT Message " + msg);
      }

      System.out.println("Send COMPLETE, sleeping for " + DEFAULT_SLEEP_INTERVAL_MS + "ms");

      
      
      
      
      
      
      // Typically the producer would move onto other work here ....
      Thread.sleep(DEFAULT_SLEEP_INTERVAL_MS);
      
      
      
      
      
      

      // We are just going to read the messages back in from the Queue
      System.out.println("INITIATING Message Consumption");
      try {
        ReceiveMessageResult result=null;
        Message m;

        // Read in the Q'ed messages
        int msgsRead=0;
        while (true) {
          result=sqsClient.receiveMessage(new ReceiveMessageRequest(queueUrl));

          if (!result.getMessages().isEmpty()) {
            m=result.getMessages().get(0);

            // Processing the message
            String content=m.getBody();
            process(content);
            
            //Delete message
            sqsClient.deleteMessage(new DeleteMessageRequest(queueUrl, m.getReceiptHandle()));

            if (++msgsRead == numMessages) {
              break;
            }
          }
        }

        System.out.println("COMPLETED SUCCESSFULLY");
      } catch (Exception e) {
        // by default AmazonSQSClient retries calls 3 times before failing,
        // so, when this rare condition occurs, simply stop
        System.err.print("Message Consumption encountered error, msg=" + e.getMessage());
        e.printStackTrace(System.err);

        rc=1;
      }
    } finally {
      if ((sqsClient != null) &&
          (queueUrl != null)) {
        sqsClient.deleteQueue(queueUrl);
      }
    }

    System.exit(rc);
  }

    private static void process(String content) {
        System.out.println("RECV Message " + content);
    }
}
