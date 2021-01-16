import java.net.*;
import java.util.*;
import java.io.*;

public class SinkholeServer {

    DatagramSocket localServerSocket;
    DNSInformation queryDNSPacket;
    byte[] inValidAddress;
    String firstAuthority;
    final List<String> rootsDNS;
    int counter;

    Scanner fileDomainNames;
    Set<String> domainNameErrors = new HashSet<String>();
    File domainNameErrorsFile;

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

    public SinkholeServer() throws IOException{
        rootsDNS = initializeRootsList();

        domainNameErrorsFile = new File("src/blocklist.txt");
        fileDomainNames = new Scanner(domainNameErrorsFile);
        fillDomainNamesToBlock(fileDomainNames);

        inValidAddress = new byte[4]; // create byte[] of 0.0.0.0

        startListen();
        queryDNSPacket = receivedQuery();
        getQueryDomain();
        if(inFileDomainNamesErr(queryDNSPacket.queryDomainName)){
            new SendDNSPacket(localServerSocket, queryDNSPacket.receiver.received, queryDNSPacket.receiver.packetLength,
                    queryDNSPacket.receiver.clientIP, true, queryDNSPacket.receiver.clientPort, true);
        }
        else {
            iterativeSearch();
        }
    }

    private void fillDomainNamesToBlock(Scanner s) {
        while (s.hasNext()){
            String lineName = s.nextLine();
            domainNameErrors.add(lineName);
        }
    }

    protected void startListen() throws IOException {
        localServerSocket = new DatagramSocket(5300);
        System.out.println("Start listening under port 5300");
    }

    protected DNSInformation receivedQuery() throws IOException {
        queryDNSPacket = new DNSInformation(localServerSocket, null, true);
        queryDNSPacket.receiver.receivedByteArray();
        queryDNSPacket.headers = new Headers(queryDNSPacket.receiver.received);
        return queryDNSPacket;
    }

    protected void getQueryDomain() {
        HandleDNSData queryPacket = new HandleDNSData(queryDNSPacket);
        queryDNSPacket.queryDomainName = queryPacket.readName(queryDNSPacket.receiver.received, 12).first;
    }

    protected boolean inFileDomainNamesErr(String domain) {
        if (domainNameErrors.contains(domain)) {
            return true;
        }
        return false;
    }

    protected void iterativeSearch() throws IOException {
        boolean found = false;
        counter = 0;
        for (int i = 0; i < rootsDNS.size(); i++) {
            System.out.println("Trying RootDns " + i);
            if (checkDNS(InetAddress.getByName(rootsDNS.get(i)))) {
                found = true;
                break;
            }
        }
        if (!found) {
            counter = 0;
            if(!checkDNS(InetAddress.getByName(firstAuthority))) {
                new SendDNSPacket(localServerSocket, queryDNSPacket.receiver.received, queryDNSPacket.receiver.packetLength,
                        queryDNSPacket.receiver.clientIP, true, queryDNSPacket.receiver.clientPort, true);
            }
        }
    }

    protected boolean checkDNS(InetAddress IP) throws IOException {
        if (counter++ > 200) {
            return false;
        }
        DNSInformation dns = new DNSInformation(localServerSocket, IP, false);
        new SendDNSPacket(localServerSocket, queryDNSPacket.receiver.received,
                queryDNSPacket.receiver.packetLength, IP, false,53, false);
        byte[] received = dns.receiver.receivedByteArray();
        Headers headers = new Headers(received);
//        headers.printHeaders();
        if (headers == null) {
            return false;
        }
        dns.headers = headers;
        dns.position = 12;
        HandleDNSData worker = new HandleDNSData(dns);
        dns.position = worker.readQuestions(received, headers.numQuestions, dns.position);
        if (headers.numAnswers > 0) { // ---> need to save the packet byte[] with the answer to send to client
            Tuple2 ans = worker.readAnswers(received, headers.numQuestions, headers.numAnswers, dns.position);
            new SendDNSPacket(localServerSocket, received, dns.receiver.packetLength,
                    queryDNSPacket.receiver.clientIP, true, queryDNSPacket.receiver.clientPort, false);
//            System.out.println("Found ANSWER");
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
