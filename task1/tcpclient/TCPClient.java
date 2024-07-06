package tcpclient;
import java.net.*;
import java.io.*;

public class TCPClient {
    
    public TCPClient() {
    }

    public byte[] askServer(String hostname, int port, byte [] toServerBytes) throws IOException {

        //Step 1: open the TCP connection
        Socket clientSocket = new Socket (hostname, port);

        //Step 2: Send data to server
        clientSocket.getOutputStream().write(toServerBytes);

        //Step3: Receive data from server and return it
        final int fixed_buffer = 1024;
        ByteArrayOutputStream dynamic_buffer = new ByteArrayOutputStream();
        byte [] datafromServer = new byte[fixed_buffer];
        int fromServer;

        while((fromServer = clientSocket.getInputStream().read(datafromServer)) != -1) {
            dynamic_buffer.write(datafromServer, 0, fromServer);
        }

        return dynamic_buffer.toByteArray();
    }
}
