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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Scanner;
import java.io.*;

import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.JsonIO;
import merrimackutil.json.JSONSerializable;

import java.nio.file.Files;
import java.nio.file.Paths;

public class SinkholeServer {

    private int port;
    private String blockFilePath;
    private String dnsAddress;
    private BlocklistChecker blockList;
    public String config = "";

    public SinkholeServer() throws FileNotFoundException {
        System.out.println(config);

        if (config == "") {
            config = "src/Sinkhole/config.json";

        }

        System.out.println(config);

        JSONObject configJsonObject = JsonIO.readObject(new File(config));
        String dnsAddress = configJsonObject.get("dns-address").toString();
        String sinkholePort = configJsonObject.get("sinkhole-port").toString();
        String blockFilePath = "src/Sinkhole/" + configJsonObject.get("block-file").toString();
        double temp1 = Double.parseDouble(sinkholePort);
        int port = (int) temp1;

        this.dnsAddress = dnsAddress;
        this.port = port;
        this.blockFilePath = blockFilePath;
        System.out.println(port);

        JSONObject BlockListObject = JsonIO.readObject(new File(blockFilePath));
        JSONArray recordArray = BlockListObject.getArray("records");
        ArrayList<BlockObject> forBlockList = new ArrayList<BlockObject>();
        for (int i = 0; i < recordArray.size(); i++) {
            // System.out.println("class"+recordArray.get(i).getClass());
            JSONObject temp = (JSONObject) recordArray.get(i);
            String host = temp.getString("host");
            String type = temp.getString("type");
            forBlockList.add(new BlockObject(type.toLowerCase(), host.toLowerCase()));
        }

        this.blockList = new BlocklistChecker(forBlockList);
        System.out.println( this.blockList);

    }

