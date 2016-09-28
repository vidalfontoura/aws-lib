/*
 * Copyright 2014, Charter Communications, All rights reserved.
 */
package com.charter.aesd.aws.ec2.client.api.impl;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.regions.Region;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.DescribeVpcsResult;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.UserIdGroupPair;
import com.amazonaws.services.ec2.model.Vpc;
import com.amazonaws.services.s3.AmazonS3Client;
import com.charter.aesd.aws.ec2.client.api.EC2Client;
import com.charter.aesd.aws.ec2.securitygroup.SecurityGroupQuery;
import com.charter.aesd.aws.enums.AWSAuthType;
import com.charter.aesd.aws.s3client.S3Client;
import org.apache.commons.collections4.CollectionUtils;
import org.mockito.Mockito;
import rx.Observable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;


/**
 * A stub AWS client for accessing EC2 resources within the AWS EC2 API.
 */
public class EC2ClientStub implements EC2Client {

    /**
     * Private method to invoke {@code Hystrix} command for each AWS EC2 APIO
     *
     * @param function
     * @return
     */
    private <T> Observable<T> invokeHystrixCommand(Supplier<T> function) {

        throw new UnsupportedOperationException();
    }


    /**
     * Returns new mock groups corresponding to queried group identifiers and names.
     *
     * @param query the query.
     * @return new mock groups corresponding to queried group identifiers and names.
     */
    @Override
    public Observable<SecurityGroup> describeSecurityGroups(final Optional<SecurityGroupQuery> query) {

        if (!query.isPresent()) {
            return Observable.empty();
        }

        SecurityGroupQuery securityGroupQuery = query.get();
        List<String> groupIds = securityGroupQuery.getGroupIds();
        if (CollectionUtils.isEmpty(groupIds)) {
            return Observable.empty();
        }

        List<SecurityGroup> securityGroups = new ArrayList<>();
        List<String> groupNames = securityGroupQuery.getGroupNames();

        for (int i = 0; i < groupIds.size(); i++) {
            SecurityGroup securityGroup = createSecurityGroup(groupIds.get(i));
            securityGroups.add(securityGroup);

            if (CollectionUtils.isEmpty(groupNames) || groupNames.size() <= i) {
                continue;
            }

            Mockito.when(securityGroup.getGroupName()).thenReturn(groupNames.get(i));
        }

        return Observable.from(securityGroups);
    }


    private SecurityGroup createSecurityGroup(@Nonnull final String groupId) {
        SecurityGroup securityGroup = Mockito.mock(SecurityGroup.class);
        Mockito.when(securityGroup.getGroupId()).thenReturn(groupId);
        Mockito.when(securityGroup.getIpPermissions()).thenReturn(createIpPermissions());
        Mockito.when(securityGroup.getIpPermissionsEgress()).thenReturn(createIpPermissions());

        return securityGroup;
    }


    private List<IpPermission> createIpPermissions() {
        List<IpPermission> ipPermissions = new ArrayList<>();
        IpPermission ipPermission = new IpPermission()
            .withIpProtocol("tcp")
            .withIpRanges("0.0.0.0/0")
            .withFromPort(0)
            .withToPort(65535);
        ipPermissions.add(ipPermission);

        return ipPermissions;
    }


    /**
     * Returns a new mock group with group identifier "1".
     *
     * @param groupName        the name of the group that should be created.
     * @param vpcId            the VPC in which the security group should be created.
     * @param groupDescription the description of the group that should be created.
     * @returns a new mock group with group identifier "1".
     */
    @Override
    public Observable<CreateSecurityGroupResult> createSecurityGroup(
        String groupName, String vpcId, Optional<String> groupDescription
    ) {

        CreateSecurityGroupResult createSecurityGroupResult = Mockito.mock(
            CreateSecurityGroupResult.class
        );
        Mockito.when(createSecurityGroupResult.getGroupId()).thenReturn("1");

        return Observable.just(createSecurityGroupResult);
    }


    @Override
    public Observable<Void> deleteSecurityGroup(String groupId) {

        return Observable.empty();
    }


    /**
     * Private method to construct {@link IpPermission} as part of a request to
     * authorize ingress or egress to a security group.
     *
     * @param fromPort           the port for which access should be granted from.
     * @param toPort             the port for which access should be granted to.
     * @param protocol           the protocol associated with the permission.
     * @param cidr               the cidr if it exists.
     * @param destinationGroupId the destinationGroupId if it exists.
     * @return {@link IpPermission}
     */
    private IpPermission constructPermission(
        int fromPort, int toPort, String protocol, Optional<String> cidr, Optional<String> destinationGroupId
    ) {

        IpPermission permission = new IpPermission()
            .withFromPort(fromPort)
            .withToPort(toPort)
            .withIpProtocol(protocol);

        if (cidr.isPresent()) {
            return permission.withIpRanges(cidr.get());
        }

        return permission.withUserIdGroupPairs(new UserIdGroupPair().withGroupId(destinationGroupId.orElse(null)));
    }


    @Override
    public Observable<Void> createSecurityGroupEgressRule(
        String groupId, int toPort, int fromPort, String protocol, Optional<String> cidr
        , Optional<String> destinationGroupId
    ) {

        return Observable.empty();
    }


    @Override
    public Observable<Void> createSecurityGroupIngressRule(
        String groupId, int toPort, int fromPort, String protocol, Optional<String> cidr
        , Optional<String> destinationGroupId
    ) {

        return Observable.empty();
    }


