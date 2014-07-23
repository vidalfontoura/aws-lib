package com.charter.aesd.aws.sqsclient;

/**
 * <p/>
 * User: matthewsmith Date: 7/22/14 Time: 3:26 PM
 *
 * @author $Author: $
 * @version $Rev: $
 * @since ${date}
 */
public interface ISNSTopicListener {
    /**
     *
     */
    void allowTopic(String queueUrl,
                    String topicArn);
} // ISNSTarget
/* @@_END: CLASS DEFINITION ------------------------------------------------ */
