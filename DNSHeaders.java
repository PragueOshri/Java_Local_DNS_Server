import java.io.*;
import java.nio.*;

public class DNSHeaders {

    public short xID;
    public short flags;
    public short qdcount;
    public short ancount;
    public short nscount;
    public short arcount;


    public DNSHeaders(byte[] buf) {
        parse(buf);
    }

    public void parse(byte[] buf) {
        ByteBuffer wrapped = ByteBuffer.wrap(buf);
        xID = wrapped.getShort(0);
        flags = wrapped.getShort(2);
        qdcount = wrapped.getShort(4);
        ancount = wrapped.getShort(6);
        nscount = wrapped.getShort(8);
        arcount = wrapped.getShort(10);
    }

    public boolean isQuery() {
        return (flags & 1) == 0;
    }

    public boolean isRD() {
        return (flags & 128) == 1;
    }


}
