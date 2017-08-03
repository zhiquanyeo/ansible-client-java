package com.zhiquanyeo.ftl.ansible.protocol;

public class CommandValidationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7251996287281720703L;
	
	public static final String INVALID_COMMAND = "INVALID_COMMAND";
	public static final String INCORRECT_NUM_PARAMS = "INCORRECT_NUM_PARAMS";
	public static final String INVALID_PARAMETER = "INVALID_PARAMETER";
	
	public CommandValidationException(String type, String message) {
		super("(" + type + ") " + message);
	}
}
