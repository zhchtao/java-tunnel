package com.smvcsh.proxy.manager.channel;

import io.netty.channel.ChannelHandlerContext;

public class ChannelRelation {

	private ChannelHandlerContext ctx;
	private String remotChannel;
	
	public ChannelRelation(ChannelHandlerContext ctx) {
		super();
		// TODO Auto-generated constructor stub
		this.ctx = ctx;
	}
	public ChannelRelation(ChannelHandlerContext ctx, String remotChannel) {
		super();
		// TODO Auto-generated constructor stub
		this.ctx = ctx;
		this.remotChannel = remotChannel;
	}
	
	public ChannelHandlerContext getCtx() {
		return ctx;
	}
	public void setCtx(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}
	public String getRemotChannel() {
		return remotChannel;
	}
	public void setRemotChannel(String remotChannel) {
		this.remotChannel = remotChannel;
	}
}
