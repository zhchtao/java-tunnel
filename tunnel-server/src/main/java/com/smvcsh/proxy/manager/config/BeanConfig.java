package com.smvcsh.proxy.manager.config;

import com.smvcsh.proxy.manager.relation.IpRelation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class BeanConfig {

	@Bean 
	public Map<Integer, IpRelation> ipRelationMap(@Value("${proxy.bus.server.list:3306:192.168.6.63:3306,8300:192.168.6.63:8300}") String proxyConfig){
		
		Map<Integer, IpRelation> relationMap = new HashMap<>();
		
		for(String proxyConf : proxyConfig.split(",")) {
			
			String[] config = proxyConf.split(":");
			
			IpRelation relation = new IpRelation();
			
			relation.setPort(Integer.parseInt(config[0]));
			relation.setRemotHost(config[1]);
			relation.setRemotPort(Integer.parseInt(config[2]));
			
			relationMap.put(relation.getPort(), relation);
		}
		return relationMap;
	}
}
