/*
 * Copyright 2014, Charter Communications, All rights reserved.
 */
package com.charter.aesd.aws.ec2.client.api.impl;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import com.amazonaws.services.ec2.model.SecurityGroup;
import com.charter.aesd.aws.ec2.client.api.EC2Client;
import com.charter.aesd.aws.ec2.securitygroup.SecurityGroupQuery;
import com.charter.aesd.aws.enums.AWSAuthType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import rx.Observable;
import rx.functions.Action1;

public class EC2ClientImplTest {

    private EC2Client client;

    @Before
    public void setUp() {

        client = new EC2ClientImpl.Builder(AWSAuthType.PROFILE).setProfileName("ec2client-test").build();
    }

    @Test
    public void testDescribeSecurityGroupsWithSingleGroup() {

        SecurityGroupQuery query =
            SecurityGroupQuery.Builder.create()
                .withGroupIds(ImmutableList.of("sg-d243aab6"))
                .build();
        Observable<SecurityGroup> groups = client.describeSecurityGroups(Optional.of(query));
        List<SecurityGroup> group = Lists.newArrayListWithCapacity(1);
        groups.single().subscribe(new Action1<SecurityGroup>() {

            public void call(SecurityGroup t1) {

                group.add(t1);
            }
            
        });
        SecurityGroup actualGroup = group.get(0);
        assertGroup(actualGroup);
    }
    
    private void assertGroup(SecurityGroup group) {
        assertThat("Group name matches", group.getGroupName(), is("ec2-security-group-test"));
        assertThat("Group id matches", group.getGroupId(), is("sg-d243aab6"));
        assertThat("Group description matches", group.getDescription(), is("Test EC2 Security Group"));
    }

}
