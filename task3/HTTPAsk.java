import java.net.*;
import java.nio.charset.StandardCharsets;

import tcpclient.TCPClient;

import java.io.*;

public class HTTPAsk {
    static int buffersize = 1024;
    public static void main( String[] args) throws Exception {

        // Step 1: Create server socket that waits for incoming connections
        ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[0]));
        int count = 0; //avoid favicon.ico request separately

        while(true) {
            if(count < 1) { 
                 //Step 2: create a new socket to communicate with HTTPAsk server
                Socket connectionSocket = serverSocket.accept();
                System.out.println("Connection to server at port: " + Integer.parseInt(args[0]));
                count ++;
    
                //Step 3: read data sent by the client
                byte[] fromClient = new byte[buffersize];
                int fromClientLength = connectionSocket.getInputStream().read(fromClient);
    
                //Step 4: convert client read data to string and get ready for parsing
                String string_clientReq = new String(fromClient, "UTF-8");
    
                //Step5: split the request by 'new row' - GET request on first position
                String [] clientReq = string_clientReq.split("\r\n");
                System.out.println("clientReq: " + clientReq[0]);
                
                //Step 6: Check the condition for HTTP 400 error
                if(!clientReq[0].startsWith("GET") || !clientReq[0].endsWith("HTTP/1.1")) {
                    String httpResponse = "HTTP/1.1 400 Bad Request\r\n\r\n" + "Error 400: Bad Request";
                    // Convert the concatenated string to bytes using UTF-8 encoding
                    byte[] httpResponseBytes = httpResponse.getBytes("UTF-8");
                    connectionSocket.getOutputStream().write(httpResponseBytes);
                    connectionSocket.close();
                }

                //Step 7: Check first condition for HTTP 404 error before parsing arguments
                if(!clientReq[0].contains("/ask")){
                    String httpResponse = "HTTP/1.1 404 Not Found\r\n\r\n";
                    // Convert the concatenated string to bytes using UTF-8 encoding
                    byte[] httpResponseBytes = httpResponse.getBytes("UTF-8");
                    connectionSocket.getOutputStream().write(httpResponseBytes);
                    connectionSocket.close();
                }
                
                //Step 8: Parse the arguments sesparated by special symbols
                int portnumber = 0;
                String hostname = null;
                boolean shutdown = false;
                Integer timeout = null;
                Integer limit = null;
                byte[] optionalData = null;

                String [] parameters = clientReq[0].split("[ ?&]");

                System.out.println(parameters[0]);

                //Step 9: Identify each parameter using further parsing
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

                //Step 10: Call TCPCliwnt with the identified above parameters - if exception throw 404 HTTP error
                try {
                    TCPClient tcpClient = new TCPClient(shutdown, timeout, limit);
                    byte [] tcpResponse = tcpClient.askServer(hostname, portnumber, optionalData);
                    String httpresponse = new String(tcpResponse, StandardCharsets.UTF_8);
                    String httpFinalResponse = "HTTP/1.1 200 OK\r\n\r\n" + httpresponse;
                    connectionSocket.getOutputStream().write(httpFinalResponse.getBytes(StandardCharsets.UTF_8));
                    connectionSocket.close();
                } catch (Exception e){
                    String httpResponse = "HTTP/1.1 404 Not Found\r\n\r\n";
                    // Convert the concatenated string to bytes using UTF-8 encoding
                    byte[] httpResponseBytes = httpResponse.getBytes("UTF-8");
                    if(count < 1) {
                        connectionSocket.getOutputStream().write(httpResponseBytes);
                        connectionSocket.close();
                    }
                }
    
            }
        }
    }
}