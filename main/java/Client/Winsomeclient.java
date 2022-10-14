package main.java.Client;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import main.java.Classes.Follow_db;
import main.java.Classes.Followlist;
import main.java.Interfaces.NotifyFollowerInterface;
import main.java.Interfaces.Rmi_interface;

import java.io.File;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Winsomeclient {
    //file json locale con la lista dei followers per ogni utente
    private final File follow_file;
    //utente corrente
    public String curret_user;
    //interfaccia rmi
    Rmi_interface remoteobject_notify;
    //stub per le notifiche
    NotifyFollowerInterface stub_notify;
    //porta tcp
    private int tcp_port;
    //porta rmi
    private int rmi_port;
    //porta per il servizio di notifica RMI callback
    private int notify_Port;
    //indirizzo del server
    private String server_address;
    //database dei followers
    private Follow_db follow_db;
    //lock per accesso al file
    private final Lock file_lock;
    //mappa utente-[follow,followers]
    private ConcurrentHashMap<String, Followlist> follow_Map;
    //boolean per controllare di aver già fatto partire un mcreceiver
    private boolean startreicever=false;

    //Constructor
    public Winsomeclient() {
        this.follow_file = new File("main/java/Client/Follow_db.json");

        this.follow_db = new Follow_db();
        this.file_lock = new ReentrantLock();
        this.follow_Map = new ConcurrentHashMap<>();
        if (follow_file.exists()) {
            this.follow_db = load_db();
        }
        this.follow_Map=follow_db.getMap_db();
    }

    //Metodi get and set
    public String getCurret_user() {
        return curret_user;
    }
    public void setCurret_user(String curret_user) {
        this.curret_user = curret_user;
    }
    public int getTcp_port() {
        return tcp_port;
    }
    public void setTcp_port(int tcp_port) {
        this.tcp_port = tcp_port;
    }
    public int getRmi_port() {
        return rmi_port;
    }
    public void setRmi_port(int rmi_port) {
        this.rmi_port = rmi_port;
    }

    public void setNotify_Port(int notify_Port) {this.notify_Port = notify_Port;}

    public String getServer_address() {
        return server_address;
    }
    public void setServer_address(String server_address) {
        this.server_address = server_address;
    }

    //Metodo per caricare il database dal file json
    private Follow_db load_db() {
        ObjectMapper objectMapper = new ObjectMapper();
        file_lock.lock();
        try {
            this.follow_db = objectMapper.readValue(follow_file, Follow_db.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        file_lock.unlock();
        return follow_db;
    }

    //Metodo remoto rmi per la registrazione
    public void registration(String nickname, String password, ArrayList<String> tags) {
        int ris = -3;
        try {
            Registry registry = LocateRegistry.getRegistry(rmi_port);
            Rmi_interface remoteobject = (Rmi_interface) registry.lookup("registry_service");
            ris = remoteobject.user_register(nickname, password, tags);
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
        switch (ris) {
            case -3:
                System.out.println("Riprovare l'operazione , username e password invalidi\n ");
                return;
            case -2:
                System.out.println("Riprovare l'operazione, inserire un numero di tag maggiori di zero e minori di cinque\n");
                return;
            case -1:
                System.out.println("Riprovare l'operazione , username e password invalidi\n ");
                return;
            case 0:
                System.out.println("Utente già registrato!\n");
                return;
            case 1:
                System.out.println("Registrazione avvenuta con successo\n");
                return;
        }
    }

    //Metodo per la registrazione alle notifche dei followers
    public void registerFollowNotify(String nickname) throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry(notify_Port);
        remoteobject_notify = (Rmi_interface) registry.lookup("notify_service");

        NotifyFollowerInterface callbackObj = new NotiyfolloerImpl(this);
        stub_notify = (NotifyFollowerInterface)
                UnicastRemoteObject.exportObject(callbackObj, 0);

        System.out.println("registrazione notifiche");
        remoteobject_notify.registerForCallback(stub_notify);

    }

    //Metodo alla buona riuscita del login.
    //avvio un thread per ascoltare i messaggi udp multicast
    //inserisco l'utente nel db.
    public void onLoginSuccess(String nickname, String responce) {
        String[] token = responce.split("-");
        this.curret_user=nickname;
        Mcareceiver mcareceiver = new Mcareceiver(token[0], token[1]);
        if(this.startreicever==false){
            Thread thread = new Thread(mcareceiver);
            thread.start();
            this.startreicever=true;
        }

        this.follow_Map = follow_db.getMap_db();
        if (!this.follow_Map.containsKey(nickname)) {
            System.out.println("Add new Followuserlist..\n");
            this.follow_Map.put(nickname, new Followlist());
        }
        update_db("", "", 0);
    }

    //Metodo per aggiornare il database dei follow/followers op=1 follow op=2 unfollow
    void update_db(String nickname, String follower, int op) {
        this.follow_Map = follow_db.getMap_db();

        if (op == 1) {
            if (!this.follow_Map.get(nickname).getFollow().contains(follower)) {
                this.follow_Map.get(nickname).getFollow().add(follower);
                if (!this.follow_Map.contains(follower)) {
                    this.follow_Map.put(follower, new Followlist());
                    this.follow_Map.get(follower).getFollowers().add(nickname);
                } else {
                    this.follow_Map.get(follower).getFollowers().add(nickname);
                }
            }
        }
        if (op == 2) {
            if (this.follow_Map.get(nickname).getFollow().contains(follower)) {
                this.follow_Map.get(nickname).getFollow().remove(follower);
                this.follow_Map.get(follower).getFollowers().remove(nickname);
            }
        }
        //aggiorno il db
        this.follow_db.setMap_db(this.follow_Map);
        this.file_lock.lock();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(this.follow_file, this.follow_db);
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        file_lock.unlock();
        this.follow_Map=follow_db.getMap_db();
    }

    //Metodo che restituisce la lista degli utenti seguiti
    public void get_user_follow(String curret_user) {
        if (this.curret_user != null) {
            this.follow_Map = this.follow_db.getMap_db();
            if (this.follow_Map.get(curret_user).getFollow().size() == 0) {
                System.out.println("non segui nessuno");
            } else {
                System.out.println(this.follow_Map.get(this.curret_user).getFollow().toString());
            }
        }else{
            System.out.println("Effettuare login\n");
        }
    }

    //Metodo che restituisce la lista degli utenti che seguono l'utente corrente
    public void get_user_followers(String curret_user) {
        if (curret_user != null) {
            this.follow_Map = this.follow_db.getMap_db();
            if (this.follow_Map.get(curret_user).getFollowers().size() == 0) {
                System.out.println("non hai followers");
            } else {
                System.out.println(this.follow_Map.get(this.curret_user).getFollowers().toString());
            }
        }else{
            System.out.println("Effettuare login\n");

        }
        return;
    }

    //metodo al logout , effettuo la deregistrazione alla callback
    public void onlogout() {
        try {
            System.out.println("Unregistering for callback");
            remoteobject_notify.unregisterForCallback(stub_notify);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
