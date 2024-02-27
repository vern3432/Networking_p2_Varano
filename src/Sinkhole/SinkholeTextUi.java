package Sinkhole;

import java.util.Scanner;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.net.MalformedURLException;
import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SinkholeTextUi {

    private static final Scanner scanner = new Scanner(System.in);

    public static void generateUI(SinkholeServer server) throws FileNotFoundException {
        String option;

        do {
            System.out.println("Usage:");
            System.out.println("sinkhole --config <config>\t\tConfigure the Sinkhole settings.");
            System.out.println("sinkhole --help\t\t\tDisplay the help.");
            System.out.println("sinkhole --start\t\t\tStart the sinkhole.");
            System.out.println("Options:");
            System.out.println("-c, --config\tConfig file to use.");
            System.out.println("-h, --help\tDisplay the help.");
            System.out.println("--start\t\tStart the sinkhole.");
            System.out.println("Enter your option:");

            option = scanner.nextLine();

            switch (option) {
                case "--help":
                    displayHelp();
                    break;
                case "--start":
                    System.out.println("Starting sinkhole...");
                    server.startServer();
                    break;
                case "--config":
                    configure(server);
                    break;
                default:
                    System.out.println("Invalid option. Use --help for usage information.");
                    System.out.println("Invalid input. Please try again.");
            }
        } while (!option.equals("--start"));
    }

    public static void displayHelp() {
        System.out.println("Sinkhole Help:");
        System.out.println("--config, -c <config>\t\tConfigure the Sinkhole settings.");
        System.out.println("--help, -h\t\t\tDisplay this help message.");
        System.out.println("--start\t\t\t\tStart the sinkhole.");
    }

    public static void configure(SinkholeServer server) throws FileNotFoundException {
        String option;

        do {
            System.out.println("Choose an option:");
            System.out.println("1. Set Config File\t\tSpecify the configuration file.");
            System.out.println("2. Choose Port\t\t\tSet the port number for the sinkhole.");
            System.out.println("3. Change DNS Address\t\tChange the DNS address for the sinkhole.");
            System.out.println("4. Add Block Site\t\tAdd a website to block.");
            System.out.println("5. Back to main menu");

            int choice = readIntInput();

            switch (choice) {
                case 1:
                    setConfigFile(server);
                    break;
                case 2:
                    choosePort(server);
                    break;
                case 3:
                    changeDNSAddress(server);
                    break;
                case 4:
                    addBlockSite(server);
                    break;
                case 5:
                    return; // Return to main menu
                default:
                    System.out.println("Invalid option.");
            }

            System.out.println("Do you want to perform another action? (yes/no)");
            option = scanner.nextLine();
        } while (option.equalsIgnoreCase("yes"));
    }

    public static void setConfigFile(SinkholeServer server) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Set Config File");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON files", "json");
        fileChooser.setFileFilter(filter);

        int returnValue = fileChooser.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            server.setconfig(selectedFile.getPath());

            System.out.println("Selected file: " + selectedFile.getAbsolutePath());
            // Add logic to handle the selected file
        }
    }

    public static void choosePort(SinkholeServer server) throws FileNotFoundException {
        System.out.println("Enter the port number:");

        int port = readIntInput();

        if (port >= 0 && port <= 65535) {
            System.out.println("Port " + port + " is valid.");
            server.addPorttoJson(port);

        } else {
            System.out.println("Invalid port number. Port number must be between 0 and 65535.");
            choosePort(server);
        }
    }

    public static void changeDNSAddress(SinkholeServer server) {
        System.out.println("Enter the new DNS address:");

        String dnsAddress = scanner.nextLine();
        try {
            InetAddress inetAddress = InetAddress.getByName(dnsAddress);
            System.out.println("DNS Address " + inetAddress.getHostAddress() + " is valid.");
            // Add logic to handle the DNS address
        } catch (UnknownHostException e) {
            System.out.println("Invalid DNS address.");
            changeDNSAddress(server);
        }
    }

    public static void addBlockSite(SinkholeServer server) throws FileNotFoundException {
        boolean validUrl = false;
        do {
            System.out.println("Enter the website URL:");
            String websiteUrl = scanner.nextLine();

            if (isValidUrl(websiteUrl)) {
                System.out.println("Website URL " + websiteUrl + " is valid.");
                System.out.println("Enter the type (a or aaaa):");
                String type = scanner.nextLine();
                if ("a".equalsIgnoreCase(type) || "aaaa".equalsIgnoreCase(type)) {
                    System.out.println("Type " + type + " is valid.");
                    validUrl = true;
                    BlockObject forJson = server.addBlockSite(websiteUrl, type);
                    server.addBlockSitetoJson(forJson);

                } else {
                    System.out.println("Invalid type. Type must be 'a' or 'aaaa'.");
                }
            } else {
                System.out.println("Invalid website URL. Please try again.");
                scanner.nextLine();
            }
        } while (!validUrl);
    }

    public static boolean isValidUrl(String url) {
        // Regular expression for validating domain names
        String regex = "^(?:[a-zA-Z0-9](?:[a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        return matcher.matches();
    }

    public static int readIntInput() {
        while (!scanner.hasNextInt()) {
            System.out.println("Invalid input. Please enter a valid integer:");
            scanner.next(); // Consume invalid input
        }
        return scanner.nextInt();
    }

    public static void main(String[] args) throws FileNotFoundException {
        SinkholeServer server = null;
        generateUI(server);
    }
}
