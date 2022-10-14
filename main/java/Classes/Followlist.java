package main.java.Classes;

import java.util.ArrayList;

//Classe di utilità per tenere traccia della
// lista di utenti seguiti e la lista degli utenti che seguono un utente
public class Followlist {
    private ArrayList<String> followers;
    private ArrayList<String> follow;

    //constructor
    public Followlist() {
        this.followers = new ArrayList<>();
        this.follow = new ArrayList<>();
    }

    //metodi get and set per le liste di followers e follow
    public ArrayList<String> getFollowers() {return followers;}

    public void setFollowers(ArrayList<String> followers) {
        this.followers = followers;
    }

    public ArrayList<String> getFollow() {
        return follow;
    }

    public void setFollow(ArrayList<String> follow) {
        this.follow = follow;
    }
}
