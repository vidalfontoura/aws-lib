/*
 * Copyright 2014, Charter Communications, All rights reserved.
 */
package com.charter.aesd.aws.ec2.client.api.impl;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfilesConfigFile;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.s3.AmazonS3Client;
import com.charter.aesd.aws.ec2.client.api.EC2Client;
import com.charter.aesd.aws.ec2.command.EC2Command;
import com.charter.aesd.aws.ec2.securitygroup.SecurityGroupQuery;
import com.charter.aesd.aws.enums.AWSAuthType;
import com.charter.aesd.aws.s3client.S3Client;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.concurrent.Callable;

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
     * This method returns all of the security groups for a particular
     * environment germane to the {@link AmazonEC2Client}.
     * 
     * @return {@code Observable<SecurityGroup>} for the environment.
     */
    @Override
    public Observable<SecurityGroup> describeSecurityGroups(final Optional<SecurityGroupQuery> query) {

        Callable<Observable<SecurityGroup>> function = new Callable<Observable<SecurityGroup>>() {

            public Observable<SecurityGroup> call() throws Exception {

                DescribeSecurityGroupsResult securityGroupsResult =
                    awsEC2Client.describeSecurityGroups(query.isPresent() ? query.get().getRequest()
                        : new DescribeSecurityGroupsRequest());
                return Observable.from(securityGroupsResult.getSecurityGroups());
            }

        };
        EC2Command<Observable<SecurityGroup>> command = new EC2Command<Observable<SecurityGroup>>(function);
        try {
            return command.run();
        } catch (Exception e) {
            LOGGER.error("Error executing AWS EC2 command", e);
        }
        return Observable.empty();
    }

    /**
     * Builder class for constructing an instance of {@link S3Client}
     * 
     */
    public static class Builder {

        private AWSAuthType authType;
        private String profileName;
        private String profileConfigFilePath;
        private ClientConfiguration config;

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

        public EC2Client build() {

            if (this.authType == AWSAuthType.PROFILE && profileConfigFilePath == null && profileName == null) {
                return new EC2ClientImpl(new AmazonEC2Client(new ProfileCredentialsProvider(), config));
            }

            if (this.authType == AWSAuthType.PROFILE && profileConfigFilePath == null && profileName != null) {
                return new EC2ClientImpl(new AmazonEC2Client(new ProfileCredentialsProvider(profileName), config));
            }

            if (this.authType == AWSAuthType.PROFILE && profileConfigFilePath != null && profileName != null) {
                return new EC2ClientImpl(new AmazonEC2Client(new ProfileCredentialsProvider(new ProfilesConfigFile(
                    profileConfigFilePath), profileName), getConfiguration()));
            }

            if (this.authType == AWSAuthType.INSTANCE_ROLE) {
                return new EC2ClientImpl(new AmazonEC2Client(new InstanceProfileCredentialsProvider(), config));
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
