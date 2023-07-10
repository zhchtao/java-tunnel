package com.smvcsh.proxy.server;

import com.smvcsh.proxy.handler.codec.ProxyDataRequestDecode;
import com.smvcsh.proxy.handler.codec.ProxyDataResponseEncode;
import com.smvcsh.proxy.server.channel.ProxyServerDataChannelHandlerAdapter;
import io.netty.channel.ChannelHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
@Component
public class ProxyTcpDataServer extends ProxyTcpServer {

	@Resource
	private ProxyServerDataChannelHandlerAdapter handler;
	
	@Override
	protected ChannelHandler[] addLast() {
		// TODO Auto-generated method stub
		return new ChannelHandler[] {new IdleStateHandler(20, 0, 0), new ProxyDataResponseEncode(), new ProxyDataRequestDecode(), handler};
	}

}
