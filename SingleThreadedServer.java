import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class SingleThreadedServer extends AbstractServer {
    
    public SingleThreadedServer(int port) {
        super(port);
    }
    
    @Override
    public void start() {
        running.set(true);
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(1000);
            System.out.println("Single-Threaded Server started on port " + port);
            
            while (running.get()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Single-Threaded Server: New connection accepted");
                    
                    handleRequest(clientSocket);
                    
                } catch (SocketTimeoutException e) {
                    // Timeout occurred, just continue and check running flag
                }
            }
        } catch (IOException e) {
            if (running.get()) {
                System.err.println("Single-Threaded Server error: " + e.getMessage());
            }
        } finally {
            stop();
        }
    }
}