package com.charter.aesd.aws.sqsclient;

/**
 * <p/>
 * Setup to aloow the consumption/listening to a SNS Topic
 * <p/>
 *
 * @author $Author: $
 * @version $Rev: $
 * @since ${date}
 */
public interface ISNSTopicListener {

    /**
     * @param queueUrl {@code String} the url that resolves to the Queue
     *        instance in the Service Provider space.
     * @param topicArn {@code String} the arn that resolves to the Topic
     *        instance in the Service Provider space.
     */
    void allowTopic(String queueUrl, String topicArn);
} // ISNSTopicListener
