/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ejericio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TCPServer {
    private String message;
    
    public int serverPort = 9999;
    private OnMessageReceived messageListener = null;
    private boolean running = false;
    int nrcli = 0;
    ServerThread[] sendclis = new ServerThread[10];
    ArrayList<ServerThread> clients = new ArrayList<>();

    PrintWriter mOut;
    BufferedReader in;
    
    ServerSocket serverSocket;

    //el constructor pide una interface OnMessageReceived
    public TCPServer(OnMessageReceived messageListener, int port) {
        this.serverPort = port;
        this.messageListener = messageListener;
    }
    public TCPServer(OnMessageReceived messageListener) {
        this.messageListener = messageListener;
    }
    
    public OnMessageReceived getMessageListener(){
        return this.messageListener;
    }
    
    public void sendMessageTCPServer(String message){
        for (int i = 0; i < clients.size(); i++) {
            if (clients.get(i) != null){
                clients.get(i).sendMessage(message);
            }
        }
    }
    

    
    public void run(){
        running = true;
        try{
            System.out.println("TCP Server"+"S : Connecting...");
            serverSocket = new ServerSocket(serverPort);
            
            while(running){
                Socket client = serverSocket.accept();
                nrcli = clients.size() + 1;
                ServerThread newClient = new ServerThread(client,this,nrcli);
                clients.add(newClient);
                newClient.start();
                
                System.out.println("Nuevo conectado:"+ clients.size()+"  conectados");
                
            }
            
        }catch( Exception e){
            System.out.println("Error"+e.getMessage());
        }finally{

        }
    }
    public  ServerThread[] getClients(){
        return sendclis;
    } 

    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
    
    public void stopServer(){
        for (int i = 0; i<clients.size();i++){
            clients.get(i).stopClient();
        }
        running = false;
        try {
            serverSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(TCPServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