    @Override
    public Observable<Void> deleteSecurityGroupIngressRule(
        String groupId, int toPort, int fromPort, String protocol, Optional<String> cidr
        , Optional<String> destinationGroupId
    ) {

        return Observable.empty();
    }


    @Override
    public Observable<Void> deleteSecurityGroupEgressRule(
        String groupId, int toPort, int fromPort, String protocol, Optional<String> cidr
        , Optional<String> destinationGroupId
    ) {

        return Observable.empty();
    }


    /**
     * Returns new mock groups corresponding to queried group vpc's.
     *
     * @param vpcs the vpc's.
     * @return new mock groups corresponding to queried group vpc's.
     */
    @Override
    public Observable<DescribeVpcsResult> describeVpcs(Optional<List<String>> vpcs) {

        DescribeVpcsResult describeVpcsResult = Mockito.mock(DescribeVpcsResult.class);
        List<Vpc> vpcList = new ArrayList<>();
        if (vpcs.isPresent()) {
            vpcs.get().forEach(vpc -> {
                vpcList.add(new Vpc().withVpcId(vpc));
            });
        }
        Mockito.when(describeVpcsResult.getVpcs()).thenReturn(vpcList);

        return Observable.just(describeVpcsResult);
    }


    /**
     * Builder class for constructing an instance of {@link S3Client}
     */
    public static class Builder {

        private AWSAuthType authType;
        private String profileName;
        private String profileConfigFilePath;
        private String awsAccountKey;
        private String awsSecretKey;
        private ClientConfiguration config;
        private Region region;


        /**
         * Constructor for {@link AWSAuthType}
         *
         * @param authType {@link AWSAuthType}
         */
        public Builder(AWSAuthType authType) {

            this.authType = authType;
            this.config = new ClientConfiguration();
        }


        /**
         * Type of authentication used to talk to AWS
         *
         * @param authType
         * @return {@link Builder}
         */
        public Builder setAuthType(AWSAuthType authType) {

            this.authType = authType;
            return this;
        }


        /**
         * Sets the name of the profile specified in the profile config, and
         * used with an auth type of {@link AWSAuthType#PROFILE} <br />
         * <br />
         * Default value is <code>"default"</code>
         *
         * @param profileName
         * @return {@link Builder}
         */
        public Builder setProfileName(String profileName) {

            this.profileName = profileName;
            return this;
        }


        /**
         * Sets the physical location of the profile config, and used with an
         * auth type of {@link AWSAuthType#PROFILE} <br />
         * <br />
         * <p>
         * Default behavior loads the profile config from
         * <code>~/.aws/credentials</code>
         *
         * @param profileConfigFilePath
         * @return {@link Builder}
         */
        public Builder setProfileConfigFilePath(String profileConfigFilePath) {

            this.profileConfigFilePath = profileConfigFilePath;
            return this;
        }


        /**
         * Sets the {@link ClientConfiguration} used to configure the
         * {@link AmazonS3Client}
         *
         * @param config {@link ClientConfiguration}
         * @return {@link Builder}
         */
        public Builder setConfig(ClientConfiguration config) {

            this.config = config;
            return this;
        }


        /**
         * Sets the AWS Account Key used to configure the
         * {@link AmazonEC2Client}
         *
         * @param awsAccountKey {@code awsAccountKey}
         * @return {@link Builder}
         */
        public Builder setAwsAccountKey(String awsAccountKey) {

            this.awsAccountKey = awsAccountKey;
            return this;
        }


        /**
         * Sets the AWS Secret Key used to configure the {@link AmazonEC2Client}
         *
         * @param awsSecretKey {@code awsSecretKey}
         * @return {@link Builder}
         */
        public Builder setAwsSecretKey(String awsSecretKey) {

            this.awsSecretKey = awsSecretKey;
            return this;
        }


        /**
         * Sets the Region uses to configure the {@link AmazonEC2Client}
         *
         * @param region
         * @return {@link Builder}
         */
        public Builder setRegion(Region region) {

            this.region = region;
            return this;
        }


        public EC2Client build() {

            return new EC2ClientStub();
        }


        /**
         * Creates a {@code ClientConfiguration} object using the System
         * properties for {@code http.proxyHost} and {@code http.proxyPort}. To
         * leverage this both host and port must be set using the -D args (i.e.,
         * {@code -Dhttp.proxyHost=my.proxy.host.com -Dhttp.proxyPort=3128} and
         * if auth is required
         * {@code -Dhttp.proxyUser=username -Dhttp.proxyPassword=password1234}.
         *
         * @return A {@ClientConfiguration}. Never
         * {@code null}.
         */
        public ClientConfiguration getConfiguration() {

            String proxyHost = System.getProperty("http.proxyHost");
            String proxyPort = System.getProperty("http.proxyPort");
            String proxyUserName = System.getProperty("http.proxyUser");
            String proxyUserPasswd = System.getProperty("http.proxyPassword");

            if (proxyHost != null) {
                config.setProxyHost(proxyHost);
            }

            if (proxyPort != null) {
                config.setProxyPort(Integer.parseInt(proxyPort));
            }

            if (proxyUserName != null) {
                config.setProxyUsername(proxyUserName);
            }

            if (proxyUserPasswd != null) {
                config.setProxyPassword(proxyUserPasswd);
            }

            return config;
        }
    }

}
