package com.smvcsh.proxy.manager;

import com.smvcsh.proxy.client.ProxyTcpBusClient;
import com.smvcsh.proxy.client.ProxyTcpDataClient;
import com.smvcsh.proxy.manager.ChannelManager;
import io.netty.channel.ChannelFuture;
import lombok.Data;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

//@Component
@Data
public class ClientChannelManager extends ChannelManager {

	private String host;
	private int port;
	private boolean starting;
	
//	@Resource
	private ProxyTcpBusClient busClient;
	private ProxyTcpDataClient dataClient;

	public ClientChannelManager(String host, int port) {
		this.busClient = new ProxyTcpBusClient(this);
		this.dataClient = new ProxyTcpDataClient(this);
		this.host = host;
		this.port = port;
	}

	public ChannelFuture busConnect(String host, int port) {
		// TODO Auto-generated method stub
		return busClient.connect(host, port);
	}
}
