import java.nio.*;

public class Headers {

    public short ID;
    public short flags;
    public short numQuestions;
    public short numAnswers;
    public short numAuthorities;
    public short numAdditionals;

    static final short QR_BIT =         (short) 0x8000;
    static final short OPCODE_MASK =    (short) 0x7800;
    static final int   OPCODE_SHIFT =   11;
    static final short AA_BIT =         (short) 0x0400;
    static final short TC_BIT =         (short) 0x0200;
    static final short RD_BIT =         (short) 0x0100;
    static final short RA_BIT =         (short) 0x0080;
    static final short RCODE_MASK =     (short) 0x000F;

    public Headers(byte[] buff) {
        parseHeader(buff);
    }

    private void parseHeader(byte[] buff) {
        ByteBuffer wrapped = ByteBuffer.wrap(buff);
        ID = wrapped.getShort(0);
        flags = wrapped.getShort(2);
        numQuestions = wrapped.getShort(4);
        numAnswers = wrapped.getShort(6);
        numAuthorities = wrapped.getShort(8);
        numAdditionals = wrapped.getShort(10);
    }

    public void changeQRToOne() {
        flags = (short)(flags | QR_BIT);
    }

    public void changeAAToZero() {
        //flags = (short)(flags & 0xfb);
        flags = (short)((flags & 0xffff) | AA_BIT);
    }

    public boolean isAAEqualOne() {
        boolean f = ((flags & 0xffff) & AA_BIT) != 0;
        System.out.println("Exists AA == " + (f ? "true" : "false"));
        return /*((flags & 0xffff) & AA_BIT) != 0*/f;
    }

    public void changeRAToOne() {
        flags = (short)((flags & 0xffff) | RA_BIT);
    }

    public void changeRDToZero() {
        flags = (short)((flags & 0xffff) & ~RD_BIT);
    }

    public void changeRcodeToThree() {
        flags = (short)(((flags & 0xffff) & (~RCODE_MASK)) | 3);
    }

    public void printHeaders() {
        short v = 3;
        System.out.println("3 has bit_0 " + (short)((3 & 0xffff) & (1 << 0)) + " and has bit_1 " +
                (short)((3 & 0xffff) & (1 << 1)) + " and has bit_2 " + (short)((3 & 0xffff) & (1 << 2)));
        System.out.println("my headers are: \n" +
                "xID=" + ID + "\n" +
                "flags=" + flags + "\n" +
                "qdcount=" + numQuestions + "\n" +
                "ancount=" + numAnswers + "\n" +
                "nscount=" + numAuthorities + "\n" +
                "arcount=" + numAdditionals + "\n" +
                "The AA bit is = " + (short)((flags & AA_BIT) == 0 ? 0 : 1) + "\n" +
                "The QR bit is = " + ((short)(flags & QR_BIT) == 0 ? 0 : 1));


    }
}
