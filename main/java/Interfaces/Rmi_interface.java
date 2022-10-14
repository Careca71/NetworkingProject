package main.java.Interfaces;

import main.java.Interfaces.NotifyFollowerInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

//Interfaccia Remota , i metodi vengono implementati dal server
public interface Rmi_interface extends Remote {

    //metodo epr la registrazione
    int user_register(String nickname, String password, ArrayList<String> tags)throws RemoteException;

    //metodo per registrarsi al servizio di callback
    void registerForCallback(NotifyFollowerInterface ClientInterface) throws RemoteException;
    //metodo per effettuare la deregistrazione al servizio di callback
    void unregisterForCallback (NotifyFollowerInterface ClientInterface) throws RemoteException;

}
