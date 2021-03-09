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
	
	private static final int MAX_MESSAGE_LEN = 100;		// Maximum message length
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
	private void formPacket(RequestType rType, String filename, String mode) {
		byte discriminator[];	// The first two elements of data (01 - read request, 02 - write request, 03 - invalid request)
		
		if (rType == RequestType.READ)
			discriminator = new byte[]{0, 1};
		else if (rType == RequestType.WRITE)
			discriminator = new byte[]{0, 2};
		else
			discriminator = new byte[]{0, 3};
		
		byte fileByte[] = filename.getBytes();	// Filename in bytes
		byte modeByte[] = mode.getBytes();		// Mode in bytes
		
		// Formatting message to be sent
		byte msg[] = new byte[MAX_MESSAGE_LEN];
		System.arraycopy(discriminator, 0, msg, 0, 2);
		System.arraycopy(fileByte, 0, msg, discriminator.length, fileByte.length);
		msg[discriminator.length+fileByte.length] = 0;
		System.arraycopy(modeByte, 0, msg, discriminator.length+fileByte.length+1, modeByte.length);
		msg[discriminator.length+fileByte.length+1+modeByte.length] = 0;
		
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
		printPacketInfo(packetOut);
		
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
		printPacketInfo(packetIn);
	}
	
	/**
	 * Run function of a client thread
	 */
	public void run() {
		String filename = "test.txt";
		String mode1 = "octet";
		String mode2 = "ocTEt";
		
		// Alternate between read and write requests, five each
		for (int i=0; i<10; i++) {
			System.out.println("\n================================ Iteration #" + (i+1) + " ================================");
			// Client forms a request message and sends it
			if (i%2 == 0) {
				formPacket(RequestType.READ, filename, mode1);
			}
			else {
				formPacket(RequestType.WRITE, filename, mode2);
			}
			System.out.println("Client forms a request message and sends it");
			rpc_send();
			
			// Wait 3 seconds at each sending
			try {
				Thread.sleep(3000);
				} catch (InterruptedException e ) {
					e.printStackTrace();
					System.exit(1);
					}
			
			// Client sends an empty message again in order to receive acknowledgement packet
			System.out.println("Client sends a message again in order to receive acknowledgement packet");
			formEmptyPacket();
			rpc_send();
			
			// Wait 3 seconds at each sending
			try {
				Thread.sleep(3000);
				} catch (InterruptedException e ) {
					e.printStackTrace();
					System.exit(1);
					}
		}
		
		// An invalid request at #11
		System.out.println("================================ Iteration #11 ================================");
		formPacket(RequestType.INVALID, filename, mode1);
		rpc_send();		// This invoke invalid request exception
	}
	
	public static void main(String args[]) {
		Thread clientThread = new Thread(new Client(), "Client");
		clientThread.start();
	}
}
