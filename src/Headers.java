import java.nio.*;

public class Headers {

    public short ID;
    public short flags;
    public short numQuestions;
    public short numAnswers;
    public short numAuthorities;
    public short numAdditionals;

    static final short QR_BIT = (short) 0x8000;
    static final short AA_BIT = (short) 0x0400;
    static final short RD_BIT = (short) 0x0100;
    static final short RA_BIT = (short) 0x0080;
    static final short RCODE_MASK = (short) 0x000F;

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
        flags = (short)((flags & 0xffff) & ~AA_BIT);
    }

    public boolean isAAEqualOne() {
        boolean f = ((flags & 0xffff) & AA_BIT) != 0;
        return f;
    }

    public void changeRAToOne() {
        flags = (short)((flags & 0xffff) | RA_BIT);
    }

    public void changeRDToZero() {
        flags = (short)((flags & 0xffff) & ~RD_BIT);
    }

    public void changeRDToOne() {
        flags = (short)(((flags & 0xffff) & ~RD_BIT) | RD_BIT);
    }

    public void changeRcodeToThree() {
        flags = (short)(((flags & 0xffff) & (~RCODE_MASK)) | 3);
    }

    public void printHeaders() {
        System.out.println("my headers are: \n" +
                "xID=" + ID + "\n" +
                "flags=" + flags + "\n" +
                "numQuestions=" + numQuestions + "\n" +
                "numAnswers=" + numAnswers + "\n" +
                "numAuthorities=" + numAuthorities + "\n" +
                "numAdditionals=" + numAdditionals + "\n" +
                "The AA bit is = " + (short)((flags & AA_BIT) == 0 ? 0 : 1) + "\n" +
                "The QR bit is = " + ((short)(flags & QR_BIT) == 0 ? 0 : 1));


    }
}
