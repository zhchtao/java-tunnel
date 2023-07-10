package com.smvcsh.proxy.manager;

import com.smvcsh.proxy.client.ProxyTcpBusClient;
import io.netty.channel.ChannelFuture;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ClientChannelManager extends ChannelManager {
	
	@Resource
	private ProxyTcpBusClient busClient;

	public ChannelFuture busConnect(String host, int port) {
		// TODO Auto-generated method stub
		return busClient.connect(host, port);
	}
	
}
