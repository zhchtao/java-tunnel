package com.smvcsh.proxy.server.channel;

import com.smvcsh.proxy.handler.ProxyDataMessage;
import com.smvcsh.proxy.handler.constants.ProxyDataMessageConstants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import org.springframework.stereotype.Component;
@Sharable
@Component
public class ProxyServerDataChannelHandlerAdapter extends ProxyServerChannelHandlerAdapter {

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		
		logger.info("data server connected {}", ctx.channel().id().asShortText());
		
		serverChannelManager.addProxyCtx(ctx);
		
	}
	
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		serverChannelManager.remove(ctx);
		logger.info("close data server {}", ctx.channel().id().asShortText());
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// TODO Auto-generated method stub
//		super.channelRead(ctx, msg);
		
		try {
			
			ProxyDataMessage data = (ProxyDataMessage) msg;
			
			logger.info("data message operate {}", data.getOperateCode());
			
			if(ProxyDataMessageConstants.OPERATE_CODE.CONNECT_CHECK == data.getOperateCode()) {
				return;
			}
			
			if(ProxyDataMessageConstants.OPERATE_CODE.CONNECT == data.getOperateCode()) {
				
				ChannelHandlerContext c = serverChannelManager.getChannelCtx(data.getTarget());
				
				serverChannelManager.addChannelCtx(c, data.getSource());
				
				return;
			}
			
			if(ProxyDataMessageConstants.OPERATE_CODE.CLOSE_CONNECT == data.getOperateCode()) {
				
				ChannelHandlerContext c = serverChannelManager.getChannelCtx(data.getTarget());
				
				if(null != c) {
					c
					.writeAndFlush(Unpooled.EMPTY_BUFFER)
					.addListener(ChannelFutureListener.CLOSE);
				}
				return;
			}
			
			String ctxId = data.getTarget();
			
			ChannelHandlerContext targetCtx = serverChannelManager.getChannelCtx(ctxId);
			if(null == targetCtx || targetCtx.isRemoved()) {
				super.channelRead(ctx, msg);
				return;
			}
			
			ByteBuf td = targetCtx.alloc().buffer(data.getData().length);
			td.writeBytes(data.getData());
			targetCtx.writeAndFlush(td);
			
		} finally {
			// TODO: handle finally clause
			ReferenceCountUtil.release(msg);
		}
		
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		// TODO Auto-generated method stub
		
		super.userEventTriggered(ctx, evt);
		
		logger.info("server time out");
		
		ctx
		.writeAndFlush(Unpooled.EMPTY_BUFFER)
		.addListener(ChannelFutureListener.CLOSE);
	}
	
	

}
