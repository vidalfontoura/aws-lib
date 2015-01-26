package com.charter.aesd.aws.enums;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;

public enum AWSAuthType {

    /**
     * Uses {@link InstanceProfileCredentialsProvider} for an
     * {@link AWSCredentialsProvider}
     */
    INSTANCE_ROLE("InstanceRole"),

    /**
     * Uses {@link ProfileCredentialsProvider} for an
     * {@link AWSCredentialsProvider}
     */
    PROFILE("Profile"),
    
    /**
     * Uses {@link BasicAWSCredentials} for an
     * {@link AWSCredentials}
     */
    CREDENTIALS("Credentials");

    private final String value;

    AWSAuthType(String value) {

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
     * @return {@link AWSAuthType}
     */
    public static AWSAuthType fromString(String name) {

        for (AWSAuthType awsAuthType : values()) {
            if (awsAuthType.getValue().equalsIgnoreCase(name)) {
                return awsAuthType;
            }
        }

        return null;
    }
}
