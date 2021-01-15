import java.net.*;
import java.util.*;
import java.io.*;

public class SinkholeServer {

    DatagramSocket localServerSocket;
    DNSInformation queryDNSPacket;
    byte[] inValidAddress;
    String firstAuthority;
    final List<String> rootsDNS;

    private List<String> initializeRootsList() {
        List<String> listOfRoots = new ArrayList<>();
//        listOfRoots.add("82.80.196.156"); // <-----------------> an Israel Bezek DNS
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

    public SinkholeServer() throws IOException{
        rootsDNS = initializeRootsList();
        inValidAddress = new byte[4]; // create byte[] of 0.0.0.0
        startListen();
        queryDNSPacket = receivedQuery();
        iterativeSearch();
    }

    protected void startListen() throws IOException {
        localServerSocket = new DatagramSocket(5300);
        System.out.println("Start listening under port 5300");
    }

    protected DNSInformation receivedQuery() throws IOException {
        queryDNSPacket = new DNSInformation(localServerSocket, null, true);
        queryDNSPacket.receiver.receivedByteArray();
        return queryDNSPacket;
    }

    protected void iterativeSearch() throws IOException {
        boolean found = false;
        for (int i = 0; i < rootsDNS.size(); i++) {
            System.out.println("Trying RootDns " + i);
            if (checkDNS(InetAddress.getByName(rootsDNS.get(i)))) {
                found = true;
                break;
            }
        }
        if (!found) {
//            if(!checkDNS(InetAddress.getByName(firstAuthority))) {
//                System.out.println("---> Is Name Error <---");
//                new SendDNSPacket(localServerSocket, queryDNSPacket.receiver.received, queryDNSPacket.receiver.packetLength,
//                        queryDNSPacket.receiver.clientIP, true, queryDNSPacket.receiver.clientPort, true);
//            }
            checkDNS(InetAddress.getByName(firstAuthority));
        }
    }

    protected boolean checkDNS(InetAddress IP) throws IOException {
        DNSInformation dns = new DNSInformation(localServerSocket, IP, false);
        new SendDNSPacket(localServerSocket, queryDNSPacket.receiver.received,
                queryDNSPacket.receiver.packetLength, IP, false,53, false);
        byte[] received = dns.receiver.receivedByteArray();
        Headers headers = new Headers(received);
        if (headers == null) {
            return false;
        }
        dns.headers = headers;
        dns.position = 12;
        HandleDNSData worker = new HandleDNSData(dns);
        dns.position = worker.readQuestions(received, headers.numQuestions, dns.position);
        if (headers.numAnswers > 0) { // ---> need to save the packet byte[] with the answer to send to client
            Tuple2 ans = worker.readAnswers(received, headers.numQuestions, headers.numAnswers, dns.position);
            System.out.println("The client IP is = " + queryDNSPacket.receiver.clientIP.getHostAddress());
            new SendDNSPacket(localServerSocket, received, dns.receiver.packetLength,
                    queryDNSPacket.receiver.clientIP, true, queryDNSPacket.receiver.clientPort, false);
            System.out.println("Found ANSWER");
            return true;
        }
        return handleAuthorities(dns, received, headers.numAuthorities, headers.numAdditionals);
    }

    protected boolean handleAuthorities(DNSInformation dns, byte[] buff, int numAuths, int numAdds) throws IOException {
        readAuthorities(dns, buff, numAuths);
        readAdditionals(dns, buff, numAdds);
        pairAuthAdds(dns);
        Iterator i = dns.authoritiesNameIP.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry j = (Map.Entry) i.next();
            if (checkDNS(InetAddress.getByAddress((byte[])j.getValue()))) {
                return true;
            }
        }
        return false;
    }

    protected void readAuthorities(DNSInformation dns, byte[] buff, int numAuths) throws IOException {
        int pos = dns.position;
        HandleDNSData worker = new HandleDNSData(dns);
        for (int i = 0; i < numAuths; ++i) {
            pos = worker.readAuthority(buff, pos, true);
            if(i == 0) {
                System.out.println("dns.authoritiesNameIP.size() = " + dns.authoritiesNameName.size());
                for ( Object key : dns.authoritiesNameName.keySet() ) {
                    firstAuthority = (String)key;
                }
            }
        }
        dns.position = pos;
    }

    protected void readAdditionals(DNSInformation dns, byte[] buff, int numAdds) throws IOException {
        int pos = dns.position;
        HandleDNSData worker = new HandleDNSData(dns);
        for (int i = 0; i < numAdds; ++i) {
            pos = worker.readAdditional(buff, pos);
        }
        dns.position = pos;
    }

    protected void pairAuthAdds(DNSInformation dns) {
        Iterator i = dns.authoritiesNameName.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry j = (Map.Entry)i.next();
            try {
                byte[] ip = (byte[])dns.additionalNameIP.get(j.getValue());
                if (ip != null && !ip.equals(inValidAddress)) {
                    dns.authoritiesNameIP.put(j.getValue(), ip);
                }
            }
            catch (Exception e) {
                System.out.println("pairAuthAdds: exception " + j.getValue());
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new SinkholeServer();
    }

}
