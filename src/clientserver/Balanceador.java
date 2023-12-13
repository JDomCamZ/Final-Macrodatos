package clientserver;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

public class Balanceador {
    TCPServer50 mTcpServer;
    Scanner sc;
    ArrayList<Integer> client = new ArrayList<Integer>();
    ArrayList<Integer> segment = new ArrayList<Integer>();
    int spark = 1;
    static int[][] data = new int[10000][2];
    int datalength;
    int segmentlength;
    int balance;
    static float stdNoise = 1;
    static float minRange = -100,maxRange =100;
    static double[] W = {2.0,3.0,-1.0,4.5};
    static double B = 10.0;
    static int sleepWait = 100;
    public static void main(String[] args) throws InterruptedException {
        //Leer datos de BBDD en .csv
        /*File file = new File("Segmento-C#", "Segmento");
        file = new File(file, "bin");
        file = new File(file, "Debug");
        file = new File(file, "netcoreapp3.1");
        file = new File(file, "bd.csv");*/
        /*File file = new File("Balanceador-Java", "src");
        file = new File(file, "main");
        file = new File(file, "resources");
        file = new File(file, "bd.csv");*/
        /*String filePath = file.getAbsolutePath();
        System.out.println(filePath);
        String delimiter = ";";
        int row = 0;
        int cont = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (cont > 0) {
                    String[] fields = line.split(delimiter);
                    for (int col = 0; col < 2; col++) {
                        if (col == 0) {
                            data[row][col] = Integer.parseInt(fields[col]);
                        }
                        if (col == 1) {
                            data[row][col] = (int) Float.parseFloat(fields[col]);
                        }
                    }
                    row++;
                }
                cont++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        Balanceador objser = new Balanceador();
        objser.iniciar();
    }
    void iniciar() throws InterruptedException {
        new Thread(
                new Runnable() {

                    @Override
                    public void run() {
                        mTcpServer = new TCPServer50(
                                new TCPServer50.OnMessageReceived(){
                                    @Override
                                    public void messageReceived(String message) throws InterruptedException {
                                            ServidorRecibe(message);
                                    }
                                }
                        );
                        mTcpServer.run();
                    }
                }
        ).start();
        //-----------------
        String salir = "n";
        sc = new Scanner(System.in);
        System.out.println("Servidor bandera 01");
        while( !salir.equals("s")){
            salir = sc.nextLine();
            if (salir.equals("balancear")) {
                 Balanceo();
            }
            //ServerEnvia(salir);
        }
        System.out.println("Servidor bandera 02");
    }
    //Metodo para balancear datos
    void ServerEnvia(String envia){
        mTcpServer.sendSparkMessageTCPServer(envia, spark);
    }
    void Balanceo(){
        datalength = mTcpServer.nrcli;
        for (int i = 2; i <= datalength; i++) {
                AddClient(i);
                String mess = "Cliente " + client.size();
                mTcpServer.sendClientMessageTCPServer(mess, i, client.size());
        }
        System.out.println("Enviado a cada Cliente");
    }
    //Recibir mensajes general
    void ServidorRecibe(String llego) throws InterruptedException {
        System.out.println("SERVIDOR40 El mensaje:" + llego);
        String[] t = llego.split("---");
        //C-- Cliente S-- Segmento
        if(t[0].equals("C")){
            ClienteRecibe(t[1]);
        }
        //S--Operacion-*****-IDCliente
        else if(t[0].equals("S")){
            SparkRecibe(t[1]);
        } else {
            if (llego.equals("DISCONNECTED")) {
                mTcpServer.nrcli--;
                System.out.println("DESCONECTADO");
            }
        }
    }
    void ClienteRecibe(String llego) throws InterruptedException {
        //1-TTT | ID Cliente - Cadena
        String[] t = llego.split("--");
        System.out.println("Mensaje del cliente " + t[0]);
        String message = t[0] + "--" + t[1];
        mTcpServer.sendSparkMessageTCPServer(message, spark);
    }
    void SparkRecibe(String llego) throws InterruptedException {
        String[] t = llego.split("--");
        System.out.println("Mensaje del spark " + t[0]);
        String message = t[1];
        mTcpServer.sendClientMessageTCPServer(message, client.get(Integer.parseInt(t[0]) - 1), Integer.parseInt(t[0]));
    }
    void AddClient(int ID) {
        client.add(ID);
    }
    
}
