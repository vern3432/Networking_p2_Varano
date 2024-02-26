package Sinkhole;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.ByteBuffer;


import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;
public class SinkholeServer {

    private final int port;
    private final String blockFilePath;
    private final String dnsAddress;
    private BlocklistChecker blockList;

    public SinkholeServer(int port, String blockFilePath,String dnsAddress) throws FileNotFoundException {
        this.dnsAddress=dnsAddress;
        this.port = port;
        this.blockFilePath = blockFilePath;
        JSONObject BlockListObject = JsonIO.readObject(new File(blockFilePath));
         JSONArray recordArray= BlockListObject.getArray("records");
         ArrayList<BlockObject> forBlockList=new ArrayList<BlockObject>();
         for(int i=0;i<recordArray.size();i++){
            // System.out.println("class"+recordArray.get(i).getClass());
            JSONObject temp=(JSONObject)recordArray.get(i);
            String host=temp.getString("host");
            String type=temp.getString("type");
            forBlockList.add(new BlockObject(type,host));
         }

        this.blockList=new BlocklistChecker(forBlockList); 
    
    }
    private String extractDomainFromQuery(DatagramPacket packet) {
        byte[] data = packet.getData();
        int length = packet.getLength();
    
        int index = 12;
        StringBuilder domainBuilder = new StringBuilder();
    
        while (index < length && data[index] != 0) {
            int labelLength = data[index];
            if ((labelLength & 0xc0) == 0xc0) {
                index += 2; // Pointers are 2 bytes long
            } else {
                // Read the label
                for (int i = 0; i < labelLength; i++) {
                    domainBuilder.append((char) data[index + 1 + i]);
                }
                domainBuilder.append('.');
                index += labelLength + 1;
            }
        }
    
        // Remove the trailing dot
        if (domainBuilder.length() > 0) {
            domainBuilder.deleteCharAt(domainBuilder.length() - 1);
        }
    
        return domainBuilder.toString();
    }
    




    public  DNSheader createDNSHeaderFromPacket(DatagramPacket packet) {
        byte[] data = packet.getData();

        short identifier = ByteBuffer.wrap(data, 0, 2).getShort();
        short flags = ByteBuffer.wrap(data, 2, 2).getShort();
        short questionCount = ByteBuffer.wrap(data, 4, 2).getShort();
        short answerCount = ByteBuffer.wrap(data, 6, 2).getShort();
        short authorityCount = ByteBuffer.wrap(data, 8, 2).getShort();
        short additionalCount = ByteBuffer.wrap(data, 10, 2).getShort();

        return new DNSheader.Builder()
                .setIdentifier(identifier)
                .setFlags(flags)
                .setQuestionCount(questionCount)
                .setAnswerCount(answerCount)
                .setAuthorityCount(authorityCount)
                .setAdditionalCount(additionalCount)
                .build();
    }
    
