import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * Server receives message from host and responds
 */
public class Server {

    public static final int SERVER_PORT = 69;
    public static final int HOST_PORT = 24;
    public static final String HOST_ADDRESS = "localhost";
    public static final int CLIENT_DATA_SIZE = 1024;
    public static final int SERVER_DATA_SIZE = 4;
    
    private DatagramSocket socket;
    private DatagramPacket requestDataPacket;
    
    public Server() throws SocketException, UnknownHostException {
        socket = new DatagramSocket(SERVER_PORT);
        requestDataPacket = new DatagramPacket(Utils.REQUEST_DATA, Utils.REQUEST_DATA.length, 
                InetAddress.getByName(HOST_ADDRESS), HOST_PORT);
    }
    
    /**
     * Validates the data received and form a return data if valid
     * 
     * @param data the data received to be validated
     * @return a corresponding data
     */
    private byte[] formReturnData(byte[] data) {
        byte[] returnData = new byte[SERVER_DATA_SIZE];
        
        Utils.assertTrue(data[0] == 0x00, "Invalid heading");
        
        if(data[1] == 0x01) {
            returnData[0] = 0x00;
            returnData[1] = 0x03;
            returnData[2] = 0x00;
            returnData[3] = 0x01;
        } else if(data[1] == 0x02) {
            returnData[0] = 0x00;
            returnData[1] = 0x04;
            returnData[2] = 0x00;
            returnData[3] = 0x00;
        } else {
            throw new AssertionError("Unknown access mode");
        }
        
        Utils.assertTrue(data[2] != 0x00, "Invalid filename");
        int breakIndex = 3;
        // go through filename
        for(; breakIndex < data.length; breakIndex++) {
            if(data[breakIndex] == 0x00) {
                break;
            }
        }
        Utils.assertTrue(breakIndex != data.length, "Oversized filename");
        
        // go through encoding
        Utils.assertTrue(data[++breakIndex] != 0x00, "Invalid encoding");
        int encodingStartIndex = breakIndex;
        for(breakIndex++; breakIndex < data.length; breakIndex++) {
            if(data[breakIndex] == 0x00) {
                break;
            }
        }
        Utils.assertTrue(breakIndex != data.length, "Oversized encoding");
        String encoding = new String(Arrays.
                copyOfRange(data, encodingStartIndex, breakIndex)).toLowerCase();
        Utils.assertTrue(encoding.equals("netascii") || encoding.equals("octet"), 
                "Invalid encoding mode: " + encoding);
        
        // assert the rest of them are all zero
        for(breakIndex++; breakIndex < data.length; breakIndex++) {
            Utils.assertTrue(data[breakIndex] == 0x00, "Empty space not all zero");
        }
        
        return returnData;
    }
    
    /**
     * Process the packet received from the host and respond
     * @throws IOException if failed sending or receiving the pakcet
     */
    public void processPacket() throws IOException {
        // send request packet
        socket.send(requestDataPacket);
        
        // receive packet
        byte[] data = new byte[CLIENT_DATA_SIZE];
        DatagramPacket packet = new DatagramPacket(data, CLIENT_DATA_SIZE);
        socket.receive(packet);
        System.out.println("Server received packet from host:");
        Utils.printPacketDetails(packet, true);
        
        // form new packet and respond
        packet.setData(formReturnData(packet.getData()));
        System.out.println("Server sent packet to host:");
        Utils.printPacketDetails(packet, false);
        socket.send(packet);
    }
    
    public static void main(String[] args) throws Throwable {
        Server server = new Server();
        Thread.sleep(50);
        while(true) {
            server.processPacket();
            Thread.sleep(200);
        }
    }
}
