package il.ac.idc.cs.sinkhole;

import java.net.*;
import java.util.*;
import java.io.*;

public class SinkholeServer {

    static final int PORT = 5300;

    private DatagramSocket localServerSocket;
    private DNSInformation queryDNSPacket;
    private byte[] inValidAddress;
    private String firstAuthority;
    private final List<String> rootsDNS;
    private int counter;

    private static String pathFileDomainNameErr;
    private Set<String> domainNameErrors = new HashSet<>();
    private File domainNameErrorsFile;

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

    public SinkholeServer() {
        rootsDNS = initializeRootsList();

        inValidAddress = new byte[4]; // create byte[] of 0.0.0.0

        startListen();
        queryDNSPacket = receivedQuery();
        getQueryDomain();

        if (searchInInputFile()) {
            new SendDNSPacket(localServerSocket, queryDNSPacket.receiver.received, queryDNSPacket.receiver.packetLength,
                    queryDNSPacket.receiver.clientIP, true, queryDNSPacket.receiver.clientPort, true);
        } else {
            iterativeSearch();
        }
    }

    private boolean searchInInputFile() {
        if (hasPathOfFile()) {
            Scanner fileDomainNames = null;
            try {
                fileDomainNames = new Scanner(domainNameErrorsFile);
            } catch (FileNotFoundException e) {
                System.err.println("File doesn't exist");
            }
            fillDomainNamesToBlock(fileDomainNames);
            return inFileDomainNamesErr(queryDNSPacket.queryDomainName);
        } else {
            return false;
        }
    }

    private void fillDomainNamesToBlock(Scanner s) {
        while (s.hasNext()) {
            String lineName = s.nextLine();
            domainNameErrors.add(lineName);
        }
    }

    private void startListen() {
        try {
            localServerSocket = new DatagramSocket(PORT);
            System.out.println("Start listening under port " + PORT);
        } catch (SocketException e) {
            System.err.println("Failed to listen on port " + PORT);
        }

    }

    private DNSInformation receivedQuery() {
        queryDNSPacket = new DNSInformation(localServerSocket, null, true);
        queryDNSPacket.receiver.receivedByteArray();
        queryDNSPacket.headers = new Headers(queryDNSPacket.receiver.received);
        return queryDNSPacket;
    }

    private void getQueryDomain() {
        HandleDNSData queryPacket = new HandleDNSData(queryDNSPacket);
        queryDNSPacket.queryDomainName = queryPacket.readName(queryDNSPacket.receiver.received, 12).first;
    }

    private boolean hasPathOfFile() {
        if (pathFileDomainNameErr != null) {
            domainNameErrorsFile = new File(pathFileDomainNameErr);
            return true;
        }
        return false;
    }

    private boolean inFileDomainNamesErr(String domain) {
        return domainNameErrors.contains(domain);
    }

    private void iterativeSearch() {
        boolean found = false;
        counter = 0;
        try {
            for (String curRootDNS : rootsDNS) {
                if (checkDNS(InetAddress.getByName(curRootDNS))) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                counter = 0;
                if (!checkDNS(InetAddress.getByName(firstAuthority))) {
                    new SendDNSPacket(localServerSocket, queryDNSPacket.receiver.received, queryDNSPacket.receiver.packetLength,
                            queryDNSPacket.receiver.clientIP, true, queryDNSPacket.receiver.clientPort, true);
                }
            }
        } catch (UnknownHostException e) {
            System.err.println("Invalid host name to get the IP address from");
        }

    }

    private boolean checkDNS(InetAddress IP) {
        if (counter++ > 200) {
            return false;
        }
        DNSInformation dns = new DNSInformation(localServerSocket, IP, false);
        new SendDNSPacket(localServerSocket, queryDNSPacket.receiver.received,
                queryDNSPacket.receiver.packetLength, IP, false, 53, false);
        byte[] received = dns.receiver.receivedByteArray();
        Headers headers = new Headers(received);
        dns.headers = headers;
        dns.position = 12;
        HandleDNSData worker = new HandleDNSData(dns);
        dns.position = worker.readQuestions(received, headers.numQuestions, dns.position);
        if (headers.numAnswers > 0) { // ---> need to save the packet byte[] with the answer to send to client
            new SendDNSPacket(localServerSocket, received, dns.receiver.packetLength,
                    queryDNSPacket.receiver.clientIP, true, queryDNSPacket.receiver.clientPort, false);
            return true;
        }
        return handleAuthorities(dns, received, headers.numAuthorities, headers.numAdditionals);
    }

    private boolean handleAuthorities(DNSInformation dns, byte[] buff, int numAuths, int numAdds) {
        readAuthorities(dns, buff, numAuths);
        readAdditionals(dns, buff, numAdds);
        pairAuthAdds(dns);

        for (Map.Entry<String, byte[]> j : dns.authoritiesNameIP.entrySet()) {
            try {
                if (checkDNS(InetAddress.getByAddress(j.getValue()))) {
                    return true;
                }
            } catch (UnknownHostException e) {
                System.err.println("Invalid host name to get the IP address from");
            }
        }
        return false;
    }

    private void readAuthorities(DNSInformation dns, byte[] buff, int numAuths) {
        int pos = dns.position;
        HandleDNSData worker = new HandleDNSData(dns);
        for (int i = 0; i < numAuths; ++i) {
            pos = worker.readAuthority(buff, pos, true);
            if (i == 0) {
                for (Object key : dns.authoritiesNameName.keySet()) {
                    firstAuthority = (String) key;
                }
            }
        }
        dns.position = pos;
    }

    private void readAdditionals(DNSInformation dns, byte[] buff, int numAdds) {
        int pos = dns.position;
        HandleDNSData worker = new HandleDNSData(dns);
        for (int i = 0; i < numAdds; ++i) {
            pos = worker.readAdditional(buff, pos);
        }
        dns.position = pos;
    }

    private void pairAuthAdds(DNSInformation dns) {
        for (Map.Entry<String, String> j : dns.authoritiesNameName.entrySet()) {
            byte[] ip = dns.additionalNameIP.get(j.getValue());
            if (ip != null && !Arrays.equals(ip, inValidAddress)) {
                dns.authoritiesNameIP.put(j.getValue(), ip);
            }
        }
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            pathFileDomainNameErr = args[0];
        }
        new SinkholeServer();
    }

}
