import java.util.*;
import java.io.*;
import java.nio.*;

public class HandleDNSData {

    DNSInformation curDNS;

    public HandleDNSData(DNSInformation dns) {
        curDNS = dns;
    }

    /**
     Get: byte[] of the received packet and offset to start to read in this packet
     Translate from the packet the name (of a query / DNS server - authority)
     Return: Tuple2<String, Integer> - String holds the name and
             the Integer holds the number of cells we need to jump to continue after the name
     */
    public Tuple2<String, Integer> readName(byte[] buff, int offset) {
        byte[] name = new byte[256];
        int index = 0;
        int numCellsToJump = 1;
        boolean isPointer = false;

        name[0] = '\0';
        while (buff[offset] != '\0') {
            if ((buff[offset] & 0xff) >= 192) { // checking if there a pointer
                int pointerOffset = (((buff[offset] & 0xff) * 256) + (buff[offset + 1] & 0xff) - 49152);
                offset = pointerOffset - 1;
                isPointer = true;
            }
            else {
                name[index++] = buff[offset];
            }
            ++offset;
            if (!isPointer) { // if not pointer need to jumps all name cells
                ++numCellsToJump;
            }
        }

        name[index] = '\0'; // put '\0' at the end of the name
        if (isPointer) { // if is pointer need to jump just 2 cells
            ++numCellsToJump;
        }

        // translate the name from byte[] to string with '.' between
        int nameLength = 0;
        int i;
        for (i = 0; i < index; ++i) {
            if (name[i] != '\0')
                ++nameLength;
            else
                break;
        }
        for (i = 0; i < nameLength; ++i) {
            index = (name[i] & 0xff);
            for (int j = 0; j < index; ++j) {
                name[i] = name[i + 1];
                ++i;
            }
            name[i] = '.';
        }
        if (i > 0)
            name[i - 1] = '\0';
        String resultName = "";
        i = 0;
        while (name[i] != '\0')
            resultName += (char)name[i++];
        return new Tuple2<>(resultName, numCellsToJump);
    }

    public int readQuestions(byte[] buff, int numQues, int offset) {
        int curPos = offset;
        int numQuesSaw = 0;
        while (numQuesSaw < numQues) {
            Tuple2 queryNameAndPos = readName(buff, curPos);
            curPos += (int)queryNameAndPos.second;
            curPos = curPos + 4; // where 2 bytes for type and another 2 bytes for class
            numQuesSaw++;
        }
        return curPos;
    }

    public Tuple2<byte[], Integer> readAnswers(byte[] buff, int numQues, int numAns, int offset) {
        byte[] ans = new byte[4];
        int curPos = offset;
        if (numAns > 0) {
            Tuple2 ansNameAndPos = readName(buff, curPos);
            curPos += (int)ansNameAndPos.second;
            curPos = curPos + 8; // 4 for type and class + 2 for TTL + 2 RDATA length
            ans = Arrays.copyOfRange(buff, curPos, curPos+4);
        }
        else {
            System.out.println("There no answers in this packet");
        }
        Tuple2 ansByteArrayIPAndPos = new Tuple2(ans, curPos);
        return ansByteArrayIPAndPos;
    }

    public int readAuthority(byte[] buff, int pos, boolean isAuth) {
        int curPos = pos;
        Tuple2 authNameWithIP = readName(buff, curPos);
        curPos += (int)authNameWithIP.second;
        ByteBuffer wrapped = ByteBuffer.wrap(buff);
        int type = wrapped.getShort(curPos);
        curPos += 10; // 2 type + 2 class + 4 TTL + 2 RDATA length
        if (type == 1) {
            byte[] ip = Arrays.copyOfRange(buff, curPos, curPos + 4);
            if (isAuth)
                curDNS.authoritiesNameIP.put(authNameWithIP.first, ip);
            else {
                curDNS.additionalNameIP.put(authNameWithIP.first, ip);
            }
            curPos += 4;
        }
        else {
            Tuple2 authNameWithoutIP = readName(buff, curPos);
            curPos += (int)authNameWithoutIP.second;
            if (isAuth) {
                curDNS.authoritiesNameName.put(authNameWithoutIP.first, authNameWithoutIP.first);
            }
        }
        return curPos;
    }

    public int readAdditional(byte[] buff, int pos) throws IOException {
        return readAuthority(buff, pos, false);
    }
}