    public void clearJsonAndWriteString(String filePath, String newContent) {
        // Read the JSON file as text
        String jsonContent = "";
        try {
            jsonContent = new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Clear the JSON file
        try {
            FileWriter fileWriter = new FileWriter(filePath, false);
            fileWriter.write("");
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Write a string to the JSON file
        try {
            FileWriter fileWriter = new FileWriter(filePath, false);
            fileWriter.write(newContent);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public BlockObject addBlockSite(String domain, String type) {
        BlockObject forReturn = new BlockObject(type, domain);
        this.blockList.addBlockSite(forReturn);
        return forReturn;

    }

    public void addBlockSitetoJson(BlockObject forJson) throws FileNotFoundException {

        JSONObject BlockListObject = JsonIO.readObject(new File(this.blockFilePath));
        JSONObject BlockListSample = JsonIO.readObject(new File("src/Sinkhole/sample.json"));
        JSONArray recordArray = BlockListObject.getArray("records");
        JSONArray BlockListSampleF = BlockListSample.getArray("records");

        ArrayList<BlockObject> forBlockList = new ArrayList<BlockObject>();
        JSONObject sample = (JSONObject) BlockListSampleF.get(0);
        System.out.println("Keyset" + sample.keySet());
        sample.put("host", forJson.getHost());
        sample.put("type", forJson.getType());
        recordArray.add(sample);
        File inputFile = new File(this.blockFilePath);
        String temp = recordArray.toJSON();
        BlockListSample.put("records", recordArray);
        clearJsonAndWriteString(blockFilePath, BlockListSample.toJSON());

    }


    public String getConfig(){
        return this.config;

    }

    public void addPorttoJson(int forJson) throws FileNotFoundException {
        System.out.println("pinter is problemaddPorttoJson");
        System.out.println( "Config path:"+getConfig());
        JSONObject BlockListObject = JsonIO.readObject(new File(getConfig()));
        BlockListObject.put("sinkhole-port", forJson);
        clearJsonAndWriteString(this.config, BlockListObject.toJSON());


    }

    public DNSheader createDNSHeaderFromPacket(DatagramPacket packet) {
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

    private void loadUI() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Enter your command:");
            String command = scanner.nextLine();
            switch (command.toLowerCase()) {
                case "--config":
                case "-c":
                    System.out.println("Config file: <config>");
                    // Handle loading configuration file
                    break;
                case "--help":
                case "-h":
                    System.out.println("Display the help.");
                    // Display help information
                    break;
                case "exit":
                    System.out.println("Exiting...");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid command. Please try again.");
            }
        }
    }

    public void startServer() throws FileNotFoundException {
        ExecutorService executor = Executors.newFixedThreadPool(10); // Arbitrary pool limit of 10 threads
        System.out.println(config);

        if (config == "") {
            config = "src/Sinkhole/config.json";

        }

        System.out.println(config);

        JSONObject configJsonObject = JsonIO.readObject(new File(config));
        String dnsAddress = configJsonObject.get("dns-address").toString();
        String sinkholePort = configJsonObject.get("sinkhole-port").toString();
        String blockFilePath = "src/Sinkhole/" + configJsonObject.get("block-file").toString();
        double temp1 = Double.parseDouble(sinkholePort);
        int port = (int) temp1;

        this.dnsAddress = dnsAddress;
        this.port = port;
        this.blockFilePath = blockFilePath;
        System.out.println(port);

        JSONObject BlockListObject = JsonIO.readObject(new File(blockFilePath));
        JSONArray recordArray = BlockListObject.getArray("records");
        ArrayList<BlockObject> forBlockList = new ArrayList<BlockObject>();
        for (int i = 0; i < recordArray.size(); i++) {
            // System.out.println("class"+recordArray.get(i).getClass());
            JSONObject temp = (JSONObject) recordArray.get(i);
            String host = temp.getString("host");
            String type = temp.getString("type");
            forBlockList.add(new BlockObject(type.toLowerCase(), host.toLowerCase()));
        }

        this.blockList = new BlocklistChecker(forBlockList);

        try {
            DatagramSocket socket = new DatagramSocket(port);
            byte[] buffer = new byte[1024];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                DNSMessageParser messageParser = new DNSMessageParser(packet.getData());
                String hostname = messageParser.extractHostname();
                DNSheader header = messageParser.extractHeader();
                // System.out.println("hostname"+hostname);
                // System.out.println("header"+header);
                System.out.println(messageParser.extractQueryType());
                DNSheader dnsHeader = createDNSHeaderFromPacket(packet);
                String query = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.US_ASCII);
                String domain = extractDomainFromQuery(packet);
                String[] parts = query.split("\\s+"); // Split by whitespace
                // loadUI();

                if (blockList.isBlocked(domain.toLowerCase(), getQueryTypeFromPacket(packet).toLowerCase())) {
                    // If domain is in blocklist, respond with the sinkhole address
                    // byte[] response = ("Sinkholed: " + domain).getBytes();
                    // DatagramPacket responsePacket = new DatagramPacket(response, response.length,
                    // packet.getAddress(), packet.getPort());
                    // socket.send(responsePacket);
                    // System.out.println("Blocked Domain: " + domain);
                    forwardBlockedDueToBlocklist(packet, socket, domain);
                } else {
                    System.out.println("Forwarding:" + domain);
                    System.out.println("True Type " + getQueryTypeFromPacket(packet));
                    forwardQueryToDNS(domain, packet, socket, dnsHeader, getQueryTypeFromPacket(packet));

                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void forwardBlockedDueToBlocklist(DatagramPacket packet, DatagramSocket socket, String domain) {
        try {
            // Construct a DNS response indicating that the domain is blocked due to the
            // blocklist
            DNSheader responseHeader = new DNSheader.Builder()
                    .setIdentifier(getIdentifierFromPacket(packet.getData()))
                    .setFlags((short) 0x8180) // Response with recursion desired and Response code: No error
                    .setQuestionCount((short) 1) // Single question in the query
                    .setAnswerCount((short) 0) // No answers in the response
                    .setAuthorityCount((short) 0)
                    .setAdditionalCount((short) 0)
                    .build();

            // Construct the blocked response
            byte[] blockedResponse = constructBlockedDNSResponse(packet.getData(), domain,
                    getQueryTypeFromPacket(packet));

            // Combine the header and send the response packet
            byte[] responseBytes = responseHeader.getBytes();
            DatagramPacket responsePacket = new DatagramPacket(blockedResponse, blockedResponse.length,
                    packet.getAddress(), packet.getPort());
            socket.send(responsePacket);
            System.out.println("Forwarded DNS response indicating domain is blocked due to blocklist.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] constructBlockedDNSResponse(byte[] requestData, String domain, String queryType) throws IOException {
        short identifier = ByteBuffer.wrap(requestData, 0, 2).getShort();
        DNSheader responseHeader = new DNSheader.Builder()
                .setIdentifier(identifier)
                .setFlags((short) 0x8180) // repsonse with recursion desired and Response code: No error
                .setQuestionCount((short) 1) // single question in the query
                .setAnswerCount((short) 2) // Two answers in the response (for IPv4 and IPv6)
                .setAuthorityCount((short) 0)
                .setAdditionalCount((short) 0)
                .build();

        byte[] domainBytes = domainToBytes(domain);
        byte[] questionSection = constructQuestionSection(domainBytes, queryType);
        byte[] answerSection = constructAnswerSection(domainBytes);
        byte[] responseBytes = new byte[DNSheader.HEADER_LENGTH + questionSection.length + answerSection.length];
        ByteBuffer responseBuffer = ByteBuffer.wrap(responseBytes);
        responseBuffer.put(responseHeader.getBytes());
        responseBuffer.put(questionSection);
        responseBuffer.put(answerSection);

        return responseBytes;
    }

    // method t=o construct the answer section with the dummy IP address of 0.0.0.0
    private byte[] constructAnswerSection(byte[] domainBytes) throws IOException {
        byte[] ipv4Address = { 0, 0, 0, 0 }; // IPv4 address: 0.0.0.0
        byte[] ipv6Address = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }; // IPv6 address: ::

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // consitrcut the first answer for IPv4 address
        outputStream.write(domainBytes);
        outputStream.write(new byte[] { 0, 1 }); // ttype
        outputStream.write(new byte[] { 0, 1 }); // Class: IN (Internet)
        outputStream.write(new byte[] { 0, 0, 0, 0 }); // tll of 0
        outputStream.write(new byte[] { 0, 4 }); // 4 bytes for ipv4
        outputStream.write(ipv4Address); // A address bytes

        // construct the second answer for ipv6 address,should come as :: for this null
        // return
        outputStream.write(domainBytes);
        outputStream.write(new byte[] { 0, 28 }); // type: aaaa
        outputStream.write(new byte[] { 0, 1 }); // class
        outputStream.write(new byte[] { 0, 0, 0, 0 }); // tll 0 seconds
        outputStream.write(new byte[] { 0, 16 }); // data length: 16 bytes for v 6
        outputStream.write(ipv6Address); // address bytes for ipv6

        return outputStream.toByteArray();
    }

    private byte[] domainToBytes(String domain) throws IOException {
        String[] labels = domain.split("\\."); // spltting domain into individual labels
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        for (String label : labels) {
            byte[] labelBytes = label.getBytes(StandardCharsets.US_ASCII);
            outputStream.write((byte) labelBytes.length); // write label length
            outputStream.write(labelBytes); // write label
        }

        outputStream.write((byte) 0); // zero byte for end of domain name
        return outputStream.toByteArray();
    }

    private short getIdentifierFromPacket(byte[] data) {
        // get the id from the frist two bytes of the packet
        return ByteBuffer.wrap(data, 0, 2).getShort();
    }

    private byte[] constructQuestionSection(byte[] domainBytes, String queryType) {
        byte[] questionSection = new byte[domainBytes.length + 4]; // 4 bytes for type and class

        // make copy of domain bytes
        System.arraycopy(domainBytes, 0, questionSection, 0, domainBytes.length);

        // conditions for qytype
        short qType;
        if (queryType.equalsIgnoreCase("A")) {
            qType = 0x0001;
        } else if (queryType.equalsIgnoreCase("AAAA")) {
            qType = 0x001c;
        } else {
            throw new IllegalArgumentException("Unsupported query type: " + queryType);
        }
        ByteBuffer.wrap(questionSection, domainBytes.length, 2).putShort(qType);
        short qClass = 0x0001;
        ByteBuffer.wrap(questionSection, domainBytes.length + 2, 2).putShort(qClass);

        return questionSection;
    }

    private void forwardQueryToDNS(String domain, DatagramPacket packet, DatagramSocket socket, DNSheader dnsHeader,
            String queryType) {
        try {
            InetAddress dnsServerAddress = InetAddress.getByName(dnsAddress);

            byte[] queryMessage = constructDNSQuery(domain, dnsHeader, queryType);

            DatagramPacket sendPacket = new DatagramPacket(queryMessage, queryMessage.length, dnsServerAddress, 53);

            socket.send(sendPacket);

            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);

            DatagramPacket responsePacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(),
                    packet.getAddress(), packet.getPort());
            socket.send(responsePacket);
            System.out.println("Forwarded and sent response from DNS server back to client.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getQueryTypeFromPacket(DatagramPacket packet) {
        byte[] data = packet.getData();
        int length = packet.getLength();
        int index = length - 4;

        short type = (short) ((data[index] << 8) | (data[index + 1] & 0xFF));

        if (type == 0x001C) {
            return "AAAA"; // IPv6 query type
        } else {
            return "A"; // IPv4 query type (assuming default)
        }
    }

    private byte[] constructDNSQuery(String domain, DNSheader dnsHeader, String queryType) {
        ByteBuffer headerBuffer = ByteBuffer.allocate(DNSheader.HEADER_LENGTH);
        headerBuffer.putShort(dnsHeader.getIdentifier());
        headerBuffer.putShort(dnsHeader.getFlags());
        headerBuffer.putShort(dnsHeader.getQuestionCount());
        headerBuffer.putShort(dnsHeader.getAnswerCount());
        headerBuffer.putShort(dnsHeader.getAuthorityCount());
        headerBuffer.putShort(dnsHeader.getAdditionalCount());
        byte[] headerBytes = headerBuffer.array();
        byte[] questionSection = constructQuestionSection(domain, queryType);
        byte[] dnsQuery = new byte[headerBytes.length + questionSection.length];
        System.arraycopy(headerBytes, 0, dnsQuery, 0, headerBytes.length);
        System.arraycopy(questionSection, 0, dnsQuery, headerBytes.length, questionSection.length);

        return dnsQuery;
    }

    private byte[] constructQuestionSection(String domain, String queryType) {
        String[] labels = domain.split("\\.");

        int totalLength = 0;
        for (String label : labels) {
            totalLength += label.length() + 1;
        }
        totalLength++;
        ByteBuffer buffer = ByteBuffer.allocate(totalLength + 4); // 4 bytes for type and class

        for (String label : labels) {
            buffer.put((byte) label.length()); // Length byte
            buffer.put(label.getBytes(StandardCharsets.US_ASCII)); // Label bytes
        }

        buffer.put((byte) 0x00);

        short qType;
        if (queryType.equalsIgnoreCase("A")) {
            qType = 0x0001; // QTYPE for A record
        } else if (queryType.equalsIgnoreCase("AAAA")) {
            qType = 0x001c; // QTYPE for AAAA record
        } else {
            throw new IllegalArgumentException("Unsupported query type: " + queryType);
        }
        buffer.putShort(qType); // Set QTYPE

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

    public void setconfig(String input) {

        this.config = input;

    }

    public static void main(String[] args) {

        try {
            // JSONObject configJsonObject = JsonIO.readObject(new File(config));
            // String dnsAddress = configJsonObject.get("dns-address").toString();
            // String sinkholePort = configJsonObject.get("sinkhole-port").toString();
            // String blockFilePath = "src/Sinkhole/" +
            // configJsonObject.get("block-file").toString();
            // double temp = Double.parseDouble(sinkholePort);
            // int port = (int) temp;
            // port, blockFilePath, dnsAddress
            SinkholeServer server = new SinkholeServer();
            SinkholeTextUi.generateUI(server);

            // server.startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
