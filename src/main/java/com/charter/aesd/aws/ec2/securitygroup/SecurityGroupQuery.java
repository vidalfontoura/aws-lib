/*
 * Copyright 2014, Charter Communications, All rights reserved.
 */
package com.charter.aesd.aws.ec2.securitygroup;

import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * The {@code SecurityGroupQuery} provides a flexible means for constructing an
 * {@link com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest}.
 * 
 * {@code com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest}
 * construction starts off with the static withGroupNames method, which returns a
 * SecurityGroupQuery.Builder that can take a chained list of group query
 * properties, until the build method is invoked to return the constructed
 * {@code SecurityGroupQuery} instance.
 * 
 * @author jappel
 */
public class SecurityGroupQuery {

    private final DescribeSecurityGroupsRequest request;

    private SecurityGroupQuery(DescribeSecurityGroupsRequest request) {

        this.request = request;
    }

    public static class Builder {

        private Collection<String> names;
        private Collection<String> ids;
        private Collection<com.amazonaws.services.ec2.model.Filter> filters;

        private Builder() {}

        public Builder withGroupNames(Collection<String> names) {

            this.names = names;
            return this;
        }

        public static Builder withGroupIds(Collection<String> ids) {

            Builder builder = new Builder();
            builder.ids = ids;
            return builder;
        }

        public Builder withFilters(Collection<Filter> filters) {

            final Collection<com.amazonaws.services.ec2.model.Filter> ec2Filters =
                Lists.newArrayListWithExpectedSize(filters.size());
            Observable.from(filters).map(new Func1<Filter, com.amazonaws.services.ec2.model.Filter>() {

                public com.amazonaws.services.ec2.model.Filter call(Filter t1) {

                    return new com.amazonaws.services.ec2.model.Filter(t1.name, t1.values);
                }

            }).subscribe(new Action1<com.amazonaws.services.ec2.model.Filter>() {

                public void call(com.amazonaws.services.ec2.model.Filter t1) {

                    ec2Filters.add(t1);
                }

            });
            this.filters = ec2Filters;
            return this;
        }

        public SecurityGroupQuery build() {

            Preconditions.checkArgument(ids != null && !ids.isEmpty(), "list of security group ids must not be empty");

            DescribeSecurityGroupsRequest request = new DescribeSecurityGroupsRequest();
            request.withGroupIds(ids);
            if (names != null && !names.isEmpty())
                request.withGroupNames(names);
            if (filters != null && !filters.isEmpty())
                request.withFilters(filters);
            return new SecurityGroupQuery(request);
        }
    }

    public static class Filter {

        private final String name;
        private final List<String> values;

        public Filter(String name, List<String> values) {

            super();
            this.name = name;
            this.values = values;
        }

    }

    public DescribeSecurityGroupsRequest getRequest() {

        return request;
    }

}
