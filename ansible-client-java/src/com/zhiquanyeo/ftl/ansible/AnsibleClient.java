package com.zhiquanyeo.ftl.ansible;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import com.zhiquanyeo.ftl.ansible.protocol.AnsiblePacket;
import com.zhiquanyeo.ftl.ansible.protocol.CommandValidationException;
import com.zhiquanyeo.ftl.ansible.protocol.ProtocolCommands;
import com.zhiquanyeo.ftl.ansible.protocol.ProtocolCommands.ClientCommand;
import com.zhiquanyeo.ftl.ansible.protocol.ProtocolCommands.CommandParam;
import com.zhiquanyeo.ftl.ansible.protocol.ProtocolValidator;
import com.zhiquanyeo.ftl.ansible.protocol.packet.ClientPacket;
import com.zhiquanyeo.ftl.ansible.protocol.packet.ProtocolPacket;
import com.zhiquanyeo.ftl.ansible.protocol.packet.ServerAsyncPacket;
import com.zhiquanyeo.ftl.ansible.protocol.packet.ServerResponsePacket;

public class AnsibleClient {
	private static class OutstandingRequest {
		public ClientPacket packet;
		public IRequestCallback callback;
		public long timestamp;
	}
	
	private static final int PACKET_TIMEOUT_MS = 5000;
	
	public static enum ConnectionState {
		NOT_CONNECTED,
		ACTIVE,
		QUEUED
	};
	
	private Socket d_socket = null;
	private OutputStream d_outStream = null;
	
	private ConnectionState d_state = ConnectionState.NOT_CONNECTED;
	private String d_host;
	private int d_port;
	
	private volatile AtomicBoolean shouldStop = new AtomicBoolean();
	
	private AnsiblePacket d_packet = new AnsiblePacket();
	
	private int d_sequence = 1;
	
	private Map<Integer, OutstandingRequest> d_outstandingRequests = new HashMap<>();
	
	private Timer d_heartbeatTimer = new Timer();
	private Timer d_reqTimeoutTimer = new Timer();
	
	public AnsibleClient(String host, int port) {
		this.d_host = host;
		this.d_port = port;
	}
	
	private void handleIncomingBuffer(byte[] buf, int len) {
		// Convert to a Byte[] and pass it off to AnsiblePacket
		if (len < 0) {
			System.err.println("Invalid length of buffer " + len);
			return;
		}
		
		Byte[] byteArray = new Byte[len];
		for (int i = 0; i < len; i++) {
			byteArray[i] = buf[i];
		}
		
		ProtocolPacket packet = d_packet.parse(byteArray);
		if (packet != null) {
			// Now decide what to do
			if (packet instanceof ServerResponsePacket) {
				ServerResponsePacket srPacket = (ServerResponsePacket)packet;
				// Get the outstanding request from the map
				OutstandingRequest oReq = this.d_outstandingRequests.get(srPacket.getSEQ());
				if (oReq == null) {
					System.err.println("Could not find outstanding request #" + srPacket.getSEQ());
					return;
				}
				
				// Look up info
				ClientCommand cmdInfo = ProtocolCommands.lookupByBytecode(oReq.packet.DID, oReq.packet.CID);
				if (cmdInfo == null) {
					System.err.println("Invalid command!");
					this.d_outstandingRequests.remove(srPacket.getSEQ());
					return;
				}
				
				this.d_outstandingRequests.remove(srPacket.getSEQ());
				
				Object retVal = null;
				if (oReq.callback != null) {
					switch (cmdInfo.returnType) {
						case "uint8": {
							int tmpVal = (int)srPacket.getDATA()[0] & 0xFF;
							retVal = new Integer(tmpVal);
						} break;
						case "int8": {
							int tmpVal = (int)srPacket.getDATA()[0];
							retVal = new Integer(tmpVal);
						} break;
						case "uint16": {
							int tmpVal = (((int)srPacket.getDATA()[0] << 8) | ((int)srPacket.getDATA()[1] & 0xFF)) & 0xFFFF;
							retVal = new Integer(tmpVal);
						} break;
						case "int16" :{
							int tmpVal = (((int)srPacket.getDATA()[0] << 8) | ((int)srPacket.getDATA()[1] & 0xFF));
							retVal = new Integer(tmpVal);
						} break;
					}
					
					if (retVal != null) {
						oReq.callback.onRequestSuccess(srPacket.getSEQ(), oReq.packet, srPacket.getMRSP(), retVal);
					}
					else {
						oReq.callback.onRequestSuccess(srPacket.getSEQ(), oReq.packet, srPacket.getMRSP());
					}
				}
			}
			else {
				ServerAsyncPacket saPacket = (ServerAsyncPacket)packet;
				// TODO Implement
			}
		}
	}
	
