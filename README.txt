NAMES: Noy Mizrahi & Prague Oshri

USERSNAMES: Mizrahi.Noy & Prague.Oshri

IDS: Noy Mizrahi - 313296071 & Prague Oshri - 204868046

SinkholeServer :
This is the main class. It listens on port 5300 and waits for a query to be received.
Then it does the iterative search for a response and sends it to the client.

DNSInformaition :
keep fields that are useful to translate the DNS packet information like headers and
HashMaps that save the relevant values in the Data section.

Header :
In this class we divided the headers into partitions and translated each part to its value.
Have functions that are manipulative on the flags bit as required.

ReceiveDNSPacket :
This class gets the Socket and a boolean which says if the packet is from a client or a server.
According to the boolean we save the IP of the client and save the packet as a byte array.

SendDNSPacket :
This class builds a packet according to the parameters that he gets, changes the headers values and sends this packet to the correct IP.

Tuple2 :
This class helps us to get from a single function two returns values (usually for String-name and int-offset) .
