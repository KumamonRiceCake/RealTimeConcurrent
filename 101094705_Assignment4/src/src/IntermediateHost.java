package src;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
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
		//Client.printPacketInfo(replyPacket);
		
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
		//Client.printPacketInfo(replyPacket);
		
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
	 * @throws IOException 
	 */
	private synchronized void receiveAndReply() {
		byte receivedData[] = new byte[MAX_MESSAGE_LEN];
		receivePacket = new DatagramPacket(receivedData, receivedData.length);
		System.out.println(currentThread().getName() + ": Waiting for Packet...\n");
		
		try {
			System.out.println("Waiting...");		// Waiting until packet comes
			receiveSocket.receive(receivePacket);
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		System.out.println(currentThread().getName() + ": Packet received.");
		//Client.printPacketInfo(receivePacket);
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

	// Measure time using System.nanoTime	
	private long nanoTimeMeasure() {
		return System.nanoTime();
	}

	// Measure time using Instant	
	private Instant instantMeasure() {
		return Instant.now();
	}

	// Write measured time to file	
	private void writeToFile(long val) throws IOException {
		try {
		    String filename = currentThread().getName() + ".txt";
		    FileWriter fw = new FileWriter(filename, true); //the true will append the new data
		    fw.write(Long.toString(val) + "\n");//appends the string to the file
		    fw.close();
		}
		catch(IOException e)
		{
		    System.err.println("IOException: " + e.getMessage());
		}
	}

	/**
	 * run function of the intermediate host thread
	 */
	public void run() {
		//long startTime = nanoTimeMeasure();
		Instant start = instantMeasure();
		
		for (int i=0; i<2000; i++) {
			receiveAndReply();
		}
		
		//long endTime = nanoTimeMeasure();
		//try {
		//	writeToFile(endTime - startTime);
		//} catch (IOException e) {
		//	e.printStackTrace();
		//}
		Instant end = instantMeasure();
		Duration interval = Duration.between(start, end);
        try {
			writeToFile(interval.toNanos());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String args[]) {
		List<byte[]> request = Collections.synchronizedList(new ArrayList<>());
		List<byte[]> acknowledgement = Collections.synchronizedList(new ArrayList<>());
		// Thread for client to server data flow
		Thread clientToServer = new Thread(new IntermediateHost(23, request, acknowledgement), "Intermediate Host for Client to Server");
		// Thread for server to client data flow
		Thread serverToClient = new Thread(new IntermediateHost(24, request, acknowledgement), "Intermediate Host for Server to Client");
		clientToServer.start();
		serverToClient.start();
		
		//Thread clientThread = new Thread(new Client(), "Client");
		//clientThread.start();
		
		//Thread serverThread = new Thread(new Server(), "Server");
		//serverThread.start();
	}
}
