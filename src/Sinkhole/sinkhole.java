package Sinkhole;

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

public class sinkhole {

  ///need to use datagram socket
  ///for testing

  private static void processAndPrintDNSQuery(DatagramPacket packet) {
    // Get the data from the packet
    byte[] data = packet.getData();
    int length = packet.getLength();

    // convert data to a String for printing
    String receivedData = new String(data, 0, length);

    // print out the received DNS query
    System.out.println("Received DNS Query:");
    System.out.println(receivedData);
  }

  public static void main(String[] args)
    throws FileNotFoundException, IOException {
    String configFilePath = "src/Sinkhole/config.json";
    File configFile = new File(configFilePath);
    try (
      BufferedReader br = new BufferedReader(new FileReader(configFilePath))
    ) {
      String line;
      while ((line = br.readLine()) != null) {
        System.out.println(line);
      }
    }
    JSONObject configJsonObject = JsonIO.readObject(configFile);
    String dns_address = configJsonObject.get("dns-address").toString();
    String sinkhole_port = configJsonObject.get("sinkhole-port").toString();
    String block_file_path = configJsonObject.get("block-file").toString();
    String block_file_location = "src/Sinkhole/" + block_file_path;
    File blockfile = new File(block_file_location);
    try (
      BufferedReader br = new BufferedReader(
        new FileReader(block_file_location)
      )
    ) {
      String line;
      while ((line = br.readLine()) != null) {
        System.out.println(line);
      }
    }
    JSONObject blockJsonObject = JsonIO.readObject(blockfile);
    JSONArray blocked_JsonArray = (JSONArray) blockJsonObject.get("records");
    System.out.println(blocked_JsonArray);

    try {
      double temp = Double.parseDouble(sinkhole_port);
      int port = (int) temp;
      try (DatagramSocket socket = new DatagramSocket(port)) {
        byte[] buffer = new byte[1024];

        while (true) {
            // recive incoming DNS query packet
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);

            // proccess and print out the received DNS query
            processAndPrintDNSQuery(packet);
        }
      }
  } catch (Exception e) {
      e.printStackTrace();
  }
}
}
