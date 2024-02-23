package com.smvcsh.proxy.manager;

import com.smvcsh.proxy.handler.ProxyDataMessage;
import com.smvcsh.proxy.handler.constants.ProxyTunnelMessageConstants;
import com.smvcsh.proxy.manager.relation.IpRelation;
import com.smvcsh.proxy.server.ProxyTcpBusServer;
import com.smvcsh.proxy.server.ProxyTcpTunnelServer;
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
	private ProxyTcpTunnelServer proxyTcpDataServer;
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

				initServer();
			}
		}).start();
		
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
			msg.setOperateCode(ProxyTunnelMessageConstants.OPERATE_CODE.CONNECT_CHECK);

			logger.info("server check {}", serverChannelManager.proxyChannlCtxSize());

			if(serverChannelManager.proxyChannlCtxSize() > 0) {
				serverChannelManager.proxyChannlCtx().writeAndFlush(msg);
			}
		} catch (Exception e) {
			// TODO: handle exception
			logger.error(e.getMessage(), e);
		}
		
	}

}
