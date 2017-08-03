package com.zhiquanyeo.ftl.ansible.protocol.packet;

import java.util.HashMap;
import java.util.Map;

public class PacketTemplates {
	public static class PacketTemplate {
		public int minPacketSize;
		public int headerSize;
		public int dLenBytes;
		public int packetStartByte;
		public Map<String, Integer> template;
	}
	
	private static Map<String, PacketTemplate> s_templates;
	
	// Static initializer
	static {
		s_templates = new HashMap<>();
		PacketTemplate clientTemplate = new PacketTemplate();
		clientTemplate.minPacketSize = 7;
		clientTemplate.headerSize = 6;
		clientTemplate.dLenBytes = 1;
		clientTemplate.packetStartByte = 2;
		clientTemplate.template = new HashMap<>();
		clientTemplate.template.put("SOP1", 0);
		clientTemplate.template.put("SOP2", 1);
		clientTemplate.template.put("DID", 2);
		clientTemplate.template.put("CID", 3);
		clientTemplate.template.put("SEQ", 4);
		clientTemplate.template.put("DLEN", 5);
		clientTemplate.template.put("DATA", 6);
		clientTemplate.template.put("CHK", 6);
		
		PacketTemplate serverResponseTemplate = new PacketTemplate();
		serverResponseTemplate.minPacketSize = 6;
		serverResponseTemplate.headerSize = 5;
		serverResponseTemplate.dLenBytes = 1;
		serverResponseTemplate.packetStartByte = 2;
		serverResponseTemplate.template = new HashMap<>();
		serverResponseTemplate.template.put("SOP1", 0);
		serverResponseTemplate.template.put("SOP2", 1);
		serverResponseTemplate.template.put("MRSP", 2);
		serverResponseTemplate.template.put("SEQ", 3);
		serverResponseTemplate.template.put("DLEN", 4);
		serverResponseTemplate.template.put("DATA", 5);
		serverResponseTemplate.template.put("CHK", 5);
		
		PacketTemplate serverAsyncTemplate = new PacketTemplate();
		serverAsyncTemplate.minPacketSize = 6;
		serverAsyncTemplate.headerSize = 5;
		serverAsyncTemplate.dLenBytes = 2;
		serverAsyncTemplate.packetStartByte = 2;
		serverAsyncTemplate.template = new HashMap<>();
		serverAsyncTemplate.template.put("SOP1", 0);
		serverAsyncTemplate.template.put("SOP2", 1);
		serverAsyncTemplate.template.put("ID_CODE", 2);
		serverAsyncTemplate.template.put("DLEN", 3); // 2 bytes long
		serverAsyncTemplate.template.put("DATA", 5);
		serverAsyncTemplate.template.put("CHK", 5);
		
		s_templates.put("Client", clientTemplate);
		s_templates.put("ServerResponse", serverResponseTemplate);
		s_templates.put("ServerAsync", serverAsyncTemplate);
	}
	
	public static PacketTemplate getTemplate(String type) {
		return s_templates.get(type);
	}
}
