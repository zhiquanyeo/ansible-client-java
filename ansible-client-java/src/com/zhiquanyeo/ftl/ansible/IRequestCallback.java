package com.zhiquanyeo.ftl.ansible;

import com.zhiquanyeo.ftl.ansible.protocol.packet.ClientPacket;

public interface IRequestCallback {
	void onRequestSuccess(int seq, ClientPacket packet, Object... args);
	void onRequestFailure(int seq, ClientPacket packet, RequestFailedException e);
}
