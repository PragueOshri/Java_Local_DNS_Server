package il.ac.idc.cs.sinkhole;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ReceiveDNSPacket {

    public InetAddress clientIP;
    DatagramSocket socket;
    DatagramPacket packet;
    public byte[] received;
    public int packetLength;
    boolean isClient;
    int clientPort;

    public ReceiveDNSPacket(DatagramSocket serverSocket, boolean isClient) throws IOException {
        socket = serverSocket;
        received = new byte[64 * 1024];
        this.isClient = isClient;
    }

    public byte[] receivedByteArray() throws IOException {
        packet = new DatagramPacket(received, received.length);
        socket.receive(packet);
        packetLength = packet.getLength();
        if (isClient) {
            clientIP = packet.getAddress();
            clientPort = packet.getPort();
        }
        return received;
    }
}
