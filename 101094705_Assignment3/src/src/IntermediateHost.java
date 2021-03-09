package src;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Jeong Won Kim (101094705)
 *
 * This is the intermediate host class
 */
public class IntermediateHost extends Thread {
	private static final int MAX_MESSAGE_LEN = 100;		// Maximum message length
	private DatagramPacket receivePacket, replyPacket;
	private DatagramSocket receiveSocket, replySocket;	// Socket for receiving and replying
	private int replyPort, receivePort;
	private List<byte[]> reqBox, ackBox;				// Shared List to store a request and a acknowledgement
	
	public IntermediateHost(int recvPort, List<byte[]> req, List<byte[]> ack) {
		try {
			receiveSocket = new DatagramSocket(recvPort);
			replySocket = new DatagramSocket();
			receivePort = recvPort;
			reqBox = req;
			ackBox = ack;
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * This return a data receiving confirming message
	 * @return confirming message in bytes
	 */
	private byte[] confirmPacket() {
		String msg = "Data is received";
		return msg.getBytes();
	}
	
	/**
	 * This sends a reply that includes receiving confirming message
	 */
	private void reply() {
		byte[] msg = confirmPacket();
		
		try {
			replyPacket = new DatagramPacket(msg, msg.length, InetAddress.getLocalHost(), replyPort);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		
		System.out.println("Intermediate Host " + this.getName() + ": delivering reply packet");
		Client.printPacketInfo(replyPacket);
		
		try {
			replySocket.send(replyPacket);
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		System.out.println("Intermediate Host " + this.getName() + ": reply packet delivered.\n");
	}
	
	/**
	 * This sends a reply that includes a user defining message
	 * @param msg - message
	 */
	private void reply(byte[] msg) {
		try {
			replyPacket = new DatagramPacket(msg, msg.length, InetAddress.getLocalHost(), replyPort);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		System.out.println("Intermediate Host " + this.getName() + ": delivering reply packet");
		Client.printPacketInfo(replyPacket);
		
		try {
			replySocket.send(replyPacket);
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		System.out.println("Intermediate Host " + this.getName() + ": reply packet delivered.\n");
	}
	
	/**
	 * This receives a packet from either the client or the server and replies to it
	 */
	private synchronized void receiveAndReply() {
		byte receivedData[] = new byte[MAX_MESSAGE_LEN];
		receivePacket = new DatagramPacket(receivedData, receivedData.length);
		System.out.println("Intermediate Host " + this.getName() + ": Waiting for Packet...\n");
		
		try {
			System.out.println("Waiting...");		// Waiting until packet comes
			receiveSocket.receive(receivePacket);
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		System.out.println("Intermediate Host " + this.getName() + ": Packet received.");
		Client.printPacketInfo(receivePacket);
		replyPort = receivePacket.getPort();

		// If the message is not empty, store it in the shared list
		if (receivePacket.getLength() != 0) {
			if (receivePort == 23)
				reqBox.add(receivedData);
			else
				ackBox.add(receivedData);
			reply();
		}
		// If the message is empty, send a reply including a message
		else {
			System.out.println("Empty message to receive data");
			while(true) {
				if (receivePort == 23 && !ackBox.isEmpty()) {
					reply(ackBox.remove(0));
					break;
				}
				else if (receivePort == 24 && !reqBox.isEmpty()) {
					reply(reqBox.remove(0));
					break;
				}
			}
		}
	}

	/**
	 * run function of the intermediate host thread
	 */
	public void run() {
		while(true) {
			receiveAndReply();
		}
	}
	
	public static void main(String args[]) {
		List<byte[]> request = Collections.synchronizedList(new ArrayList<>());
		List<byte[]> acknowledgement = Collections.synchronizedList(new ArrayList<>());
		// Thread for client to server data flow
		Thread clientToServer = new Thread(new IntermediateHost(23, request, acknowledgement), "Intermediate Host for packets going from the Client to the Server");
		// Thread for server to client data flow
		Thread serverToClient = new Thread(new IntermediateHost(24, request, acknowledgement), "Intermediate Host for packets going from the Server to the Client");
		clientToServer.start();
		serverToClient.start();
	}
}
