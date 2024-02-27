package Sinkhole;

public class ServerLogic implements Runnable {
    private volatile boolean running = true;

    @Override
    public void run() {
        // Implement your server startup code here
        System.out.println("Server started...");
        // Your server's main loop or any other server logic
        while (running) {
            // Server logic goes here
        }
        System.out.println("Server stopped.");
    }

    public void stop() {
        running = false;
    }
}