import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Client {
    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Usage: java Client <url> <concurrent_clients> <requests_per_client> <delay_ms>");
            System.out.println("Example: java Client http://localhost:8080/ 10 5 100");
            return;
        }
        
        String url = args[0];
        int concurrentClients = Integer.parseInt(args[1]);
        int requestsPerClient = Integer.parseInt(args[2]);
        int delayMs = Integer.parseInt(args[3]);
        
        // Determine server type from URL
        String serverType = "Unknown";
        if (url.contains("8080")) {
            serverType = "SingleThreaded";
        } else if (url.contains("8081")) {
            serverType = "MultiThreaded";
        } else if (url.contains("8082")) {
            serverType = "ThreadPool";
        }
        
        System.out.println("Testing URL: " + url);
        System.out.println("Server type: " + serverType);
        System.out.println("Concurrent clients: " + concurrentClients);
        System.out.println("Requests per client: " + requestsPerClient);
        System.out.println("Delay between requests: " + delayMs + "ms");
        
        ExecutorService executor = Executors.newFixedThreadPool(concurrentClients);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();
        
        // Track response times
        AtomicInteger responseTimeTotal = new AtomicInteger(0);
        AtomicInteger responseTimeCount = new AtomicInteger(0);
        AtomicInteger minResponseTime = new AtomicInteger(Integer.MAX_VALUE);
        AtomicInteger maxResponseTime = new AtomicInteger(0);
        
        for (int i = 0; i < concurrentClients; i++) {
            executor.execute(() -> {
                for (int j = 0; j < requestsPerClient; j++) {
                    try {
                        long requestStartTime = System.currentTimeMillis();
                        sendRequest(url);
                        long requestEndTime = System.currentTimeMillis();
                        int responseTime = (int)(requestEndTime - requestStartTime);
                        
                        // Update response time stats
                        responseTimeTotal.addAndGet(responseTime);
                        responseTimeCount.incrementAndGet();
                        
                        // Update min response time (using atomic operations)
                        int currentMin;
                        do {
                            currentMin = minResponseTime.get();
                            if (responseTime >= currentMin) break;
                        } while (!minResponseTime.compareAndSet(currentMin, responseTime));
                        
                        // Update max response time (using atomic operations)
                        int currentMax;
                        do {
                            currentMax = maxResponseTime.get();
                            if (responseTime <= currentMax) break;
                        } while (!maxResponseTime.compareAndSet(currentMax, responseTime));
                        
                        successCount.incrementAndGet();
                        
                        if (delayMs > 0) {
                            TimeUnit.MILLISECONDS.sleep(delayMs);
                        }
                    } catch (IOException | InterruptedException | URISyntaxException e) {
                        failureCount.incrementAndGet();
                        System.err.println("Request failed: " + e.getMessage());
                    }
                }
            });
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            System.err.println("Test interrupted: " + e.getMessage());
        }
        
        long endTime = System.currentTimeMillis();
        double totalSeconds = (endTime - startTime) / 1000.0;
        int totalRequests = successCount.get() + failureCount.get();
        double requestsPerSecond = totalRequests / totalSeconds;
        
        // Calculate average response time
        double avgResponseTime = responseTimeCount.get() > 0 ? 
            (double)responseTimeTotal.get() / responseTimeCount.get() : 0;
        
        // Display test results
        System.out.println("\nTest completed:");
        System.out.println("Total time: " + totalSeconds + " seconds");
        System.out.println("Successful requests: " + successCount.get());
        System.out.println("Failed requests: " + failureCount.get());
        System.out.println("Requests per second: " + requestsPerSecond);
        System.out.println("Min response time: " + (minResponseTime.get() == Integer.MAX_VALUE ? 0 : minResponseTime.get()) + " ms");
        System.out.println("Max response time: " + maxResponseTime.get() + " ms");
        System.out.println("Avg response time: " + String.format("%.2f", avgResponseTime) + " ms");
        
        // Log the test results
        ServerPerformanceLogger.logTestResult(
            serverType,
            concurrentClients,
            requestsPerClient,
            delayMs,
            totalSeconds,
            successCount.get(),
            failureCount.get(),
            requestsPerSecond,
            minResponseTime.get() == Integer.MAX_VALUE ? 0 : minResponseTime.get(),
            maxResponseTime.get(),
            avgResponseTime
        );
        
        // Load historical data first
        ServerPerformanceLogger.loadHistoricalData();

        // Then display comparison
        ServerPerformanceLogger.displayComparison();
    }
    
    private static void sendRequest(String urlStr) throws IOException, URISyntaxException {
        URL url = new URI(urlStr).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            StringBuilder response = new StringBuilder();
            
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            
            // Uncomment to print response
            System.out.println("Response: " + response.toString());
        }
    }
}