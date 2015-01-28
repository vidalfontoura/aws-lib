/*
 * Copyright 2014, Charter Communications, All rights reserved.
 */
package com.charter.aesd.aws.ec2.client.api;

import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.charter.aesd.aws.ec2.securitygroup.SecurityGroupQuery;

import java.util.Optional;

import rx.Observable;

/**
 * Set of germane messages that are available to be performed against AWS EC2
 * API.
 * 
 * @author jappel
 */
public interface EC2Client {

    /**
     * Returns a result object that contains the germane list of security groups
     * based on the state of {@link SecurityGroupQuery} object.
     * 
     * @param query represents the query that is to be performed against the AWS
     *        EC2 environment.
     * @return a {@code Observable<SecurityGroup>}
     */
    Observable<SecurityGroup> describeSecurityGroups(Optional<SecurityGroupQuery> query);

    /**
     * Returns a {@link CreateSecurityGroupResult} that contains the result of
     * trying to create a security group for the supplied arguments.
     * 
     * @param groupName the name of the group that should be created.
     * @param vpcId the VPC in which the security group should be created.
     * @return a {@code Observable<CreateSecurityGroupResult>}
     */
    Observable<CreateSecurityGroupResult> createSecurityGroup(String groupName, String vpcId,
                                                              Optional<String> groupDescription);

    /**
     * Deletes a security group associated with the group ID that is passed is
     * deleted.
     * 
     * @param groupId the id of the group that is to be deleted.
     * @return a {@code Observable<CreateSecurityGroupResult>}
     */
    Observable<Void> deleteSecurityGroup(String groupId);

    /**
     * Creates a security group rule based on the supplied arguments. The cidr
     * and destinationGroupId arguments are mutually exclusive and one of them
     * is required to be present.
     * 
     * @param groupId the source group id for the rule
     * @param toPort the port to which the rule allows access
     * @param fromPort the port from which the rule access
     * @param protocol the protocol associated with the rule
     * @param cidr the CIDR IP address range to be associated if present.
     * @param destinationGroupId the ID of the destination security group Id
     * @throw IllegalArgumentException if both the cidr and destinationGroupId
     *        arguments are not present.
     * @return {@code Observable<Void>}
     */
    Observable<Void> createSecurityGroupEgressRule(String groupId, int toPort, int fromPort, String protocol,
                                                   Optional<String> cidr, Optional<String> destinationGroupId);

    /**
     * Creates a security group rule based on the supplied arguments. The cidr
     * and destinationGroupId arguments are mutually exclusive and one of them
     * is required to be present.
     * 
     * @param groupId the source group id for the rule
     * @param toPort the port to which the rule allows access
     * @param fromPort the port from which the rule access
     * @param protocol the protocol associated with the rule
     * @param cidr the CIDR IP address range to be associated if present.
     * @param destinationGroupId the ID of the destination security group Id
     * @throw IllegalArgumentException if both the cidr and destinationGroupId
     *        arguments are not present.
     * @return {@code Observable<Void>}
     */
    Observable<Void> createSecurityGroupIngressRule(String groupId, int toPort, int fromPort, String protocol,
                                                    Optional<String> cidr, Optional<String> destinationGroupId);

}
