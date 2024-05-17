package com.smvcsh.proxy.manager;

import com.smvcsh.proxy.client.ProxyTcpClient;
import com.smvcsh.proxy.handler.ProxyDataMessage;
import com.smvcsh.proxy.handler.constants.ProxyTunnelMessageConstants;
import io.netty.channel.ChannelFuture;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Component
public class ManagerInit {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("#{'${proxy.data.server:192.168.1.100:10000}'.split(',')}")
	private List<String> tunnelServerList;
	private List<ClientChannelManager> cmList;
	private ExecutorService executors;
	@PostConstruct
	private void init() {
		executors = Executors.newFixedThreadPool(tunnelServerList.size() + 1);
		cmList = tunnelServerList.stream()
				.map(tunnelServer -> tunnelServer.split(":"))
				.filter(tunnelServer -> ArrayUtils.getLength(tunnelServer) == 2)
				.map(tunnelServerInfo -> new ClientChannelManager(tunnelServerInfo[0], Integer.parseInt(tunnelServerInfo[1])))
				.collect(Collectors.toList());
		cmList.forEach(this::initClient);
	}
	@PreDestroy
	private void destroy() {
		logger.info("executors shutdown......");
		executors.shutdown();
	}
	public void initClient(ClientChannelManager manager) {
		executors.submit(() -> {

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

					logger.info("start tunnel client");

					f = client.connect(manager.getHost(), manager.getPort());

					logger.info("start tunnel client complete!:{}", f.channel());

				} catch (Exception e) {
					// TODO Auto-generated catch block
					logger.error(e.getMessage(), e);
				}

			} while (f == null || !f.isSuccess());
			manager.setStarting(false);
		});
	}


	@Scheduled(cron = "0/10 * * * * ?")
	private void healthCheck() {
		ProxyDataMessage msg = new ProxyDataMessage();
		msg.setOperateCode(ProxyTunnelMessageConstants.OPERATE_CODE.CONNECT_CHECK);
		cmList.parallelStream().forEach(manager -> {

			try {

				logger.info("client check {}", manager.tunnelCtxSize());

				if(manager.tunnelCtxSize() > 0) {

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
