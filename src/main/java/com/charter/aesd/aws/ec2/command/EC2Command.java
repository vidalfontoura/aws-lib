/*
 * Copyright 2014, Charter Communications, All rights reserved.
 */
package com.charter.aesd.aws.ec2.command;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;

import java.util.function.Supplier;

/**
 * Generic class for wrapping AWS EC2 calls in Hystrix.
 * 
 * @param <R>
 */
public class EC2Command<R> extends HystrixCommand<R> {

    private static final HystrixCommand.Setter EC2_SETTER = Setter
        .withGroupKey(HystrixCommandGroupKey.Factory.asKey("AwsLib"))
        .andCommandKey(HystrixCommandKey.Factory.asKey("EC2"));

    private final Supplier<R> function;

    public EC2Command(Supplier<R> function) {

        super(EC2_SETTER);
        this.function = function;
    }

    @Override
    public R run() throws Exception {

        return function.get();
    }

}
