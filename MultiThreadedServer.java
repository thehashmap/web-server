import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class MultiThreadedServer extends AbstractServer {
    
    public MultiThreadedServer(int port) {
        super(port);
    }
    
    @Override
    public void start() {
        running.set(true);
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(1000); // Set timeout to allow checking running flag
            System.out.println("Multi-Threaded Server started on port " + port);
            
            while (running.get()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Multi-Threaded Server: New connection accepted");
                    
                    // Create a new thread for each request
                    Thread clientThread = new Thread(() -> {
                        handleRequest(clientSocket);
                    });
                    clientThread.start();
                    
                } catch (SocketTimeoutException e) {
                    // Timeout occurred, just continue and check running flag
                }
            }
        } catch (IOException e) {
            if (running.get()) {
                System.err.println("Multi-Threaded Server error: " + e.getMessage());
            }
        } finally {
            stop();
        }
    }
}