package src;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import src.Client.RequestType;

/**
 * @author Jeong Won Kim (101094705)
 *
 * This is the server class
 */
public class Server extends Thread {
	private static final int MAX_MESSAGE_LEN = 100;		// Maximum message length
	private DatagramPacket packetOut, packetIn;			// Packet going out and packet coming in
	private DatagramSocket networkSocket;
	   
	public Server() {
		try {
			networkSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Parse a packet to confirm that the format is valid
	 * @param p - packet to be verified
	 * @return Request type (One of READ, WRITE, INVALID)
	 */
	private RequestType verifyPacket(DatagramPacket p) {
		byte data[] = p.getData();
		String filename = "";
		String mode = "";
		int modeIndex = 0;
		
		System.out.println("Verifying data format...");
		// Verifying filename part of data
		for (int i=2; i<data.length; i++) {
			if (data[i] == 0) {
				filename = new String(Arrays.copyOfRange(data, 2, i), 0, i-2);
				modeIndex = i+1;
				System.out.println("Filename: " + filename);
				break;
			}
			if (i==data.length) {
				System.out.println("Filename invalid: " + filename);
				return RequestType.INVALID;
			}
		}
		
		// Verifying mode part of data
		for (int j=modeIndex; j<data.length; j++) {
			if (data[j] == 0) {
				mode = new String(Arrays.copyOfRange(data, modeIndex, j), 0, j-modeIndex);
				mode = mode.toLowerCase();
				System.out.println("Mode: " + mode);
				break;
			}
			if (j==data.length) {
				System.out.println("Mode invalid:" + mode);
				return RequestType.INVALID;
			}
		}

		// Determining the request type: READ, WRITE or INVALID request
		if (data[0] == 0 && data[1] == 1) {
			System.out.println("This is a read request\n");
			return RequestType.READ;
		}
		else if (data[0] == 0 && data[1] == 2) {
			System.out.println("This is a write request\n");
			return RequestType.WRITE;
		}
		else {
			System.out.println("Discriminator invalid!");
			return RequestType.INVALID;
		}
	}

	/**
	 * Based on the type of request, form a packet with response
	 * @param t - request type
	 * @throws Exception - In valid request exception
	 */
	private void formPacket(RequestType t) throws Exception {
		byte response[] = null;
		switch(t) {
		case READ:		// Response for read request
			System.out.println("Responding to read request");
			response = new byte[] {0, 3, 0, 1};
			break;
		case WRITE:		// Response for write request
			System.out.println("Responding to write request");
			response = new byte[] {0, 4, 0, 0};
			break;
		default:		// If request is invalid, throw an exception and quit
			throw new Exception("Invalid request!!");
		}
		
		try {
			packetOut = new DatagramPacket(response, response.length, InetAddress.getLocalHost(), 24);
		} catch(UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * This forms a packet with an empty message.
	 * This is the indicator for data request from the server side
	 */
	private void formEmptyPacket() {
		byte msg[] = new byte[0];
		
		try {
			packetOut = new DatagramPacket(msg, msg.length, InetAddress.getLocalHost(), 24);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This sends a packet and receive a reply
	 */
	private void rpc_send() {
		// Send a packet
		System.out.println("Server: Requesting data to client...");
		Client.printPacketInfo(packetOut);
		
		try {
			networkSocket.send(packetOut);
			packetOut = null;
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		System.out.println("Server: Packet sent.\n");
		
		// Receive a reply packet
		byte data[] = new byte[MAX_MESSAGE_LEN];
		packetIn = new DatagramPacket(data, data.length);
		System.out.println("Server: Waiting for response from client.\n");
		
		try {
			System.out.println("Waiting...");		// Waiting until packet comes
			networkSocket.receive(packetIn);
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		System.out.println("Server: Packet received.");
		Client.printPacketInfo(packetIn);
	}
	
	/**
	 * run function of a server thread
	 */
	public void run() {
		int i=1;
		while(true) {
			System.out.println("\n================================ Iteration #" + i + " ================================");
			
			// Server sends an empty message in order to receive request packet from client
			if (packetIn == null) {
				System.out.println("Server sends an empty message in order to receive request packet from client");
				formEmptyPacket();
				rpc_send();
			}
			else {
				// Server forms an acknowledgement message and sends it
				RequestType receivedType = verifyPacket(packetIn);
				try {
					formPacket(receivedType);
				} catch (Exception e) {
					e.printStackTrace();
				}
				rpc_send();
				packetIn = null;
			}
			
			// Wait 3 seconds at each request
			try {
				Thread.sleep(3000);
				} catch (InterruptedException e ) {
					e.printStackTrace();
					System.exit(1);
					}
			
			i++;
		}
	}
	
	   
	public static void main(String args[]) throws Exception {
		Thread serverThread = new Thread(new Server(), "Server");
		serverThread.start();
	}
}
 