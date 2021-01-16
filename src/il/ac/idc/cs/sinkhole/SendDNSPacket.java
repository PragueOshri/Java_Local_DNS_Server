package il.ac.idc.cs.sinkhole;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class SendDNSPacket {

    private DatagramPacket packet;
    private byte[] message;
    private int port;

    public SendDNSPacket(DatagramSocket socket, byte[] buff, int length,
                         InetAddress IP, boolean isAns, int port, boolean isNameErr) {
        DatagramSocket serverSocket = socket;
        this.port = port;
        message = Arrays.copyOf(buff, length);
        buildPacket(IP, isAns, isNameErr);
        try {
            serverSocket.send(packet);
        } catch (IOException e) {
//            todo: handle exception
        }
    }

    private void buildPacket(InetAddress IP, boolean isAns, boolean isNameErr) {
        Headers headers = new Headers(message);
        if (isAns) {
            headers.changeQRToOne();
            headers.changeRDToOne();
            headers.changeAAToZero();
            headers.changeRAToOne();
            fillMessageFlags(headers);
        } else {
            headers.changeRDToZero();
            fillMessageFlags(headers);
        }
        if (isNameErr) {
            headers.changeRcodeToThree();
            fillMessageFlags(headers);
        }
        packet = new DatagramPacket(message, message.length, IP, port);
    }

    private void fillMessageFlags(Headers header) {
        ByteBuffer wrapper = ByteBuffer.wrap(message);
        wrapper.putShort(0, header.ID);
        wrapper.putShort(2, header.flags);
    }

}
