package main.java.Interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
//Interfaccia remota per Rmi callback
public interface NotifyFollowerInterface extends Remote {

    //metodo invocato dal server per aggiornare la lista dei followers
    public void notifyfollower(String nickname, String follower, int op) throws RemoteException;
}
