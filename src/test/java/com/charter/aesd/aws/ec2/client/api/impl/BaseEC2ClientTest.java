/*
 * Copyright 2015, Charter Communications, All rights reserved.
 */
package com.charter.aesd.aws.ec2.client.api.impl;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.charter.aesd.aws.ec2.client.api.EC2Client;
import com.charter.aesd.aws.enums.AWSAuthType;

import java.util.List;

import org.junit.Before;

public abstract class BaseEC2ClientTest {

    EC2Client client;

    @Before
    public void setUp() {

        client =
            Boolean.getBoolean("use.iam.role") ? new EC2ClientImpl.Builder(AWSAuthType.INSTANCE_ROLE).build()
                : new EC2ClientImpl.Builder(AWSAuthType.PROFILE).setProfileName("ec2client-test")
                    .setRegion(Region.getRegion(Regions.fromName("us-east-1"))).build();
    }

    void assertGroup(SecurityGroup group, String name, String id, String description, IpPermission inbound) {

        assertThat("Group name matches", group.getGroupName(), is(name));
        assertThat("Group id matches", group.getGroupId(), is(id));
        assertThat("Group description matches", group.getDescription(), is(description));
        assertThat("Inbound permission matches", group.getIpPermissions().get(0), is(inbound));
    }

    void assertGroup(SecurityGroup group, String name, String id, String description, List<IpPermission> inbound) {

        assertThat("Group name matches", group.getGroupName(), is(name));
        assertThat("Group id matches", group.getGroupId(), is(id));
        assertThat("Group description matches", group.getDescription(), is(description));
        inbound.containsAll(group.getIpPermissions());
    }

    void assertGroup(SecurityGroup group, String name, String id, String description, IpPermission inbound,
                     IpPermission outbound) {

        assertThat("Group name matches", group.getGroupName(), is(name));
        assertThat("Group id matches", group.getGroupId(), is(id));
        assertThat("Group description matches", group.getDescription(), is(description));
        assertThat("Inbound permission matches", group.getIpPermissions().get(0), is(inbound));
        assertThat("Outbound permission matches", group.getIpPermissionsEgress().get(0), is(outbound));
    }

}
