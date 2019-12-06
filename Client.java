//File name: Client.java
//Author Name: Sukanya
//Description: This file contains Client code, which initiates the join, leave and message a room 
//requests

//Importing the required libraries

import java.net.Socket;
import java.io.IOException;
import java.io.PrintStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;

public class Client{
     
    //Initializing the variables
	static Socket sock = null;
    static OutputStream opstream = null;//accepts output bytes and sends them
    static PrintStream outToServer = null;//adds functionality to output stream, to print representations of various data values
    static InputStream ipstream = null;//accepts input bytes and receives them
    
    static BufferedReader inputFromClient = null;
    static DataInputStream inputFromServer = null;
	
    static String clientName = "client" + (int)(Math.random() * 100);//Random function to generate client names
    
        public static void main(String[] args) throws Exception{
        int portNumber = 8080;//Default Portnumber
        
        sock = new Socket("localhost", portNumber);

        opstream = sock.getOutputStream(); 
        outToServer = new PrintStream(opstream);
        outToServer.println("create " + clientName);
        outToServer.flush();
        outToServer.println();
 
        ipstream = sock.getInputStream();
        inputFromServer = new DataInputStream(ipstream);
        inputFromClient = new BufferedReader(new InputStreamReader(System.in));
		
		ReadAndSend read = new ReadAndSend();
        read.start();
        
        recieveMessageAndPrint print = new recieveMessageAndPrint();
        print.start();
        
        
    }
      //This function contains logic for client which reads the inputs from client and sends them to server
    static class ReadAndSend extends Thread{       
        @Override
        public void run(){
            String messageToServer;
            while(true){
                try{
                	messageToServer = inputFromClient.readLine();
                    outToServer.println(messageToServer);
                    if(messageToServer.equals("quit")) System.exit(0); //exit(0) is used to indicate successful termination                  
                    outToServer.flush();
                } catch(Exception e){
                    break;
                }
            }
        }
    }
    
  //This function contains logic for server which recieves the keyboard input from client 
    static class recieveMessageAndPrint extends Thread{
        @Override
        public void run() {
            String messageFromServer;
            while(true) {
                try{
                	messageFromServer = inputFromServer.readLine();
                    if(messageFromServer.startsWith("quit")) System.exit(0);
                    System.out.println(messageFromServer);
                } catch(Exception e) {
                    System.err.println("Server unexpectedly stopped... Exiting to handle server crash gracefully");
                    System.exit(0);
                }
            }
        }
    }
}