import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

public class Sender {

    BufferedReader br;
    DatagramSocket clientSocket;
    InetAddress IPAddress;
    PacketBuilder builder;
    byte[] buildedBuf;
    DatagramPacket packet;

    public Sender(DatagramSocket sendSocket, byte[] _buf, int length, InetAddress IP) throws IOException{
        clientSocket = sendSocket;
        builder = new PacketBuilder(_buf, length);
        builder.build();
//        buildedBuf = builder.bufWrapper.array();
        buildedBuf = builder.buf;
        System.out.println("builded buff size is = " + buildedBuf.length);
        IPAddress = IP;
        sendPacket(IPAddress);
        System.out.println("Send the packet to IP = " + IPAddress.toString());


//        br = new BufferedReader(new InputStreamReader(System.in));
//        clientSocket = new DatagramSocket();
//        IPAddress = packet.getAddress();



    }

    public void sendPacket(InetAddress IP) throws IOException{
        System.out.println("buildbuf length = " + buildedBuf.length);
        System.out.println("The next IP we found is = " + IP.getHostAddress());
        ByteBuffer bufWrapper = ByteBuffer.wrap(buildedBuf);
//        System.out.println("char = " + (char)bufWrapper.get(28));
//        System.out.println("int = " + (int)bufWrapper.get(29));
//        System.out.println("type = " + bufWrapper.getShort(30));
//        System.out.println("class = " + bufWrapper.getShort(32));
        packet = new DatagramPacket(buildedBuf, buildedBuf.length, IP, 53);
        clientSocket.send(packet);

    }


}
