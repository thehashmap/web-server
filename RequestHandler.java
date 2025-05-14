import java.io.*;
import java.net.Socket;
import java.util.Date;

public class RequestHandler {
    private final Socket clientSocket;
    
    public RequestHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }
    
    public void handle() throws IOException {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream())
        ) {
            String requestLine = in.readLine();
            if (requestLine == null) {
                return;
            }
            
            System.out.println("Received request: " + requestLine);
            
            String headerLine;
            while ((headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
                // Just reading & discarding the headers
            }
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            String serverType = determineServerType();
            String response = 
                """
                HTTP/1.1 200 OK\r
                Content-Type: text/html\r
                Date: """ + new Date() + "\r\n" +
                "Connection: close\r\n" +
                "\r\n" +
                "<!DOCTYPE html>\r\n" +
                "<html>\r\n" +
                "<head><title>Simple Java Web Server</title></head>\r\n" +
                "<body>\r\n" +
                "<h1>Hello from " + serverType + "</h1>\r\n" +
                "<p>Request processed by thread: " + Thread.currentThread().getName() + "</p>\r\n" +
                "<p>Current time: " + new Date() + "</p>\r\n" +
                "</body>\r\n" +
                "</html>";
            
            out.write(response);
            out.flush();
            
        } finally {
            clientSocket.close();
        }
    }
    
    private String determineServerType() {
        int port = clientSocket.getLocalPort();
        return switch (port) {
            case 8080 -> "Single-Threaded Server";
            default -> "Unknown Server Type";
        };
    }
}