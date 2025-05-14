import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ThreadPoolServer extends AbstractServer {
    private final int poolSize;
    private ExecutorService threadPool;
    
    public ThreadPoolServer(int port, int poolSize) {
        super(port);
        this.poolSize = poolSize;
    }
    
    @Override
    public void start() {
        running.set(true);
        threadPool = Executors.newFixedThreadPool(poolSize);
        
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(1000); // Set timeout to allow checking running flag
            System.out.println("Thread Pool Server started on port " + port + " with pool size " + poolSize);
            
            while (running.get()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Thread Pool Server: New connection accepted");
                    
                    // Submit task to thread pool
                    threadPool.execute(() -> {
                        handleRequest(clientSocket);
                    });
                    
                } catch (SocketTimeoutException e) {
                    // Timeout occurred, just continue and check running flag
                }
            }
        } catch (IOException e) {
            if (running.get()) {
                System.err.println("Thread Pool Server error: " + e.getMessage());
            }
        } finally {
            stop();
        }
    }
    
    @Override
    public void stop() {
        super.stop();
        
        if (threadPool != null) {
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
            }
            System.out.println("Thread pool shut down");
        }
    }
}