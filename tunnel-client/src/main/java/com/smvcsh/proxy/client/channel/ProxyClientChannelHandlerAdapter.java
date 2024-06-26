package com.smvcsh.proxy.client.channel;

import com.smvcsh.proxy.manager.ClientChannelManager;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author taotao
 *
 */
@Sharable
@RequiredArgsConstructor
public class ProxyClientChannelHandlerAdapter extends ChannelInboundHandlerAdapter {
	
	protected static Logger logger = LoggerFactory.getLogger(ProxyClientChannelHandlerAdapter.class);
	
	@NonNull
	protected ClientChannelManager clientChannelManager;

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error(ctx.toString(), cause);

		if (ctx.isRemoved()) {
			return;
		}

		try {

			ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
					.addListener(ChannelFutureListener.CLOSE);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			ctx.close();
		}
	}
}
