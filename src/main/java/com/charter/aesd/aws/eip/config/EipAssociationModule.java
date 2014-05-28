/*
 * Copyright 2014, Charter Communications, All rights reserved.
 */
package com.charter.aesd.aws.eip.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.charter.aesd.aws.eip.AssociateEip;
import com.charter.aesd.aws.eip.EipPoolLookup;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.netflix.config.DynamicPropertyFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EipAssociationModule extends AbstractModule{

    private static final Logger logger = LoggerFactory.getLogger(EipAssociationModule.class);

    private final String EIP_TXT_RECORD_PROP_NAME = "com.charter.aws.eip.txt-record";

    private final DynamicPropertyFactory dynamicPropertyFactory;

    public EipAssociationModule() {
        dynamicPropertyFactory = DynamicPropertyFactory.getInstance();
    }
    @Override
    protected void configure() {
        final EipPoolLookup eipPoolLookup = new EipPoolLookup(dynamicPropertyFactory.getStringProperty(EIP_TXT_RECORD_PROP_NAME, ""));
        final AmazonEC2Client client = new AmazonEC2Client(new InstanceProfileCredentialsProvider());

        client.setRegion(Region.getRegion(Regions.US_WEST_2));
        try {
            final AssociateEip associateEip = new AssociateEip(client, eipPoolLookup);
            associateEip.associate();
        }catch (Exception ex){
            logger.error("Failed to associate a EIP address", ex);
            throw new IllegalStateException(ex);
        }
    }

    @Provides
    @Singleton
    public EipPoolLookup getEipPoolLookup(){
        return new EipPoolLookup(dynamicPropertyFactory.getStringProperty(EIP_TXT_RECORD_PROP_NAME, ""));
    }

    @Provides
    @Singleton
    public AmazonEC2Client getAmazonEC2Client() {
        return new AmazonEC2Client(new InstanceProfileCredentialsProvider());
    }
}
