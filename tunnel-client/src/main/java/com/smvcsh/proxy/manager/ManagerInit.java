package com.smvcsh.proxy.manager;

import com.smvcsh.proxy.client.ProxyTcpClient;
import com.smvcsh.proxy.handler.ProxyDataMessage;
import com.smvcsh.proxy.handler.constants.ProxyDataMessageConstants;
import com.smvcsh.proxy.manager.relation.IpRelation;
import io.netty.channel.ChannelFuture;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ManagerInit implements ApplicationRunner {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Resource
	private Map<Integer, IpRelation> ipRelationMap;
	
	@Value("${proxy.data.server:192.168.1.100:10000}")
	private List<String> dataServer;
	private List<ClientChannelManager> cmList;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		cmList = dataServer.stream()
				.map(dataServer -> dataServer.split(":"))
				.filter(dataServer -> ArrayUtils.getLength(dataServer) == 2)
				.map(dataServer -> new ClientChannelManager(dataServer[0], Integer.parseInt(dataServer[1])))
				.collect(Collectors.toList());
		// TODO Auto-generated method stub
		cmList.forEach(manager -> new Thread(() -> {
			initClient(manager);
		}).start());

	}
	public void initClient(ClientChannelManager manager) {
		// TODO Auto-generated method stub
		
		synchronized (manager) {
			
			if(manager.isStarting()) {
				
				return;
			}

			manager.setStarting(true);
		}
		
		ChannelFuture f = null;
		
		long step = 0;
		
		do {
			
			try {
				
				logger.info("step:{}", step);
				
				if(step ++ > 0){
					
					Thread.sleep(5000);
				}
				
				ProxyTcpClient client = manager.getDataClient();
				
				logger.info("start proxy client");

				f = client.connect(manager.getHost(), manager.getPort());

				logger.info(f.channel().toString());

				logger.info("start proxy client complete!");
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage(), e);
			}
			
		} while (f == null || !f.isSuccess());
		manager.setStarting(false);
	}


	@Scheduled(cron = "0/10 * * * * ?")
	private void healthCheck() {
		ProxyDataMessage msg = new ProxyDataMessage();
		msg.setOperateCode(ProxyDataMessageConstants.OPERATE_CODE.CONNECT_CHECK);
		cmList.parallelStream().forEach(manager -> {

			try {

				logger.info("client check {}", manager.proxyChannlCtxSize());

				if(manager.proxyChannlCtxSize() > 0) {

					manager.proxyChannlCtx().writeAndFlush(msg);
				}else {

					initClient(manager);
				}
			} catch (Exception e) {
				// TODO: handle exception
				logger.error(e.getMessage(), e);
			}

		});

		
	}

}
