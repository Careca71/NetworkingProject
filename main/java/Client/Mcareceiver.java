package main.java.Client;

import java.io.IOException;
import java.net.*;

//Classe runnable per ricevere messaggi multicast
public class Mcareceiver implements Runnable{
    //indirizzo multicast
    String mcaAddress;
    //porta multicast
    int mcaport;
    //constructor
    public Mcareceiver(String s, String s1) {
        String[] address=s.split("=");
        String[] port=s1.split("=");
        mcaAddress=address[1];
        mcaport=Integer.parseInt(port[1]);
    }

    //Metodo run
    @Override
    public void run() {
        //creo una nuova multicast socket alla porta disegnata
        try(MulticastSocket mcsocket= new MulticastSocket(mcaport);){
            //creo un nuovo gruppo
            InetSocketAddress group=new InetSocketAddress(InetAddress.getByName(mcaAddress),mcaport);
            NetworkInterface networkInterface=NetworkInterface.getByInetAddress(InetAddress.getByName(mcaAddress));
            //mi aggiungo al gruppo
            mcsocket.joinGroup(group, networkInterface);

            byte[] buffer=new byte[1024];

            while (true ){
                //ottengo il messaggio
                DatagramPacket packet=new DatagramPacket(buffer,
                        buffer.length);
                mcsocket.receive(packet);
                String msg=new String(packet.getData(),
                        packet.getOffset(),packet.getLength());
                //lo stampo
                System.out.println("[Multicast UDP message received] >> \n"+msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
