import java.nio.*;
import java.util.Arrays;

public class PacketBuilder {


    DNSHeaders headers;
    int bitRD;
    ByteBuffer bufWrapper;
    byte[] buf;


    public PacketBuilder(byte[] _buf, int length) {
        headers = new DNSHeaders(_buf);
//        if(headers.isRD()){
//            changeRDToZero();
//        }
        buf = Arrays.copyOf(_buf, length);
    }

    private void changeRDToZero() {
        headers.flags = (short)(headers.flags & 0xfffffeff);
        bitRD = headers.flags & 128;
        System.out.println("The RD bit on the new flag is: " + (headers.flags & 128));
    }

    public void build() {
        bufWrapper = ByteBuffer.wrap(buf);
        bufWrapper.putShort(0, headers.xID);
        bufWrapper.putShort(2, headers.flags);
        bufWrapper.putShort(4, headers.qdcount);
        bufWrapper.putShort(6, headers.ancount);
        bufWrapper.putShort(8, headers.nscount);
        bufWrapper.putShort(10, headers.arcount);
    }

// need to build two function:
    // create a query packet
    // create a response packet

}
