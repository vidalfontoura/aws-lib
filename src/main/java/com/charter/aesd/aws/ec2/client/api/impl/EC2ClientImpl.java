/*
 * Copyright 2014, Charter Communications, All rights reserved.
 */
package com.charter.aesd.aws.ec2.client.api.impl;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfilesConfigFile;
import com.amazonaws.regions.Region;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupEgressRequest;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.DescribeVpcsRequest;
import com.amazonaws.services.ec2.model.DescribeVpcsResult;
import com.amazonaws.services.ec2.model.RevokeSecurityGroupEgressRequest;
import com.amazonaws.services.ec2.model.RevokeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.s3.AmazonS3Client;
import com.charter.aesd.aws.ec2.client.api.EC2Client;
import com.charter.aesd.aws.ec2.command.EC2Command;
import com.charter.aesd.aws.ec2.securitygroup.SecurityGroupQuery;
import com.charter.aesd.aws.enums.AWSAuthType;
import com.charter.aesd.aws.s3client.S3Client;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;

/**
 * AWS client for accessing EC2 resources within the AWS EC2 API<br />
 * Use {@link EC2ClientImpl.Builder} to construct an instance of
 * {@link EC2Client}
 * 
 * @author jappel
 */
public class EC2ClientImpl implements EC2Client {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().getClass());
    private final AmazonEC2Client awsEC2Client;

    private EC2ClientImpl(final AmazonEC2Client client) {

        this.awsEC2Client = client;
    }

    /**
     * Private method to invoke {@code Hystrix} command for each AWS EC2 APIO
     * 
     * @param function
     * @return
     */
    private <T> Optional<T> invokeHystrixCommand(Supplier<T> function) {

        EC2Command<T> command = new EC2Command<T>(function);
        try {
            return Optional.of(command.run());
        } catch (Exception e) {
            LOGGER.error("Error executing AWS EC2 command", e);
        }
        return Optional.empty();
    }

    @Override
    public Observable<SecurityGroup> describeSecurityGroups(final Optional<SecurityGroupQuery> query) {

        Supplier<Observable<SecurityGroup>> function =
            () -> {
                DescribeSecurityGroupsResult securityGroupsResult =
                    awsEC2Client.describeSecurityGroups(query.isPresent() ? query.get().getRequest()
                        : new DescribeSecurityGroupsRequest());
                return Observable.from(securityGroupsResult.getSecurityGroups());
            };
        return invokeHystrixCommand(function).orElse(Observable.empty());
    }

    @Override
    public Observable<CreateSecurityGroupResult> createSecurityGroup(String groupName, String vpcId,
                                                                     Optional<String> groupDescription) {

        Supplier<Observable<CreateSecurityGroupResult>> function = () -> {

            CreateSecurityGroupRequest request = new CreateSecurityGroupRequest();
            if (groupDescription.isPresent())
                request.withGroupName(groupName).withVpcId(vpcId).withDescription(groupDescription.get());
            else
                request.withGroupName(groupName).withVpcId(vpcId);
            return Observable.just(awsEC2Client.createSecurityGroup(request));
        };
        return invokeHystrixCommand(function).orElse(Observable.empty());
    }

    @Override
    public Observable<Void> deleteSecurityGroup(String groupId) {

        Supplier<Observable<Void>> function = () -> {
            awsEC2Client.deleteSecurityGroup(new DeleteSecurityGroupRequest().withGroupId(groupId));
            return Observable.empty();
        };
        return invokeHystrixCommand(function).orElse(Observable.empty());
    }

    @Override
    public Observable<Void> createSecurityGroupEgressRule(String groupId, int toPort, int fromPort, String protocol,
                                                          Optional<String> cidr, Optional<String> destinationGroupId) {

        if (!cidr.isPresent() && !destinationGroupId.isPresent() || cidr.isPresent() && destinationGroupId.isPresent())
            throw new IllegalArgumentException("Either a CIDR or destination security group ID must be passed");
        Supplier<Observable<Void>> function = () -> {
            AuthorizeSecurityGroupEgressRequest request = new AuthorizeSecurityGroupEgressRequest();
            request.withGroupId(groupId);
            if (cidr.isPresent())
                request.withCidrIp(cidr.get()).withFromPort(fromPort).withToPort(toPort).withIpProtocol(protocol);
            else
                request.withSourceSecurityGroupOwnerId(destinationGroupId.get());
            awsEC2Client.authorizeSecurityGroupEgress(request);
            return Observable.empty();
        };
        return invokeHystrixCommand(function).orElse(Observable.empty());
    }

    @Override
    public Observable<Void> createSecurityGroupIngressRule(String groupId, int toPort, int fromPort, String protocol,
                                                           Optional<String> cidr, Optional<String> destinationGroupId) {

        if (!cidr.isPresent() && !destinationGroupId.isPresent() || cidr.isPresent() && destinationGroupId.isPresent())
            throw new IllegalArgumentException("Either a CIDR or destination security group ID must be passed");
        Supplier<Observable<Void>> function = () -> {
            AuthorizeSecurityGroupIngressRequest request = new AuthorizeSecurityGroupIngressRequest();
            request.withGroupId(groupId);
            if (cidr.isPresent())
                request.withCidrIp(cidr.get()).withFromPort(fromPort).withToPort(toPort).withIpProtocol(protocol);
            else
                request.withSourceSecurityGroupOwnerId(destinationGroupId.get());
            awsEC2Client.authorizeSecurityGroupIngress(request);
            return Observable.empty();
        };
        return invokeHystrixCommand(function).orElse(Observable.empty());
    }

    @Override
    public Observable<Void> deleteSecurityGroupIngressRule(String groupId, int toPort, int fromPort, String protocol,
                                                           Optional<String> cidr, Optional<String> destinationGroupId) {

        if (!cidr.isPresent() && !destinationGroupId.isPresent() || cidr.isPresent() && destinationGroupId.isPresent())
            throw new IllegalArgumentException("Either a CIDR or destination security group ID must be passed");
        Supplier<Observable<Void>> function = () -> {
            RevokeSecurityGroupIngressRequest request = new RevokeSecurityGroupIngressRequest();
            request.withGroupId(groupId);
            if (cidr.isPresent())
                request.withCidrIp(cidr.get()).withFromPort(fromPort).withToPort(toPort).withIpProtocol(protocol);
            else
                request.withSourceSecurityGroupOwnerId(destinationGroupId.get());
            awsEC2Client.revokeSecurityGroupIngress(request);
            return Observable.empty();
        };
        return invokeHystrixCommand(function).orElse(Observable.empty());
    }

    @Override
    public Observable<Void> deleteSecurityGroupEgressRule(String groupId, int toPort, int fromPort, String protocol,
                                                          Optional<String> cidr, Optional<String> destinationGroupId) {

        if (!cidr.isPresent() && !destinationGroupId.isPresent() || cidr.isPresent() && destinationGroupId.isPresent())
            throw new IllegalArgumentException("Either a CIDR or destination security group ID must be passed");
        Supplier<Observable<Void>> function = () -> {
            RevokeSecurityGroupEgressRequest request = new RevokeSecurityGroupEgressRequest();
            request.withGroupId(groupId);
            if (cidr.isPresent())
                request.withCidrIp(cidr.get()).withFromPort(fromPort).withToPort(toPort).withIpProtocol(protocol);
            else
                request.withSourceSecurityGroupOwnerId(destinationGroupId.get());
            awsEC2Client.revokeSecurityGroupEgress(request);
            return Observable.empty();
        };
        return invokeHystrixCommand(function).orElse(Observable.empty());
    }
    
    @Override
    public Observable<DescribeVpcsResult> describeVpcs(Optional<List<String>> vpcs) {
        
        Supplier<Observable<DescribeVpcsResult>> function = () -> {
            DescribeVpcsRequest request = new DescribeVpcsRequest();
            if (vpcs.isPresent())
                request.withVpcIds(vpcs.get());
            return Observable.just(awsEC2Client.describeVpcs(request));
        };
        return invokeHystrixCommand(function).orElse(Observable.empty());
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

            EC2ClientImpl client = null;
            if (this.authType == AWSAuthType.CREDENTIALS && awsAccountKey != null && awsSecretKey != null) {
                client =
                    new EC2ClientImpl(new AmazonEC2Client(new BasicAWSCredentials(awsAccountKey, awsSecretKey),
                        getConfiguration()));
            }

            if (this.authType == AWSAuthType.PROFILE && profileConfigFilePath == null && profileName != null) {
                client = new EC2ClientImpl(new AmazonEC2Client(new ProfileCredentialsProvider(profileName), config));
            }

            if (this.authType == AWSAuthType.PROFILE && profileConfigFilePath != null && profileName != null) {
                client =
                    new EC2ClientImpl(new AmazonEC2Client(new ProfileCredentialsProvider(new ProfilesConfigFile(
                        profileConfigFilePath), profileName), getConfiguration()));
            }

            if (this.authType == AWSAuthType.INSTANCE_ROLE) {
                client = new EC2ClientImpl(new AmazonEC2Client(new InstanceProfileCredentialsProvider(), config));
            }

            if (client != null && region != null) {
                client.awsEC2Client.setRegion(region);
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
