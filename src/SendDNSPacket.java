import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class SendDNSPacket {

    DatagramSocket serverSocket;
    DatagramPacket packet;
    byte[] message;
    int port;

    public SendDNSPacket(DatagramSocket socket, byte[] buff, int length,
                         InetAddress IP, boolean isAns, int port, boolean isNameErr) throws IOException {
        serverSocket = socket;
        this.port = port;
        message = Arrays.copyOf(buff, length);
        buildPacket(IP, isAns, isNameErr);
        serverSocket.send(packet);
    }

    private void buildPacket(InetAddress IP, boolean isAns, boolean isNameErr) throws IOException {
        Headers headers = new Headers(message);
        if (isAns) {
            headers.changeQRToOne();
            headers.changeRDToOne();
            headers.changeAAToZero();
            headers.changeRAToOne();
            fillMessageFlags(headers);
        }
        else {
            headers.changeRDToZero();
            fillMessageFlags(headers);
        }
        if (isNameErr) {
            headers.changeRcodeToThree();
            fillMessageFlags(headers);
        }
        //fillMessage(buff, length);
        packet = new DatagramPacket(message, message.length, IP, port);
    }

    private void fillMessageFlags(Headers header) {
        ByteBuffer wrapper = ByteBuffer.wrap(message);
        wrapper.putShort(0,header.ID);
        wrapper.putShort(2,header.flags);
    }
/*
    private void fillMessage(byte[] buff, int length) {
        ByteBuffer wrapper = ByteBuffer.wrap(message);
        wrapper.put(buff, 4, length - 4);

    }
*/
}
