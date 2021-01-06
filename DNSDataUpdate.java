import java.nio.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class DNSDataUpdate {

    String nameQueryAddress;
    String nameNextServer; // find at authority
    String nameCurServer; // find at additional

    HashMap authoritiesNamesIP;
    HashMap additionalNames;

    public DNSDataUpdate(byte[] buff, int offset, int length) {

    }

    public String parseName(byte[] buff, int offset, int length) {
        String addressName = "";
        int nameLength = 0;
        for (int i = offset; i < offset + length; i++) {
            if (buff[i] != '\0') {
                ++nameLength; // number of steps we need to do until getting the '\0'
            }
            else {
                break;
            }
        }
        int currentPos = offset;
        int end = currentPos + nameLength;
        while (currentPos < end) { // want to stop when getting the '\0'
            int numChars = buff[currentPos] & 0xff; // read the label ---> HAVE to be a positive number.
            currentPos++;
            for (int i = currentPos; i < numChars; i++) {
                addressName += (char)buff[i];
            }
            currentPos += numChars;
        }
        return addressName;
    }

    // Three options for reading a Name section:
    // 1. Represents as sequence of #chars then chars until '\0'.
    // 2. A pointer which starts with 11 - byte that represent an int grader than 192.
    // 3. A combination of 1 & 2, starts with labels and then a pointer.
    public Tuple2<String, Integer> getName(byte[] buff, int pos) {
        String name = "";
        int curPos = pos;
        Tuple2<String, Integer> nameAndPos;

        while (buff[curPos] != '\0') {
            if ((buff[curPos] & 0xFF) >= 192) { // check if there a pointer ---> MSB 11
                int pointer = (((buff[curPos] & 0xff) * 256) + (buff[curPos + 1] & 0xff) - 49152);
                name += parseName(buff, pointer, buff.length-pointer);
                Integer newPos = curPos + 2;
                nameAndPos = new Tuple2<>(name, newPos);
                return nameAndPos;
            }
            int numChars = (int)buff[curPos];
            curPos++;
            int end = curPos + numChars;
            while (curPos < end) {
                name += (char)buff[curPos];
                curPos++;
            }
        }
        Integer newPos = curPos;
        nameAndPos = new Tuple2<>(name, newPos);
        return nameAndPos;
    }

    public int readQuestions(byte[] buff, int numQues) {
        int curPos = 12;
        int numQuesSaw = 0;
        while (numQuesSaw < numQues) {
            Tuple2 queryNameAndPos = getName(buff, curPos);
            curPos = (int)queryNameAndPos.v;
            int type = (buff[curPos] & 0xFF * 256 + (int)buff[++curPos]);
            curPos = curPos + 4; // where 2 bytes for type and another 2 bytes for class
            numQuesSaw++;
        }
        return curPos;
    }

    public byte[] readAnswers(byte[] buff, int numQues, int numAns) {
        byte[] ans = new byte[4];
        int curPos = readQuestions(buff, numQues);
        if (numAns > 0) {
            Tuple2 ansNameAndPos = getName(buff, curPos);
            curPos = (int)ansNameAndPos.v;
            curPos = curPos + 8; // 4 for type and class + 2 for TTL + 2 RDATA length
            ans = Arrays.copyOfRange(buff, curPos, curPos+4);
        }
        else {
            System.out.println("There no answers in  this packet");
        }
        return ans;
    }

    // we call this function only if there NO answer
    // and if #authorities > 0
    public byte[] readAuthorities(byte[] buff, int numQues, int numAuth) {
        byte[] auth = new byte[4];
        int curPos = readQuestions(buff, numQues);
        int numAuthSaw = 1;
        while (numAuth <= numAuth) {
            Tuple2 authNameAndPos = getName(buff, curPos);
            curPos = (int)authNameAndPos.v;
            curPos++;
            int type = buff[curPos] | buff[++curPos] << 8; // translate the type
            if (type == 1) {
                curPos = curPos + 6; // 2 class + 2 TTL + 2 RDATA length
                auth = Arrays.copyOfRange(buff, curPos, curPos+4);
                authoritiesNamesIP.put(authNameAndPos.k, auth);
            }
            else { // if type NOT 1, there only name without IP

                curPos = curPos + 6; // 2 class + 2 TTL + 2 RDATA length
                if (buff[curPos] != '\0') {
                    authNameAndPos = getName(buff, curPos);
                    curPos = (int)authNameAndPos.v;
                    curPos++;
                    authoritiesNamesIP.put(authNameAndPos.k, 0); // the IP of this name suppose to be in the additional
                }
            }
            numAuthSaw++;
        }
        //HashMap lastAuthAndPos = new
        return auth; // might be empty, need to check if NOT all zeros
    }

    public void readAdditional(byte[] buff, int numQues, int numAuth, int numAdd) {
        byte[] add = new byte[4];

    }
}
