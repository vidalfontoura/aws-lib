/*
 * Copyright 2014, Charter Communications, All rights reserved.
 */
package com.charter.aesd.aws.ec2.client.api;

import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.charter.aesd.aws.ec2.securitygroup.SecurityGroupQuery;

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
     * @return a {@link DescribeSecurityGroupsResult}
     */
    Observable<SecurityGroup> describeSecurityGroups(SecurityGroupQuery query);

}
