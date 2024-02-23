package com.smvcsh.proxy.client;

import com.smvcsh.proxy.client.channel.ProxyClientTunnelChannelHandlerAdapter;
import com.smvcsh.proxy.handler.codec.ProxyDataRequestDecode;
import com.smvcsh.proxy.handler.codec.ProxyDataResponseEncode;
import com.smvcsh.proxy.manager.ClientChannelManager;
import io.netty.channel.ChannelHandler;
import io.netty.handler.timeout.IdleStateHandler;

//@Component
public class ProxyTcpTunnelClient extends ProxyTcpClient {
	
//	@Resource
	private ProxyClientTunnelChannelHandlerAdapter channel;

	public ProxyTcpTunnelClient(ClientChannelManager manager) {
		channel = new ProxyClientTunnelChannelHandlerAdapter(manager);
	}

	@Override
	protected ChannelHandler[] addLast() {
		// TODO Auto-generated method stub
		return new ChannelHandler[] {new IdleStateHandler(20, 0, 0), new ProxyDataResponseEncode(), new ProxyDataRequestDecode(), channel};
	}

}
