package Sinkhole;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Double;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.print.DocFlavor.STRING;
import merrimackutil.json.*;
import merrimackutil.json.JSONSerializable;
import merrimackutil.json.JsonIO;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.*;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import merrimackutil.json.JsonIO;
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
        System.out.println(BlockListObject.toString());
        this.blockList=new BlocklistChecker(null); 
    
    }

    public void startServer() {
        try {
            DatagramSocket socket = new DatagramSocket(port);
            byte[] buffer = new byte[1024];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                processAndPrintDNSQuery(packet);
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
