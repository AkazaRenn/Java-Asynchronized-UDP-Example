import java.net.DatagramPacket;
import java.util.Arrays;

/**
 * Methods to help with the usage of packets, sockets, etc.
 */
public class Utils {
    
    public static final byte[] REQUEST_DATA = {0x09, 0x09, 0x09, 0x09};
    
    /**
     * Transform a byte array into a String ignoring tailing 0's
     * 
     * @param bytes the byte array to be transformed
     * @return a string representing the byte array
     */
    public static String byteArrayAsString(byte[] bytes) {
        return Arrays.toString(byteArrayWithoutTail(bytes));
    }

    /**
     * Trimming the byte array
     * 
     * @param bytes the byte array to be trimmed
     * @return the trimmed byte array
     */
    public static byte[] byteArrayWithoutTail(byte[] bytes) {
        return Arrays.copyOfRange(bytes, 0, findTailOfBytes(bytes));
    }
    
    /**
     * Finds the index of the start of tailing 0's in the byte array
     * 
     * @param bytes the byte array to be found the tailing 0's index
     * @return the index of the start of tailing 0's
     */
    public static int findTailOfBytes(byte[] bytes) {
        for(int i = 3; i < bytes.length - 1; i++) {
            if(bytes[i] == (byte)0x00 && bytes[i + 1] == (byte)0x00) {
                return i + 1;
            }
        }
        return bytes.length;
    }
    
    /**
     * Prints the packet details
     * 
     * @param packet the packet to be printed
     * @param printDataAsString whether convert the data in the packet into a string and print
     */
    public static void printPacketDetails(DatagramPacket packet, boolean printDataAsString) {
        System.out.println("Address: " + packet.getSocketAddress());
        if(printDataAsString) {
            System.out.println("Data as string: " + new String(byteArrayWithoutTail(packet.getData())));
        }
        System.out.println("Data as bytes: " + byteArrayAsString(packet.getData()));
        System.out.println("Offset: " + packet.getOffset());
        System.out.println();
    }
    
    /**
     * Assert condition is true, throw AssertionError otherwise
     * 
     * @param condition the condition to be asserted
     * @param errorMsg error message of the error if condition is false
     */
    public static void assertTrue(boolean condition, String errorMsg) {
        if(!condition) {
            throw new AssertionError(errorMsg);
        }
    }
    
    /**
     * Return whether the bytes given indicates a data request
     * 
     * @param bytes bytes to be checked
     * @return whether the bytes given indicates a data request
     */
    public static boolean isRequest(byte[] bytes) {
        return Arrays.equals(Arrays.copyOfRange(bytes, 0, 4), REQUEST_DATA);
    }
}
