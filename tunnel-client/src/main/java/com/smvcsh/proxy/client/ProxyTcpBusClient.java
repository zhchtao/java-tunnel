package com.smvcsh.proxy.client;

import com.smvcsh.proxy.client.channel.ProxyClientBusChannelHandlerAdapter;
import com.smvcsh.proxy.manager.ClientChannelManager;
import io.netty.channel.ChannelHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
//@Component
public class ProxyTcpBusClient extends ProxyTcpClient {
	
//	@Resource
	private ProxyClientBusChannelHandlerAdapter channel;

	public ProxyTcpBusClient(ClientChannelManager manager) {
		this.channel = new ProxyClientBusChannelHandlerAdapter(manager);
	}

	@Override
	protected ChannelHandler[] addLast() {
		// TODO Auto-generated method stub
		return new ChannelHandler[] {channel};
	}

}
