/*
 * Copyright 2014, Charter Communications, All rights reserved.
 */
package com.charter.aesd.aws.eip.config;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.charter.aesd.aws.eip.AssociateEip;
import com.charter.aesd.aws.eip.EipPoolLookup;
import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.netflix.config.DynamicPropertyFactory;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Module searches a pool of available EIP's and associates one to
 *  the instance of the current application
 *
 * @author darrenbathgate
 *
 */
public class EipAssociationModule extends AbstractModule{

    private static final Logger logger = LoggerFactory.getLogger(EipAssociationModule.class);

    private final String EIP_TXT_RECORD_PROP_NAME = "com.charter.aws.eip.txt-record";
    private final String DEPLOYMENT_REGION_PROP_NAME = "archaius.deployment.region";

    private final DynamicPropertyFactory dynamicPropertyFactory;

    public EipAssociationModule() {
        dynamicPropertyFactory = DynamicPropertyFactory.getInstance();
    }

    @Override
    protected void configure() {
        final EipPoolLookup eipPoolLookup = new EipPoolLookup(dynamicPropertyFactory.getStringProperty(EIP_TXT_RECORD_PROP_NAME, ""));
        final AmazonEC2Client client = new AmazonEC2Client(new InstanceProfileCredentialsProvider());

        try {
            final String region = System.getProperty(DEPLOYMENT_REGION_PROP_NAME);
            Preconditions.checkArgument(StringUtils.isNotEmpty(region), "System property missing: " + DEPLOYMENT_REGION_PROP_NAME);

            client.setRegion(Region.getRegion(Regions.fromName(region)));

            final AssociateEip associateEip = new AssociateEip(client, eipPoolLookup);
            associateEip.associate();
        }catch (Exception ex){
            logger.error("Failed to associate an EIP address", ex);
            throw new IllegalStateException(ex);
        }
    }
}
