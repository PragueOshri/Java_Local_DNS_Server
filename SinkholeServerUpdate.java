import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

public class SinkholeServerUpdate {

    byte[] queryPacket;
    byte[] rootResPacket;
    byte[] auth1ResPacket;
    byte[] auth2ResPacket;

    InetAddress clientIPAddress;
    InetAddress nextIPAddress;
    InetAddress answerIPAddress;

    final List<String> rootsDNS;

    HashSet authorities1Names;
    HashSet authorities2Names;
    HashSet additionalNames;

    Receiver queryReceiver;
    Receiver rootReceiver;
    Receiver auth1Receiver;
    Receiver auth2Receiver;
    Receiver auth3Receiver;

    DNSHeaders rootHeaders;
    DNSHeaders auth1Headers;
    DNSHeaders auth2Headers;
    DNSHeaders auth3Headers; // here we expect to see #answers > 0

    DNSData rootData;
    DNSData auth1Data;
    DNSData auth2Data;
    DNSData auth3Data; // here we expect to get the answer - IPAddress

    Sender sender;

    DatagramSocket localServerSocket;

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

    public SinkholeServerUpdate() throws IOException {
        rootsDNS = initializeRootsList();
        startDNS();
        getQueryRecv();
        iterateSearch();
    }

    public void startDNS() throws IOException {
        localServerSocket = new DatagramSocket(5300);
    }

    public void getQueryRecv() throws IOException {
        queryReceiver = new Receiver(localServerSocket);
        queryPacket = queryReceiver.recvClientByteArray();
        clientIPAddress = queryReceiver.clientIP;
    }

    public void iterateSearch() throws IOException {
        for (int i = 0; i < rootsDNS.size(); i++) {
            nextIPAddress = nextIPAddress.getByName(rootsDNS.get(i));
            sender = new Sender(localServerSocket, queryPacket, queryPacket.length, nextIPAddress);
            rootReceiver = new Receiver(localServerSocket);
            rootResPacket = rootReceiver.recvRespondByteArray();
            rootHeaders = new DNSHeaders(rootResPacket);
            if (rootHeaders.ancount > 0) { // #answer > 0
                // get answer
            }
            if (rootHeaders.nscount > 0) { // #authorities > 0
                // get authorities
            }
            if (rootHeaders.arcount > 0) { // #additional > 0
                // get additional
            }
            // check if there a match between name of auth to name of add
        }
    }

}
