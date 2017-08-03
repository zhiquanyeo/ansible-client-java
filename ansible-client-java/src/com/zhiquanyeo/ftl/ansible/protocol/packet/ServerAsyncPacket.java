package com.zhiquanyeo.ftl.ansible.protocol.packet;

public class ServerAsyncPacket extends ProtocolPacket {
	private int d_idCode;
	private int d_dataLen;
	
	public ServerAsyncPacket(int ID_CODE, int DLEN) {
		this.d_idCode = ID_CODE;
		this.d_dataLen = DLEN;
	}
	
	public int getID_CODE() {
		return this.d_idCode;
	}
	
	public int getDLEN() {
		return this.d_dataLen;
	}
}
