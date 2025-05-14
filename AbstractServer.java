import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractServer {
    protected final int port;
    protected ServerSocket serverSocket;
    protected final AtomicBoolean running = new AtomicBoolean(false);
    
    public AbstractServer(int port) {
        this.port = port;
    }
    
    public abstract void start();
    
    public void stop() {
        running.set(false);
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }
    }
    
    protected void handleRequest(Socket clientSocket) {
        try {
            // Request handler with common HTTP response logic
            RequestHandler handler = new RequestHandler(clientSocket);
            handler.handle();
        } catch (IOException e) {
            System.err.println("Error handling client request: " + e.getMessage());
        }
    }
}