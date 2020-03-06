package com.charter.aesd.aws.sqsclient.util;

import com.amazonaws.auth.policy.Policy;
import com.amazonaws.auth.policy.Principal;
import com.amazonaws.auth.policy.Resource;
import com.amazonaws.auth.policy.Statement;
import com.amazonaws.auth.policy.actions.SQSActions;
import com.amazonaws.auth.policy.conditions.ArnCondition;
import com.amazonaws.auth.policy.conditions.ConditionFactory;

/**
 *
 * @author $Author: $
 * @version $Rev: $
 * @since ${date}
 */
public class AWSPolicyDelegate extends BaseSQSPolicy {

    /**
     *
     */
    public AWSPolicyDelegate(final String policyName, final String queueArn,
        final String topicArn) {

        super(policyName, queueArn, topicArn);
    }

    /**
     *
     */
    public String toJson() {

        Policy policy =
            new Policy().withStatements(new Statement(Statement.Effect.Allow)
                .withPrincipals(Principal.AllUsers)
                .withActions(SQSActions.SendMessage)
                .withResources(new Resource(getQueueArn()))
                .withConditions(
                    new ArnCondition(ArnCondition.ArnComparisonType.ArnEquals,
                        ConditionFactory.SOURCE_ARN_CONDITION_KEY,
                        getTopicArn())));
        policy.setId(getPolicyName());

        return policy.toJson();
    }
} // AWSPolicyDelegate
