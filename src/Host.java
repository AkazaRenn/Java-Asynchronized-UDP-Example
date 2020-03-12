import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Host connecting client and server and transfers packets between them
 */
public class Host extends Thread {

    public static final int HOST_PORT_CLIENT = 23;
    public static final int HOST_PORT_SERVER = 24;
    public static final int CLIENT_PORT = 70;
    public static final int SERVER_PORT = 69;
    public static final int CLIENT_DATA_SIZE = 1024;
    public static final int SERVER_DATA_SIZE = 4;

    private DatagramSocket socket;
    private LinkedBlockingQueue<byte[]> sourceDataQueue;
    private LinkedBlockingQueue<byte[]> targetDataQueue;
    private String source, target;
    private int dataSize;
    
    public Host(int port, int dataSize, String source, String target, 
            LinkedBlockingQueue<DatagramPacket> packetQueue, 
            LinkedBlockingQueue<byte[]> sourceDataQueue, 
            LinkedBlockingQueue<byte[]> targetDataQuene)
                    throws SocketException, UnknownHostException {
        socket = new DatagramSocket(port);
        this.dataSize = dataSize;
        this.source = source;
        this.target = target;
        this.sourceDataQueue = sourceDataQueue;
        this.targetDataQueue = targetDataQuene;
    }
    
    /**
     * Receive the packet and do corresponding operations
     * @throws IOException if failed sending or receiving the packet
     * @throws InterruptedException 
     */
    private void receivePacket() throws IOException, InterruptedException {
        // received packet
        DatagramPacket packet = new DatagramPacket(new byte[dataSize], dataSize);
        socket.receive(packet);
        
        System.out.print("Host received packet from " + source.toLowerCase() + ":");
        if(Utils.isRequest(packet.getData())) {
            System.out.println(" data request");
            byte[] data = sourceDataQueue.poll(1, TimeUnit.DAYS);
            packet.setData(data);
            socket.send(packet);
        } else {
            System.out.println();
            Utils.printPacketDetails(packet, true);
            if(!targetDataQueue.offer(packet.getData())) {
                System.out.println(target + " data queue full, ignoring this packet.");
            }
        }    
    }
    
    @Override
    public void run() {
        while(true) {
            try {
                receivePacket();
            } catch (IOException | InterruptedException e) {
                System.exit(1);
            }
        }
    }
    
    public static void main(String[] args) throws Throwable {
        LinkedBlockingQueue<DatagramPacket> packetQueue = new LinkedBlockingQueue <DatagramPacket>();
        LinkedBlockingQueue<byte[]> serverDataQueue = new LinkedBlockingQueue <byte[]>();
        LinkedBlockingQueue<byte[]> clientDataQueue = new LinkedBlockingQueue <byte[]>();
        new Host(HOST_PORT_CLIENT, CLIENT_DATA_SIZE, "Client", "Server", packetQueue, clientDataQueue, serverDataQueue).start();
        new Host(HOST_PORT_SERVER, SERVER_DATA_SIZE, "Server", "Client", packetQueue, serverDataQueue, clientDataQueue).start();
    }
}
