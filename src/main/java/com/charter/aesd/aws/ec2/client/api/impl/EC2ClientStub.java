/*
 * Copyright 2014, Charter Communications, All rights reserved.
 */
package com.charter.aesd.aws.ec2.client.api.impl;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.regions.Region;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupEgressRequest;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.DescribeVpcsRequest;
import com.amazonaws.services.ec2.model.DescribeVpcsResult;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.RevokeSecurityGroupEgressRequest;
import com.amazonaws.services.ec2.model.RevokeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.UserIdGroupPair;
import com.amazonaws.services.ec2.model.Vpc;
import com.amazonaws.services.s3.AmazonS3Client;
import com.charter.aesd.aws.ec2.client.api.EC2Client;
import com.charter.aesd.aws.ec2.command.EC2Command;
import com.charter.aesd.aws.ec2.securitygroup.SecurityGroupQuery;
import com.charter.aesd.aws.enums.AWSAuthType;
import com.charter.aesd.aws.s3client.S3Client;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;


/**
 * AWS client for accessing EC2 resources within the AWS EC2 API<br />
 * Use {@link EC2ClientStub.Builder} to construct an instance of
 * {@link EC2Client}
 *
 * @author jappel
 */
public class EC2ClientStub implements EC2Client {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().getClass());

    /**
     * Private method to invoke {@code Hystrix} command for each AWS EC2 APIO
     * 
     * @param function
     * @return
     */
    private <T> Observable<T> invokeHystrixCommand(Supplier<T> function) {

        EC2Command<T> command = new EC2Command<T>(function);
        try {
            return command.observe().subscribeOn(Schedulers.io());
        } catch (Exception e) {
            LOGGER.error("Error executing AWS EC2 command", e);
        }
        return Observable.empty();
    }

    @Override
    public Observable<SecurityGroup> describeSecurityGroups(final Optional<SecurityGroupQuery> query) {

        Supplier<List<SecurityGroup>> function =
            () -> {
                List<SecurityGroup> securityGroups = new ArrayList<>();

                List<String> groupIds = query.get().getGroupIds();
                List<String> groupNames = query.get().getGroupNames();
                for (int i = 0; i < groupIds.size(); i++) {
                    String groupId = groupIds.get(i);
                    String groupName = groupNames.get(i);
                    SecurityGroup securityGroup = Mockito.mock(SecurityGroup.class);
                    securityGroups.add(securityGroup);
                    Mockito.when(securityGroup.getGroupId()).thenReturn(groupId);
                    Mockito.when(securityGroup.getGroupName()).thenReturn(groupName);
                }

                return securityGroups;
            };
        return invokeHystrixCommand(function).flatMap(lst -> Observable.from(lst));
    }

    @Override
    public Observable<CreateSecurityGroupResult> createSecurityGroup(
        String groupName, String vpcId, Optional<String> groupDescription
    ) {

        Supplier<CreateSecurityGroupResult> function = () -> {

            CreateSecurityGroupRequest request = new CreateSecurityGroupRequest();
            if (groupDescription.isPresent())
                request.withGroupName(groupName).withVpcId(vpcId).withDescription(groupDescription.get());
            else
                request.withGroupName(groupName).withVpcId(vpcId);

            CreateSecurityGroupResult createSecurityGroupResult = Mockito.mock(
                CreateSecurityGroupResult.class
            );
            Mockito.when(createSecurityGroupResult.getGroupId()).thenReturn("2137139");

            return createSecurityGroupResult;
        };
        return invokeHystrixCommand(function);
    }

    @Override
    public Observable<Void> deleteSecurityGroup(String groupId) {

        Supplier<Void> function = () -> {
            return null;
        };
        return invokeHystrixCommand(function);
    }

    /**
     * Private method to construct {@link IpPermission} as part of a request to
     * authorize ingress or egress to a security group.
     * 
     * @param fromPort the port for which access should be granted from
     * @param toPort the port for which access should be granted to
     * @param protocol the protocol associated with the permission
     * @param cidr the cidr if it exists
     * @param destinationGroupId the destinationGroupId if it exists
     * @return {@link IpPermission}
     */
    private IpPermission constructPermission(
        int fromPort, int toPort, String protocol, Optional<String> cidr
        , Optional<String> destinationGroupId
    ) {

        IpPermission permission = new IpPermission().withFromPort(fromPort).withToPort(toPort).withIpProtocol(protocol);
        return cidr.isPresent() ? permission.withIpRanges(cidr.get()) : permission
            .withUserIdGroupPairs(new UserIdGroupPair().withGroupId(destinationGroupId.get()));
    }

    @Override
    public Observable<Void> createSecurityGroupEgressRule(
        String groupId, int toPort, int fromPort, String protocol, Optional<String> cidr
        , Optional<String> destinationGroupId
    ) {

        if (!cidr.isPresent() && !destinationGroupId.isPresent() || cidr.isPresent() && destinationGroupId.isPresent())
            throw new IllegalArgumentException("Either a CIDR or destination security group ID must be passed");
        Supplier<Void> function = () -> {
            AuthorizeSecurityGroupEgressRequest request = new AuthorizeSecurityGroupEgressRequest();
            IpPermission permission = constructPermission(fromPort, toPort, protocol, cidr, destinationGroupId);

            return null;
        };
        return invokeHystrixCommand(function);
    }

    @Override
    public Observable<Void> createSecurityGroupIngressRule(
        String groupId, int toPort, int fromPort, String protocol, Optional<String> cidr
        , Optional<String> destinationGroupId
    ) {

        if (!cidr.isPresent() && !destinationGroupId.isPresent() || cidr.isPresent() && destinationGroupId.isPresent())
            throw new IllegalArgumentException("Either a CIDR or destination security group ID must be passed");
        Supplier<Void> function = () -> {
            AuthorizeSecurityGroupIngressRequest request = new AuthorizeSecurityGroupIngressRequest();
            IpPermission permission = constructPermission(fromPort, toPort, protocol, cidr, destinationGroupId);

            return null;
        };
        return invokeHystrixCommand(function);
    }

    @Override
    public Observable<Void> deleteSecurityGroupIngressRule(
        String groupId, int toPort, int fromPort, String protocol, Optional<String> cidr
        , Optional<String> destinationGroupId
    ) {

        if (!cidr.isPresent() && !destinationGroupId.isPresent() || cidr.isPresent() && destinationGroupId.isPresent())
            throw new IllegalArgumentException("Either a CIDR or destination security group ID must be passed");
        Supplier<Void> function = () -> {
            RevokeSecurityGroupIngressRequest request = new RevokeSecurityGroupIngressRequest();
            IpPermission permission = constructPermission(fromPort, toPort, protocol, cidr, destinationGroupId);

            return null;
        };
        return invokeHystrixCommand(function);
    }

    @Override
    public Observable<Void> deleteSecurityGroupEgressRule(
        String groupId, int toPort, int fromPort, String protocol, Optional<String> cidr
        , Optional<String> destinationGroupId
    ) {

        if (!cidr.isPresent() && !destinationGroupId.isPresent() || cidr.isPresent() && destinationGroupId.isPresent())
            throw new IllegalArgumentException("Either a CIDR or destination security group ID must be passed");
        Supplier<Void> function = () -> {
            RevokeSecurityGroupEgressRequest request = new RevokeSecurityGroupEgressRequest();
            IpPermission permission = constructPermission(fromPort, toPort, protocol, cidr, destinationGroupId);

            return null;
        };
        return invokeHystrixCommand(function);
    }

    @Override
    public Observable<DescribeVpcsResult> describeVpcs(Optional<List<String>> vpcs) {

        Supplier<DescribeVpcsResult> function = () -> {
            DescribeVpcsRequest request = new DescribeVpcsRequest();
            if (vpcs.isPresent())
                request.withVpcIds(vpcs.get());

            DescribeVpcsResult describeVpcsResult = Mockito.mock(DescribeVpcsResult.class);
            List<Vpc> vpcList = new ArrayList<>();
            vpcs.get().forEach(vpc -> {
                vpcList.add(new Vpc().withVpcId(vpc));
            });
            Mockito.when(describeVpcsResult.getVpcs()).thenReturn(vpcList);

            return describeVpcsResult;
        };
        return invokeHystrixCommand(function);
    }

    /**
     * Builder class for constructing an instance of {@link S3Client}
     * 
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
         * 
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

            EC2ClientStub client = null;
            if (this.authType == AWSAuthType.CREDENTIALS && awsAccountKey != null && awsSecretKey != null) {
                client = new EC2ClientStub();
            }

            if (this.authType == AWSAuthType.PROFILE && profileConfigFilePath == null && profileName != null) {
                client = new EC2ClientStub();
            }

            if (this.authType == AWSAuthType.PROFILE && profileConfigFilePath != null && profileName != null) {
                client = new EC2ClientStub();
            }

            if (this.authType == AWSAuthType.INSTANCE_ROLE) {
                client = new EC2ClientStub();
            }

            if (client != null && region != null) {
                return client;
            }

            if (client != null) {
                return client;
            }

            throw new IllegalStateException("Invalid S3Client configuration");
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
         *         {@code null}.
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
