package tcpclient;
import java.net.*;
import java.io.*;

public class TCPClient {

    //Step 1: declare global variables so that they can be used in the askServer method
    boolean shutdown;
    Integer timeout;
    Integer limit;
    
    //Step 2: assign the global variables the TCPClient method parameters by using 'this' keyword
    public TCPClient(boolean shutdown, Integer timeout, Integer limit) {

        this.shutdown = shutdown;
        this.timeout = timeout;
        this.limit = limit;
    }

    public byte[] askServer(String hostname, int port, byte [] toServerBytes) throws IOException {
    //Step 3: Open the connection and if necessary write data to server
        Socket clientSocket = new Socket (hostname, port);

        if(toServerBytes != null) {
            clientSocket.getOutputStream().write(toServerBytes);
            shutdown = true;
        }

    //Step 4: Close outgoing connection if condition is met        
        if(shutdown == true) {
            clientSocket.shutdownOutput(); //close outgoing stream
        }
        
        final int fixed_buffer = 12; //depending on the buffer size - for smaller strings I see or not the truncated data
        ByteArrayOutputStream dynamic_buffer = new ByteArrayOutputStream();
        byte [] datafromServer = new byte[fixed_buffer];
        int fromServer; 
        int totaldata = 0; //initilaize variable to check how much data will be received
        boolean read = true;

    //Step 5: If there is a timeout limit, set it    
        try{
        if(timeout != null) {
            clientSocket.setSoTimeout(timeout); 
        }
       
    //Step 6: As long as you can read from the file and one of the limit is not met - write data in buffer        
        while(read == true) { 
            fromServer = clientSocket.getInputStream().read(datafromServer);       
            if(fromServer == -1) 
                read = false;
         
        
            if(limit != null && fromServer != -1) {
                if(totaldata >= limit || fromServer >= limit) {
                    clientSocket.close();
                    read = false;
                    return dynamic_buffer.toByteArray();
                }

                dynamic_buffer.write(datafromServer, 0, fromServer); //write data in dynamic buffer
                totaldata = totaldata + fromServer; //sum up the received data
            }
            else if(fromServer != -1) {dynamic_buffer.write(datafromServer, 0, fromServer);}

        }
        
        //Step 8: if no time/limit condition is imposed, when done: just close connection and return all data 
        clientSocket.close();
        return dynamic_buffer.toByteArray(); }
        catch (Exception e){
            clientSocket.close();
            return dynamic_buffer.toByteArray(); }
        }
    }

