package com.zhiquanyeo.ftl.ansible.protocol;

import com.zhiquanyeo.ftl.ansible.protocol.ProtocolCommands.ClientCommand;
import com.zhiquanyeo.ftl.ansible.protocol.ProtocolCommands.CommandParam;

public class ProtocolValidator {
	public static final int MIN_SERVER_PACKET_SIZE = 6;
	public static final int MIN_CLIENT_PACKET_SIZE = 7;
	
	private static void verifyIntegerParam(String type, Object value) {
		Integer test = (Integer)value;
	}
	
	public static void validateCommand(String command, Object[] args) throws CommandValidationException {
		ClientCommand cmd = ProtocolCommands.lookupByCommand(command);
		if (cmd == null) {
			throw new CommandValidationException(CommandValidationException.INVALID_COMMAND, "'" + command + "' is not a valid command.");
		}
		
		if (cmd.params == null) {
			return;
		}
		
		if (cmd.params.size() > 0 && args.length < cmd.params.size()) {
			throw new CommandValidationException(CommandValidationException.INCORRECT_NUM_PARAMS,
					"Expected " + cmd.params.size() + " but got " + args.length);
		}
		
		// Look through the params and see if we can coerce to the correct form
		for (int i = 0; i < cmd.params.size(); i++) {
			CommandParam currParam = cmd.params.get(i);
			try {
				switch (currParam.type) {
					case "uint8":
					case "int8":
					case "uint16":
					case "int16":
						verifyIntegerParam(currParam.type, args[i]);
						break;
					
				}
			}
			catch (ClassCastException e) {
				throw new CommandValidationException(CommandValidationException.INVALID_PARAMETER,
						"Argument " + i + " (" + currParam.name + ") has invalid type. Expected " + currParam.type);
			}
		}
	}
}
