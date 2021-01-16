import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class DNSInformation {

    InetAddress IPAddress;
    public int position;

    public Headers headers;
    public ReceiveDNSPacket receiver;

    public String queryDomainName;

    public HashMap authoritiesNameIP;
    public HashMap additionalNameIP;
    public HashMap authoritiesNameName;

    public DNSInformation(DatagramSocket serverSocket, InetAddress IP, boolean isClient) throws IOException {
        receiver = new ReceiveDNSPacket(serverSocket, isClient);
        position = 0;
        authoritiesNameIP = new HashMap<String, byte[]>();
        additionalNameIP = new HashMap<String, byte[]>();
        authoritiesNameName = new HashMap<String, String>();
        IPAddress = IP;
    }
}
