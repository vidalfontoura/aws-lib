/*
 * Copyright 2014, Charter Communications, All rights reserved.
 */
package com.charter.aesd.aws.eip;

import com.google.common.base.Preconditions;
import com.netflix.config.DynamicStringProperty;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

public class EipPoolLookup {

    private static final Logger logger = LoggerFactory.getLogger(EipPoolLookup.class);

    private final DynamicStringProperty dnsTxtRecord;

    public EipPoolLookup(DynamicStringProperty dnsTxtRecord) {
        this.dnsTxtRecord = dnsTxtRecord;
    }

    public List<String> getEipList() throws TextParseException {
        final List<String> ipAddresses = new ArrayList<String>();

        final String dnsTextRecordString = dnsTxtRecord.get();

        Preconditions.checkArgument(StringUtils.isNotEmpty(dnsTextRecordString), "Property " + dnsTxtRecord.getName() + " cannot be empty");

        logger.info("Searching TXT record for EIP pool: " + dnsTextRecordString);

        final Lookup lookup = new Lookup(dnsTextRecordString, Type.TXT);

        final Record[] records = lookup.run();

        for (Record record: records) {
            final String stringRecord = record.rdataToString();
            final String[] splitRecord = stringRecord.split(" ");

            for (String ipString: splitRecord) {
                ipAddresses.add(ipString.replaceAll("\"", ""));
            }
        }

        logger.info("EIP pool found from DNS lookup:" + ipAddresses);

        return ipAddresses;
    }

}
