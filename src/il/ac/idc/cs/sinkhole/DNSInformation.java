package il.ac.idc.cs.sinkhole;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;

public class DNSInformation {

    InetAddress IPAddress;
    public int position;

    public Headers headers;
    public ReceiveDNSPacket receiver;

    public String queryDomainName;

    public HashMap<String, byte[]> authoritiesNameIP;
    public HashMap<String, byte[]> additionalNameIP;
    public HashMap<String, String> authoritiesNameName;

    public DNSInformation(DatagramSocket serverSocket, InetAddress IP, boolean isClient) throws IOException {
        receiver = new ReceiveDNSPacket(serverSocket, isClient);
        position = 0;
        authoritiesNameIP = new HashMap<>();
        additionalNameIP = new HashMap<>();
        authoritiesNameName = new HashMap<>();
        IPAddress = IP;
    }
}
