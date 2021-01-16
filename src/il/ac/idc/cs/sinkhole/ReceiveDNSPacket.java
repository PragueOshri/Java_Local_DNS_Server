package il.ac.idc.cs.sinkhole;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ReceiveDNSPacket {

    InetAddress clientIP;
    private DatagramSocket socket;
    byte[] received;
    int packetLength;
    private boolean isClient;
    int clientPort;

    public ReceiveDNSPacket(DatagramSocket serverSocket, boolean isClient) {
        socket = serverSocket;
        received = new byte[64 * 1024];
        this.isClient = isClient;
    }

    byte[] receivedByteArray() {
        try {
            DatagramPacket packet = new DatagramPacket(received, received.length);
            socket.receive(packet);
            packetLength = packet.getLength();
            if (isClient) {
                clientIP = packet.getAddress();
                clientPort = packet.getPort();
            }
        } catch (IOException e) {
            System.err.println("Failed to received packet");
        }
        return received;
    }
}
