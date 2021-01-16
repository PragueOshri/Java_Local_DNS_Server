package il.ac.idc.cs.sinkhole;

import java.util.*;
import java.nio.*;

class HandleDNSData {

    private DNSInformation curDNS;

    HandleDNSData(DNSInformation dns) {

        curDNS = dns;
    }

    /**
     * Get: byte[] of the received packet and offset to start to read in this packet
     * Translate from the packet the name (of a query / DNS server - authority)
     * Return: Tuple2<String, Integer> - String holds the name and
     * the Integer holds the number of cells we need to jump to continue after the name
     */
    PairTuple<String, Integer> readName(byte[] buff, int offset) {
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
            } else {
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
        StringBuilder resultName = new StringBuilder();
        i = 0;
        while (name[i] != '\0')
            resultName.append((char) name[i++]);
        return new PairTuple<>(resultName.toString(), numCellsToJump);
    }

    int readQuestions(byte[] buff, int numQues, int offset) {
        int curPos = offset;
        int numQuesSaw = 0;
        while (numQuesSaw < numQues) {
            PairTuple<String, Integer> queryNameAndPos = readName(buff, curPos);
            curPos += queryNameAndPos.second;
            curPos = curPos + 4; // where 2 bytes for type and another 2 bytes for class
            numQuesSaw++;
        }
        return curPos;
    }

    int readAuthority(byte[] buff, int pos, boolean isAuth) {
        int curPos = pos;
        PairTuple<String, Integer> authNameWithIP = readName(buff, curPos);
        curPos += authNameWithIP.second;
        ByteBuffer wrapped = ByteBuffer.wrap(buff);
        int type = wrapped.getShort(curPos);
        curPos += 10; // 2 type + 2 class + 4 TTL + 2 RDATA length
        if (type == 1) {
            byte[] ip = Arrays.copyOfRange(buff, curPos, curPos + 4);
            if (isAuth) {
                curDNS.authoritiesNameIP.put(authNameWithIP.first, ip);
            } else {
                curDNS.additionalNameIP.put(authNameWithIP.first, ip);
            }
            curPos += 4;
        } else {
            PairTuple<String, Integer> authNameWithoutIP = readName(buff, curPos);
            curPos += authNameWithoutIP.second;
            if (isAuth) {
                curDNS.authoritiesNameName.put(authNameWithoutIP.first, authNameWithoutIP.first);
            }
        }
        return curPos;
    }

    int readAdditional(byte[] buff, int pos) {
        return readAuthority(buff, pos, false);
    }
}
