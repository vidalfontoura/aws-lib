package com.charter.aesd.aws.ec2.client.api;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;

public enum EC2AuthType {

    /**
     * Uses {@link InstanceProfileCredentialsProvider} for an
     * {@link AWSCredentialsProvider}
     */
    INSTANCE_ROLE("InstanceRole"),

    /**
     * Uses {@link ProfileCredentialsProvider} for an
     * {@link AWSCredentialsProvider}
     */
    PROFILE("Profile");

    private final String value;

    EC2AuthType(String value) {

        this.value = value;
    }

    public String getValue() {

        return value;
    }

    /**
     * Gets the enum from a String name<br />
     * Options: Credentials, InstanceRole
     * 
     * @param name
     * @return {@link EC2AuthType}
     */
    public static EC2AuthType fromString(String name) {

        for (EC2AuthType s3AuthType : values()) {
            if (s3AuthType.getValue().equalsIgnoreCase(name)) {
                return s3AuthType;
            }
        }

        return null;
    }
}
