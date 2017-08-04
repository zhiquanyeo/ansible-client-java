package com.zhiquanyeo.ftl.ansible.protocol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.zhiquanyeo.ftl.ansible.protocol.packet.ClientPacket;
import com.zhiquanyeo.ftl.ansible.protocol.packet.PacketUtils;
import com.zhiquanyeo.ftl.ansible.protocol.packet.ProtocolPacket;
import com.zhiquanyeo.ftl.ansible.protocol.packet.ServerAsyncPacket;
import com.zhiquanyeo.ftl.ansible.protocol.packet.ServerResponsePacket;

public class AnsiblePacket {
	public static final int MIN_BUFFER_SIZE = 6;
	
	// Packet Constants
	
	
	private ArrayList<Byte> d_partialBuffer = new ArrayList<>();
	
	// TODO Implement
	public ArrayList<Byte> create(ClientPacket packet) {
		ArrayList<Byte> buf = new ArrayList<>();
		
		Byte sop1 = (byte)0xFF;
		Byte sop2 = packet.calculateSOP2();
		Byte DID = (packet.DID == null) ? 0x00 : (byte)((int)packet.DID);
		Byte CID = (packet.CID == null) ? 0x01 : (byte)((int)packet.CID);
		Byte SEQ = (packet.SEQ == null) ? 0x00 : (byte)((int)packet.SEQ);
		Byte[] DATA = (packet.DATA == null) ? new Byte[0] : packet.DATA;
		
		int dLen = DATA.length + 1; // Add 1 for checksum
		int checksum = 0x00;
		
		buf.add(sop1);
		buf.add(sop2);
		buf.add(DID);
		buf.add(CID);
		buf.add(SEQ);
		buf.add((byte)dLen);
		buf.addAll(Arrays.asList(DATA));
		
		checksum = PacketUtils.checksum(buf.subList(2, buf.size()));
		
		buf.add((byte)checksum);
		
		return buf;
	}
	
	public ProtocolPacket parse(Byte[] buffer) {
		List<Byte> bufByteList = Arrays.asList(buffer);
		
		ArrayList<Byte> actingBuffer = new ArrayList<>();
		
		if (this.d_partialBuffer.size() > 0) {
			actingBuffer.addAll(this.d_partialBuffer);
			actingBuffer.addAll(bufByteList);

			this.d_partialBuffer.clear();
		}
		else {
			actingBuffer.addAll(bufByteList);
			this.d_partialBuffer.addAll(bufByteList);
		}
		
		// Perform Checks
		if (this.checkSOPs(actingBuffer)) {
			// Check minimum length
			if (this.checkMinSize(actingBuffer)) {
				if (this.checkExpectedSize(actingBuffer) > -1) {
					return this.parseImpl(actingBuffer);
				}
			}
			
			this.d_partialBuffer.clear();
			this.d_partialBuffer.addAll(actingBuffer);
		}
		
		return null;
	}
	
	
	// Helper Functions
	@SuppressWarnings("unused")
	private ProtocolPacket parseImpl(ArrayList<Byte> buffer) {
		int sop1 = buffer.get(ProtocolPacket.FIELDS.sop1.pos) & 0xFF;
		int sop2 = buffer.get(ProtocolPacket.FIELDS.sop2.pos) & 0xFF;
		int bByte2 = buffer.get(ProtocolPacket.FIELDS.mrspIdCode) & 0xFF;
		int bByte3 = buffer.get(ProtocolPacket.FIELDS.seqMsb) & 0xFF;
		int bByte4 = buffer.get(ProtocolPacket.FIELDS.dlenLsb) & 0xFF;
		
		int dLen = this.extractDlen(buffer);
		
		ProtocolPacket retPkt;
		
		if (buffer.get(ProtocolPacket.FIELDS.sop2.pos) == ProtocolPacket.FIELDS.sop2.sync) {
			retPkt = new ServerResponsePacket(bByte2, bByte3, dLen);		
		}
		else {
			retPkt = new ServerAsyncPacket(bByte2, dLen);
		}
		
		// Create new buffer for data that is dlen - 1 in size
		ArrayList<Byte> dataBuf = new ArrayList<>();
		for (int i = ProtocolPacket.FIELDS.size; i < (ProtocolPacket.FIELDS.size + dLen - 1); i++) {
			dataBuf.add(buffer.get(i));
		}
		
		int checksum = buffer.get(ProtocolPacket.FIELDS.size + dLen - 1);
		
		Byte[] tmp = new Byte[dataBuf.size()];
		dataBuf.toArray(tmp);
		retPkt.setDATA(tmp);
		
		retPkt.setChecksum(checksum);
		
		// TODO Finish implementing
		this.dealWithExtraBytes(buffer);
		return this.verifyChecksum(buffer, retPkt);
	}
	
