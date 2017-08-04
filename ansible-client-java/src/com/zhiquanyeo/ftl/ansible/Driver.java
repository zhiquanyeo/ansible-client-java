package com.zhiquanyeo.ftl.ansible;

import com.zhiquanyeo.ftl.ansible.AnsibleClient.ConnectionState;
import com.zhiquanyeo.ftl.ansible.protocol.CommandValidationException;
import com.zhiquanyeo.ftl.ansible.protocol.ProtocolValidator;

public class Driver {

	public static void main(String[] args) {
		
		AnsibleClient aClient = new AnsibleClient("localhost", 41236);
		aClient.addListener(new IAnsibleClientListener() {

			@Override
			public void onClientStateChanged(ConnectionState oldState, ConnectionState newState) {
				System.out.println("State changed from " + oldState.toString() + " to " + newState.toString());
			}

			@Override
			public void onAsyncEventReceived(String eventType, Byte[] data) {
				System.out.println("Received Async Event (" + eventType + ") with " + data.length + " bytes of payload");
			}
			
		});
		boolean result = aClient.connect();
		if (result) {
			System.out.println("Connection Successful");
		}
		else {
			System.out.println("Connection Failed");
		}
	}
	

}
