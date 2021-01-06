import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;


public class Receiver {

    DatagramSocket socket;
    byte[] recvBuf;
    DatagramPacket packet;
    InetAddress clientIP;
    InetAddress IPAddress;
//    String receivedPacketData;
    int numByteInMessage;

    public Receiver(DatagramSocket _socket){
        socket = _socket;
        recvBuf = new byte[64*1024];

//        receivedPacketData = ByteBuffer.wrap(recvRespondByteArray(), packet.getOffset(), packet.getLength()).toString();
    }

    public byte[] recvClientByteArray() {
        try {
            packet = new DatagramPacket(recvBuf, recvBuf.length);
            socket.receive(packet);
            numByteInMessage = packet.getLength();
            clientIP = packet.getAddress();
            return Arrays.copyOf(recvBuf, numByteInMessage);
        }
        catch (IOException e) {
            System.err.println("Exception:  " + e);
        }
        return(null);
    }


    public byte[] recvRespondByteArray() {
        try {
            packet = new DatagramPacket(recvBuf, recvBuf.length);
            socket.receive(packet);
            int byteCount = packet.getLength();
            IPAddress = packet.getAddress();
//            System.out.println("size of message: " + byteCount);
//            receivedPacketData = Arrays.copyOfRange(packet.getData(), 0, byteCount);
            return Arrays.copyOf(recvBuf, byteCount);
        }
        catch (IOException e) {
            System.err.println("Exception:  " + e);
            e.printStackTrace();
        }
        return(null);
    }
}
