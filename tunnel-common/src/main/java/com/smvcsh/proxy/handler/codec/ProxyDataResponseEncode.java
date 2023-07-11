package com.smvcsh.proxy.handler.codec;

import com.smvcsh.proxy.handler.ProxyDataMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
public class ProxyDataResponseEncode extends MessageToByteEncoder<ProxyDataMessage> {

	private Logger logger = LoggerFactory.getLogger(ProxyDataResponseEncode.class);
	
	@Override
	protected void encode(ChannelHandlerContext ctx, ProxyDataMessage msg, ByteBuf out) throws Exception {
		// TODO Auto-generated method stub
		
		
		try {
			
			String header = msg.dataHeader();
			
			out.writeByte('-');
			out.writeCharSequence(header, Charset.defaultCharset());
			out.writeBytes(msg.getData());
			out.writeByte('-');
		} catch (Exception e) {
			// TODO: handle exception
			
			logger.error("encode error", e);
			
			ctx
			.writeAndFlush(Unpooled.EMPTY_BUFFER)
			.addListener(ChannelFutureListener.CLOSE);
		}
	}

}
