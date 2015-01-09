/*
 * Copyright 2014, Charter Communications, All rights reserved.
 */
package com.charter.aesd.aws.ec2.securitygroup;

import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The {@code SecurityGroupQuery} provides a flexible means for constructing an
 * {@link com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest}.
 * 
 * {@code com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest}
 * construction starts off with the static create method, which returns
 * a SecurityGroupQuery.Builder that can take a chained list of group query
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

        private Builder() {

        }

        public static Builder clone(SecurityGroupQuery query) {

            Builder builder = new Builder();
            builder.names = query.request.getGroupIds();
            builder.ids = query.request.getGroupIds();
            builder.filters = query.request.getFilters();
            return builder;
        }

        public static Builder create() {

            Builder builder = new Builder();
            return builder;
        }

        public Builder withGroupNames(Collection<String> names) {

            this.names = names;
            return this;
        }

        public Builder withGroupIds(Collection<String> ids) {

            this.ids = ids;
            return this;
        }

        public Builder withFilters(Collection<Filter> filters) {

            final Collection<com.amazonaws.services.ec2.model.Filter> ec2Filters = new ArrayList<>(filters.size());
            for (Filter filter : filters) 
                ec2Filters.add(new com.amazonaws.services.ec2.model.Filter(filter.name, filter.values));
            this.filters = ec2Filters;
            return this;
        }

        public SecurityGroupQuery build() {

            Preconditions.checkArgument((ids != null && !ids.isEmpty()) || (names != null && !names.isEmpty()),
                "list of security group ids or names must not be empty");

            DescribeSecurityGroupsRequest request = new DescribeSecurityGroupsRequest();
            if (ids != null && !ids.isEmpty())
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

    public List<String> getGroupNames() {

        return request.getGroupNames();
    }
    
    public List<String> getGroupIds() {

        return request.getGroupIds();
    }


}
