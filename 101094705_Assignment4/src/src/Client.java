package src;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * @author Jeong Won Kim (101094705)
 *
 * This is the Client class
 */
public class Client extends Thread {
	// This represents the three request types
	enum RequestType {
		  READ,
		  WRITE,
		  INVALID
		  }
	
	private static final int MAX_MESSAGE_LEN = 1000;	// Maximum message length
	private DatagramPacket packetOut, packetIn;			// Packet going out and packet coming in
	private DatagramSocket networkSocket;
	
	public Client() {
		try {
			networkSocket = new DatagramSocket();		// Socket for sending and receiving
		} catch (SocketException e) {
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
			packetOut = new DatagramPacket(msg, msg.length, InetAddress.getLocalHost(), 23);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This forms a packet with designated data
	 * @param rType - request type
	 * @param filename
	 * @param mode - netascii or octet, any mix of cases, e.g. ocTEt
	 */
	private void formPacket(String filename) {
		byte fileByte[] = filename.getBytes();	// Filename in bytes
		
		// Formatting message to be sent
		byte msg[] = new byte[MAX_MESSAGE_LEN];
		System.arraycopy(fileByte, 0, msg, 0, fileByte.length);
		
		try {
			packetOut = new DatagramPacket(msg, msg.length, InetAddress.getLocalHost(), 23);
		} catch(UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * This prints information about a packet and prints its data in both bytes and string
	 * @param p - packet to be printed
	 */
	public static void printPacketInfo(DatagramPacket p) {
		int len = p.getLength();
		String dataString = new String(p.getData(), 0, len);
		
		System.out.println("From host: " + p.getAddress());
		System.out.println("Host port: " + p.getPort());
		System.out.println("Length: " + len);
		System.out.println("Data in bytes: " + p.getData());
		System.out.println("Data in string: " + dataString);
		System.out.println();
	}
	
	/**
	 * This sends a packet and receive a reply
	 */
	private void rpc_send() {
		// Send a packet
		System.out.println("Client: Sending packet to server...");
		//printPacketInfo(packetOut);
		
		try {
			networkSocket.send(packetOut);
			packetOut = null;
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		System.out.println("Client: Packet sent.\n");
		
		// Receive a reply packet
		byte data[] = new byte[MAX_MESSAGE_LEN];
		packetIn = new DatagramPacket(data, data.length);
		System.out.println("Client: Waiting for response from server.");
		
		try {
			System.out.println("Waiting...");		// Waiting until packet comes
			networkSocket.receive(packetIn);
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		System.out.println("Client: Packet received.\n");
		//printPacketInfo(packetIn);
	}
	
	/**
	 * Run function of a client thread
	 */
	public void run() {
		String filename = "test.txt";
		
		// Client sends 1000 packets
		for (int i=0; i<1000; i++) {
			System.out.println("\n================================ Iteration #" + (i+1) + " ================================");
			// Client forms a request message and sends a filename of 1000 bytes
			formPacket(filename);

			System.out.println("Client forms a request message and sends it");
			rpc_send();
			
			// Client sends an empty message again in order to receive acknowledgement packet
			System.out.println("Client sends a message again in order to receive acknowledgement packet");
			formEmptyPacket();
			rpc_send();
		}
	}
	
	public static void main(String args[]) {
		Thread clientThread = new Thread(new Client(), "Client");
		clientThread.start();
	}
}
