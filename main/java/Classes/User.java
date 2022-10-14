package main.java.Classes;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

//Classe per l'identificazione di un Utente all'interno di Winsome
public class User {
    //nickname
    private  String nickname;
    //password
    private String password;
    //tags
    private ArrayList<String> tags;
    //portafoglio
    public double wallet;
    //storico delle transazioni
    public ConcurrentHashMap<String, Double> transation=new ConcurrentHashMap<String, Double>();

    //constructors
    public User(){
        super();
    }

    public User(String nickname, String password, ArrayList<String> tags) {
        this.nickname=nickname;
        this.password=password;
        this.tags=tags;
        this.wallet=0;
    }

    //metodi get and set
    public String getNickname() {
        return nickname;
    }
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public ArrayList<String> getTags() {
        return tags;
    }
    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }
    public String getPassword() {
        return password;
    }

    //Metodo per la verifica della password
    public int pass_control(String password){
        if(this.password.equals(password))return 1;
        else return 0;
    }

    //metodo per aggiornare il valore del portafoglio
    public void increaseWallet(double guadagno,int post_id,int it) {
        wallet+=guadagno;
        if(post_id==0){
            transation.put("increase - for like/comment",guadagno);
        }else {
            transation.put("increase"+post_id+"-"+it,guadagno);
        }
        return;
    }
}
