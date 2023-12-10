package ejericio;


import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ServerImp {
   TCPServer streamingServer;
   Scanner sc;
   int streamingServerPort = 9999;
   boolean keepsending;
   static float stdNoise = 1;
   static float minRange = -100,maxRange =100;
   static double[] W = {2.0,3.0,-1.0,4.5};
   static double B = 10.0;
   static int sleepWait = 100;
   
   
   public ServerImp(int streamPort,int wait, float std){
       streamingServerPort = streamPort;
       keepsending = true;
       stdNoise = std;
       sleepWait = wait;
   }
   
   void iniciar(){
       new Thread(){
            @Override
            public void run() {
                streamingServer = new TCPServer(
                    new TCPServer.OnMessageReceived(){
                        @Override
                        public void messageReceived(String message){
                            ServidorRecibeStreaming(message);
                        }
                    }
                ,streamingServerPort);
                streamingServer.run();                   
            }
       }.start();
       
        new Thread(){
            @Override
            public void run(){
                while(keepsending){
                    try {
                        Thread.sleep(sleepWait);                        
                        String sends = genLinear();
                        ServidorEnvia(sends,streamingServer);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ServerImp.class.getName()).log(Level.SEVERE, null, ex);
                    }            
                }                
            }            
        }.start();
       
       
        String salir = "m";
        sc = new Scanner(System.in);
        System.out.println("Servidor bandera 01");
        while( !salir.equals("q")){
            salir = sc.nextLine();
        }
        keepsending = false;
        System.out.println("Ending");            
        streamingServer.stopServer();   
   }

    void ServidorRecibeStreaming(String llego){
       System.out.println("Mensaje Stream:" + llego);
       if (llego.equals("DISCONNECTED")) {
           int lastIndex = streamingServer.clients.size() - 1;
           streamingServer.clients.remove(lastIndex);
           System.out.println("DESCONECTADO");
       }
   }
   
   
   void ServidorEnvia(String envia, TCPServer sv){
        if (sv != null) {
            sv.sendMessageTCPServer(envia);
        }
   }
   
    private static String genLinear() {
        int m = W.length;
        Random rand = new Random();
        String s ="";
        double Y = 0;

        for (int i = 0; i < m; i++) {
            float X = rand.nextFloat()*(maxRange - minRange) + minRange;
            Y += W[i] * X;
            s+=X;
            if (i < m - 1) {
              s+=",";
            }
        }
        double noise = rand.nextGaussian() * stdNoise;
        Y += B + noise;
        s+="#";
        s+=Y;
        return s;
    }
   

   
}
