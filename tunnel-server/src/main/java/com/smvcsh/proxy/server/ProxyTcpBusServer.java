package com.smvcsh.proxy.server;

import com.smvcsh.proxy.server.channel.ProxyServerBusChannelHandlerAdapter;
import io.netty.channel.ChannelHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
@Component
public class ProxyTcpBusServer extends ProxyTcpServer {

	@Resource
	private ProxyServerBusChannelHandlerAdapter handler;
	
	@Override
	protected ChannelHandler[] addLast() {
		// TODO Auto-generated method stub
		return new ChannelHandler[] {handler};
	}

}
