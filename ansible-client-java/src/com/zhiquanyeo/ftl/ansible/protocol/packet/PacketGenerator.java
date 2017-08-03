package com.zhiquanyeo.ftl.ansible.protocol.packet;

import java.util.ArrayList;

public class PacketGenerator {
	public static class PacketGeneratorResult {
		private ArrayList<ProtocolPacket> d_packets;
		private boolean d_success = false;
		private int d_bytesConsumed = 0;
		
		public PacketGeneratorResult() {
			d_packets = new ArrayList<>();
		}
		
		public void setSuccess(boolean val) {
			d_success = val;
		}
		
		public boolean getSuccess() {
			return d_success;
		}
		
		public void setBytesConsumed(int val) {
			d_bytesConsumed = val;
		}
		
		public int getBytesConsumed() {
			return d_bytesConsumed;
		}
		
		public void addPacket(ProtocolPacket pkt) {
			d_packets.add(pkt);
		}
	}
	
	public static PacketGeneratorResult processBuffer(ArrayList<Byte> buffer) {
		PacketGeneratorResult result = new PacketGeneratorResult();
		
		if (buffer.size() >= 6) { // Ensure minimum packet size
			// find a good place to start processing
			
		}
		
		return result;
	}
}
