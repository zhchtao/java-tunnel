package com.smvcsh.proxy.handler;

import org.apache.commons.lang3.StringUtils;

import com.smvcsh.base.exception.ProjectException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
/**
 * -(999999)99999|source|target|host|port|op-
 * @author taotao
 *
 */
public class ProxyDataMessage {
	
	private final static char DATA_LINE = '-';
	private final static byte HEAD_BYTE_LENGTH=5;
	private final static byte BYTE_LENGTH=6;
	/**
	 * 真实数据长度
	 */
	private int dataLength;
	
	private String host = StringUtils.EMPTY;
	private int port = 0;
	
	private String source = StringUtils.EMPTY;
	private String target = StringUtils.EMPTY;
	/**
	 * -1 close connect
	 * 0 connect
	 * 1 data
	 * 2 check
	 */
	private int operateCode;

	private byte[] data = new byte[0];
	
	public ProxyDataMessage() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ProxyDataMessage(ByteBuf msg) {
		super();
		// TODO Auto-generated constructor stub
		
		/**
		 * 数据分割线
		 */
		if(msg.readByte() != DATA_LINE) {
			throw new ProjectException();
		}
		
		byte[] lengthByte = new byte[BYTE_LENGTH];
		msg.readBytes(lengthByte);
		
		int length = Integer
				.parseInt(
						new String(
								lengthByte
								)
						)
				;
		
		lengthByte = new byte[HEAD_BYTE_LENGTH];
		
//		msg.readByte();
		
		msg.readBytes(lengthByte);
		
		int headLegth = Integer
				.parseInt(
						new String(
								lengthByte
								)
						)
				;
		
		byte[] header = new byte[headLegth];
		
		msg.readBytes(header);
		
		String[] headers = new String(header).trim().split("\\|");
		/**
		 * 数据的长度：总长度-头信息长度-数据分割线长度
		 */
		this.dataLength = length - headLegth - BYTE_LENGTH - HEAD_BYTE_LENGTH - 2;
		
		this.source = headers[1];
		this.target = headers[2];
		this.host = headers[3];
		this.port = Integer.parseInt(headers[4]);
		this.operateCode = Integer.parseInt(headers[5]);
		
		data = new byte[this.dataLength];
		
		msg.readBytes(data);
		/**
		 * 数据分割线
		 */
		if(msg.readByte() != DATA_LINE) {
			throw new ProjectException();
		}
		
		
	}

	public static boolean isReadable(ByteBuf msg) {
		// TODO Auto-generated method stub
		
		if(!msg.isReadable()) {
			
			return false;
		}
		
		if((char)ByteBufUtil.getBytes(msg, 0, 1)[0] != DATA_LINE) {
			throw new ProjectException("data error!");
		}
		
		// +2 添加数据分割线占用字符
		if(msg.readableBytes() < BYTE_LENGTH + 2) {
			
			return false;
		}
		
		int length = Integer
		.parseInt(
				new String(
						ByteBufUtil.getBytes(msg, msg.readerIndex() + 1, BYTE_LENGTH)
						)
				)
		;
		
		if(msg.readableBytes() < length) {
			
			return false;
		}
		
		return true;
	}
	
	public static ProxyDataMessage parse(ByteBuf in) {
		// TODO Auto-generated method stub
		return new ProxyDataMessage(in);
	}
	
	public String dataHeader(){
		StringBuilder sb = new StringBuilder();
		sb
		.append("|")
		.append(this.source)
		.append("|")
		.append(this.target)
		.append("|")
		.append(this.host)
		.append("|")
		.append(this.port)
		.append("|")
		.append(this.operateCode)
		;
		int length = BYTE_LENGTH + HEAD_BYTE_LENGTH+ sb.length() +this.data.length +2;
		int headLength = sb.length();
		
		sb.insert(0, String.format("%0" + BYTE_LENGTH + "d%0"+ HEAD_BYTE_LENGTH + "d", length, headLength));
		
		return sb.toString();
	}

	public int getDataLength() {
		return dataLength;
	}

	public void setDataLength(int dataLength) {
		this.dataLength = dataLength;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public int getOperateCode() {
		return operateCode;
	}

	public void setOperateCode(int operateCode) {
		this.operateCode = operateCode;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
	
	
	
}
