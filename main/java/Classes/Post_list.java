package main.java.Classes;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//Classe di utilità per salvare la lista di post nel file json
public class Post_list {
    //file json contenente la lista dei followers
    private final File follow_file;
    //lock per accesso al file
    private final Lock file_lock;
    //lista di post
    ArrayList<Post> posts;
    Followlist followlist;
    //lista dei follower per ogni utente
    ConcurrentHashMap<String, Followlist> follow_Map;
    //Db dei followers
    private Follow_db follow_db;

    //constructor
    public Post_list() {
        this.posts = new ArrayList<>();
        follow_file = new File("main/java/Client/Follow_db.json");
        follow_db = new Follow_db();
        file_lock = new ReentrantLock();
        ConcurrentHashMap<String, Followlist> follow_Map = new ConcurrentHashMap<>();
        if (follow_file.exists()) {
            follow_db = load_db(file_lock, follow_file, follow_db);
        }
    }

    //metodo get and set per ottenere la lista dei post
    public ArrayList<Post> getPosts() {return posts;}

    public void setPosts(ArrayList<Post> posts) {this.posts = posts;}

    //Metodo per visualizzare il blog dell'utente corrente
    public ArrayList<Post> blog(String author) {
        ArrayList<Post> blog = new ArrayList<>();
        for (int i = 0; i < posts.size(); i++) {
            if (posts.get(i).author.equals(author) || posts.get(i).rewin_user.contains(author)) blog.add(posts.get(i));
        }
        return blog;
    }

    //metodo per visualizzare il feed dell'utente corrente
    public ArrayList<Post> feed(String nickname) {
        ArrayList<Post> feed = new ArrayList<>();
        if (follow_file.exists()) {
            follow_db = load_db(file_lock, follow_file, follow_db);
        } else {
            return feed;
        }
        follow_Map = follow_db.getMap_db();
        ArrayList<String> follow = follow_Map.get(nickname).getFollow();
        for (int i = 0; i < posts.size(); i++) {
            for (int j = 0; j < follow.size(); j++) {
                if (posts.get(i).author.equals(follow.get(j))) {
                    feed.add(posts.get(i));
                }
            }
        }
        return feed;
    }

    //Metodo per la lettura del database dal file json
    private Follow_db load_db(Lock file_lock, File follow_file, Follow_db follow_db) {
        ObjectMapper objectMapper = new ObjectMapper();
        file_lock.lock();
        try {
            follow_db = objectMapper.readValue(follow_file, Follow_db.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        file_lock.unlock();
        return follow_db;
    }

    //Metodo per la verifica dei followers
    public boolean check_follow(String author, String nickname) {
        follow_Map = follow_db.getMap_db();
        return follow_Map.get(nickname).getFollow().contains(author);
    }

}
