NAMES: Noy Mizrahi & Prague Oshri

USERSNAMES: Mizrahi.Noy & Prague.Oshri

IDS: Noy Mizrahi - 313296071 & Prague Oshri - 204868046

SinkholeServer :
This is the main class. It listens on port 5300 and waits for a query to be received.
Then it does the iterative search for a response and sends it to the client.

DNSInformation :
keep fields that are useful to translate the DNS packet information like headers and
HashMaps that save the relevant values in the Data section.

Headers :
This class divide the headers into parts and translate each part to its value.

ReceiveDNSPacket :
This class gets the Socket and a boolean which says if the packet is from a client or a server.
According to the boolean we save the IP of the client and save the packet as a byte array.

SendDNSPacket :
This class builds a packet according to the parameters that it gets, changes the headers values and sends it to the correct IP.

PairTuple :
A generic object that holds tuple of two.