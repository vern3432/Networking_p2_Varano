package Sinkhole;


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

public class PacketHandler implements Runnable {
    private DatagramPacket packet;
    private DatagramSocket socket;

    public PacketHandler(DatagramPacket packet, DatagramSocket socket) {
        this.packet = packet;
        this.socket = socket;
    }

    @Override
    public void run() {
        DNSMessageParser messageParser = new DNSMessageParser(packet.getData());
        String hostname = messageParser.extractHostname();
        DNSheader header = messageParser.extractHeader();
        DNSheader dnsHeader = createDNSHeaderFromPacket(packet);
        String query = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.US_ASCII);
        String domain = extractDomainFromQuery(packet);
        String[] parts = query.split("\\s+");
        if (blockList.isBlocked(domain, getQueryTypeFromPacket(packet))) {
            byte[] response = ("Sinkholed: " + domain).getBytes();
            DatagramPacket responsePacket = new DatagramPacket(response, response.length, packet.getAddress(),
                    packet.getPort());
            try {
                socket.send(responsePacket);
                System.out.println("Blocked Domain: " + domain);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            forwardQueryToDNS(domain, packet, socket, dnsHeader, getQueryTypeFromPacket(packet));
        }
    }
}
