//File name: Server.java
//Author Name: Sukanya
//Description: This file contains Server code, which can handle requests from client and displays all the activities the client does.

//Importing the required libraries

import java.util.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;

public class Server {
    
    public static HashMap<String, clientThread> clients = new HashMap<>();
    public static HashMap<String, Set<String>> roomClientMap = new HashMap<>();
    public static HashMap<String, Set<String>> clientRoomMap = new HashMap<>();
    
	public static void main(String[] args) throws Exception {
        
        int portNumber = 8080;        
        ServerSocket serversock = new ServerSocket(portNumber);
        System.out.println("Server is Running on port " + portNumber);
        
        serverclientReader s = new serverclientReader();
        s.start();
 
        String messageFromClient, messageToClient = "";
        while(true) {
            try{
                Socket sock = serversock.accept(); //Establishing connection between Client and server.
                DataInputStream inputstream = new DataInputStream(sock.getInputStream());
                PrintStream outputstream = new PrintStream(sock.getOutputStream());
                if((messageFromClient = inputstream.readLine()) != null){
                    if(messageFromClient.contains( "create")) {
                        String name = messageFromClient.split(" ")[1];
                        clients.put(name, new clientThread(name, sock));
                        clients.get(name).start();                        
                        messageToClient = "Welcome " + name;
                        outputstream.println(messageToClient);
                    }
                }
                outputstream.flush();
            } catch(Exception e){
                break;
            }
            
        }
        serversock.close();
    }
    
	//This function contains logic if the keyboard input is quit and closes down the connection
    public static class serverclientReader extends Thread{
        @Override
        public void run(){
            String inFromOperator;
            BufferedReader keyRead = new BufferedReader(new InputStreamReader(System.in));
            while(true){
                try{
                	inFromOperator = keyRead.readLine();
                    if(inFromOperator.startsWith("quit")) {
                    	closeDown();
                        System.exit(0);
                    }
                } catch(Exception e){
                    
                }
            }
        }
        
        //This function contains logic for closing down the connection with a message if the keyboard input is quit
        public void closeDown() {
            for(String key : clients.keySet()){
                clients.get(key).opstream.println("Server is down...Please try again later.");
                clients.get(key).opstream.println("quit");
                clients.get(key).opstream.close();
                try {
                    clients.get(key).ipstream.close();
                    clients.get(key).clientSock.close();
                } catch (Exception ex) {
                    
                }
            }
        }
    }

  //This function contains logic for client after connection 
	public static class clientThread extends Thread{
	    String clientName;
	    Socket clientSock = null;
	    PrintStream opstream = null;
	    DataInputStream ipstream = null;
	    
	    public clientThread(String name, Socket clientSocket) {
	        this.clientSock = clientSocket;
	        this.clientName = name;
	    }
	
	    @Override
	    public void run() {
	        try {
	            ipstream = new DataInputStream(clientSock.getInputStream());
	            opstream = new PrintStream(clientSock.getOutputStream());
	            System.out.println(clientName + " connected");
	            String name = ipstream.readLine().trim();
	            synchronized(this){
	                while (true) {
	                    String line = ipstream.readLine();
						String[] lineArray = line.split(" ");
	                    if(lineArray.length > 0){
		                    String operation = lineArray[0].toLowerCase();
		                    //Regular IRC operations
		                    switch (operation) {
		                        case "messageroom":
		                            groupMessage(line);
		                            break;
		                        case "private":
		                            privateMessage(line);
		                            break;
		                        case "createroom":
		                        	createRoom(line);
		                        	break;
		                        case "joinroom":
		                        	joinRoom(line);
		                        	break;
		                        case "listrooms":
		                        	listRooms();
		                        	break;
		                        case "listmembers":
		                        	listMembers(line);
		                        	break;
		                        case "leaveroom":
		                        	leaveRoom(line);
		                        	break;
		                        default:
		                        	if (line.startsWith("quit")) {
				                    	cliendThreadCloseDown();
				                    	Thread.currentThread().stop();
				                    }
		                        	invalidCommand();
		                            break;
		                    }
		                    
		                    if (line.startsWith("quit")) {
		                    	cliendThreadCloseDown();
		                    	Thread.currentThread().stop();
		                    }
	                    }
	                }
	            }
	        } catch (Exception e) {
	        	cliendThreadCloseDown();
	        }
	    }
	    
	    // //This function contains logic for any invalid command 
	    public void invalidCommand() {
	    	opstream.println("Invalid command..Please give correct command");
	    }
	    
