package main.java.Classes;


import java.util.concurrent.ConcurrentHashMap;

//Classe di utilità per memorizzare in un file json la mappa Nickname-utente
public class Userslist {

    private ConcurrentHashMap<String, User> users;
    //Construtctor
    public Userslist() {
        super();
        users = new ConcurrentHashMap<>();
    }
    //metodi get and set
    public Userslist(ConcurrentHashMap<String, User> users) {
        this.users = users;
    }

    public ConcurrentHashMap<String, User> getUsers() {
        return users;
    }

    public void setUsers(ConcurrentHashMap<String, User> users) {
        this.users = users;
    }
}
