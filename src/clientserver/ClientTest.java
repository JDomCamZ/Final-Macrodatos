package clientserver;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class ClientTest {
    TCPClient50 mTcpClient;
    Scanner sc;
    int cli;
    public static void main(String[] args)  {
        ClientTest objcli = new ClientTest();
        objcli.iniciar();
    }

    void iniciar(){
        new Thread(
                new Runnable() {

                    @Override
                    public void run() {
                        mTcpClient = new TCPClient50("192.168.0.19",
                                new TCPClient50.OnMessageReceived(){
                                    @Override
                                    public void messageReceived(String message){
                                        ClienteRecibe(message);
                                    }
                                }
                        );
                        mTcpClient.run();
                    }
                }
        ).start();
        //---------------------------

        String salir = "n";
        sc = new Scanner(System.in);
        System.out.println("Cliente bandera 01");
        while( !salir.equals("s")){
            salir = sc.nextLine();
            ClienteEnvia(salir);
        }
        System.out.println("Cliente bandera 02");

    }
    void ClienteRecibe(String llego){
        String[] t = llego.split(" ");
        if (t[0].equals("Cliente")) {
            System.out.println("El número de cliente es: " + t[1]);
            cli = Integer.parseInt(t[1]);
        }
        else{
            System.out.println("CLINTE50 El mensaje::" + llego);
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            //ClienteEnvia("cttlisto");
        }


    }
    void ClienteEnvia(String envia){
        if (mTcpClient != null) {
            envia = "C---" + cli + "--" + envia;
            mTcpClient.sendMessage(envia);
        }
    }

}
