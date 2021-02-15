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
 * This is the intermediate host class
 */
public class Server {
	private static final int MAX_MESSAGE_LEN = 100;		// Maximum message length
	private DatagramPacket writeRequest, readRequest;
	private DatagramSocket receiveSocket;
	private int hostPort;
	   
	public Server() {
		try {
			receiveSocket = new DatagramSocket(69);
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Receive a request from client
	 * @return Request from client
	 */
	public DatagramPacket receiveRequest() {
		byte data[] = new byte[MAX_MESSAGE_LEN];
		readRequest = new DatagramPacket(data, data.length);
		System.out.println("Server: Waiting for Packet from client\n");
		
		try {
			System.out.println("Waiting...");		// Waiting until packet comes
			receiveSocket.receive(readRequest);
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		System.out.println("Server: Packet received.");
		Client.printPacketInfo(readRequest);
		hostPort = readRequest.getPort();
		
		return readRequest;
	}
	
	/**
	 * Parse a packet to confirm that the format is valid
	 * @param p - packet to be verified
	 * @return Request type (One of READ, WRITE, INVALID)
	 */
	public RequestType verifyPacket(DatagramPacket p) {
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
	 * Based on the type of request, sending a response to client
	 * @param t - request type
	 * @throws Exception - In valid request exception
	 */
	public void response(RequestType t) throws Exception {
		DatagramSocket responseSocket = null;
		try {
			responseSocket = new DatagramSocket();
		} catch (SocketException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
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
			writeRequest = new DatagramPacket(response, response.length, InetAddress.getLocalHost(), hostPort);
		} catch(UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		System.out.println("Server: Sending response to client");
		Client.printPacketInfo(writeRequest);
		
		try {
			responseSocket.send(writeRequest);
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		System.out.println("Server: Packet sent.\n");
		responseSocket.close();		// Close response socket
	}
	   
	public static void main(String args[]) throws Exception {
		Server server = new Server();
		int i=1;
		while(true) {
			System.out.println("\n================================ Iteration #" + i + " ================================");
			DatagramPacket receiveSocket = server.receiveRequest();
			RequestType reqType = server.verifyPacket(receiveSocket);
			server.response(reqType);
			i++;
		}
	}
}
 