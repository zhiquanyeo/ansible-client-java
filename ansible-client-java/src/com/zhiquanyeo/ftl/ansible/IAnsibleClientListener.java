package com.zhiquanyeo.ftl.ansible;

import com.zhiquanyeo.ftl.ansible.AnsibleClient.ConnectionState;

public interface IAnsibleClientListener {
	void onClientStateChanged(ConnectionState oldState, ConnectionState newState);
	void onAsyncEventReceived(String eventType, Byte[] data);
}
