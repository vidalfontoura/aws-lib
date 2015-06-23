/*
 * Copyright 2014, Charter Communications, All rights reserved.
 */
package com.charter.aesd.aws.ec2.client.api.impl;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.DescribeVpcsResult;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.UserIdGroupPair;
import com.amazonaws.services.ec2.model.Vpc;
import com.charter.aesd.aws.ec2.securitygroup.SecurityGroupQuery;
import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import rx.Observable;

@RunWith(JUnit4.class)
public class DescribeEC2ClientTest extends BaseEC2ClientTest {

    CreateSecurityGroupResult firstGrpResult;
    CreateSecurityGroupResult secondGrpResult;

    @Before
    @Override
    public void setUp() {

        super.setUp();
        Observable<CreateSecurityGroupResult> result =
            client.createSecurityGroup("ec2-security-group-test", "vpc-3a125f5f",
                Optional.of("Test EC2 Security Group"));
        Observable<CreateSecurityGroupResult> result2 =
            client.createSecurityGroup("ec2-security-group-test-2", "vpc-3a125f5f",
                Optional.of("Test EC2 Security Group 2"));
        firstGrpResult = result.toBlocking().first();
        secondGrpResult = result2.toBlocking().first();
    }

    @After
    public void tearDown() {

        if (firstGrpResult != null && secondGrpResult != null) {
            client.deleteSecurityGroup(firstGrpResult.getGroupId()).toBlocking().first();
            client.deleteSecurityGroup(secondGrpResult.getGroupId()).toBlocking().first();
        }
    }

    @Test
    public void testDescribeSecurityGroupsWithSingleGroup() {

        client
            .createSecurityGroupIngressRule(firstGrpResult.getGroupId(), 80, 80, "tcp", Optional.of("0.0.0.0/0"),
                Optional.empty()).toBlocking().first();
        client
            .createSecurityGroupEgressRule(firstGrpResult.getGroupId(), 8080, 8080, "tcp", Optional.empty(),
                Optional.of(secondGrpResult.getGroupId())).toBlocking().first();
        client
            .deleteSecurityGroupEgressRule(firstGrpResult.getGroupId(), 0, 65535, "-1", Optional.of("0.0.0.0/0"),
                Optional.empty()).toBlocking().first();

        SecurityGroupQuery query =
            SecurityGroupQuery.Builder.create().withGroupIds(ImmutableList.of(firstGrpResult.getGroupId())).build();
        Observable<SecurityGroup> groups = client.describeSecurityGroups(Optional.of(query));
        final IpPermission inboundPerm =
            new IpPermission().withFromPort(80).withToPort(80).withIpProtocol("tcp").withIpRanges("0.0.0.0/0");
        final IpPermission outboundPerm =
            new IpPermission()
                .withFromPort(8080)
                .withToPort(8080)
                .withIpProtocol("tcp")
                .withUserIdGroupPairs(
                    Collections.singleton(new UserIdGroupPair().withGroupId(secondGrpResult.getGroupId()).withUserId(
                        "460570964411")));
        assertGroup(groups.toBlocking().first(), "ec2-security-group-test", firstGrpResult.getGroupId(),
            "Test EC2 Security Group", inboundPerm, outboundPerm);
    }

    @Test
    public void testDescribeSecurityGroupsWithAllOutbound() {

        client
            .createSecurityGroupIngressRule(firstGrpResult.getGroupId(), 8080, 8080, "tcp", Optional.empty(),
                Optional.of(secondGrpResult.getGroupId())).toBlocking().first();

        SecurityGroupQuery query =
            SecurityGroupQuery.Builder.create().withGroupIds(ImmutableList.of(firstGrpResult.getGroupId())).build();
        Observable<SecurityGroup> groups = client.describeSecurityGroups(Optional.of(query));
        final IpPermission inboundPerm =
            new IpPermission()
                .withFromPort(8080)
                .withToPort(8080)
                .withIpProtocol("tcp")
                .withUserIdGroupPairs(
                    new UserIdGroupPair().withGroupId(secondGrpResult.getGroupId()).withUserId("460570964411"));
        final IpPermission outboundPerm = new IpPermission().withIpProtocol("-1").withIpRanges("0.0.0.0/0");
        assertGroup(groups.toBlocking().first(), "ec2-security-group-test", firstGrpResult.getGroupId(),
            "Test EC2 Security Group", inboundPerm, outboundPerm);
    }

