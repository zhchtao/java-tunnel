package com.smvcsh.proxy.server;

import com.smvcsh.proxy.handler.codec.ProxyDataRequestDecode;
import com.smvcsh.proxy.handler.codec.ProxyDataResponseEncode;
import com.smvcsh.proxy.server.channel.ProxyServerTunnelChannelHandlerAdapter;
import io.netty.channel.ChannelHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
@Component
public class ProxyTcpTunnelServer extends ProxyTcpServer {

	@Resource
	private ProxyServerTunnelChannelHandlerAdapter handler;
	
	@Override
	protected ChannelHandler[] addLast() {
		// TODO Auto-generated method stub
		return new ChannelHandler[] {new IdleStateHandler(20, 0, 0), new ProxyDataResponseEncode(), new ProxyDataRequestDecode(), handler};
	}

}
