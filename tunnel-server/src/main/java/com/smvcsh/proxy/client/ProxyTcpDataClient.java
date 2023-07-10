package com.smvcsh.proxy.client;

import com.smvcsh.proxy.client.channel.ProxyClientDataChannelHandlerAdapter;
import com.smvcsh.proxy.handler.codec.ProxyDataRequestDecode;
import com.smvcsh.proxy.handler.codec.ProxyDataResponseEncode;
import io.netty.channel.ChannelHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
@Component
public class ProxyTcpDataClient extends ProxyTcpClient {
	
	@Resource
	private ProxyClientDataChannelHandlerAdapter channel;
	
	@Override
	protected ChannelHandler[] addLast() {
		// TODO Auto-generated method stub
		return new ChannelHandler[] {new IdleStateHandler(20, 0, 0), new ProxyDataResponseEncode(), new ProxyDataRequestDecode(), channel};
	}

}
