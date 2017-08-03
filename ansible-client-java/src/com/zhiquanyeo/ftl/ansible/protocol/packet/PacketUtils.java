package com.zhiquanyeo.ftl.ansible.protocol.packet;

import java.util.List;

public class PacketUtils {
	public static int checksum(List<Byte> buffer) {
		int value = 0x00;
		for (int i = 0; i < buffer.size(); i++) {
			value += (short)buffer.get(i);
		}
		return (value % 256) ^ 0xFF;
	}
}
