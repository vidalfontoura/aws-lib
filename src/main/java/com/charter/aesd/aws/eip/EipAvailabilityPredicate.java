/*
 * Copyright 2014, Charter Communications, All rights reserved.
 */
package com.charter.aesd.aws.eip;

import com.amazonaws.services.ec2.model.Address;
import com.google.common.base.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EipAvailabilityPredicate implements Predicate<Address>{

    private static final Logger logger = LoggerFactory.getLogger(EipAvailabilityPredicate.class);

    @Override
    public boolean apply(Address input) {

        final String publicIp = input.getPublicIp();
        final String instanceId = input.getInstanceId();
        final String networkInterfaceId = input.getNetworkInterfaceId();
        final String associationId = input.getAssociationId();
        final String domain = input.getDomain();

        final boolean avialable =
                publicIp != null &&
                associationId == null &&
                "vpc".equals(domain);

        logger.info("EIP[" + publicIp + 
                "], Instance[" + instanceId + 
                "], ENI[" + networkInterfaceId + 
                "], Association ID[" + associationId + 
                "], Domain[" + domain + 
                "]: " + (avialable ? "AVAILABLE" : "NOT AVAILABLE"));

        return avialable;
    }

}
