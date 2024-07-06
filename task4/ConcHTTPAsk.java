import java.net.*;
import java.nio.charset.StandardCharsets;

import tcpclient.TCPClient;

import java.io.*;

public class ConcHTTPAsk {
    
    public static void main( String[] args) throws Exception {

        // Step 1: Create the server socket that waits for incoming connections (it never closes - runs in a forever loop)
        ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[0]));
        while(true) {
            //Step 2: create a new socket to communicate with HTTPAsk server
            System.out.println("Connection to server at port: " + Integer.parseInt(args[0]));
            Socket connectionSocket = serverSocket.accept();

            //Step 3: create the runnable object and a new thread for the new client and
            //pass the connection socket as parameter to the constructor
            MyRunnable runnable = new MyRunnable(connectionSocket);
            Thread ThreadObject = new Thread(runnable);
            ThreadObject.start();
        }        
    }
}   

 class MyRunnable implements Runnable{
    
    Socket connectionSocket;
    static int buffersize = 1024;

    //Step 4: initialize the connection socket using the constructor
    public MyRunnable(Socket connectionSocket) {
        this.connectionSocket = connectionSocket;
    }

    public void run() {
        try{
            //Step 5: read data sent by the client
            byte[] fromClient = new byte[buffersize];
            int fromClientLength = connectionSocket.getInputStream().read(fromClient);
    
            //Step 6: convert client read data to string and get ready for parsing
            String string_clientReq = new String(fromClient, "UTF-8");
    
            //Step 7: split the request by 'new row' - GET request on first position
            String [] clientReq = string_clientReq.split("\r\n");
            System.out.println("clientReq: " + clientReq[0]);
                
            //Step 8: Check the condition for HTTP 400 error
            if(!clientReq[0].startsWith("GET") || !clientReq[0].endsWith("HTTP/1.1")) {
                String httpResponse = "HTTP/1.1 400 Bad Request\r\n\r\n" + "Error 400: Bad Request";
                byte[] httpResponseBytes = httpResponse.getBytes("UTF-8");
                connectionSocket.getOutputStream().write(httpResponseBytes);
                connectionSocket.close();
            }

            //Step 9: Check first condition for HTTP 404 error before parsing arguments
            if(!clientReq[0].contains("/ask")){
                String httpResponse = "HTTP/1.1 404 Not Found\r\n\r\n";
                byte[] httpResponseBytes = httpResponse.getBytes("UTF-8");
                connectionSocket.getOutputStream().write(httpResponseBytes);
                connectionSocket.close();
            }
                
            //Step 10: Parse the arguments sesparated by special symbols
            int portnumber = 0;
            String hostname = null;
            boolean shutdown = false;
            Integer timeout = null;
            Integer limit = null;
            byte[] optionalData = null;

            String [] parameters = clientReq[0].split("[ ?&]");

            System.out.println(parameters[0]);

            //Step 11: Identify each parameter using further parsing
            for(int i = 0; i < parameters.length - 1; i++) {
                if(parameters[i].contains("hostname") == true) {
                    String [] host = parameters[i].split("=");
                    hostname = host[1];
                    System.out.println("h " + hostname);
                }

                if(parameters[i].contains("port") == true) {
                    String [] port = parameters[i].split("=");
                    portnumber = Integer.parseInt(port[1]);
                    System.out.println("p " + portnumber);
                }

                if(parameters[i].contains("shutdown") == true) {
                    String []shut = parameters[i].split("=");
                    shutdown = Boolean.parseBoolean(shut[1]);
                    System.out.println("s " + shutdown);
                }

                if(parameters[i].contains("limit") == true) {
                    String []lim = parameters[i].split("=");
                    limit = Integer.parseInt(lim[1]);
                    System.out.println("l " + limit);
                }

                if(parameters[i].contains("timeout") == true) {
                    String [] time= parameters[i].split("=");
                    timeout = Integer.parseInt(time[1]);
                    System.out.println("t " + timeout);
                }

                if(parameters[i].contains("string") == true) {
                    String [] time= parameters[i].split("=");
                    // time[1] = time[1] + '\n'; //add break line
                    optionalData = time[1].getBytes();
                    System.out.println("str " + time[1]);
                }
            }

            //Step 12: Call TCPClient with the identified above parameters - if exception throw 404 HTTP error
            try {
                TCPClient tcpClient = new TCPClient(shutdown, timeout, limit);
                byte [] tcpResponse = tcpClient.askServer(hostname, portnumber, optionalData);
                String httpresponse = new String(tcpResponse, StandardCharsets.UTF_8);
                String httpFinalResponse = "HTTP/1.1 200 OK\r\n\r\n" + httpresponse;
                connectionSocket.getOutputStream().write(httpFinalResponse.getBytes(StandardCharsets.UTF_8));
                connectionSocket.close();
                System.out.println("Call TCP");
            } catch (Exception e){
                String httpResponse = "HTTP/1.1 404 Not Found\r\n\r\n";
                byte[] httpResponseBytes = httpResponse.getBytes("UTF-8");
                connectionSocket.getOutputStream().write(httpResponseBytes);
                connectionSocket.close();
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}    
 
