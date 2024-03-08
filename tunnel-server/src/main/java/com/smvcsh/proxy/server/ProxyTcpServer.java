package com.smvcsh.proxy.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ProxyTcpServer {
	
	private final EventLoopGroup bossGroup;
	private final EventLoopGroup workGroup;
	private final ServerBootstrap bootstrap = new ServerBootstrap();
	private ChannelFuture channelFuture;
	
	private final ProxyTcpServer server = this;

	public ProxyTcpServer() {
		super();
		// TODO Auto-generated constructor stub
		
		bossGroup = this.bossGroup();
		workGroup = this.workGroup();
		
		bootstrap
		.group(bossGroup, workGroup)
		.channel(NioServerSocketChannel.class)
		.childHandler(new ChannelInitializer<SocketChannel>() {

			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				// TODO Auto-generated method stub
				ch.pipeline().addLast(server.addLast());
			}
		})
		.option(ChannelOption.SO_BACKLOG, 128)
		.childOption(ChannelOption.SO_KEEPALIVE, true)
		;
		
	}
	
	private EventLoopGroup workGroup() {
		// TODO Auto-generated method stub
		return new NioEventLoopGroup(4);
	}

	private EventLoopGroup bossGroup() {
		// TODO Auto-generated method stub
		return new NioEventLoopGroup(2);
	}

	protected abstract ChannelHandler[] addLast();

	public void start(int port) {
		try {
			channelFuture = bootstrap.bind(port).sync();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage(), e);
			Thread.currentThread().interrupt();
		}

    }
	public void stop() {
		try {
			channelFuture.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			workGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
		
	}
	
}
