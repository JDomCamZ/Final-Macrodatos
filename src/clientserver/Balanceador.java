package clientserver;

import java.util.ArrayList;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class Balanceador {
    TCPServer50 mTcpServer;
    Scanner sc;
    ArrayList<Integer> client = new ArrayList<Integer>();
    int spark = 1;
    int datalength;
    static Connection con= null;
    static PreparedStatement pr= null;
    public static void main(String[] args) throws InterruptedException, ClassNotFoundException {
        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/macro","root","12345678");
        } catch (Exception e) {
            System.out.println(e);
        }
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
        
        String sql = "INSERT INTO consultas (cliente,texto) VALUES (?, ?)"; 
        try (PreparedStatement preparedStatement = con.prepareStatement(sql)) {
                 System.out.println("TRATA DE INSERTAR");
                // Establecer los valores de los parámetros
                preparedStatement.setString(1, t[0]);
                preparedStatement.setString(2, t[1]);

                // Ejecutar la inserción
                int filasAfectadas = preparedStatement.executeUpdate();
                // Verificar el resultado
                if (filasAfectadas > 0) {
                    System.out.println("Datos insertados correctamente.");
                } else {
                    System.out.println("No se pudo insertar datos.");
                }
            } catch(Exception e){
                
            }
        
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
