package com.zhiquanyeo.ftl.ansible.protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * Async Events from the server
 * @author zhiquan
 *
 */
public class ProtocolEvents {
	
	public static class AnsibleServerEvent {
		public String name;
		// TODO more?
		
		public AnsibleServerEvent(String n) {
			this.name = n;
		}
	}
	
	private static Map<Integer, AnsibleServerEvent> s_eventMap = new HashMap<>();
	static {
		s_eventMap.put(0x01, new AnsibleServerEvent("ASYNC:POWER_NOTIFICATION"));
	}
	
	public static AnsibleServerEvent lookupByBytecode(int ID_CODE) {
		return s_eventMap.get(ID_CODE);
	}
}
