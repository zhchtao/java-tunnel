package com.smvcsh.proxy.server.channel;

import com.smvcsh.proxy.manager.ServerChannelManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
public class ProxyServerChannelHandlerAdapter extends ChannelInboundHandlerAdapter {

	protected static Logger logger = LoggerFactory.getLogger(ProxyServerChannelHandlerAdapter.class);
	
	@Resource
	protected ServerChannelManager serverChannelManager;
	
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		
		serverChannelManager.remove(ctx);
		logger.info("close server {}", ctx.channel().id().asShortText());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		// TODO Auto-generated method stub
		logger.error(ctx.toString(), cause);
		super.exceptionCaught(ctx, cause);
	}

}
