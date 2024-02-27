package Sinkhole;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class UIThread implements Runnable {
    private volatile boolean running = true;
    private ServerLogic serverLogic;

    public UIThread(ServerLogic serverLogic) {
        this.serverLogic = serverLogic;
    }

    @Override
    public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Text-based UI started. Type 'exit' to stop the server.");

        try {
            // Start a separate thread for handling user input
            Thread userInputThread = new Thread(() -> {
                try {
                    while (running) {
                        String input = reader.readLine();
                        if ("exit".equalsIgnoreCase(input.trim())) {
                            running = false;
                            serverLogic.stop(); // Stop the server logic
                            System.out.println("Server stopping...");
                            break; // Exit the loop
                        } else {
                            System.out.println("Unknown command. Type 'exit' to stop the server.");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            // Start the user input thread
            userInputThread.start();

            // Simulate another process running concurrently
            Thread otherProcessThread = new Thread(() -> {
                while (running) {
                    // Simulate another process running concurrently
                    System.out.println("Another process running concurrently...");
                    try {
                        Thread.sleep(2000); // Simulate a delay of 2 seconds
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });

            // Start the other process thread
            otherProcessThread.start();

            // Wait for both threads to finish
            userInputThread.join();
            otherProcessThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
