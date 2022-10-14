package main.java.Server;
import main.java.Classes.Worker_Request;
import main.java.Utils.MyConfigFIleReader;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.rmi.AlreadyBoundException;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;


//MainClass del server
public class MainClass {
    private static MyConfigFIleReader myConfigFIleReader=new MyConfigFIleReader();
    public static void main(String[] args) throws IOException, AlreadyBoundException {
        if(args.length!=1){
            System.err.println(
                    "Inserire Server_Configfile! \n" +
                    "Usage: javac Winserver_Main ServerConfigfile.txt \n");
            System.exit(1);;
        }
        //inizializzo le variabili prese dal file di configurazione
        String file_name=args[0];
        Winserver server=new Winserver();
        init_variables(server,file_name);
        System.out.println("###-----Server Started-----####");

        //creo una socketchannel e un selettore
        ServerSocketChannel serverSocketChannel=null;
        Selector selector=null;

        //Avvio il servizio di registrazione
        server.registration_service();
        server.notify_sevice();
        server.startMcaServer();
        try {
            //apro il canale che rappresenta la mia connessione tcp
            serverSocketChannel=serverSocketChannel.open();
            ServerSocket ss=serverSocketChannel.socket();
            InetSocketAddress address=new InetSocketAddress(server.getTcp_port());
            //lego la socket a una porta
            ss.bind(address);

            //configuro il canale non bloccante
            serverSocketChannel.configureBlocking(false);


            //apro il canale e lo registro sul selettore
            selector=selector.open();
            serverSocketChannel.register(selector,SelectionKey.OP_ACCEPT);

        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true){
            //bloccante. Resto in attesa di un evento
            try {
                selector.select();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
            //prendo le chiavi sui canali pronti e ne prendo la chiave
            Set <SelectionKey> readykes=selector.selectedKeys();
            Iterator<SelectionKey> iterator= readykes.iterator();

            while (iterator.hasNext()){
                //rimuovo la chiave dal selected set (non dal registered set)
                SelectionKey key=iterator.next();
                iterator.remove();
                //Definisco il socketchannel del client
                SocketChannel client;

                try {
                    //accetto evento connessione
                        if(key.isAcceptable()) {
                            //accetto la connessione del client.
                            client = ((ServerSocketChannel) (key.channel())).accept();
                            System.out.println("Connessione accettata" + client);
                            client.configureBlocking(false);
                            //mi preparo per la read
                            registerRead(selector, client);
                        }else if(key.isReadable()){
                            //prendo il canale client attaccato sulla OP.READ
                            SocketChannel client_channel= (SocketChannel) key.channel();
                            //prendo l'array di bytebuffer dall'attachment
                            ByteBuffer []buffer= (ByteBuffer[]) key.attachment();
                            client_channel.read(buffer);
                            if(!buffer[0].hasRemaining()){
                                buffer[0].flip();
                                int len=buffer[0].getInt();
                                if(buffer[1].position()==len){
                                    String request= new String(buffer[1].array()).trim();
                                    Worker_Request worker_request=new Worker_Request(server,request,client_channel,selector);
                                    Executors.newCachedThreadPool().execute(new Thread(worker_request));
                                   // attendo l'elaborazione e la risposta della richiesto
                                    while (worker_request.responce==null){
                                        Thread.sleep(1000);
                                    }
                                    //registro il client sulla read
                                    client_channel.register(selector,SelectionKey.OP_WRITE,worker_request.responce);
                                }
                            }

                        }else if(key.isWritable()){
                            //Invio la risposta al client e mi registro per la read
                            SocketChannel client_channel=(SocketChannel) key.channel();
                            String response= (String) key.attachment();
                            ByteBuffer buf_response= ByteBuffer.wrap(response.getBytes());
                            client_channel.write(buf_response);
                            if(!buf_response.hasRemaining()){
                                buf_response.clear();
                                registerRead(selector,client_channel);
                            }
                        }
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }

        //inizializzo le variabili prese dal file di configurazione
    private static void init_variables(Winserver server, String file_name) {
            File configfile=new File(file_name);
            myConfigFIleReader.read_Server_config_file(configfile);
            server.setAddress(myConfigFIleReader.getAddress());
            server.setTcp_port(myConfigFIleReader.getTcpport());
            server.setRmi_registrationport(myConfigFIleReader.getRmiport());
            server.setMulticast(myConfigFIleReader.getMulticast());
            server.setMcaport(myConfigFIleReader.getMcaport());
            server.setNotify_port(myConfigFIleReader.getNotifyport());
            server.setTimeout(myConfigFIleReader.getTimeout());
            System.out.println("Indirizzo: "+server.getAddress());
            System.out.println("Porta RMI: "+server.getRmi_registrationport());
            System.out.println("TCP_PORT: "+server.getTcp_port());
            System.out.println("NOTIFYPORT: "+server.getNotify_port());
            System.out.println("MULTICAST: "+server.getMulticast());
            System.out.println("MCAPORT: "+server.getMcaport());
            System.out.println("TIMEOUT: "+server.getTimeout());

    }

    private static void registerRead(Selector selector, SocketChannel client){
        ByteBuffer len=ByteBuffer.allocate(Integer.BYTES);
        ByteBuffer message=ByteBuffer.allocate(1024);
        ByteBuffer [] buffer={len,message};
        //aggiungo l'array di buffer come attachment aggiungendo il canale
        //del client al selettore sull'operazione read
        try {
            client.register(selector,SelectionKey.OP_READ,buffer);
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }
    }

}



