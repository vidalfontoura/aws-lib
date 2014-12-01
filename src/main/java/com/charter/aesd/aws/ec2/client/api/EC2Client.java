/*
 * Copyright 2014, Charter Communications, All rights reserved.
 */
package com.charter.aesd.aws.ec2.client.api;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfilesConfigFile;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.s3.AmazonS3Client;
import com.charter.aesd.aws.s3client.S3Client;

import java.util.List;

/**
 * AWS client for accessing EC2 resources within the AWS EC2 API<br />
 * Use {@link EC2Client.Builder} to construct an instance of {@link EC2Client}
 * 
 * @author jappel
 */
public class EC2Client {

    private final AmazonEC2Client awsEC2Client;

    EC2Client(final AmazonEC2Client client) {

        this.awsEC2Client = client;
    }

    /**
     * This method returns all of the security groups for a particular
     * environment germane to the {@link AmazonEC2Client}.
     * 
     * @return {@code List<SecurityGroup>} for the environment.
     */
    public List<SecurityGroup> findAllSecurityGroups() {

        DescribeSecurityGroupsRequest securityRequest = new DescribeSecurityGroupsRequest();
        DescribeSecurityGroupsResult securityDescription = awsEC2Client.describeSecurityGroups(securityRequest);
        return securityDescription.getSecurityGroups();
    }

    /**
     * Builder class for constructing an instance of {@link S3Client}
     * 
     */
    public static class Builder {

        private EC2AuthType authType;
        private String profileName;
        private String profileConfigFilePath;
        private ClientConfiguration config;

        /**
         * Constructor for {@link EC2AuthType}
         * 
         * @param authType {@link EC2AuthType}
         */
        public Builder(EC2AuthType authType) {

            this.authType = authType;
            this.config = new ClientConfiguration();
        }

        /**
         * Type of authentication used to talk to AWS
         * 
         * @param authType
         * @return {@link Builder}
         */
        public Builder setAuthType(EC2AuthType authType) {

            this.authType = authType;
            return this;
        }

        /**
         * Sets the name of the profile specified in the profile config, and
         * used with an auth type of {@link EC2AuthType#PROFILE} <br />
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
         * auth type of {@link EC2AuthType#PROFILE} <br />
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

            if (this.authType == EC2AuthType.PROFILE && profileConfigFilePath == null && profileName == null) {
                return new EC2Client(new AmazonEC2Client(new ProfileCredentialsProvider(), config));
            }

            if (this.authType == EC2AuthType.PROFILE && profileConfigFilePath == null && profileName != null) {
                return new EC2Client(new AmazonEC2Client(new ProfileCredentialsProvider(profileName), config));
            }

            if (this.authType == EC2AuthType.PROFILE && profileConfigFilePath != null && profileName != null) {
                return new EC2Client(new AmazonEC2Client(new ProfileCredentialsProvider(new ProfilesConfigFile(
                    profileConfigFilePath), profileName), getConfiguration()));
            }

            if (this.authType == EC2AuthType.INSTANCE_ROLE) {
                return new EC2Client(new AmazonEC2Client(new InstanceProfileCredentialsProvider(), config));
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
