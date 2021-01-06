import java.nio.*;

public class DNSData {

    String address;
    String nameNextServer; // find at authority
    String nameCurServer; // find at additional


    int numberOfQuestions;
    int numberOfAnswer;
    int numberOfAuthority;
    int numberOfAdditional;
    int getIndexOfAnswer;

    public DNSData(byte[] buf, int offset, int length) {
        parse(buf, offset, length);
    }

    private void parse(byte[] buf, int offset, int length) {
        ByteBuffer wrapped = ByteBuffer.wrap(buf, offset, length);
        int nameLength = 0;
//        System.out.println("offset = " + offset + ", length = " + length);
//        System.out.println("buf[" + offset + "] = " + buf[offset]);
        for (int i = offset; i < offset + length; ++i) {
            if (buf[i] != '\0') {
                ++nameLength; // number of steps we need to do until getting the '\0'
            }
            else {
                break;
            }
        }
//        System.out.println("NameLength = " + name_length);
        int currentPos = offset;
        int end = currentPos + nameLength;
        while (currentPos < end) { // want to stop when getting the '\0'
//            System.out.println("curPos = " + currentPos + ", end = " + end);
            int numChars = buf[currentPos] & 0xff; // read the label --------> HAVE to be a positive number.
            currentPos++;
            for (int i = currentPos; i < currentPos + numChars; i++) {
                address += (char)buf[i];
            }
            address += '.';
            currentPos += numChars;
        }


        // Three options for reading a Name section:
        // 1. Represents as sequence of #chars then chars until '\0'.
        // 2. A pointer which starts with 11 - byte that represent an int grader than 192.
        // 3. A combination of 1 & 2, starts with labels and then a pointer.
//        public Tuple2<String, Integer> getName(byte[] _bufOfAllPacket, int position) {
//            String addressName = "";
//            Integer newPos = 0;
//            int curPos = position;
//
////        System.out.println("1. _bufOfAllPacket[" + curPos + "] = " + (_bufOfAllPacket[curPos] & 0xff));
//            while (_bufOfAllPacket[curPos] != '\0') {
////            System.out.println("2. _bufOfAllPacket[" + curPos + "] = " + (_bufOfAllPacket[curPos] & 0xff));
//                if ((_bufOfAllPacket[curPos] & 0xFF) >= 192) { // there a pointer to the name
////                System.out.println("3. _bufOfAllPacket[" + curPos + "] = " + (_bufOfAllPacket[curPos] & 0xff));
//                    // to get the offset of the pointer
//                    int pointer = (((_bufOfAllPacket[curPos] & 0xff) * 256) + (_bufOfAllPacket[curPos + 1] & 0xff) - 49152);
////                System.out.println("pointer = " + pointer);
//                    DNSData addressData = new DNSData(_bufOfAllPacket, pointer, _bufOfAllPacket.length-pointer);
//                    addressName += addressData.address;
//                    newPos = curPos+2;
////                System.out.println("the new position after reading the name = " + newPos);
//                    return new Tuple2<>(addressName, newPos);
//                }
////            System.out.println("4. _bufOfAllPacket[" + curPos + "] = " + (int)_bufOfAllPacket[curPos]);
//                int numChars = (int)_bufOfAllPacket[curPos];
//                curPos++;
//                int end = curPos + numChars;
//                while (curPos < end) {
////                System.out.println("4.1. _bufOfAllPacket[" + curPos + "] = " + (int)_bufOfAllPacket[curPos] + "a=" + (int)'a');
//                    addressName += (char)_bufOfAllPacket[curPos];
//                    curPos++;
//                }
////            System.out.println("5. addressName = " + addressName);
//            }
////        System.out.println("6. found end of string");
//            newPos = curPos;
//            return new Tuple2<>(addressName, newPos);
//        }

//        System.out.println("address = " + address);
//        System.out.println("out of the while loop in parse function");



//        for (int i = offset; i < name_length; i++) {
//            int currentPos = i;
//            int numChars = (int)buf[currentPos];
//            for (int j = currentPos+1; j < currentPos + numChars; j++) {
//                address += (char)buf[j];
//            }
//            address += '.';
//            i += numChars;
//        }
    }
}
