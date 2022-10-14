package main.java.Client;

import main.java.Interfaces.NotifyFollowerInterface;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
//classe per implementare il metodo remoto per l'aggiornamento dei followers

public class NotiyfolloerImpl extends RemoteObject implements NotifyFollowerInterface {
    private Winsomeclient winsomeclient;
    //Construttore
    public NotiyfolloerImpl(Winsomeclient winsomeclient) throws RemoteException {
        super();
        this.winsomeclient=winsomeclient;
    }

    //Metodo per l'aggionamento del database dei followers
    @Override
    public void notifyfollower(String nickname,String follower,int op) throws RemoteException {

            this.winsomeclient.update_db(nickname,follower,op);
            return ;
    }
}
