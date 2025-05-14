import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("Simple Web Server Implementation");
        System.out.println("====================================");
        System.out.println("1. Single-Threaded Server");
        System.out.println("2. Multi-Threaded Server");
        System.out.println("3. Thread Pool Server");
        System.out.println("0. Exit");
        
        try (Scanner scanner = new Scanner(System.in)) {
            int choice;
            
            do {
                System.out.print("\nEnter your choice: ");
                choice = scanner.nextInt();
                
                switch (choice) {
                    case 1 -> runSingleThreadedServer();
                    case 2 -> runMultiThreadedServer();
                    case 3 -> runThreadPoolServer();
                    case 0 -> System.out.println("Exiting...");
                    default -> System.out.println("Invalid choice!");
                }
            } while (choice != 0);
        }
    }
    
    private static void runSingleThreadedServer() {
        System.out.println("Starting Single-Threaded Server on port 8080...");
        SingleThreadedServer server = new SingleThreadedServer(8080);
        Thread serverThread = new Thread(() -> {
            server.start();
        });
        serverThread.start();
        
        System.out.println("Server started. Press Enter to stop.");
        try (Scanner scanner = new Scanner(System.in)) {
            scanner.nextLine();
        }
        
        server.stop();
        System.out.println("Single-Threaded Server stopped.");
    }
    
    private static void runMultiThreadedServer() {
        System.out.println("Starting Multi-Threaded Server on port 8081...");
        MultiThreadedServer server = new MultiThreadedServer(8081);
        Thread serverThread = new Thread(() -> {
            server.start();
        });
        serverThread.start();
        
        System.out.println("Server started. Press Enter to stop.");
        try (Scanner scanner = new Scanner(System.in)) {
            scanner.nextLine();
        }
        
        server.stop();
        System.out.println("Multi-Threaded Server stopped.");
    }
    
    private static void runThreadPoolServer() {
        System.out.println("Starting Thread Pool Server on port 8082...");
        ThreadPoolServer server = new ThreadPoolServer(8082, 10); // Pool size of 10
        Thread serverThread = new Thread(() -> {
            server.start();
        });
        serverThread.start();
        
        System.out.println("Server started. Press Enter to stop.");
        try (Scanner scanner = new Scanner(System.in)) {
            scanner.nextLine();
        }
        
        server.stop();
        System.out.println("Thread Pool Server stopped.");
    }
    

    
    
}