package com.zhiquanyeo.ftl.ansible.protocol.packet;

public class ServerResponsePacket extends ProtocolPacket {
	
	// Static Generator
	public ServerResponsePacket generateFromBuffer(byte[] buf, int len) {
		return null;
	}
	
	private int d_mrsp;
	private int d_seq;
	private int d_dataLen;
	
	public ServerResponsePacket(int MRSP, int SEQ, int DLEN) {
		this.d_type = "ServerResponse";
		this.d_headerSize = 5;
		this.d_minPacketSize = 6;
		
		this.d_mrsp = MRSP;
		this.d_seq = SEQ;
		this.d_dataLen = DLEN;
	}
	
	public int getMRSP() {
		return this.d_mrsp;
	}
	
	public int getSEQ() {
		return this.d_seq;
	}
	
	public int getDLEN() {
		return this.d_dataLen;
	}
}
