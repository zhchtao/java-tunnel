package com.smvcsh.proxy.client;

import com.smvcsh.base.exception.ProjectException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ProxyTcpClient {
	
	private ProxyTcpClient client = this;
	
	private static Logger logger = LoggerFactory.getLogger(ProxyTcpClient.class);
	
	private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(2);
	private Bootstrap b = new Bootstrap();
	
	protected ProxyTcpClient() {
		super();
		// TODO Auto-generated constructor stub
		
		b.group(eventLoopGroup).channel(NioSocketChannel.class).option(ChannelOption.SO_KEEPALIVE, true).handler(new ChannelInitializer<SocketChannel>() {
			
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				// TODO Auto-generated method stub
				ch.pipeline().addLast(client.addLast());
			}
		});
	}
	
	protected abstract ChannelHandler[] addLast();
	
	public ChannelFuture connect(String host, int port) {
		try {
			return b.connect(host, port).sync();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage(), e);
			throw new ProjectException(e);
		}
		
	}
	public void stop() {
		try {
//			f.channel().closeFuture().sync();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			eventLoopGroup.shutdownGracefully();
		}
	}
	
}
