package com.charter.aesd.aws.sqsclient.util;

/**
 * <p/>
 * User: matthewsmith Date: 7/23/14 Time: 2:28 PM
 *
 * @author $Author: $
 * @version $Rev: $
 * @since ${date}
 */
public class DefaultSNSSQSPolicy extends BaseSQSPolicy {

    /**
     *
     */
    private final static String ALLOW_SNS_PUBLISH_POLICY = "{\n" +
      "\"Version\":\"2012-10-17\",\n" +
      "\"Statement\": [\n" +
      "  {\n" +
      "    \"Sid\": \"{SQS_POLICY_NAME}\",\n" +
      "    \"Effect\": \"Allow\",\n" +
      "    \"Principal\": {\n" +
      "      \"AWS\": \"*\"\n" +
      "    },\n" +
      "    \"Action\": \"sqs:SendMessage\",\n" +
      "    \"Resource\": \"{SQS_QUEUE_ARN}\",\n" +
      "    \"Condition\": {\n" +
      "      \"ArnEquals\": {\n" +
      "        \"aws:SourceArn\": \"{SNS_TOPIC_ARN}\"\n" +
      "      }\n" +
      "    }\n" +
      "  }\n" +
      "]\n" +
    "}";

    /**
     *
     */
    public DefaultSNSSQSPolicy(final String policyName,
                               final String queueArn,
                               final String topicArn) {
        super(policyName,
              queueArn,
              topicArn);
    }

    /**
     *
     */
    public String toJson() {
        return ALLOW_SNS_PUBLISH_POLICY.replaceAll("\\{SQS_POLICY_NAME\\}", getPolicyName())
                                       .replaceAll("\\{SQS_QUEUE_ARN\\}", getQueueArn())
                                       .replaceAll("\\{SNS_TOPIC_ARN\\}", getTopicArn());
    }
} // DefaultSNSSQSPolicy
