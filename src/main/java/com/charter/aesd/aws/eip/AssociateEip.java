/*
 * Copyright 2014, Charter Communications, All rights reserved.
 */
package com.charter.aesd.aws.eip;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.Address;
import com.amazonaws.services.ec2.model.AssociateAddressRequest;
import com.amazonaws.services.ec2.model.DescribeAddressesRequest;
import com.amazonaws.services.ec2.model.DescribeAddressesResult;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.TextParseException;

public class AssociateEip {

    private static final Logger logger = LoggerFactory.getLogger(AssociateEip.class);

    private static final String INSTANCE_ID_PROP_NAME = "archaius.deployment.serverId";

    private final AmazonEC2Client client;
    private final EipPoolLookup poolLookup;

    public AssociateEip(AmazonEC2Client client, EipPoolLookup poolLookup) {
        this.client = client;
        this.poolLookup = poolLookup;
    }


    public void associate() throws TextParseException {
        final String instanceId = System.getProperty(INSTANCE_ID_PROP_NAME);
        Preconditions.checkArgument(StringUtils.isNotEmpty(instanceId), "System property missing: " + INSTANCE_ID_PROP_NAME);

        final List<String> eipPool = poolLookup.getEipList();

        final DescribeAddressesRequest describeAddressesRequest = new DescribeAddressesRequest()
            .withPublicIps(eipPool);

        DescribeAddressesResult result = client.describeAddresses(describeAddressesRequest);

        final List<Address> addresses = result.getAddresses();

        for (Address address : addresses) {
            if (instanceId.equals(address.getInstanceId())) {
                logger.warn("Instance ID " + instanceId + " already has an EIP of " + address.getPublicIp());
                return;
            }
        }

        final ArrayList<Address> filteredAddresses = Lists.newArrayList(Iterables.filter(addresses, new EipAvailabilityPredicate()));

        Preconditions.checkArgument(filteredAddresses.size() > 0, "At least one EIP from the pool needs to be available for use");

        final Address targetAddress = filteredAddresses.get(0);

        logger.info("Associating EIP[" + targetAddress.getPublicIp() + "] to instance[" + instanceId + "]");

        final AssociateAddressRequest associateAddressRequest = new AssociateAddressRequest(instanceId, targetAddress.getPublicIp());
        associateAddressRequest.setAllocationId(targetAddress.getAllocationId());
        client.associateAddress(associateAddressRequest);
    }
}
