package com.charter.aesd.aws.sqsclient.util;

import com.charter.aesd.aws.sqsclient.ISQSPolicy;

/**
 * <p/>
 * User: matthewsmith Date: 7/23/14 Time: 2:38 PM
 *
 * @author $Author: $
 * @version $Rev: $
 * @since ${date}
 */
abstract public class BaseSQSPolicy implements ISQSPolicy {

    /**
     *
     */
    private String _policyName = null;
    private String _queueArn = null;
    private String _topicArn = null;

    /**
     *
     */
    protected BaseSQSPolicy (final String policyName,
                             final String queueArn,
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

        return _policyName;
    }

    /**
     *
     * @param pPolicyName
     */
    public void setPolicyName(final String pPolicyName) {

        _policyName = pPolicyName;
    }

    /**
     *
     * @return
     */
    public String getQueueArn() {

        return _queueArn;
    }

    /**
     *
     * @param pQueueArn
     */
    public void setQueueArn(final String pQueueArn) {

        _queueArn = pQueueArn;
    }

    /**
     *
     * @return
     */
    public String getTopicArn() {

        return _topicArn;
    }

    /**
     *
     * @param pTopicArn
     */
    public void setTopicArn(final String pTopicArn) {

        _topicArn = pTopicArn;
    }
} // BaseSQSPolicy
