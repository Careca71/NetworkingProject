package main.java.Server;

import main.java.Classes.User;
import main.java.Interfaces.Rmi_interface;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import main.java.Classes.Userslist;
import main.java.Interfaces.NotifyFollowerInterface;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

//Classe per l'implementazionde dei metodi rmi remoti
public class Register_rmi extends RemoteServer implements Rmi_interface {
    //db degli utenti
    File user_db;
    //lock sul file
    Lock file_lock;
    //lista degli utenti
    Userslist ul;
    //lista dei client registrati sul  servizio di notifica
    private ArrayList<NotifyFollowerInterface> clients;
    //nome dell'utente corrente
    String nick_cU;

    //COnstructor
    public Register_rmi(Userslist ul, File user_db, Lock file_lock) {
        super();
        this.file_lock=file_lock;
        this.user_db=user_db;
        this.ul=ul;
    }
    //Constructors
    public Register_rmi(String nick_cU) {
        super();
        clients=new ArrayList<NotifyFollowerInterface>();
        this.nick_cU=nick_cU;
    }


    //Metodo per la registrazione di un utente
    @Override
    public int user_register(String nickname, String password, ArrayList<String> tags) throws RemoteException {
        //ottengo la hashmap utenti
        ConcurrentHashMap<String,User> users=ul.getUsers();
        if(nickname==null || password==null){
            return -3;
        }


        if(tags.size()==0){
            return -2;
        }

        if(tags.size()>5){
            return -2;
        }


        if(password.equals("")){
            return -1;
        }

        if(nickname.equals("")){
            return  -1;
        }
        if(users.containsKey(nickname)) {
            return 0;
        } else{
            //Inserisco l'utente nel database
            insert(ul,nickname,password,tags);
            return 1;
        }

        
    }
    //Metodo per la registrazione al servizio di notifica
    @Override
    public synchronized void registerForCallback(NotifyFollowerInterface Client) throws RemoteException {
        if (!clients.contains(Client)&&clients!=null) {
            clients.add(Client);
            System.out.println("New client registered.");
        }
    }
    //Metodo per la deregistrazione al servizio di notifica
    @Override
    public synchronized void unregisterForCallback(NotifyFollowerInterface Client) throws RemoteException {
            if (clients.remove(Client))
                System.out.println("Client unregistered");
            else
                System.out.println("Unable to unregister client");

    }

    //Metod per aggiornare il db remoto dei followers del client
    public void update(String nickname,String follower,int op) throws RemoteException {
        doCallbacks(nickname,follower,op);
    }

    //metodo per effettuare la callback
    private void doCallbacks(String nickname, String follower, int op) throws RemoteException {
            System.out.println("Starting callbacks.");
            Iterator i = clients.iterator( );
            while (i.hasNext()) {
                NotifyFollowerInterface client =
                        (NotifyFollowerInterface) i.next();
                client.notifyfollower(nickname,follower,op);
            }
            System.out.println("Callbacks complete.");
    }



    //Metodo per aggiornare il database degli utenti registrati a winsome
    private void insert(Userslist ul,String nickname, String password, ArrayList<String> tags) {
        //inserisco nella hashmap
        User user=new User(nickname,password,tags);
        System.out.println(password);
        ConcurrentHashMap<String,User> users=new ConcurrentHashMap<>();
        users=ul.getUsers();
        users.put(nickname,user);
        ul.setUsers(users);
        file_lock.lock();
        ObjectMapper objectMapper=new ObjectMapper();
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(user_db,ul);
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        file_lock.unlock();
    }


}
