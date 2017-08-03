package com.zhiquanyeo.ftl.ansible.protocol;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.zhiquanyeo.ftl.ansible.protocol.ProtocolCommands.ClientCommand;

public class ProtocolCommands {
	/**
	 * Internal representation of a Client Command Parameter definition
	 * @author zhiquan
	 *
	 */
	public static class CommandParam {
		public String name;
		public int byteOffset;
		public int length;
		public String type;
		
		public CommandParam(String n, int o, int l, String r) {
			this.name = n;
			this.byteOffset = o;
			this.length = l;
			this.type = r;
		}
	}
	
	public static class ClientCommand {
		public String name;
		public int deviceId;
		public int commandId;
		public String returnType;
		public List<CommandParam> params;
		
		public ClientCommand(String n, int d, int c, String r, List<CommandParam> p) {
			this.name = n;
			this.deviceId = d;
			this.commandId = c;
			this.returnType = r;
			this.params = p;
		}
	}
	
	private static Map<String, ClientCommand> s_commands = new HashMap<>();
	private static Map<String, ClientCommand> s_bytecodeCommandMap = new HashMap<>();
	public static final int ProtocolVersionMajor = 1;
	public static final int ProtocolVersionMinor = 0;
	
	public static ClientCommand lookupByCommand(String command) {
		return s_commands.get(command);
	}
	
	public static ClientCommand lookupByBytecode(int DID, int CID) {
		String queryStr = DID + "-" + CID;
		return s_bytecodeCommandMap.get(queryStr);
	}
	
	static {
		// System Level Commands
		s_commands.put("SYS:HBEAT", new ClientCommand("SYS_HBEAT", 0, 3, "uint8", null));
		s_commands.put("SYS:VERS", new ClientCommand("SYS:VERS", 0, 4, "uint16", null));
		s_commands.put("SYS:CLOSE", new ClientCommand("SYS:CLOSE", 0, 5, "void", null));
		
		// Robot Commands
		s_commands.put("ROBOT:GET_DIGITAL", new ClientCommand("ROBOT:GET_DIGITAL", 1, 1, "uint8",
					Arrays.asList(new CommandParam("port", 0, 1, "uint8"))));
		s_commands.put("ROBOT:GET_ANALOG", new ClientCommand("ROBOT:GET_ANALOG", 1, 2, "uint16",
				Arrays.asList(new CommandParam("port", 0, 1, "uint8"))));
		s_commands.put("ROBOT:SET_DIGITAL", new ClientCommand("ROBOT:SET_DIGITAL", 1, 3, "void",
				Arrays.asList(new CommandParam("port", 0, 1, "uint8"),
							  new CommandParam("value", 1, 1, "uint8"))));
		s_commands.put("ROBOT:SET_ANALOG", new ClientCommand("ROBOT:SET_ANALOG", 1, 4, "void",
				Arrays.asList(new CommandParam("port", 0, 1, "uint8"),
							  new CommandParam("value", 1, 2, "uint16"))));
		s_commands.put("ROBOT:SET_PWM", new ClientCommand("ROBOT:SET_PWM", 1, 5, "void",
				Arrays.asList(new CommandParam("port", 0, 1, "uint8"),
							  new CommandParam("value", 1, 2, "int16"))));
		
		// Populate the reverse lookup
		Iterator<Entry<String, ClientCommand>> it = s_commands.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, ClientCommand> pair = it.next();
			ClientCommand cmd = pair.getValue();
			String byteStr = cmd.deviceId + "-" + cmd.commandId;
			s_bytecodeCommandMap.put(byteStr, pair.getValue());
		}
	}
}


