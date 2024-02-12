package Sinkhole;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Double;
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
  public static void main(String[] args) throws FileNotFoundException, IOException {
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

          @SuppressWarnings("deprecation")
          String dns_address = configJsonObject.get("dns-address").toString();
          String sinkhole_port = configJsonObject.get("sinkhole-port").toString();
          String block_file = configJsonObject.get("block-file").toString();

          


  }

}
