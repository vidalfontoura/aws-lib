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
     * Returns a {@link CreateSecurityGroupResult} that contains the result of trying to create
     * a security group for the supplied arguments.
     * 
     * @param groupName the name of the group that should be created.
     * @param vpcId the VPC in which the security group should be created.
     * @return a {@code Observable<CreateSecurityGroupResult>}
     */
    Observable<CreateSecurityGroupResult> createSecurityGroup(String groupName, String vpcId,
                                                              Optional<String> groupDescription);
    
    /**
     * Returns a {@link Void} when a security group associated with the group ID that is passed is deleted.
     * @param groupId the id of the group that is to be deleted.
     * @return a {@code Observable<CreateSecurityGroupResult>}
     */
    Observable<Void> deleteSecurityGroup(String groupId);

}
