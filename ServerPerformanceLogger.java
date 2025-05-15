import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerPerformanceLogger {
    // Define log file location
    private static final String LOG_DIR = "server_logs";
    private static final String LOG_FILE = LOG_DIR + "/performance_log.csv";
    private static final String COMPARISON_FILE = LOG_DIR + "/comparison_results.txt";
    
    // Cache for current test session results
    private static final Map<String, TestResult> currentSessionResults = new HashMap<>();
    
    // TestResult class to store performance metrics
    public static class TestResult {
        String serverType;
        int concurrentClients;
        int requestsPerClient;
        int delayMs;
        double totalSeconds;
        int successCount;
        int failureCount;
        double requestsPerSecond;
        int minResponseTime;
        int maxResponseTime;
        double avgResponseTime;
        
        public TestResult(String serverType, int concurrentClients, int requestsPerClient, 
                         int delayMs, double totalSeconds, int successCount, int failureCount,
                         double requestsPerSecond, int minResponseTime, int maxResponseTime, 
                         double avgResponseTime) {
            this.serverType = serverType;
            this.concurrentClients = concurrentClients;
            this.requestsPerClient = requestsPerClient;
            this.delayMs = delayMs;
            this.totalSeconds = totalSeconds;
            this.successCount = successCount;
            this.failureCount = failureCount;
            this.requestsPerSecond = requestsPerSecond;
            this.minResponseTime = minResponseTime;
            this.maxResponseTime = maxResponseTime;
            this.avgResponseTime = avgResponseTime;
        }
        
        public String toCsvString() {
            return String.format("%s,%d,%d,%d,%.2f,%d,%d,%.2f,%d,%d,%.2f",
                serverType, concurrentClients, requestsPerClient, delayMs,
                totalSeconds, successCount, failureCount, requestsPerSecond,
                minResponseTime, maxResponseTime, avgResponseTime);
        }
        
        @Override
        public String toString() {
            return String.format("""
                                 Server: %s, Clients: %d, Requests/client: %d, Delay: %dms
                                 Total time: %.2fs, Success: %d, Failed: %d
                                 Requests/sec: %.2f, Min RT: %dms, Max RT: %dms, Avg RT: %.2fms""",
                serverType, concurrentClients, requestsPerClient, delayMs,
                totalSeconds, successCount, failureCount, requestsPerSecond,
                minResponseTime, maxResponseTime, avgResponseTime);
        }
    }
    
    // Initialize the log file if it doesn't exist
    static {
        try {
            // Create directories if they don't exist
            File directory = new File(LOG_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            // Create log file with headers if it doesn't exist
            File logFile = new File(LOG_FILE);
            if (!logFile.exists()) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile))) {
                    writer.write("""
                                 Timestamp,ServerType,ConcurrentClients,RequestsPerClient,DelayMs,TotalTime,SuccessCount,FailureCount,RequestsPerSecond,MinResponseTime,MaxResponseTime,AvgResponseTime
                                 """);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to initialize log file: " + e.getMessage());
        }
    }
    
    // Log a test result
    public static void logTestResult(String serverType, int concurrentClients, int requestsPerClient, 
                                    int delayMs, double totalSeconds, int successCount, int failureCount,
                                    double requestsPerSecond, int minResponseTime, int maxResponseTime, 
                                    double avgResponseTime) {
        
        // Create a test result object
        TestResult result = new TestResult(
            serverType, concurrentClients, requestsPerClient, delayMs,
            totalSeconds, successCount, failureCount, requestsPerSecond,
            minResponseTime, maxResponseTime, avgResponseTime
        );
        
        // Store in current session map - use a key that includes server type
        String fullKey = getFullTestKey(serverType, concurrentClients, requestsPerClient, delayMs);
        currentSessionResults.put(fullKey, result);
        
        // Log to file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String timestamp = dateFormat.format(new Date());
            writer.write(timestamp + "," + result.toCsvString() + "\n");
            System.out.println("Test result logged: " + result.toCsvString());
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
        
        System.out.println("\nPerformance data logged to: " + LOG_FILE);
    }
    
    // Display a comparison of test results from the current session
    public static void displayComparison() {
        System.out.println("\n=== SERVER PERFORMANCE COMPARISON ===");
        
        // Group test results by test configuration (excluding server type)
        Map<String, List<TestResult>> groupedResults = new HashMap<>();
        
        for (Map.Entry<String, TestResult> entry : currentSessionResults.entrySet()) {
            TestResult result = entry.getValue();
            
            // Use a configuration key that does NOT include server type
            String configKey = getConfigKey(result.concurrentClients, result.requestsPerClient, result.delayMs);
            
            // Add this result to the appropriate group
            if (!groupedResults.containsKey(configKey)) {
                groupedResults.put(configKey, new ArrayList<>());
            }
            groupedResults.get(configKey).add(result);
        }
        
        // For configurations that have multiple server types, display comparison
        StringBuilder comparisonReport = new StringBuilder();
        comparisonReport.append("=== SERVER PERFORMANCE COMPARISON REPORT ===\n");
        comparisonReport.append("Generated: ").append(new Date()).append("\n\n");
        
        boolean hasComparisons = false;
        
        for (Map.Entry<String, List<TestResult>> entry : groupedResults.entrySet()) {
            List<TestResult> results = entry.getValue();
            if (results.size() > 1) {
                hasComparisons = true;
                System.out.println("\nConfiguration: " + formatConfigKeyForDisplay(entry.getKey()));
                comparisonReport.append("Configuration: ").append(formatConfigKeyForDisplay(entry.getKey())).append("\n");
                
                System.out.println(String.format("%-15s %-15s %-15s %-15s %-15s",
                    "Server Type", "Req/sec", "Avg Resp Time", "Min Resp Time", "Max Resp Time"));
                System.out.println("---------------------------------------------------------------------");
                
                comparisonReport.append(String.format("%-15s %-15s %-15s %-15s %-15s\n",
                    "Server Type", "Req/sec", "Avg Resp Time", "Min Resp Time", "Max Resp Time"));
                comparisonReport.append("---------------------------------------------------------------------\n");
                
                // Sort results by requests per second (descending)
                results.sort((a, b) -> Double.compare(b.requestsPerSecond, a.requestsPerSecond));
                
                for (TestResult result : results) {
                    System.out.println(String.format("%-15s %-15.2f %-15.2f %-15d %-15d",
                        result.serverType, result.requestsPerSecond, result.avgResponseTime,
                        result.minResponseTime, result.maxResponseTime));
                    
                    comparisonReport.append(String.format("%-15s %-15.2f %-15.2f %-15d %-15d\n",
                        result.serverType, result.requestsPerSecond, result.avgResponseTime,
                        result.minResponseTime, result.maxResponseTime));
                }
                
                // Calculate performance differences
                if (results.size() >= 2) {
                    TestResult fastest = results.get(0);
                    TestResult slowest = results.get(results.size() - 1);
                    double speedup = fastest.requestsPerSecond / slowest.requestsPerSecond;
                    
                    System.out.println(String.format("\nPerformance difference: %.2fx speedup (%s vs %s)",
                        speedup, fastest.serverType, slowest.serverType));
                    
                    comparisonReport.append(String.format("\nPerformance difference: %.2fx speedup (%s vs %s)\n",
                        speedup, fastest.serverType, slowest.serverType));
                }
                
                System.out.println();
                comparisonReport.append("\n");
            }
        }
        
        if (!hasComparisons) {
            System.out.println("No comparisons available yet. Run tests on multiple server types with the same configuration to see comparisons.");
            comparisonReport.append("No comparisons available yet. Run tests on multiple server types with the same configuration to see comparisons.\n");
        }
        
        // Save comparison report to file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(COMPARISON_FILE))) {
            writer.write(comparisonReport.toString());
            System.out.println("Comparison report saved to: " + COMPARISON_FILE);
        } catch (IOException e) {
            System.err.println("Failed to write comparison report: " + e.getMessage());
        }
    }
    
    // Helper method to generate a unique key for each test configuration including server type
    private static String getFullTestKey(String serverType, int concurrentClients, int requestsPerClient, int delayMs) {
        return String.format("%s_%d_%d_%d", serverType, concurrentClients, requestsPerClient, delayMs);
    }
    
    // Helper method to generate a config key WITHOUT server type (for grouping)
    private static String getConfigKey(int concurrentClients, int requestsPerClient, int delayMs) {
        return String.format("%d_%d_%d", concurrentClients, requestsPerClient, delayMs);
    }
    
    // Format config key for user-friendly display
    private static String formatConfigKeyForDisplay(String configKey) {
        String[] parts = configKey.split("_");
        return String.format("Clients: %s, Requests/client: %s, Delay: %sms", parts[0], parts[1], parts[2]);
    }
    
    // Utility method to read and display historical data
    public static void displayHistoricalData() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(LOG_FILE));
            System.out.println("\n=== HISTORICAL PERFORMANCE DATA ===");
            
            for (String line : lines) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.err.println("Failed to read log file: " + e.getMessage());
        }
    }
    
    // Method to load historical data from the CSV file
    public static void loadHistoricalData() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(LOG_FILE));
            
            // Skip header line
            if (lines.size() <= 1) {
                System.out.println("No historical data available");
                return;
            }
            
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                String[] parts = line.split(",");
                
                // Parse CSV format - skip timestamp at index 0
                if (parts.length >= 12) {
                    String serverType = parts[1];
                    int concurrentClients = Integer.parseInt(parts[2]);
                    int requestsPerClient = Integer.parseInt(parts[3]);
                    int delayMs = Integer.parseInt(parts[4]);
                    double totalTime = Double.parseDouble(parts[5]);
                    int successCount = Integer.parseInt(parts[6]);
                    int failureCount = Integer.parseInt(parts[7]);
                    double requestsPerSecond = Double.parseDouble(parts[8]);
                    int minResponseTime = Integer.parseInt(parts[9]);
                    int maxResponseTime = Integer.parseInt(parts[10]);
                    double avgResponseTime = Double.parseDouble(parts[11]);
                    
                    TestResult result = new TestResult(
                        serverType, concurrentClients, requestsPerClient, delayMs,
                        totalTime, successCount, failureCount, requestsPerSecond,
                        minResponseTime, maxResponseTime, avgResponseTime
                    );
                    
                    // Add to current session results
                    String fullKey = getFullTestKey(serverType, concurrentClients, requestsPerClient, delayMs);
                    currentSessionResults.put(fullKey, result);
                }
            }
            
            System.out.println("Historical data loaded: " + (currentSessionResults.size()) + " records");
            
        } catch (IOException e) {
            System.err.println("Failed to load historical data: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Error parsing data from log file: " + e.getMessage());
        }
    }
}