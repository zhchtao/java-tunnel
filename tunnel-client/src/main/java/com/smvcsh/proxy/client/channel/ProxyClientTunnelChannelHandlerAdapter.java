package com.smvcsh.proxy.client.channel;

import com.smvcsh.proxy.handler.ProxyDataMessage;
import com.smvcsh.proxy.handler.constants.ProxyTunnelMessageConstants;
import com.smvcsh.proxy.manager.ClientChannelManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
/**
 * @author taotao
 *
 */
@Sharable
//@Component
public class ProxyClientTunnelChannelHandlerAdapter extends ProxyClientChannelHandlerAdapter {

	public ProxyClientTunnelChannelHandlerAdapter(@NonNull ClientChannelManager clientChannelManager) {
		super(clientChannelManager);
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		
		logger.info("tunnel client remove:{}", ctx.channel().id().asShortText());
		
		clientChannelManager.removeTunnelCtx(ctx);
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		logger.info("tunnel client add:{}", ctx.channel().id().asShortText());
		clientChannelManager.addTunnelCtx(ctx);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// TODO Auto-generated method stub
		try {
			
			ProxyDataMessage data = (ProxyDataMessage) msg;
			
			logger.info("tunnel message operate:{}", data.getOperateCode());
			
			if(ProxyTunnelMessageConstants.OPERATE_CODE.CONNECT_CHECK == data.getOperateCode()) {
				return;
			}
			
			String target = data.getTarget();
			String source = data.getSource();
			
			ChannelHandlerContext targetCtx = clientChannelManager.getChannelCtx(StringUtils.isNotBlank(target) ? target: source);

			
			if(ProxyTunnelMessageConstants.OPERATE_CODE.CLOSE_CONNECT == data.getOperateCode()) {
				
				if(null != targetCtx) {
					targetCtx
					.writeAndFlush(Unpooled.EMPTY_BUFFER)
					.addListener(ChannelFutureListener.CLOSE);
				}
				
				return;
			}
			
			if(targetCtx == null) {
				
				ChannelFuture f = clientChannelManager.busConnect(data.getHost(), data.getPort());
				targetCtx = clientChannelManager.getChannelCtx(f.channel().id().asLongText());
				
				clientChannelManager.addChannelCtx(targetCtx, source);
				
				data.setTarget(data.getSource());
				data.setSource(f.channel().id().asLongText());
				
				ctx.writeAndFlush(data);
				
			}
			
			if(ProxyTunnelMessageConstants.OPERATE_CODE.CONNECT == data.getOperateCode()) {
				return;
			}
			
			
			logger.info("server response:{}", targetCtx.channel().id().asShortText());
			
			ByteBuf buf = targetCtx.alloc().buffer(data.getDataLength());
			buf.writeBytes(data.getData());
			
			targetCtx.writeAndFlush(buf);
			
		} finally {
			// TODO: handle finally clause
			ReferenceCountUtil.release(msg);
		}
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		// TODO Auto-generated method stub

		logger.info("tunnel time out");

		ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
				.addListener(ChannelFutureListener.CLOSE);

		super.userEventTriggered(ctx, evt);
	}
	
}
