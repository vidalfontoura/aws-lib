package com.charter.aesd.aws.sqsclient.util;

import com.charter.aesd.aws.sqsclient.ISQSPolicy;

/**
 *
 * @author $Author: $
 * @version $Rev: $
 * @since ${date}
 */
abstract public class BaseSQSPolicy implements ISQSPolicy {

    /**
     *
     */
    private String policyName = null;
    private String queueArn = null;
    private String topicArn = null;

    /**
     *
     */
    protected BaseSQSPolicy(final String policyName, final String queueArn,
        final String topicArn) {

        setPolicyName(policyName);
        setQueueArn(queueArn);
        setTopicArn(topicArn);
    }

    /**
     *
     * @return
     */
    public String getPolicyName() {

        return this.policyName;
    }

    /**
     *
     * @param pPolicyName
     */
    public void setPolicyName(final String pPolicyName) {

        this.policyName = pPolicyName;
    }

    /**
     *
     * @return
     */
    public String getQueueArn() {

        return this.queueArn;
    }

    /**
     *
     * @param pQueueArn
     */
    public void setQueueArn(final String pQueueArn) {

        this.queueArn = pQueueArn;
    }

    /**
     *
     * @return
     */
    public String getTopicArn() {

        return this.topicArn;
    }

    /**
     *
     * @param pTopicArn
     */
    public void setTopicArn(final String pTopicArn) {

        this.topicArn = pTopicArn;
    }
} // BaseSQSPolicy
