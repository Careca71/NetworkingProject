package main.java.Server;

import java.io.IOException;
import java.net.*;

//Classe runnnable per l'invio di messaggi upd multicast
public class McaServer implements Runnable {
    //indirizzo di multicast
    private String mcaAddress;
    //porta di multicast
    private int mcaport;
    //timeout tra un messaggio e l'altro
    private int timeout;
    //server
    private Winserver winserver;
    //constructor
    public McaServer(String mcaAddress, int mcaport, int timeout, Winserver winserver) {
        this.mcaAddress = mcaAddress;
        this.mcaport = mcaport;
        this.timeout = timeout;
        this.winserver = winserver;
    }

    //Metodo run
    @Override
    public void run() {
        //creo una socket udp
        try (DatagramSocket udpSocket = new DatagramSocket();) {
            System.out.println("Start McaServer..\n");

            InetAddress mcIpaddress = InetAddress.getByName(mcaAddress);
            //avvio un ciclo e dormo per la durata del timeout
            while (true) {
                try {
                    Thread.sleep(timeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //prendo il messaggio multicast da inviare dal server
                byte[] msg = winserver.send_mCawallet().getBytes();
                //creo un nuovo datagramma con il messaggio e la sua lunghezza
                DatagramPacket packet = new DatagramPacket(msg, msg.length);
                //Imposto indirizzo e porta
                packet.setAddress(mcIpaddress);
                packet.setPort(mcaport);
                //mando il pacchetto
                udpSocket.send(packet);
                System.out.println("Mcamessage sent..\n");
            }
        } catch (IOException e ){
            e.printStackTrace();
        }
    }
}
