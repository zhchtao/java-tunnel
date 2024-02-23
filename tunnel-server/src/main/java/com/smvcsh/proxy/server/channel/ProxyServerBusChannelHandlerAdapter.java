package com.smvcsh.proxy.server.channel;

import com.smvcsh.proxy.handler.ProxyDataMessage;
import com.smvcsh.proxy.handler.constants.ProxyTunnelMessageConstants;
import com.smvcsh.proxy.manager.channel.ChannelRelation;
import com.smvcsh.proxy.manager.relation.IpRelation;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.util.Map;

@Component
@Sharable
public class ProxyServerBusChannelHandlerAdapter extends ProxyServerChannelHandlerAdapter {
	
	@Resource
	private Map<Integer, IpRelation> ipRelationMap;

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// TODO Auto-generated method stub
		
		try {
			
			ByteBuf data = (ByteBuf) msg;
			
			while(data.isReadable()) {
				
				byte[] b = new byte[data.readableBytes()];
				data.readBytes(b);
				
				ChannelRelation channelRelation = serverChannelManager.getChannelRelation(ctx);
				String target = channelRelation.getRemotChannel();
				
				ProxyDataMessage proxyData = new ProxyDataMessage();
				
				if(StringUtils.isBlank(target)) {
					
					IpRelation ip = ipRelationMap.get(((InetSocketAddress)ctx.channel().localAddress()).getPort());
					proxyData.setHost(ip.getRemotHost());
					proxyData.setPort(ip.getPort());
					
				}
				
				
				proxyData.setOperateCode(ProxyTunnelMessageConstants.OPERATE_CODE.DATA_PROXY);
				proxyData.setData(b);
				proxyData.setSource(ctx.channel().id().asLongText());
				proxyData.setTarget(StringUtils.trimToEmpty(target));
				
				serverChannelManager.proxyChannlCtx().writeAndFlush(proxyData);
				
			}
		} finally {
			// TODO: handle finally clause
			ReferenceCountUtil.release(msg);
		}
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) {
		// TODO Auto-generated method stub
		
		try {
			
			logger.info("bus server connected {}", ctx.channel().id().asShortText());
			
			ChannelHandlerContext proxyDataCtx = serverChannelManager.proxyChannlCtx();
			
			if(null == serverChannelManager.getChannelRelation(ctx)) {
				
				serverChannelManager.addChannelCtx(ctx);
				
				ProxyDataMessage  msg = new ProxyDataMessage();
				
				IpRelation ip = ipRelationMap.get(((InetSocketAddress)ctx.channel().localAddress()).getPort());
				
				msg.setHost(ip.getRemotHost());
				msg.setPort(ip.getRemotPort());
				msg.setOperateCode(ProxyTunnelMessageConstants.OPERATE_CODE.CONNECT);
				msg.setSource(ctx.channel().id().asLongText());
				
				proxyDataCtx.writeAndFlush(msg);
				
			}
		} catch (Exception e) {
			// TODO: handle exception
			ctx
			.writeAndFlush(Unpooled.EMPTY_BUFFER)
			.addListener(ChannelFutureListener.CLOSE);
		}
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		try {

			ChannelRelation relation = serverChannelManager.getChannelRelation(ctx);

			if(null != relation) {
				ProxyDataMessage proxyData = new ProxyDataMessage();

				proxyData.setOperateCode(ProxyTunnelMessageConstants.OPERATE_CODE.CLOSE_CONNECT);
				proxyData.setSource(ctx.channel().id().asLongText());
				proxyData.setTarget(relation.getRemotChannel());

				serverChannelManager.proxyChannlCtx().writeAndFlush(proxyData);
			}
		} finally {
			serverChannelManager.remove(ctx);
			logger.info("bus server close {}", ctx.channel().id().asShortText());
		}

	}
	
	
}
