package com.smvcsh.proxy.client.channel;

import javax.annotation.Resource;

import com.smvcsh.proxy.manager.ClientChannelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
/**
 * @author taotao
 *
 */
@Sharable
public class ProxyClientChannelHandlerAdapter extends ChannelInboundHandlerAdapter {
	
	protected static Logger logger = LoggerFactory.getLogger(ProxyClientChannelHandlerAdapter.class);
	
	@Resource
	protected ClientChannelManager clientChannelManager;

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		// TODO Auto-generated method stub
		logger.error(ctx.toString(), cause);
	}
}
