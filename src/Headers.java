import java.nio.*;

public class Headers {

    public short ID;
    public short flags;
    public short numQuestions;
    public short numAnswers;
    public short numAuthorities;
    public short numAdditionals;

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
        flags = (short)(flags | 0x1000);
    }

    public void changeAAToZero() {
        flags = (short)(flags & 0xfb);
    }

    public boolean isAAEqualOne() {
        return (short)(flags & 0x04) == 32;
    }

    public void changeRAToOne() {
        flags = (short)(flags | 0x0001);
    }

    public void changeRDToZero() {
        flags = (short)(flags & 0xfffd);
    }

    public void changeRcodeToThree() {
        flags = (short)(flags & 0xfff3);
    }
}
