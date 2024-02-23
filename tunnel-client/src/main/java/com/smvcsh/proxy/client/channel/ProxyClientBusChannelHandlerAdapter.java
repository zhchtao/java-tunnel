package com.smvcsh.proxy.client.channel;

import com.smvcsh.proxy.handler.ProxyDataMessage;
import com.smvcsh.proxy.handler.constants.ProxyTunnelMessageConstants;
import com.smvcsh.proxy.manager.ClientChannelManager;
import com.smvcsh.proxy.manager.channel.ChannelRelation;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import lombok.NonNull;

/**
 * @author taotao
 *
 */
@Sharable
//@Component
public class ProxyClientBusChannelHandlerAdapter extends ProxyClientChannelHandlerAdapter {

	public ProxyClientBusChannelHandlerAdapter(@NonNull ClientChannelManager clientChannelManager) {
		super(clientChannelManager);
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) {
		// TODO Auto-generated method stub
		try {

			ChannelRelation relation = clientChannelManager.getChannelRelation(ctx);

			if(null != relation) {
				ProxyDataMessage proxyData = new ProxyDataMessage();

				proxyData.setOperateCode(ProxyTunnelMessageConstants.OPERATE_CODE.CLOSE_CONNECT);
				proxyData.setSource(ctx.channel().id().asLongText());
				proxyData.setTarget(relation.getRemotChannel());

				clientChannelManager.proxyChannlCtx().writeAndFlush(proxyData);
			}
		} finally {

			clientChannelManager.remove(ctx);

			logger.info("bus client close {}", ctx.channel().id().asShortText());
		}

	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		logger.info("bus client add {}", ctx.channel().id().asShortText());
		clientChannelManager.addChannelCtx(ctx);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// TODO Auto-generated method stub
		
		try {
			
			ByteBuf data = (ByteBuf) msg;
			
			while(data.isReadable()) {
				
				byte[] b = new byte[data.readableBytes()];
				data.readBytes(b);
				
				ChannelRelation channelRelation = clientChannelManager.getChannelRelation(ctx);
				String target = channelRelation.getRemotChannel();
				
				ProxyDataMessage proxyData = new ProxyDataMessage();
				
				proxyData.setOperateCode(ProxyTunnelMessageConstants.OPERATE_CODE.DATA_PROXY);
				proxyData.setData(b);
				proxyData.setSource(ctx.channel().id().asLongText());
				proxyData.setTarget(target);
				
				clientChannelManager.proxyChannlCtx().writeAndFlush(proxyData);
				
			}
			
		} finally {
			// TODO: handle finally clause
			ReferenceCountUtil.release(msg);
		}
	}
	
}
