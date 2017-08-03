package com.zhiquanyeo.ftl.ansible;

import com.zhiquanyeo.ftl.ansible.protocol.CommandValidationException;
import com.zhiquanyeo.ftl.ansible.protocol.ProtocolValidator;

public class Driver {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		testCall("SYS:CONN");
//		testCall("ROBOT:GET_DIGITAL");
//		testCall("ROBOT:GET_DIGITAL", "hi");
//		testCall("ROBOT:GET_DIGITAL", 1);
		
		AnsibleClient aClient = new AnsibleClient("localhost", 41236);
		aClient.connect();
	}
	
	public static void testCall(String cmd, Object... args) {
		try {
			ProtocolValidator.validateCommand(cmd, args);
		} catch (CommandValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
