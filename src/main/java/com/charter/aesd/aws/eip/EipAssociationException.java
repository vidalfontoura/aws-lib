/*
 * Copyright 2014, Charter Communications, All rights reserved.
 */
package com.charter.aesd.aws.eip;

public class EipAssociationException extends Exception{
    private static final long serialVersionUID = 8371483533745317853L;

    /**
     * Construct a {@code EipAssociationException} object
     */
    public EipAssociationException() {
    }

    /**
     * @param errorMessage
     *            the error message
     */
    public EipAssociationException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * @param cause
     *            the cause of the error
     */
    public EipAssociationException(Throwable cause) {
        super(cause);
    }

    /**
     * @param errorMessage
     *            the error message
     * @param cause
     *            the cause of the error
     */
    public EipAssociationException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }
}
