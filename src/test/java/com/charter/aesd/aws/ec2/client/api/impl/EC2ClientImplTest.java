/*
 * Copyright 2014, Charter Communications, All rights reserved.
 */
package com.charter.aesd.aws.ec2.client.api.impl;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.UserIdGroupPair;
import com.charter.aesd.aws.ec2.client.api.EC2Client;
import com.charter.aesd.aws.ec2.securitygroup.SecurityGroupQuery;
import com.charter.aesd.aws.enums.AWSAuthType;
import com.google.common.collect.ImmutableList;

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
            SecurityGroupQuery.Builder.create().withGroupIds(ImmutableList.of("sg-d243aab6")).build();
        Observable<SecurityGroup> groups = client.describeSecurityGroups(Optional.of(query));
        final IpPermission inboundPerm = new IpPermission().withFromPort(80).withToPort(80).withIpProtocol("tcp").withIpRanges("0.0.0.0/0");
        final IpPermission outboundPerm = new IpPermission().withFromPort(8080).withToPort(8080).withIpProtocol("tcp").withUserIdGroupPairs(new UserIdGroupPair().withGroupId("sg-8e52a6ea").withUserId("460570964411"));
        groups.forEach(new Action1<SecurityGroup>() {

            public void call(SecurityGroup obj) {

                assertGroup(obj, "ec2-security-group-test", "sg-d243aab6", "Test EC2 Security Group", inboundPerm,
                    outboundPerm);

            }

        });
    }
    
    @Test
    public void testDescribeSecurityGroupsWithAllOutbound() {

        SecurityGroupQuery query =
            SecurityGroupQuery.Builder.create().withGroupIds(ImmutableList.of("sg-8e52a6ea")).build();
        Observable<SecurityGroup> groups = client.describeSecurityGroups(Optional.of(query));
        final IpPermission inboundPerm = new IpPermission().withFromPort(8080).withToPort(8080).withIpProtocol("tcp").withUserIdGroupPairs(new UserIdGroupPair().withGroupId("sg-d243aab6").withUserId("460570964411"));
        final IpPermission outboundPerm = new IpPermission().withIpProtocol("-1").withIpRanges("0.0.0.0/0");
        groups.forEach(new Action1<SecurityGroup>() {

            public void call(SecurityGroup obj) {
                assertGroup(obj,"ec2-security-group-test-2","sg-8e52a6ea","Second Security Test group",inboundPerm,outboundPerm);
            }
        });
    }
    
    @Test
    public void testDescribeSecurityGroupsWithMultipleInboundCIDRs() {

        SecurityGroupQuery query =
            SecurityGroupQuery.Builder.create().withGroupIds(ImmutableList.of("sg-51946235")).build();
        Observable<SecurityGroup> groups = client.describeSecurityGroups(Optional.of(query));
        final IpPermission inboundPerm = new IpPermission().withFromPort(8080).withToPort(8080).withIpProtocol("tcp").withIpRanges("0.0.0.0/0","1.0.0.0/32");
        groups.forEach(new Action1<SecurityGroup>() {

            public void call(SecurityGroup obj) {
                assertGroup(obj,"ec2-security-group-test-3","sg-51946235","Third Test Security Group",inboundPerm);
            }
        });
    }
    
    @Test
    public void testDescribeSecurityGroupsWithRemainingProtocols() {

        SecurityGroupQuery query =
            SecurityGroupQuery.Builder.create().withGroupIds(ImmutableList.of("sg-10956374")).build();
        Observable<SecurityGroup> groups = client.describeSecurityGroups(Optional.of(query));
        final IpPermission inboundPerm = new IpPermission().withFromPort(8080).withToPort(8080).withIpProtocol("udp").withIpRanges("0.0.0.0/0");
        final IpPermission inboundPerm1 = new IpPermission().withIpProtocol("icmp").withIpRanges("0.0.0.0/0");
        groups.forEach(new Action1<SecurityGroup>() {

            public void call(SecurityGroup obj) {
                assertGroup(obj,"ec2-security-group-test-4","sg-10956374","Fourth Test Security Group",ImmutableList.of(inboundPerm,inboundPerm1));
            }
        });
    }
    
    private void assertGroup(SecurityGroup group, String name, String id, String description, IpPermission inbound) {

        assertThat("Group name matches", group.getGroupName(), is(name));
        assertThat("Group id matches", group.getGroupId(), is(id));
        assertThat("Group description matches", group.getDescription(), is(description));
        assertThat("Inbound permission matches", group.getIpPermissions().get(0), is(inbound));
    }
    
    private void assertGroup(SecurityGroup group, String name, String id, String description, List<IpPermission> inbound) {

        assertThat("Group name matches", group.getGroupName(), is(name));
        assertThat("Group id matches", group.getGroupId(), is(id));
        assertThat("Group description matches", group.getDescription(), is(description));
        inbound.containsAll(group.getIpPermissions());
    }
    
    private void assertGroup(SecurityGroup group, String name, String id, String description, IpPermission inbound, IpPermission outbound) {

        assertThat("Group name matches", group.getGroupName(), is(name));
        assertThat("Group id matches", group.getGroupId(), is(id));
        assertThat("Group description matches", group.getDescription(), is(description));
        assertThat("Inbound permission matches", group.getIpPermissions().get(0), is(inbound));
        assertThat("Outbound permission matches", group.getIpPermissionsEgress().get(0), is(outbound));
    }

}