	private ProtocolPacket verifyChecksum(ArrayList<Byte> buffer, ProtocolPacket packet) {
		ArrayList<Byte> bSlice = new ArrayList<>();
		int dLen = this.extractDlen(buffer);
		for (int i = ProtocolPacket.FIELDS.mrspIdCode; i < ProtocolPacket.FIELDS.checksum + dLen - 1; i++) {
			bSlice.add(buffer.get(i));
		}
		int checksum = PacketUtils.checksum(bSlice);
		
		// If we got an incorrect checksum, cleanup everything
		if ((byte)checksum != packet.getChecksum()) {
			packet = null;
			this.d_partialBuffer.clear();
			System.err.println("Incorrect checksum, packet discarded");
		}
		
		return packet;
	}
	
	private void dealWithExtraBytes(ArrayList<Byte> buffer) {
		// If the packet was parsed successfully, and the buffer and
		// expected size of the buffer are the same, clean up
		// the partialBuffer, otherwise assign extraBytes to partialBuffer
		int expectedSize = this.checkExpectedSize(buffer);
		if (buffer.size() > expectedSize) {
			this.d_partialBuffer.clear();
			for (int i = expectedSize; i < buffer.size(); i++) {
				this.d_partialBuffer.add(buffer.get(i));
			}
		}
		else {
			this.d_partialBuffer.clear();
		}
	}
	
	private boolean checkSOPs(ArrayList<Byte> buffer) {
		if (!this.checkSOP1(buffer)) {
			return false;
		}
		return this.checkSOP2(buffer) != CheckSOP2Result.FAILED;
	}
	
	private boolean checkSOP1(ArrayList<Byte> buffer) {
		return(buffer.get(ProtocolPacket.FIELDS.sop1.pos) == ProtocolPacket.FIELDS.sop1.hex);
	}
	
	private static enum CheckSOP2Result {
		SYNC,
		ASYNC,
		FAILED
	};
	
	private CheckSOP2Result checkSOP2(ArrayList<Byte> buffer) {
		int sop2 = buffer.get(ProtocolPacket.FIELDS.sop2.pos);
		
		if (sop2 == ProtocolPacket.FIELDS.sop2.sync) {
			return CheckSOP2Result.SYNC;
		}
		else if (sop2 == ProtocolPacket.FIELDS.sop2.async) {
			return CheckSOP2Result.ASYNC;
		}

		return CheckSOP2Result.FAILED;
	}
	
	private boolean checkMinSize(ArrayList<Byte> buffer) {
		return buffer.size() >= MIN_BUFFER_SIZE;
	}
	
	private int extractDlen(ArrayList<Byte> buffer) {
		if (buffer.get(ProtocolPacket.FIELDS.sop2.pos) == ProtocolPacket.FIELDS.sop2.sync) {
			return buffer.get(ProtocolPacket.FIELDS.dlenLsb) & 0xFF;
		}
		
		return ((buffer.get(ProtocolPacket.FIELDS.seqMsb) << 8) | buffer.get(ProtocolPacket.FIELDS.dlenLsb)) & 0xFFFF;
	}
	
	private int checkExpectedSize(ArrayList<Byte> buffer) {
		// Size = buffer fields size (SOP1, SOP2, MRSP, SEQ and DLEN) + DLEN
		int expectedSize = ProtocolPacket.FIELDS.size + this.extractDlen(buffer);
		int bufferSize = buffer.size();
		return (bufferSize < expectedSize) ? -1 : expectedSize;
	}
}
