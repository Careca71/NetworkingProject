package main.java.Classes;

import java.util.concurrent.ConcurrentHashMap;

//Classe di utilità per il salvataggio in un file json
// la mappa Utente- [lista di followers, lista di follow]
public class Follow_db {
    private ConcurrentHashMap<String, Followlist> map_db;
    //COnstructor
    public Follow_db() {
        map_db = new ConcurrentHashMap<>();
    }
    //metodi get and set della hashmap
    public ConcurrentHashMap<String, Followlist> getMap_db() {
        return map_db;
    }

    public void setMap_db(ConcurrentHashMap<String, Followlist> map_db) {
        this.map_db = map_db;
    }

}
