import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Client sending requests to the host
 */
public class Client {
    
    public static final String HOST_ADDRESS = "localhost";
    public static final int HOST_PORT = 23;
    public static final int CLIENT_PORT = 70;
    public static final int CLIENT_DATA_SIZE = 1024;
    public static final int SERVER_DATA_SIZE = 4;

    private DatagramSocket socket;
    private DatagramPacket requestDataPacket;
    
    public Client() throws SocketException, UnknownHostException {
        socket = new DatagramSocket(CLIENT_PORT);
        requestDataPacket = new DatagramPacket(Utils.REQUEST_DATA, Utils.REQUEST_DATA.length, 
                InetAddress.getByName(HOST_ADDRESS), HOST_PORT);
    }
    
    /**
     * Insert bytes in bytes in to array from startingIndex
     * 
     * @param array array of bytes to which the bytes will be inserted
     * @param startingIndex index where the bytes will start to be inserted
     * @param bytes array of bytes from which the bytes will be inserted
     * 
     * @return the array after insertion
     */
    private byte[] insertBytes(byte[] array, int startingIndex, byte[] bytes) {
        for(int i = 0; i < bytes.length; i++) {
            array[i + startingIndex] = bytes[i];
        }
        return array;
    }
    
    /**
     * Build the message to be sent
     * 
     * @param accessMode access mode of the file, 0x01 for read nad 0x02 for write
     * @param filename name of the file to be accessed
     * @param encoding encoding of the file
     * 
     * @return a built message
     */
    private byte[] buildMsg(byte accessMode, String filename, String encoding) {
        byte[] msg = new byte[CLIENT_DATA_SIZE];
        msg[0] = 0x00;
        msg[1] = accessMode;
        insertBytes(msg, 2, filename.getBytes());
        msg[filename.length() + 2] = 0x00;
        insertBytes(msg, filename.length() + 3, encoding.getBytes());
        msg[filename.length() + encoding.length() + 3] = 0x00;
        
        return msg;
    }
    
    /**
     * Create a packet from msg
     * 
     * @param msg the message to be sent
     * @return a DatagramPacket to be sent with message and address set
     */
    private DatagramPacket createPacket(byte[] msg) {
        try {
            return new DatagramPacket(msg, msg.length, InetAddress.getByName(HOST_ADDRESS), HOST_PORT);
        } catch (UnknownHostException e) {
            return null;
        }
    }
    
    /**
     * Send a packet with an invalid message as required by the handout
     * @throws IOException if failed sending the packet
     */
    public void sendInvalidPacket() throws IOException {
        byte[] data = {0x00, 0x01, 0x02, 0x03};
        DatagramPacket sendPacket = createPacket(data);
        System.out.println("Client sent packet to host:");
        Utils.printPacketDetails(sendPacket, false);
        socket.send(sendPacket);
    }
    
    /**
     * As required by the handout, build a packet and send it to the host
     * 
     * @param accessMode access mode of the file
     * @param filename name of the file
     * @param encodingMode encoding of the file
     * @throws IOException if failed sending the packet
     */
    public void sendPacket(byte accessMode, String filename, String encodingMode) throws IOException {
        // build a message and send packet to host
        DatagramPacket sendPacket = createPacket(buildMsg(accessMode, filename, encodingMode));
        System.out.println("Client sent packet to host:");
        Utils.printPacketDetails(sendPacket, true);
        socket.send(sendPacket);
    }
    
    /**
     * As required by the handout, send a request packet to the host, then 
     * receive a response from the host and print it
     * 
     * @return the data received
     * @throws IOException if failed sending or receiving the packet
     */
    public byte[] receivePacket() throws IOException {
        // send request packet
        socket.send(requestDataPacket);
        
        // receive response
        DatagramPacket recvPacket = new DatagramPacket(new byte[SERVER_DATA_SIZE], SERVER_DATA_SIZE);
        socket.receive(recvPacket);
        System.out.println("Client received packet from host:");
        Utils.printPacketDetails(recvPacket, false);
        return recvPacket.getData();
    }
    
    public static void main(String[] args) throws Throwable {
        Client client = new Client();
        client.sendPacket((byte)0x01, "test1.txt", "netascii");
        Thread.sleep(100);
        client.receivePacket();
        Thread.sleep(100);
        client.sendPacket((byte)0x02, "test2.txt", "neTAscii");  
        Thread.sleep(100);
        client.receivePacket();
        Thread.sleep(100);
        client.sendPacket((byte)0x01, "test3.txt", "ocTEt");
        Thread.sleep(100);
        client.receivePacket();
        Thread.sleep(100);
        client.sendPacket((byte)0x01, "test4.txt", "octeT");
        Thread.sleep(100);
        client.receivePacket();
        Thread.sleep(100);
        client.sendPacket((byte)0x02, "test5.txt", "NeTascIi");
        Thread.sleep(100);
        client.receivePacket();
        Thread.sleep(100);
        client.sendPacket((byte)0x01, "test6.txt", "netasCIi");
        Thread.sleep(100);
        client.receivePacket();
        Thread.sleep(100);
        client.sendPacket((byte)0x02, "test7.txt", "Octet");
        Thread.sleep(100);
        client.receivePacket();
        Thread.sleep(100);
        client.sendPacket((byte)0x01, "test8.txt", "oCTet");
        Thread.sleep(100);
        client.receivePacket();
        Thread.sleep(100);
        client.sendPacket((byte)0x02, "test9.txt", "netASCii");
        Thread.sleep(100);
        client.receivePacket();
        client.sendPacket((byte)0x01, "test10.txt", "oCtEt");
        Thread.sleep(100);
        client.receivePacket();
        Thread.sleep(100);
        client.sendInvalidPacket();
    }
}
