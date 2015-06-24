/*
 * Copyright 2015, Charter Communications, All rights reserved.
 */
package com.charter.aesd.aws.ec2.client.api.impl;

import static org.junit.Assert.assertNotNull;

import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;

import java.util.Optional;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import rx.Observable;

@RunWith(JUnit4.class)
public class CreateEC2ClientTest extends BaseEC2ClientTest {

    CreateSecurityGroupResult createResult;

    @After
    public void tearDown() {

        if (createResult != null)
            client.deleteSecurityGroup(createResult.getGroupId()).toBlocking().single();
    }

    @Test
    public void testCreateSecurityGroupAndDelete() {

        Observable<CreateSecurityGroupResult> result =
            client.createSecurityGroup("test1234", "vpc-3a125f5f", Optional.of("test group desc"));
        createResult = result.toBlocking().single();
        assertNotNull(createResult.getGroupId());

    }

    @Test
    public void testCreateSecurityGroupIngressRuleAndDelete() {

        Observable<CreateSecurityGroupResult> result =
            client.createSecurityGroup("test1234", "vpc-3a125f5f", Optional.of("test group desc"));
        createResult = result.toBlocking().single();
        assertNotNull(createResult.getGroupId());

        client
            .createSecurityGroupIngressRule(createResult.getGroupId(), 8080, 8080, "tcp", Optional.empty(),
                Optional.of(createResult.getGroupId())).toBlocking().single();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateSecurityGroupIngressRuleAndDeleteError() {

        client.createSecurityGroupIngressRule("1234", 8080, 8080, "tcp", Optional.empty(), Optional.empty())
            .toBlocking().single();
    }

    @Test
    public void testCreateSecurityGroupEgressRuleAndDelete() {

        Observable<CreateSecurityGroupResult> result =
            client.createSecurityGroup("test1234", "vpc-3a125f5f", Optional.of("test group desc"));
        createResult = result.toBlocking().single();
        assertNotNull(createResult.getGroupId());

        client
            .createSecurityGroupEgressRule(createResult.getGroupId(), 8080, 8080, "tcp", Optional.of("0.0.0.0/0"),
                Optional.empty()).toBlocking().single();
    }

}