	    //This function contains logic for cliendThread close Down
	    public void cliendThreadCloseDown() {
	    	try {
	            opstream.println("See you later! " + clientName);
		    	System.out.println(clientName + " disconnected!");
	            clientRoomMap.remove(clientName);
	            for(String room: roomClientMap.keySet()) {
	          	  if(roomClientMap.get(room).contains(clientName)) {
	          		  roomClientMap.get(room).remove(clientName);
	          		  for(String key: roomClientMap.get(room))
	        				clients.get(key).opstream.println(clientName + "just left the room " + room);
	          	  }
	            }
	            clients.remove(clientName);
	            opstream.close();
	            ipstream.close();
	            clientSock.close();
	        } catch (Exception ex) {
                
            }
	    }
	    
	    // This function contains logic for Client which can create a room
	    // Command: createroom <ROOMNAME/>
	  
		public void createRoom(String s){
	    	String room = s.split(" ")[1];
	    	
	    	if(!roomClientMap.containsKey(room)) {
	    		roomClientMap.put(room, new HashSet<String>());
		    	opstream.println("Room " + room + " created");
		        System.out.println("Room " + room + " created by " +clientName);
	    	}else opstream.println("Room " + room + " already exits");
	    }
	    
	    // This function contains logic for Client which can join in single or multiple rooms
	    // Command: joinroom <ROOMNAME/>
	    public void joinRoom(String s){
	    	String room = s.split(" ")[1];
	    	        
	    	if(!roomClientMap.containsKey(room)) {
	    		opstream.println("This Room " + room + " doesn't exist...");
	    		return;
	    	}else if(!clientRoomMap.containsKey(clientName)) {
	    		clientRoomMap.put(clientName, new HashSet<String>());
	    	}else if(roomClientMap.get(room).contains(clientName) || clientRoomMap.get(clientName).contains(room)) {
	    		opstream.println("You are already there in this room: " + room);
	    		return;
	    	}	    	
	    	clientRoomMap.get(clientName).add(room);
    		roomClientMap.get(room).add(clientName);
    		opstream.println("Joined to Room: " + room); 
    		for(String key: roomClientMap.get(room)) {
    			if(!key.equals(this.clientName)){
    				clients.get(key).opstream.println(clientName + " joined the room " + room);
	            }
			}
    		System.out.println(clientName + " joined room " + room);
	    }
	    
	    // This function contains logic for Client which can leave any room
	    // Command: leaveroom <ROOMNAME/>
	    public void leaveRoom(String s) {
	    	String room = s.split(" ")[1];
	    	if(!roomClientMap.containsKey(room)) {
	    		opstream.println("Room " + room + " doesn't exist..");
	    		return;
	    	}

	    	if(clientRoomMap.containsKey(clientName) && clientRoomMap.get(clientName).contains(room)) {
	    		clientRoomMap.get(clientName).remove(room);
    			roomClientMap.get(room).remove(clientName);
    			opstream.println("You left room " + room);
    			for(String key: roomClientMap.get(room)) {
    				clients.get(key).opstream.println(clientName + " left the room " + room);
    			}
    			System.out.println(clientName + " left the room " + room);
    			return;
	    	}
	    	opstream.println("You are not member of room " + room);
	    }
	    
	    // This function contains logic for Client which can list members of any room
	    // Command: listmembers <ROOMNAME/>
	    public void listMembers(String s) {
	    	String room = s.split(" ")[1];
	    	if(!roomClientMap.containsKey(room)) {
	    		opstream.println("Room " + room + " doesn't exist!");
	    		return;
	    	}
	    	for(String client: roomClientMap.get(room)) opstream.println(client);
	    }
	    
	    // This function contains logic for Client which can list rooms
	    //command: listrooms
	    public void listRooms() {
	    	for(String s: roomClientMap.keySet()) opstream.println(s);
	    } 
	    
	    //This function contains logic for Client which can send message to room. 
	    // Command: messageroom <ROOMNAME/> <MESSAGE/>
	    public void groupMessage(String s){
	        String room = s.split(" ")[1];
	        String message = this.clientName + ":" + s.substring(s.indexOf(room));
	        if(!roomClientMap.get(room).contains(this.clientName)) {
	        	opstream.println("You are not member of this room " + room);
	        	return;
	        }
	        roomClientMap.get(room).forEach((key) -> {
	            if(key.equals(this.clientName)){
	                clients.get(key).opstream.println("Message Sent");
	            }
	            clients.get(key).opstream.println(message);
	        });
	        System.out.println(this.clientName + " broadcasted the message");
	    }
	    
	    //This function contains logic for Client which can send private message to any client
	    // Command: private <CLIENTNAME/> <MESSAGE/>
	    public void privateMessage(String s){
	        String user = s.split(" ")[1];
	        String message = this.clientName + ":" + s.substring(s.indexOf(user) + user.length());
	        
	        if(clients.containsKey(user)) clients.get(user).opstream.println(message);
	        else {
	        	opstream.println("Client " + user + " doesn't exist "); 
	        	return;
	        }
	        clients.get(clientName).opstream.println("Message Sent");
	        System.out.println(clientName + " sent private message to " + user);
	    }
	}
}