	public void connect() {
		if (this.d_state != ConnectionState.NOT_CONNECTED) {
			return;
		}
		
		try {
			this.d_socket = new Socket(this.d_host, this.d_port);
			this.d_outStream = this.d_socket.getOutputStream();
		}
		catch (UnknownHostException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		final Thread listeningThread = new Thread() {
			@Override
			public void run() {
				try {
					InputStream inStream = d_socket.getInputStream();
					while (!shouldStop.get()) {
						byte[] inBuf = new byte[255];
						int bytesRead = inStream.read(inBuf, 0, inBuf.length);
						handleIncomingBuffer(inBuf, bytesRead);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		listeningThread.start();
		
		// Set up the heartbeat
		d_heartbeatTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				sendRequest("SYS:HBEAT", new IRequestCallback() {
					@Override
					public void onRequestSuccess(int seq, ClientPacket packet, Object... args) {
						// args[0] will be MRSP for response messages, and ID_CODE for async
						System.out.println("HBEAT success. State = " + (Integer)args[1]);
					}
					@Override
					public void onRequestFailure(int seq, ClientPacket packet, RequestFailedException e) {
						System.out.println("HBEAT Packet #" + seq + " failed..." + e.toString());
					}
				});
			}
		}, 0, 250);
		
		d_reqTimeoutTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				long currTime = System.currentTimeMillis();
				synchronized(d_outstandingRequests) {
					ArrayList<Integer> deletes = new ArrayList<>();
					// Iterate through the outstanding requests and wipe out any which are expired
					Iterator<Entry<Integer, OutstandingRequest>> it = d_outstandingRequests.entrySet().iterator();
					while (it.hasNext()) {
						Map.Entry<Integer, OutstandingRequest> pair = (Entry<Integer, OutstandingRequest>)it.next();
						if (currTime - pair.getValue().timestamp > PACKET_TIMEOUT_MS) {
							deletes.add(pair.getKey());
						}
					}
					
					// Now iterate through the list of things to delete
					for (int i = 0; i < deletes.size(); i++) {
						int seq = deletes.get(i);
						OutstandingRequest oReq = d_outstandingRequests.get(seq);
						if (oReq.callback != null) {
							oReq.callback.onRequestFailure(seq, oReq.packet, new RequestFailedException("TIMED_OUT"));
						}
						d_outstandingRequests.remove(seq);
					}
				}
				
			}
		}, 0, 500);
	}
	
	public synchronized boolean sendRequest(String requestType, Object... arguments) {
		try {
			ProtocolValidator.validateCommand(requestType, arguments);
		}
		catch(CommandValidationException e) {
			e.printStackTrace();
			return false;
		}
		
		// By this point, cmdInfo will be valid since we've gone through validation
		ClientCommand cmdInfo = ProtocolCommands.lookupByCommand(requestType);
		
		// Figure out if there is a callback
		IRequestCallback callback = null;
		
		if ((cmdInfo.params == null || cmdInfo.params.size() == 0) && arguments.length > 0) {
			if (arguments[0] instanceof IRequestCallback) {
				callback = (IRequestCallback)arguments[0];
			}
		}
		else if ((cmdInfo.params != null && cmdInfo.params.size() > 0) && arguments.length > cmdInfo.params.size()) {
			if (arguments[cmdInfo.params.size()] instanceof IRequestCallback) {
				callback = (IRequestCallback)arguments[cmdInfo.params.size()];
			}
		}
		
		int currSeq = this.d_sequence++;
		
		// Set up the outstanding request
		OutstandingRequest oReq = new OutstandingRequest();
		
		// Build the data buffer
		ArrayList<Byte> dBuf = new ArrayList<>();
		if (cmdInfo.params != null) {
			for (int i = 0; i < cmdInfo.params.size(); i++) {
				CommandParam cParam = cmdInfo.params.get(i);
				switch(cParam.type) {
					case "int8":
					case "uint8":
						// Write as it, single byte
						dBuf.add((Byte)arguments[i]);
						break;
					case "int16":
					case "uint16":
						// calculate MSB/LSB
						int dMsb, dLsb;
						Integer val = (Integer)arguments[i];
						dMsb = (val.intValue() >> 8) & 0xFF;
						dLsb = (val.intValue()) & 0xFF;
						dBuf.add((byte)dMsb);
						dBuf.add((byte)dLsb);
				}
			}
		}
		Byte[] dBufArr = new Byte[dBuf.size()];
		dBuf.toArray(dBufArr);
		
		ClientPacket cPkt = new ClientPacket();
		cPkt.SEQ = currSeq;
		cPkt.DID = cmdInfo.deviceId;
		cPkt.CID = cmdInfo.commandId;
		cPkt.DATA = dBufArr;
		cPkt.requestAck = true;
		cPkt.resetTimeout = true;
		
		ArrayList<Byte> sendBufAL = d_packet.create(cPkt);
		// generate the 'b'yte array
		byte[] sendBuf = new byte[sendBufAL.size()];
		for (int i = 0; i < sendBufAL.size(); i++) {
			sendBuf[i] = sendBufAL.get(i);
		}
		
		oReq.packet = cPkt;
		oReq.timestamp = System.currentTimeMillis();
		if (callback != null) {
			oReq.callback = callback;
		}
		
		// Insert into outstanding requests
		this.d_outstandingRequests.put(currSeq, oReq);
		
		// Send
		try {
			this.d_outStream.write(sendBuf);
			this.d_outStream.flush();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		if (this.d_sequence > 255) {
			this.d_sequence = 1;
		}
		
		return true;
	}
	
	// Potentially Public Interface?
}
