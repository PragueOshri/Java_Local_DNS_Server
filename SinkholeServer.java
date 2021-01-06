import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SinkholeServer {

    final List<String> rootsDNS;
    InetAddress clientIP;
    InetAddress IPAddress;
    DatagramSocket localServerSocket;
//    DatagramPacket clientPacketRecv;
//    byte[] clientByteRecv;

    Receiver receiver;
    byte[] bufOfAllQueryPacket;
    byte[] bufOfAllPacket;

    int numberOfQuestions;
    int numberOfAnswer;
    int numberOfAuthority;
    int numberOfAdditional;
    boolean haveAnswer;
    PacketBuilder builder;
    Sender sender;

    private List<String> initializeRootsList() {
        List<String> listOfRoots = new ArrayList<>();
        listOfRoots.add("198.41.0.4");
        listOfRoots.add("199.9.14.201");
        listOfRoots.add("192.33.4.12");
        listOfRoots.add("199.7.91.13");
        listOfRoots.add("192.203.230.10");
        listOfRoots.add("192.5.5.241");
        listOfRoots.add("192.112.36.4");
        listOfRoots.add("198.97.190.53");
        listOfRoots.add("192.36.148.17");
        listOfRoots.add("192.58.128.30");
        listOfRoots.add("193.0.14.129");
        listOfRoots.add("199.7.83.42");
        listOfRoots.add("202.12.27.33");
        return listOfRoots;
    }


    public SinkholeServer() throws IOException {
        rootsDNS = initializeRootsList();
        System.out.println("1 \n");

        // listen on port 5300
        localServerSocket = new DatagramSocket(5300);
        System.out.println("2 \n");
//        clientByteRecv = new byte[64*1024];
//        clientPacketRecv = new DatagramPacket(clientByteRecv, clientByteRecv.length);
        System.out.println("waiting for packets \n");

        // first packet that received
        // save the client IP address to send him the answer
        receiver = new Receiver(localServerSocket);
        bufOfAllPacket = receiver.recvClientByteArray();
        bufOfAllQueryPacket = Arrays.copyOf(bufOfAllPacket, bufOfAllPacket.length);
        System.out.println("The size of the packet we received = " + receiver.numByteInMessage);

        System.out.println("bufOfAllPacket.length = " + bufOfAllPacket.length);

//        for (int i = 0; i < bufOfAllPacket.length; ++i) {
//            if (bufOfAllPacket[i] >= 'A' && bufOfAllPacket[i] <= 'z')
//                System.out.println("_bufOfAllPacket[" + i + " = " + (char) bufOfAllPacket[i]);
//            else
//                System.out.println("_bufOfAllPacket[" + i + " = " + (bufOfAllPacket[i] & 0xff));
//        }
//        ByteBuffer bufWrapper = ByteBuffer.wrap(bufOfAllPacket);
//        System.out.println("char = " + (char)bufWrapper.get(28));
//        System.out.println("int = " + (int)bufWrapper.get(29));
//        System.out.println("type = " + bufWrapper.getShort(30));
//        System.out.println("class = " + bufWrapper.getShort(32));




//        localServerSocket.receive(clientPacketRecv);
//        int byteCount = clientPacketRecv.getLength();
//        clientIP = clientPacketRecv.getAddress();
//        System.out.println("the client IP is = " + clientIP.toString());

        // select a random root server from our list
        // send a query packet to a rootServer
        String rootIPstr = rootsDNS.get(chooseDNS());
        System.out.println("The root DNS that chosen is = " + rootIPstr);
        InetAddress rootIP = InetAddress.getByName(rootIPstr);
        System.out.println("The root DNS that chosen is = " + rootIP.getHostAddress());
//        builder = new PacketBuilder(clientByteRecv);
//        sender = new Sender(clientByteRecv, byteCount, rootIP);
        sender = new Sender(localServerSocket, bufOfAllPacket, receiver.numByteInMessage, rootIP);
        System.out.println("waiting for packets after sending...\n");
        if (true) {
            // received responds
            receiver = new Receiver(localServerSocket);


            bufOfAllPacket = receiver.recvRespondByteArray();
            System.out.println("received a packet from a root DNS");
            DNSHeaders headers = getHeaders();
            if (headers.ancount > 0) {
                byte[] ansAddress = findAnsIP(bufOfAllPacket, headers.qdcount, headers.ancount);
                IPAddress = IPAddress.getByAddress(ansAddress);
                System.out.println("The answer IP we found is = " + IPAddress.getHostAddress());
            }

            for (int i = 0; i < 40; ++i) {
                if (bufOfAllPacket[i] >= 'A' && bufOfAllPacket[i] <= 'z')
                    System.out.println("_bufOfAllPacket[" + i + " = " + (char) bufOfAllPacket[i]);
                else
                    System.out.println("_bufOfAllPacket[" + i + " = " + (bufOfAllPacket[i] & 0xff));
            }


            // -------------------------------------------------------------------------------------------------------
            DNSData data = new DNSData(bufOfAllPacket, 12, bufOfAllPacket.length - 12); // <------------ something wrong HERE
            // -------------------------------------------------------------------------------------------------------
            System.out.println("The address we want to find = " + data.address);
            byte[] ipAddress = findNextIP(bufOfAllPacket, headers.qdcount, headers.nscount, headers.arcount);
//            System.out.println("The next IP we found is: ");
//            for (int i = 0; i < 4; i++) {
//                System.out.println((ipAddress[i] & 0xff) + ".");
//            }

            IPAddress = IPAddress.getByAddress(ipAddress);
            System.out.println("The next IP we found is = " + IPAddress.getHostAddress());

            sender = new Sender(localServerSocket, bufOfAllQueryPacket, bufOfAllQueryPacket.length, IPAddress);
            System.out.println("waiting for packets after sending to authority...\n");
            receiver = new Receiver(localServerSocket);
            bufOfAllPacket = receiver.recvRespondByteArray();
            System.out.println("received a packet from a Authority DNS");
            DNSHeaders authHeaders = getHeaders();
            if (authHeaders.ancount > 0) {
                byte[] ansAddress = findAnsIP(bufOfAllPacket, authHeaders.qdcount, authHeaders.ancount);
                IPAddress = IPAddress.getByAddress(ansAddress);
                System.out.println("The answer IP we found is = " + IPAddress.getHostAddress());
            }
            ipAddress = findNextIP(bufOfAllPacket, authHeaders.qdcount, authHeaders.nscount, authHeaders.arcount);
            IPAddress = IPAddress.getByAddress(ipAddress);
            System.out.println("The next IP we found is = " + IPAddress.getHostAddress());

            sender = new Sender(localServerSocket, bufOfAllQueryPacket, bufOfAllQueryPacket.length, IPAddress);
            System.out.println("waiting for packets after sending to authority...\n");
//            receiver = new Receiver(localServerSocket);
//            bufOfAllPacket = receiver.recvRespondByteArray();
//            System.out.println("received a packet from a Authority DNS");
//            DNSHeaders SecAuthHeaders = getHeaders();
//            if (SecAuthHeaders.ancount > 0) {
//                byte[] ansAddress = findAnsIP(bufOfAllPacket, SecAuthHeaders.qdcount, SecAuthHeaders.ancount);
//                IPAddress = IPAddress.getByAddress(ansAddress);
//                System.out.println("The answer IP we found is = " + IPAddress.getHostAddress());
//            }

//        builder = new PacketBuilder(bufOfAllPacket);
//        System.out.println("build packet RD bit is = " + builder.bitRD);


            haveAnswer = (headers.ancount > 0) ? true : false;
            if (haveAnswer) {
                // look for the answer in the answer section in the data
                int startPosOfAns = getIndex(bufOfAllPacket, 12, numberOfQuestions);

            }


            numberOfQuestions = headers.qdcount;
            numberOfAnswer = headers.ancount;
            numberOfAuthority = headers.nscount;
            numberOfAdditional = headers.arcount;


            if (headers.isQuery()) { // if we get a DNS query
                // send the query to a root DNS
            }
        }
    }



    public int chooseDNS() {
        return new Random().nextInt(13);
    }


    // extract the different elements of the packet headers
    public DNSHeaders getHeaders(){
        DNSHeaders headers = new DNSHeaders(bufOfAllPacket);
        if (headers != null) {
            System.out.println("received packet");
            System.out.println("my headers are: \n" +
                    "xID=" + headers.xID + "\n" +
                    "flags=" + headers.flags + "\n" +
                    "qdcount=" + headers.qdcount + "\n" +
                    "ancount=" + headers.ancount + "\n" +
                    "nscount=" + headers.nscount + "\n" +
                    "arcount=" + headers.arcount);
        }
        return headers;
    }


    public int getIndex(byte[] buf, int offset, int numOfVar) {
        int curPos = offset;
        int numVarWeSaw = 0;
        while (numVarWeSaw < numOfVar) {
            while (buf[curPos] != '\0') {
                curPos++;
            }
            numVarWeSaw++;
        }
        return curPos;
    }

    // Three options for reading a Name section:
    // 1. Represents as sequence of #chars then chars until '\0'.
    // 2. A pointer which starts with 11 - byte that represent an int grader than 192.
    // 3. A combination of 1 & 2, starts with labels and then a pointer.
    public Tuple2<String, Integer> getName(byte[] _bufOfAllPacket, int position) {
        String addressName = "";
        Integer newPos = 0;
        int curPos = position;

//        System.out.println("1. _bufOfAllPacket[" + curPos + "] = " + (_bufOfAllPacket[curPos] & 0xff));
        while (_bufOfAllPacket[curPos] != '\0') {
//            System.out.println("2. _bufOfAllPacket[" + curPos + "] = " + (_bufOfAllPacket[curPos] & 0xff));
            if ((_bufOfAllPacket[curPos] & 0xFF) >= 192) {
//                System.out.println("3. _bufOfAllPacket[" + curPos + "] = " + (_bufOfAllPacket[curPos] & 0xff));
                // to get the offset of the pointer
                int pointer = (((_bufOfAllPacket[curPos] & 0xff) * 256) + (_bufOfAllPacket[curPos + 1] & 0xff) - 49152);
//                System.out.println("pointer = " + pointer);
                DNSData addressData = new DNSData(_bufOfAllPacket, pointer, _bufOfAllPacket.length-pointer);
                addressName += addressData.address;
                newPos = curPos+2;
//                System.out.println("the new position after reading the name = " + newPos);
                return new Tuple2<>(addressName, newPos);
            }
//            System.out.println("4. _bufOfAllPacket[" + curPos + "] = " + (int)_bufOfAllPacket[curPos]);
            int numChars = (int)_bufOfAllPacket[curPos];
            curPos++;
            int end = curPos + numChars;
            while (curPos < end) {
//                System.out.println("4.1. _bufOfAllPacket[" + curPos + "] = " + (int)_bufOfAllPacket[curPos] + "a=" + (int)'a');
                addressName += (char)_bufOfAllPacket[curPos];
                curPos++;
            }
//            System.out.println("5. addressName = " + addressName);
        }
//        System.out.println("6. found end of string");
        newPos = curPos;
        return new Tuple2<>(addressName, newPos);
    }

    // Reading the questions section, includes name + type + class
    public int overTheQues(byte[] _bufOfAllPacket, int _numQues) {
        int curPos = 12; // we always want to start right after the headers
        int numQueSaw = 0;
        if (_numQues > 0) {
            while (numQueSaw < _numQues) {
//                System.out.println("before calling the getNAme function");
                Tuple2 addAndPos = getName(_bufOfAllPacket, curPos);
//                System.out.println("the address = " + addAndPos.k + " the pos is = " + addAndPos.v);
                curPos = (int)addAndPos.v;
                int type = (_bufOfAllPacket[curPos] & 0xFF * 256 + (int)_bufOfAllPacket[++curPos]);
//                System.out.println("The type is = " + type);
                curPos = curPos + 4; // after null there 4 byte of type and class
                numQueSaw++;
            }
//            System.out.println("The position after reading the questions = " + curPos);
        }
        return curPos;
    }

    // we call this function only if number of answers is bigger than 0
    public byte[] findAnsIP(byte[] _bufOfAllPacket, int _numQues, int _numAns) {
        byte[] ans = new byte[4];
        int curPos = overTheQues(_bufOfAllPacket, _numQues);
//        for (int i = curPos; i < bufOfAllPacket.length; ++i) {
//            if (_bufOfAllPacket[i] >= 'A' && _bufOfAllPacket[i] <= 'z')
//                System.out.println("_bufOfAllPacket[" + i + "] = " + (char)_bufOfAllPacket[i]);
//            else
//                System.out.println("_bufOfAllPacket[" + i + "] = " + (_bufOfAllPacket[i] & 0xff));
//        }

        int numAnsSaw = 0;
        while (numAnsSaw < _numAns) {
            Tuple2 addAndPos = getName(_bufOfAllPacket, curPos);
            curPos = (int)addAndPos.v; // here the curPos at the end of the name in the Answer section
            System.out.println("Pos after reading the the Answer Name = " + curPos);
            curPos = curPos + 4; // we forward the curPos the type and class (each represents with 2 bytes)
            curPos = curPos + 2; // we forward the curPos the TTL -> 2 byte
            curPos = curPos + 2; // we forward the curPos the RDATA length  -> 2 byte
            ans = Arrays.copyOfRange(_bufOfAllPacket, curPos, curPos+4);
            break;
        }
        return ans;
    }

    // we call this function only if number of answers is 0 and the number of authorities grader than 0
    public byte[] findNextIP(byte[] _bufOfAllPacket, int _numQues, int _numAuth, int _numAdd){
        byte[] ans = new byte[4];
//        System.out.println("before getting the questions");
        int curPos = overTheQues(_bufOfAllPacket, _numQues); // read the questions sections and update the position
        System.out.println("_numAuth = " + _numAuth);
        for (int i = curPos; i < curPos + 20; ++i) {
            if (_bufOfAllPacket[i] >= 'A' && _bufOfAllPacket[i] <= 'z')
                System.out.println("_bufOfAllPacket[" + i + "] = " + (char)_bufOfAllPacket[i]);
            else
                System.out.println("_bufOfAllPacket[" + i + "] = " + (_bufOfAllPacket[i] & 0xff));
        }
        int numAuthSaw = 0;
        while (numAuthSaw < _numAuth) { // until I read all the authorities
            System.out.println("The num of authorities we saw already = " + numAuthSaw);
            Tuple2 addAndPos = getName(_bufOfAllPacket, curPos);
            curPos = (int)addAndPos.v; // here the curPos at the end of the name in the Authority section
            curPos++;
            int type = _bufOfAllPacket[curPos] | _bufOfAllPacket[++curPos] << 8; // translate the type
            if (type == 1) { // if the authority from type A
                curPos = curPos + 2; // we forward the curPos the class -> 2 byte
                curPos = curPos + 2; // we forward the curPos the TTL -> 2 byte
                curPos = curPos + 2; // we forward the curPos the RDATA length  -> 2 byte
                ans = Arrays.copyOfRange(_bufOfAllPacket, curPos, curPos+4);
//                curPos = curPos + 4;
//                return ans;
            }
            else { // if type is not 1, need to get the additional section
                curPos = curPos + 2; // we forward the curPos the class -> 2 byte
                curPos = curPos + 2; // we forward the curPos the TTL -> 2 byte
                curPos = curPos + 2; // we forward the curPos the RDATA length  -> 2 byte
                if (_bufOfAllPacket[curPos++] != '\0') {
//                    curPos = curPos+4;
                    addAndPos = getName(_bufOfAllPacket, curPos);
                    curPos = (int)addAndPos.v;
                    curPos++;
                }
            }
            numAuthSaw++;
        }
        if (_numAdd > 0) {
//            System.out.println("The current position just before reading the additional = " + curPos);
            Tuple2 addAndPos = getName(_bufOfAllPacket, curPos);
            curPos = (int)addAndPos.v;
            curPos = curPos + 4 + 4 + 2; // first 4 (type+class) second 4 (for valid respond 600sec) 2 for length
            ans = Arrays.copyOfRange(_bufOfAllPacket, curPos, curPos+4);
        }
        return ans;
    }

//    public short search(byte[] buf, int offset) {
//        return buf[]
//    }



    public static void main(String[] args) throws IOException {
        System.out.println("Im here\n");
        new SinkholeServer();
    }





//    DatagramSocket localServerSocket;
//    byte[] receivedData;
//    InetAddress localIP;
//    DatagramSocket clientSocket;
//    int headers;
//    char flags;
//    boolean isQuery;
//
//    public SinkholeServer() throws Exception{
//        localServerSocket = new DatagramSocket(5300);
//        receivedData = new byte[1024];
//
//    }
//
//    public void waitForMessage() throws Exception {
//        while(true){
//            DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
//            localServerSocket.receive(receivedPacket);
//            receivedData = receivedPacket.getData();
//            localIP = receivedPacket.getAddress();
//            // call to function that check the receive
//            checkingReceivedData(receivedData);
//
//        }
//    }
//
//    public void checkingReceivedData(byte[] receivedData) {
//        if((receivedData[3] & 128) == 0){
//            isQuery = true;
//        }
//
//    }


    // listen

    // get a request from client

    // checking if the request is valid

    // translating the headers
        // if it is a request



    // looking for the IP of the url


        // request to a root DNS
            // checking if the answer is IP or not
                // save the address to TLD
                    // do a request to TLD
                    // checking if the answer is IP or not
                        // save the address to authorative DNS
                            // do a request to authorative DNS
                            // Get and save the IP
    // send the IP to the client

    // listen


}
