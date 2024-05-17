package com.smvcsh.proxy.manager;

import com.smvcsh.proxy.handler.ProxyDataMessage;
import com.smvcsh.proxy.handler.constants.ProxyTunnelMessageConstants;
import com.smvcsh.proxy.manager.relation.IpRelation;
import com.smvcsh.proxy.server.ProxyTcpBusServer;
import com.smvcsh.proxy.server.ProxyTcpTunnelServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Map;

@Component
public class ManagerInit {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Resource
	private ChannelManager serverChannelManager;

	@Resource
	private ProxyTcpTunnelServer proxyTcpTunnelServer;
	@Resource
	private ProxyTcpBusServer proxyTcpBusServer;
	
	@Resource
	private Map<Integer, IpRelation> ipRelationMap;
	
	@Value("${proxy.data.server.port:10000}")
	private int dataServerPort;
	@Value("${proxy.data.server.host:127.0.0.1}")
	private String dataServerHost;

	@PostConstruct
	private void init() {
		new Thread(() -> initServer()).start();
	}

	private void initServer() {
		try {
			logger.info("start proxy tunnel server with {} ......", dataServerPort);
			proxyTcpTunnelServer.start(dataServerPort);
			logger.info("start proxy tunnel server complete!");
			
			for(IpRelation relation : ipRelationMap.values()) {
				logger.info("start proxy bus server with {} ......", relation.getPort());
				proxyTcpBusServer.start(relation.getPort());
			}
			logger.info("start proxy bus server complete!");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			System.exit(0);
		}
		
	}
	
	@Scheduled(cron = "0/10 * * * * ?")
	private void healthCheck() {
		try {
			
			ProxyDataMessage msg = new ProxyDataMessage();
			msg.setOperateCode(ProxyTunnelMessageConstants.OPERATE_CODE.CONNECT_CHECK);

			logger.info("server check {}", serverChannelManager.tunnelCtxSize());

			if(serverChannelManager.tunnelCtxSize() > 0) {
				serverChannelManager.proxyChannlCtx().writeAndFlush(msg);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
	}

}
