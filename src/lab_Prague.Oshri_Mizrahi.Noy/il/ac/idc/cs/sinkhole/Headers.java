package il.ac.idc.cs.sinkhole;

import java.nio.*;

public class Headers {

    short ID;
    short flags;
    short numQuestions;
    short numAnswers;
    short numAuthorities;
    short numAdditionals;

    private static final short QR_BIT = (short) 0x8000;
    private static final short AA_BIT = (short) 0x0400;
    private static final short RD_BIT = (short) 0x0100;
    private static final short RA_BIT = (short) 0x0080;
    private static final short RCODE_MASK = (short) 0x000F;

    Headers(byte[] buff) {
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

    void changeQRToOne() {
        flags = (short) (flags | QR_BIT);
    }

    void changeAAToZero() {
        flags = (short) ((flags & 0xffff) & ~AA_BIT);
    }

    void changeRAToOne() {
        flags = (short) ((flags & 0xffff) | RA_BIT);
    }

    void changeRDToZero() {
        flags = (short) ((flags & 0xffff) & ~RD_BIT);
    }

    void changeRDToOne() {
        flags = (short) (((flags & 0xffff) & ~RD_BIT) | RD_BIT);
    }

    void changeRcodeToThree() {
        flags = (short) (((flags & 0xffff) & (~RCODE_MASK)) | 3);
    }

    // For debugging
    public void printHeaders() {
        System.out.println("Headers: \n" +
                "xID=" + ID + "\n" +
                "flags=" + flags + "\n" +
                "numQuestions=" + numQuestions + "\n" +
                "numAnswers=" + numAnswers + "\n" +
                "numAuthorities=" + numAuthorities + "\n" +
                "numAdditionals=" + numAdditionals + "\n" +
                "The AA bit is = " + (short) ((flags & AA_BIT) == 0 ? 0 : 1) + "\n" +
                "The QR bit is = " + ((short) (flags & QR_BIT) == 0 ? 0 : 1));


    }
}
