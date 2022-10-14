package main.java.Client;
import main.java.Utils.MyConfigFIleReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.rmi.NotBoundException;
import java.util.ArrayList;

//MainClass del client

public class Mainclient {

    private static String HELP = "Welcome to Winsome!\n list of user operation:" +
            "\nregister <username> <password> <tags>\n" +
            "login <username> <password>\n" +
            "logout <username>\n" +
            "list_user\n" +
            "list_followers\n" +
            "list_follow\n"+
            "unfollow <username>\n"+
            "blog\n"+
            "feed\n" +
            "post <title> <text> (titolo e testo tra virgolette)\n"+
            "show_post <id>\n"+
            "delete <idPost>\n"+
            "rewin <idPost>\n"+
            "rate <idPost> <vote> (voto -1 o +1)\n"+
            "add_comment <idPost> <comment> (commento tra virgolette)\n"+
            "get_wallet\n"
            +"getwalletbit\n\nEnjoy.+\n\n digitare 'help' per visualizzare il messaggio";

    //definisco un mio reader per il file di configurazione
    private static final MyConfigFIleReader myConfigFIleReader = new MyConfigFIleReader();

    public static void main(String[] args) throws IOException, NotBoundException {
        String file_name = null;
        if (args.length != 1) {
            System.err.println(
                    "Inserire Client_Configfile! \n" +
                            "Usage: javac WinClient_Main ClientConfigfile.txt \n");
            System.exit(1);
        }

        System.out.println(HELP);
        //ottengo il nome del client configfile
        file_name = args[0];
        //creo un winsomeclient
        Winsomeclient client = new Winsomeclient();
        //assegno i valori presi dal file di configurazione
        init_variables(client, file_name);
        //creo una nuova socketchannel
        SocketChannel server = null;
        //Creo un reader per leggere l'input da tastiera
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String input = "";
        //apro una socketchannel alla porta definita dal file di configurazione
        SocketChannel client_channel = SocketChannel.open(new InetSocketAddress(InetAddress.getLocalHost(), client.getTcp_port()));

        //avvio il ciclo finchè il client non digita logout
        while (true && !(input.equals("logout"))) {

            //leggo input da tastiera
            input = reader.readLine();
            //la elaboro
            String[] comando = input.split(" ");
            //lista dei tag dell'utente
            ArrayList<String> tags = new ArrayList<>();
            //se il comando è registra lo effettuo tramite rmi
            if (comando[0].equals("register")) {
                if (comando.length < 3) {
                    System.out.println("Effettuare login o registrazione nel modo corretto!\n");
                } else {
                    for (int i = 3; i < comando.length; i++) tags.add(comando[i]);
                    client.registration(comando[1], comando[2], tags);
                }

            } else {
                //se il comando non èriguardante la lista dei followers
                if ((!comando[0].equals("list_followers")) && (!comando[0].equals("list_follow"))) {
                    //creo un buffer per la lunghezza del messaggio
                    ByteBuffer length = ByteBuffer.allocate(Integer.BYTES);
                    //inserisco la dimensione del messaggio
                    length.putInt(input.length());
                    //riporto buffer al suo primo indice
                    length.flip();
                    //scrivo sul canale la lunghezzaa del buffer
                    client_channel.write(length);
                    //eggettuo una clear
                    length.clear();
                    //scrivo sul canale il messaggio
                    ByteBuffer readBuffer = ByteBuffer.wrap(input.getBytes());
                    client_channel.write(readBuffer);
                    readBuffer.clear();

                    //creo un buffer per la risposta del server
                    ByteBuffer reply = ByteBuffer.allocate(1024);
                    client_channel.read(reply);
                    reply.flip();
                    String responce = new String(reply.array()).trim();
                    //se l'operazione era quella di login e la risposta è risultata positiva registro
                    //il client al servizio multicast e al servizio delle notifche per i followers
                    if (comando[0].equals("login") && (responce.contains("correttamente"))) {
                        client.registerFollowNotify(comando[1]);
                        client.onLoginSuccess(comando[1], responce);
                        client.setCurret_user(comando[1]);
                    }
                    //operazione di logout
                    if (comando[0].equals("logout") && (responce.contains("Goodbye"))) client.onlogout();
                    System.out.printf("Server ha inviato: %s\n", new String(reply.array()).trim());
                    reply.clear();
                } else {
                    //consulto il db locale per la lista dei followers
                    if (comando[0].equals("list_follow")) client.get_user_follow(client.getCurret_user());
                    if (comando[0].equals("list_followers")) client.get_user_followers(client.getCurret_user());
                }

                if(comando[0].equals("help"))System.out.println(HELP);


            }
        }
        //chiudo il client
        client_channel.close();
    }

    //inizializzo le variabili del client prese dal file di configurazione
    private static void init_variables(Winsomeclient client, String file_name) {
        File configfile = new File(file_name);
        myConfigFIleReader.read_Client_config_file(configfile);
        client.setServer_address(myConfigFIleReader.getAddress());
        client.setTcp_port(myConfigFIleReader.getTcpport());
        client.setRmi_port(myConfigFIleReader.getRmiport());
        client.setNotify_Port(myConfigFIleReader.getNotifyport());
    }

}


