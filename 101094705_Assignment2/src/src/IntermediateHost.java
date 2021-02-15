package src;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * @author Jeong Won Kim (101094705)
 *
 * This is the intermediate host class
 */
public class IntermediateHost {
	private static final int MAX_MESSAGE_LEN = 100;		// Maximum message length
	private DatagramPacket writeRequest, readRequest;
	private DatagramSocket receiveSocket, sendReceiveSocket;
	private int clientPort;
	
	public IntermediateHost() {
		try {
			receiveSocket = new DatagramSocket(23);
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Receive request from client
	 * @return request from client
	 */
	public DatagramPacket receiveRequest() {
		byte receivedData[] = new byte[MAX_MESSAGE_LEN];
		readRequest = new DatagramPacket(receivedData, receivedData.length);
		System.out.println("Intermediate Host: Waiting for Packet from client...\n");
		
		try {
			System.out.println("Waiting...");		// Waiting until packet comes
			receiveSocket.receive(readRequest);
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		System.out.println("Intermediate Host: Packet received.");
		Client.printPacketInfo(readRequest);
		clientPort = readRequest.getPort();
		
		return readRequest;
	}

	/**
	 * Deliver a request from client to server
	 * @param p - packet to be sent
	 * @param destSocket - destination port
	 */
	public void deliverRequest(DatagramPacket p, int destSocket) {
		byte data[] = p.getData();
		
		// Form a packet containing exactly what it received
		writeRequest = new DatagramPacket(data, p.getLength(), p.getAddress(), destSocket);
		
		System.out.println("Intermediate Host: delivering packet to server");
		Client.printPacketInfo(writeRequest);
		
		try {
			sendReceiveSocket.send(writeRequest);
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		System.out.println("Intermediate Host: Packet delivered.\n");
	}
	
	/**
	 * Receive a response from server and send it back to client
	 */
	public void receiveAndEcho() {
		// Receiving a response from server
		byte receivedData[] = new byte[4];
		readRequest = new DatagramPacket(receivedData, receivedData.length);
		System.out.println("Intermediate Host: Waiting for response from server\n");
		
		try {
			System.out.println("Waiting...");
			sendReceiveSocket.receive(readRequest);
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		System.out.println("Intermediate Host: Packet received.");
		Client.printPacketInfo(readRequest);
		
		
		// Sending the response back to client
		byte data[] = readRequest.getData();
		// Form a packet containing exactly what it received
		writeRequest = new DatagramPacket(data, readRequest.getLength(), readRequest.getAddress(), clientPort);
		
		System.out.println("Intermediate Host: echoing packet to client");
		Client.printPacketInfo(writeRequest);
		
		try {
			sendReceiveSocket.send(writeRequest);
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		System.out.println("Intermediate Host: Packet echoed.\n");
	}
	
	public static void main(String args[]) {
		IntermediateHost imhost = new IntermediateHost();
		int i=1;
		while(true) {
			System.out.println("\n================================ Iteration #" + i + " ================================");
			DatagramPacket receiveSocket = imhost.receiveRequest();
			imhost.deliverRequest(receiveSocket, 69);
			imhost.receiveAndEcho();
			i++;
		}
	}
}
