package com.zhiquanyeo.ftl.ansible.protocol.packet;

import java.util.HashMap;
import java.util.Map;

public abstract class ProtocolPacket {
	
	public static class FIELDS {
		public static int size = 5;
		public static class sop1 {
			public static int pos = 0;
			public static byte hex = (byte)0xFF;
		};
		public static class sop2 {
			public static int pos = 1;
			public static byte sync = (byte)0xFF;
			public static byte async = (byte)0xFE;
		};
		public static byte mrspHex = 0x00;
		public static byte seqHex = 0x00;
		public static int mrspIdCode = 2;
		public static int seqMsb = 3;
		public static int dlenLsb = 4;
		public static int checksum = 5;
	}
	
	public static enum PacketType {
		SERVER_RESPONSE,
		SERVER_ASYNC,
		INVALID
	};
	
	public static PacketType identifyServerPacket(byte[] data) {
		if (data.length < 6) {
			return PacketType.INVALID;
		}
		
		if (data[0] == 0xFF && data[1] == 0xFF) {
			return PacketType.SERVER_RESPONSE;
		}
		else if (data[0] == 0xFF && data[1] == 0xFE) {
			return PacketType.SERVER_ASYNC;
		}
		return PacketType.INVALID;
	}
	
	protected String d_type;
	protected int d_minPacketSize;
	protected int d_headerSize;
	
	protected Byte[] d_data;
	
	protected int d_checksum;
	
	public String getType() {
		return this.d_type;
	}
	
	public int getMinPacketSize() {
		return this.d_minPacketSize;
	}
	
	public int getHeaderSize() {
		return this.d_headerSize;
	}
	
	public Byte[] getDATA() {
		return this.d_data;
	}
	
	public void setDATA(Byte[] data) {
		this.d_data = data;
	}
	
	public int getChecksum() {
		return this.d_checksum;
	}
	
	public void setChecksum(int val) {
		this.d_checksum = val;
	}
}
