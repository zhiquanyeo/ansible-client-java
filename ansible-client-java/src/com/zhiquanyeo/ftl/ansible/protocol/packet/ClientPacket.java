package com.zhiquanyeo.ftl.ansible.protocol.packet;

public class ClientPacket {
	public Integer DID = null;
	public Integer CID = null;
	public Integer SEQ = null;
	public Byte[] DATA = null;
	public boolean resetTimeout = false;
	public boolean requestAck = false;
	
	public Byte calculateSOP2() {
		int sop2 = 0xFC;
		if (resetTimeout) {
			sop2 |= 0x02;
		}
		if (requestAck) {
			sop2 |= 0x01;
		}
		
		return (byte)sop2;
	}
}
