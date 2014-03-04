package com.github.hrobertson.headtrackudp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPSender implements Runnable {
	
	private DatagramSocket datagramSocket;
	private InetAddress receiverAddress;
	private int port;
	private byte[] data;
	
	public UDPSender (DatagramSocket datagramSocket, InetAddress receiverAddress, int port, byte[] data) {
		this.datagramSocket = datagramSocket;
		this.receiverAddress = receiverAddress;
		this.port = port;
		this.data = data;
	}
	
	@Override
	public void run() {
		DatagramPacket packet = new DatagramPacket(data, data.length, receiverAddress, port);
		
		try {
			datagramSocket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
