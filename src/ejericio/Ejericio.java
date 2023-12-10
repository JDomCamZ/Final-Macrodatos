/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package ejericio;

/**
 *
 * @author Jorge
 */
public class Ejericio {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int alen = args.length;    
        int streamP = 4444;
        int wait = 10;
        float std = (float)0.5;
        
        if (alen > 0)
            streamP = Integer.parseInt(args[0]);
        if (alen > 1)
            wait = Integer.parseInt(args[1]);
        if (alen > 2)
            std = Float.parseFloat(args[2]);

        new ServerImp(streamP,wait, std).iniciar();
    }
    
}