    @Test
    public void testDescribeSecurityGroupsWithMultipleInboundCIDRs() {

        client
            .createSecurityGroupIngressRule(firstGrpResult.getGroupId(), 8080, 8080, "tcp", Optional.of("0.0.0.0/0"),
                Optional.empty()).toBlocking().first();
        client
            .createSecurityGroupIngressRule(firstGrpResult.getGroupId(), 8080, 8080, "tcp", Optional.of("1.0.0.0/32"),
                Optional.empty()).toBlocking().first();
        client
            .deleteSecurityGroupEgressRule(firstGrpResult.getGroupId(), 0, 65535, "-1", Optional.of("0.0.0.0/0"),
                Optional.empty()).toBlocking().first();

        SecurityGroupQuery query =
            SecurityGroupQuery.Builder.create().withGroupIds(ImmutableList.of(firstGrpResult.getGroupId())).build();
        Observable<SecurityGroup> groups = client.describeSecurityGroups(Optional.of(query));
        final IpPermission inboundPerm =
            new IpPermission().withFromPort(8080).withToPort(8080).withIpProtocol("tcp")
                .withIpRanges("0.0.0.0/0", "1.0.0.0/32");
        assertGroup(groups.toBlocking().first(), "ec2-security-group-test", firstGrpResult.getGroupId(),
            "Test EC2 Security Group", inboundPerm);
    }

    @Test
    public void testDescribeSecurityGroupsWithRemainingProtocols() {

        client
            .createSecurityGroupIngressRule(firstGrpResult.getGroupId(), 8080, 8080, "udp", Optional.of("0.0.0.0/0"),
                Optional.empty()).toBlocking().first();
        client
            .createSecurityGroupIngressRule(firstGrpResult.getGroupId(), -1, -1, "icmp", Optional.of("0.0.0.0/0"),
                Optional.empty()).toBlocking().first();
        client
            .deleteSecurityGroupEgressRule(firstGrpResult.getGroupId(), 0, 65535, "-1", Optional.of("0.0.0.0/0"),
                Optional.empty()).toBlocking().first();

        SecurityGroupQuery query =
            SecurityGroupQuery.Builder.create().withGroupIds(ImmutableList.of(firstGrpResult.getGroupId())).build();
        Observable<SecurityGroup> groups = client.describeSecurityGroups(Optional.of(query));
        final IpPermission inboundPerm =
            new IpPermission().withFromPort(8080).withToPort(8080).withIpProtocol("udp").withIpRanges("0.0.0.0/0");
        final IpPermission inboundPerm1 = new IpPermission().withIpProtocol("icmp").withIpRanges("0.0.0.0/0");
        assertGroup(groups.toBlocking().first(), "ec2-security-group-test", firstGrpResult.getGroupId(),
            "Test EC2 Security Group", ImmutableList.of(inboundPerm, inboundPerm1));
    }

    @Test
    public void testDescribeVpcs() {

        Observable<DescribeVpcsResult> result =
            client.describeVpcs(Optional.of(Collections.singletonList("vpc-3a125f5f")));
        DescribeVpcsResult describeResult = result.toBlocking().first();
        Vpc returnedVpc = describeResult.getVpcs().get(0);
        assertThat("VPC names matches", returnedVpc.getVpcId(), is("vpc-3a125f5f"));
        assertThat("Name tag matchees",
            returnedVpc.getTags().stream().anyMatch(obj -> obj.getValue().equals("dev_environment_vpc")), is(true));
    }

}
