
package ejericio;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ServerThread extends Thread{
    
    private Socket client;
    private TCPServer tcpserver;               
    private boolean running = false;
    private int clientID;  
    public PrintWriter mOut;
    public BufferedReader in;
    private TCPServer.OnMessageReceived messageListener = null;
    private String message;

    public ServerThread(Socket client_, TCPServer tcpserver_, int clientID_) {
        this.client = client_;
        this.tcpserver = tcpserver_;
        this.clientID = clientID_;
    }
    

    
    public void run() {
        running = true;
        try {
            try {               
                boolean soycontador = false;                
                mOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
                System.out.println("TCP Server"+ "C: Sent.");
                messageListener = tcpserver.getMessageListener();
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                while (running) {
                    message = in.readLine();

                    if (message != null && messageListener != null) {
                        if (message.equals("DISCONNECT")) {
                            // El cliente quiere desconectarse
                            System.out.println("Client " + clientID + " se ha desconectado.");
                            tcpserver.getClients()[clientID] = null;  // Marca este cliente como desconectado
                            stopClient();  // Detén el hilo del servidor
                            break;  // Sal del bucle
                        }
                        // Otro procesamiento de mensajes
                        messageListener.messageReceived(message);
                    }

                    message = null;
                }
                System.out.println("RESPONSE FROM CLIENT"+ "S: Received Message: '" + message + "'");
            } catch (Exception e) {
                System.out.println("TCP Server"+ "S: Error"+ e);
            } finally {
                client.close();
            }

        } catch (Exception e) {
            System.out.println("TCP Server"+ "C: Error"+ e);
        }
    }
    
    public void stopClient(){
        running = false;        
        try {
            client.close();
            in.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void sendMessage(String message){//funcion de trabajo
        if (mOut != null && !mOut.checkError()) {
            mOut.println( message);
            mOut.flush();
        }
    }
    
}