    public void startServer() {
        try {
            DatagramSocket socket = new DatagramSocket(port);
            byte[] buffer = new byte[1024];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                DNSMessageParser messageParser = new DNSMessageParser(packet.getData());
                String hostname = messageParser.extractHostname();
                DNSheader header = messageParser.extractHeader();
                System.out.println("hostname"+hostname);
                System.out.println("header"+header);
                System.out.println(messageParser.extractQueryType());
                DNSheader dnsHeader = createDNSHeaderFromPacket(packet);
                    String query = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.US_ASCII);
                String domain = extractDomainFromQuery(packet);
                String[] parts = query.split("\\s+"); // Split by whitespace
                if (blockList.isBlocked(domain)) {
                    // If domain is in blocklist, respond with the sinkhole address
                    byte[] response = ("Sinkholed: " + domain).getBytes();
                    DatagramPacket responsePacket = new DatagramPacket(response, response.length, packet.getAddress(), packet.getPort());
                    socket.send(responsePacket);
                    System.out.println("Blocked Domain: " + domain);
                } else {

                    System.out.println("Forwarding:"+domain);
                    forwardQueryToDNS(domain, packet, socket,dnsHeader);



                            }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void forwardQueryToDNS(String domain, DatagramPacket packet, DatagramSocket socket, DNSheader dnsHeader) {
        try {
            InetAddress dnsServerAddress = InetAddress.getByName(dnsAddress);
    
            byte[] queryMessage = constructDNSQuery(domain, dnsHeader);
    
            DatagramPacket sendPacket = new DatagramPacket(queryMessage, queryMessage.length, dnsServerAddress, 53);
    
            socket.send(sendPacket);
    
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);
    
            DatagramPacket responsePacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), packet.getAddress(), packet.getPort());
            socket.send(responsePacket);
            System.out.println("Forwarded and sent response from DNS server back to client.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    

private byte[] constructDNSQuery(String domain, DNSheader dnsHeader) {
    ByteBuffer headerBuffer = ByteBuffer.allocate(DNSheader.HEADER_LENGTH);
    headerBuffer.putShort(dnsHeader.getIdentifier());
    headerBuffer.putShort(dnsHeader.getFlags());
    headerBuffer.putShort(dnsHeader.getQuestionCount());
    headerBuffer.putShort(dnsHeader.getAnswerCount());
    headerBuffer.putShort(dnsHeader.getAuthorityCount());
    headerBuffer.putShort(dnsHeader.getAdditionalCount());
    byte[] headerBytes = headerBuffer.array();
    byte[] questionSection = constructQuestionSection(domain);
    byte[] dnsQuery = new byte[headerBytes.length + questionSection.length];
    System.arraycopy(headerBytes, 0, dnsQuery, 0, headerBytes.length);
    System.arraycopy(questionSection, 0, dnsQuery, headerBytes.length, questionSection.length);

    return dnsQuery;
}

    
    
    
private byte[] constructQuestionSection(String domain) {
    String queryType = domain.contains(":") ? "AAAA" : "A";

    String[] labels = domain.split("\\.");

    int totalLength = 0;
    for (String label : labels) {
        totalLength += label.length() + 1; // Include the length byte
    }
    totalLength++; // Add 1 byte for the termination byte (0x00)

    ByteBuffer buffer = ByteBuffer.allocate(totalLength + 4); // 4 bytes for type and class

    for (String label : labels) {
        buffer.put((byte) label.length()); // Length byte
        buffer.put(label.getBytes(StandardCharsets.US_ASCII)); // Label bytes
    }

    buffer.put((byte) 0x00);

    if (queryType.equalsIgnoreCase("A")) {
        System.out.println("A");

        buffer.putShort((short) 0x0001); // QTYPE for A record
    } else if (queryType.equalsIgnoreCase("AAAA")) {
        System.out.println("AAAA");
        buffer.putShort((short) 0x001c); // QTYPE for AAAA record
    } else {
        throw new IllegalArgumentException("Unsupported query type: " + queryType);
    }

    buffer.putShort((short) 0x0001); // QCLASS for IN class

    byte[] questionSection = buffer.array();

    return questionSection;
}

        
    

    private void processAndPrintDNSQuery(DatagramPacket packet) {
        byte[] data = packet.getData();
        int length = packet.getLength();
        String receivedData = new String(data, 0, length);
        System.out.println("Received DNS Query:");
        System.out.println(receivedData);
    }

    public static void main(String[] args) {
        try {
            JSONObject configJsonObject = JsonIO.readObject(new File("src/Sinkhole/config.json"));
            String dnsAddress = configJsonObject.get("dns-address").toString();
            String sinkholePort = configJsonObject.get("sinkhole-port").toString();
            String blockFilePath = "src/Sinkhole/" + configJsonObject.get("block-file").toString();
            double temp = Double.parseDouble(sinkholePort);
            int port = (int) temp;
            SinkholeServer server = new SinkholeServer(port, blockFilePath,dnsAddress);
            server.startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
