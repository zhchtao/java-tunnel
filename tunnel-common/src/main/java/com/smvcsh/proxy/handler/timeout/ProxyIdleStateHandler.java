package com.smvcsh.proxy.handler.timeout;

import java.util.concurrent.TimeUnit;

import io.netty.handler.timeout.IdleStateHandler;

public class ProxyIdleStateHandler extends IdleStateHandler {

	public ProxyIdleStateHandler(long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit) {
		super(readerIdleTime, writerIdleTime, allIdleTime, unit);
		// TODO Auto-generated constructor stub
	}
	

}
