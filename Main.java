import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("Simple Web Server Implementation");
        System.out.println("====================================");
        System.out.println("1. Single-Threaded Server");
        System.out.println("2. Multi-Threaded Server");
        System.out.println("3. Thread Pool Server");
        System.out.println("4. Compare All Servers");
        System.out.println("5. View Comparison Reports");
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
                    case 4 -> compareAllServers();
                    case 5 -> viewComparisonReport();
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
    
    private static void compareAllServers() {
        System.out.println("Starting all servers for comparison...");

        SingleThreadedServer singleServer = new SingleThreadedServer(8080);
        MultiThreadedServer multiServer = new MultiThreadedServer(8081);
        ThreadPoolServer poolServer = new ThreadPoolServer(8082, 10);

        Thread singleThread = new Thread(() -> singleServer.start());
        Thread multiThread = new Thread(() -> multiServer.start());
        Thread poolThread = new Thread(() -> poolServer.start());

        singleThread.start();
        multiThread.start();
        poolThread.start();

        System.out.println("All servers started:");
        System.out.println("- Single-Threaded: http://localhost:8080");
        System.out.println("- Multi-Threaded:  http://localhost:8081");
        System.out.println("- Thread Pool:     http://localhost:8082");
        System.out.println("\nUse a tool like Apache Benchmark (ab) to compare performance.");
        System.out.println("Example: ab -n 1000 -c 100 http://localhost:8080/");
        System.out.println("\nPress Enter to stop all servers.");
        try (Scanner scanner = new Scanner(System.in)) {
            scanner.nextLine();
        }

        singleServer.stop();
        multiServer.stop();
        poolServer.stop();

        System.out.println("All servers stopped.");
    }
    
    private static void viewComparisonReport() {
        System.out.println("\nDisplaying server performance comparison...");
        // Load historical data first
        ServerPerformanceLogger.loadHistoricalData();

        // Then display comparison
        ServerPerformanceLogger.displayComparison();
    }
}