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
public class Client {
	// This represents the three request types
	enum RequestType {
		  READ,
		  WRITE,
		  INVALID
		  }
	
	private static final int MAX_MESSAGE_LEN = 100;		// Maximum message length
	private DatagramPacket writeRequest, readRequest;
	private DatagramSocket networkSocket;
	
	public Client() {
		try {
			networkSocket = new DatagramSocket();	// Socket for sending and receiving
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Receiving packet from server
	 */
	public void receivePacket() {
		byte data[] = new byte[MAX_MESSAGE_LEN];
		readRequest = new DatagramPacket(data, data.length);
		System.out.println("Client: Waiting for response from server.\n");
		
		try {
			System.out.println("Waiting...");		// Waiting until packet comes
			networkSocket.receive(readRequest);
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		System.out.println("Client: Packet received.");
		printPacketInfo(readRequest);
	}
	
	/**
	 * Write a packet with formatted data and send it to server
	 * @param rType - request type
	 * @param filename
	 * @param mode - netascii or octet, any mix of cases, e.g. ocTEt
	 */
	public void writePacket(RequestType rType, String filename, String mode) {
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
			writeRequest = new DatagramPacket(msg, msg.length, InetAddress.getLocalHost(), 23);
		} catch(UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		System.out.println("Client: Sending packet to server...");
		printPacketInfo(writeRequest);
		
		try {
			networkSocket.send(writeRequest);
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		System.out.println("Client: Packet sent.\n");
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
	
	public static void main(String args[]) {
		Client client = new Client();
		String filename = "test.txt";
		String mode1 = "octet";
		String mode2 = "ocTEt";
		
		// Alternate between read and write requests, five each
		for (int i=0; i<10; i++) {
			System.out.println("\n================================ Iteration #" + (i+1) + " ================================");
			if (i%2 == 0) {
				client.writePacket(RequestType.READ, filename, mode1);
			}
			else {
				client.writePacket(RequestType.WRITE, filename, mode2);
			}
			client.receivePacket();
			// Wait 3 seconds at each request
			try {
				Thread.sleep(3000);
				} catch (InterruptedException e ) {
					e.printStackTrace();
					System.exit(1);
					}
		}
		// An invalid request at #11
		System.out.println("================================ Iteration #11 ================================");
		client.writePacket(RequestType.INVALID, filename, mode1);
		client.receivePacket();
	}
}
