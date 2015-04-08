package com.charter.aesd.aws.s3client.enums;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;

public enum S3AuthType {

    /**
     * Uses {@link InstanceProfileCredentialsProvider} for an {@link AWSCredentialsProvider}
     */
    INSTANCE_ROLE("InstanceRole"),

    /**
     * Uses {@link ProfileCredentialsProvider} for an {@link AWSCredentialsProvider}
     */
    PROFILE("Profile");

    private final String value;

    S3AuthType(String value) {

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
     * @return {@link S3AuthType}
     */
    public static S3AuthType fromString(String name) {

        for (S3AuthType s3AuthType : values()) {
            if (s3AuthType.getValue().equalsIgnoreCase(name)) {
                return s3AuthType;
            }
        }

        return null;
    }
}
