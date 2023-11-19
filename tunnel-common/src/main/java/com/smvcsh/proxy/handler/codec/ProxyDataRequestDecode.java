package com.smvcsh.proxy.handler.codec;

import com.smvcsh.proxy.handler.ProxyDataMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
public class ProxyDataRequestDecode extends ByteToMessageDecoder {
	
	private Logger logger = LoggerFactory.getLogger(ProxyDataRequestDecode.class);

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		// TODO Auto-generated method stub
		try {
			while (ProxyDataMessage.isReadable(in)) {
				ProxyDataMessage res = ProxyDataMessage.parse(in);
				out.add(res);
			}
		} catch (Exception e) {
			// TODO: handle exception
			
			logger.error("decode error", e);
			
//			in.clear();
			
			ctx
			.writeAndFlush(Unpooled.EMPTY_BUFFER)
			.addListener(ChannelFutureListener.CLOSE);
//			ctx.close();
		}
	}

}
