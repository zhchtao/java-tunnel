package com.smvcsh.proxy.manager;

import com.smvcsh.base.exception.ProjectException;
import com.smvcsh.proxy.manager.channel.ChannelRelation;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChannelManager {
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	protected List<ChannelHandlerContext> channelHandlers = new ArrayList<>();
	
	protected Map<String, ChannelRelation> idChannelHandlerMap = new HashMap<>();
	
	private int step = 0;
	/**
	 * list代替queue，避免并发集合为空
	 * @return
	 */
	public ChannelHandlerContext proxyChannlCtx() {
		
		if(channelHandlers.isEmpty()) {
			throw new ProjectException("暂无连接可用！");
		}
		
		step = (step + 1) % channelHandlers.size();
		
		ChannelHandlerContext proxyDataCtx = channelHandlers.get(step);
		
		if(proxyDataCtx.isRemoved()) {
			throw new ProjectException("proxyDataCtx is Unavailable!");
		}
		
		return proxyDataCtx;
	}
	
	public void addTunnelCtx(ChannelHandlerContext ctx) {
		
		channelHandlers.add(ctx);
	}
	
	public void addChannelCtx(ChannelHandlerContext ctx) {
		
		idChannelHandlerMap.put(ctx.channel().id().asLongText(), new ChannelRelation(ctx));
	}
	
	public void addChannelCtx(ChannelHandlerContext ctx, String remoteChannel) {
		
		idChannelHandlerMap.put(ctx.channel().id().asLongText(), new ChannelRelation(ctx, remoteChannel));
		idChannelHandlerMap.put(remoteChannel, idChannelHandlerMap.get(ctx.channel().id().asLongText()));
	}
	
	public ChannelHandlerContext getChannelCtx(String ctxId) {
		// TODO Auto-generated method stub
		ChannelRelation ctx = getChannelRelation(ctxId);
		return ctx == null ? null : ctx.getCtx();
	}
	
	public ChannelRelation getChannelRelation(String ctxId) {
		// TODO Auto-generated method stub
		return idChannelHandlerMap.get(ctxId);
	}
	
	public ChannelRelation getChannelRelation(ChannelHandlerContext ctx) {
		// TODO Auto-generated method stub
		return getChannelRelation(ctx.channel().id().asLongText());
	}

	public void remove(ChannelHandlerContext targetCtx) {
		// TODO Auto-generated method stub
		synchronized(targetCtx) {
			String key = targetCtx.channel().id().asLongText();
			
			if(idChannelHandlerMap.containsKey(key)) {
				ChannelRelation channelRelation = idChannelHandlerMap.remove(key);
				idChannelHandlerMap.remove(channelRelation.getRemotChannel());
			}
		}
	}

	public int proxyChannlCtxSize() {
		// TODO Auto-generated method stub
		return this.channelHandlers.size();
	}

	public void removeTunnelCtx(ChannelHandlerContext ctx) {
		channelHandlers.remove(ctx);
		idChannelHandlerMap.values()
				.forEach(v -> {
					try {

						logger.info("close ctx:{}", v.getCtx());
						v.getCtx().close();
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				});
		idChannelHandlerMap.clear();
	}
}
