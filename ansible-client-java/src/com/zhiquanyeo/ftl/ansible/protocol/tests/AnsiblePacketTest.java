package com.zhiquanyeo.ftl.ansible.protocol.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import com.zhiquanyeo.ftl.ansible.protocol.AnsiblePacket;
import com.zhiquanyeo.ftl.ansible.protocol.packet.ClientPacket;
import com.zhiquanyeo.ftl.ansible.protocol.packet.ProtocolPacket;
import com.zhiquanyeo.ftl.ansible.protocol.packet.ServerAsyncPacket;
import com.zhiquanyeo.ftl.ansible.protocol.packet.ServerResponsePacket;

public class AnsiblePacketTest {
	
	@Test
	public void createClientPacket() {
		ClientPacket pkt = new ClientPacket();
		pkt.DID = 0;
		pkt.CID = 2;
		pkt.SEQ = 4;
		
		Byte[] data = { 0x01, 0x02, 0x03, 0x04 };
		pkt.DATA = data;
		pkt.resetTimeout = true;
		
		Byte[] expected = {(byte)0xFF, (byte)0xFE, 0x00, 0x02, 0x04, 0x05, 0x01, 0x02, 0x03, 0x04, (byte)0xEA};
		
		AnsiblePacket packet = new AnsiblePacket();
		ArrayList<Byte> output = packet.create(pkt);
		Byte[] outputArr = new Byte[output.size()];
		output.toArray(outputArr);
		
		assertArrayEquals(outputArr, expected);
	}

	@Test
	public void decodeServerResponsePacket() {
		Byte[] testBuffer = { (byte)0xFF, (byte)0xFF, 0x00, 0x04, 0x05, 0x01, 0x02, 0x03, 0x04, (byte)0xEC };
		
		Byte[] testData = {0x01, 0x02, 0x03, 0x04};
		AnsiblePacket packet = new AnsiblePacket();
		ServerResponsePacket pkt = (ServerResponsePacket)packet.parse(testBuffer);
		assertNotEquals(pkt, null);
		assertEquals(pkt.getMRSP(), 0);
		assertEquals(pkt.getSEQ(), 4);
		assertArrayEquals(pkt.getDATA(), testData);
	}

	@Test
	public void decodeServerAsyncPacket() {
		Byte[] testBuffer = { (byte)0xFF, (byte)0xFE, 0x06, 0x00, 0x05, 0x01, 0x02, 0x03, 0x04, (byte)0xEA };
		Byte[] testData = { 0x01, 0x02, 0x03, 0x04};
		
		AnsiblePacket packet = new AnsiblePacket();
		ServerAsyncPacket pkt = (ServerAsyncPacket)packet.parse(testBuffer);
		assertNotEquals(pkt, null);
		assertEquals(pkt.getID_CODE(), 0x06);
		assertArrayEquals(pkt.getDATA(), testData);
	}
	
	@Test
	public void handlePartialPacketBuffer() {
		Byte[] testBuf1 = { (byte)0xFf, (byte)0xFF, 0x00, 0x04 };
		Byte[] testBuf2 = { 0x05, 0x01, 0x02, 0x03, 0x04, (byte)0xEC };
		
		Byte[] testData = { 0x01, 0x02, 0x03, 0x04 };
		
		AnsiblePacket packet = new AnsiblePacket();
		ProtocolPacket pkt = packet.parse(testBuf1);
		assertEquals(pkt, null);
		pkt = packet.parse(testBuf2);
		assertNotEquals(pkt, null);
		
		assertArrayEquals(pkt.getDATA(), testData);
	}
}
