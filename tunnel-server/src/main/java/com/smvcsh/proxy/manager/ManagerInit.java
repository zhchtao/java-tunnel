package com.smvcsh.proxy.manager;

import com.smvcsh.proxy.client.ProxyTcpClient;
import com.smvcsh.proxy.client.ProxyTcpDataClient;
import com.smvcsh.proxy.handler.ProxyDataMessage;
import com.smvcsh.proxy.handler.constants.ProxyDataMessageConstants;
import com.smvcsh.proxy.manager.relation.IpRelation;
import com.smvcsh.proxy.server.ProxyTcpBusServer;
import com.smvcsh.proxy.server.ProxyTcpDataServer;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Component
public class ManagerInit implements ApplicationRunner {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Resource
	private ChannelManager serverChannelManager;
	
	@Resource
	private ChannelManager clientChannelManager;
	
	@Resource
	private ProxyTcpDataClient proxyTcpDataClient;
	@Resource
	private ProxyTcpDataServer proxyTcpDataServer;
	@Resource
	private ProxyTcpBusServer proxyTcpBusServer;
	
	@Resource
	private Map<Integer, IpRelation> ipRelationMap;
	
	@Value("${proxy.data.server.port:10000}")
	private int dataServerPort;
	@Value("${proxy.data.server.host:127.0.0.1}")
	private String dataServerHost;
	
	@Value("${proxy.data.server.type:test}")
//	@Value("${proxy.data.server.type:server}")
//	@Value("${proxy.data.server.type:client}")
	private String serverType;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		// TODO Auto-generated method stub
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				switch (serverType) {
				
				case "server":
					initServer();
					break;
				case "client":
					initClient();
					
					break;
				case "test":
					initServer();
					initClient();
					
					break;
					
				default:
					break;
				}
				
			}
		}).start();
		
	}

	/**
	 * 是否正在启动
	 */
	private boolean isStarting = false;
	
	public void initClient() {
		// TODO Auto-generated method stub
		
		synchronized (clientChannelManager) {
			
			if(isStarting) {
				
				return;
			}
			
			isStarting = true;
		}
		
		ChannelFuture f = null;
		
		long step = 0;
		
		do {
			
			try {
				
				logger.info("step:{}", step);
				
				if(step ++ > 0){
					
					Thread.sleep(5000);
				}
				
				ProxyTcpClient client = proxyTcpDataClient;
				
				logger.info("start proxy client");
				
				f = client.connect(dataServerHost, dataServerPort);
				
				logger.info(f.channel().toString());
				
				logger.info("start proxy client complete!");
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage(), e);
			}
			
		} while (f == null || !f.isSuccess());
		
		isStarting = false;
	}

	private void initServer() {
		// TODO Auto-generated method stub
		try {
			logger.info("start proxy data server with {}......", dataServerPort);
			proxyTcpDataServer.start(dataServerPort);
			logger.info("start proxy data server complete!");
			
			for(IpRelation relation : ipRelationMap.values()) {
				logger.info("start proxy bus server with {}......", relation.getPort());
				proxyTcpBusServer.start(relation.getPort());
			}
			logger.info("start proxy bus server complete!");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage(), e);
			System.exit(0);
		}
		
	}
	
	@Scheduled(cron = "0/10 * * * * ?")
	private void healthCheck() {
		try {
			
			ProxyDataMessage msg = new ProxyDataMessage();
			msg.setOperateCode(ProxyDataMessageConstants.OPERATE_CODE.CONNECT_CHECK);
			
			switch (this.serverType) {
			
			case "server":
				
				logger.info("server check {}", serverChannelManager.proxyChannlCtxSize());
				
				if(serverChannelManager.proxyChannlCtxSize() > 0) {
					serverChannelManager.proxyChannlCtx().writeAndFlush(msg);
				}
				
				break;
			case "client":
				
				logger.info("client check {}", clientChannelManager.proxyChannlCtxSize());
				
				if(clientChannelManager.proxyChannlCtxSize() > 0) {
					
					clientChannelManager.proxyChannlCtx().writeAndFlush(msg);
				}else {
					
					initClient();
				}
				
				break;

			default:
				
				break;
			}
		} catch (Exception e) {
			// TODO: handle exception
			logger.error(e.getMessage(), e);
		}
		
	}

}
