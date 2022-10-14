package main.java.Classes;
import java.util.ArrayList;

//Classe Post, identifica i post degli utenti all'interno del Winsome
public class Post {
    //Variabili
    //Id univoco per ogni post
    public int id;

    //Autore del post
    public String author;

    //titolo
    public String title;
    //testo
    private String text;


    //variabili per il calcolo del guadagno
    public int n_iteration;
    public double guadagno;
    public int new_persons_like;
    public int new_personsComment;
    public int same_personComment;

    //lista utenti che hanno effettuato il rewin del post
    public ArrayList<String> rewin_user = new ArrayList<>();
    //Lista dei like che hanno lasciato gli utenti
    private ArrayList<Integer> like;
    //Numero di commenti
    private int nComments;
    //Lista delle persone che hanno lasciato un voto e lista delle persone che hanno commentato
    private final ArrayList<String> list_userLike = new ArrayList<>();
    private final ArrayList<String> list_userComm = new ArrayList<>();
    //commenti
    private ArrayList<String> comments = new ArrayList<>();
    //Constructor
    public Post() {
        super();
    }
    //Constructor
    public Post(int id, String author, String title, String text) {
        super();
        this.id = id;
        this.author = author;
        this.title = title;
        this.text = text;
        this.like = new ArrayList<Integer>();
        this.nComments = 0;
        this.new_persons_like = 0;
        this.guadagno = 0;
        this.n_iteration = 1;
        this.same_personComment = 0;
        this.new_personsComment = 0;
    }

    //Metodi get and set
    public ArrayList<String> getList_userLike() {
        return list_userLike;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ArrayList<Integer> getLike() {
        return like;
    }

    public int getnComments() {
        return nComments;
    }

    public void setnComments(int nComments) {
        this.nComments = nComments;
    }

    public ArrayList<String> getComments() {
        return comments;
    }

    public void setComments(ArrayList<String> comments) {
        this.comments = comments;
    }

    //Metodo per aggiungere un commento.
    public int add_Comment(String user, String comment) {
        if (!list_userComm.contains(user)) {
            new_personsComment++;
            list_userComm.add(user);
        }
        same_personComment++;
        comments.add(user + " | " + comment);
        return 1;
    }

    //metodo per aggiungere un voto
    public void add_vote(String nickname, int vote) {
        like.add(vote);
        new_persons_like++;
        list_userLike.add(nickname);
    }
    //metodo per il calcolo del guadagno ricevuto dal post.
    //se il numero di nuove interazioni è nullo l'incremento sarà nullo,
    // altrimenti verrà applicata la formula definita da traccia
    public double calculate_increase() {
        int sum_like = 0;
        int max;
        double log_like = 1;
        double log_comment = 1;

        if (new_persons_like == 0 && new_personsComment == 0) {
            return 0;
        } else {
            if (new_persons_like != 0) {
                for (int i = 0; i < like.size(); i++) {
                    sum_like = sum_like + like.get(i);
                }
                max = Math.max(sum_like, 0) + 1;
                log_like = Math.log(max);
            }
            if (new_personsComment != 0) {
                log_comment = Math.log((2 / (1 + (Math.exp(-(same_personComment - 1))))) + 1 * new_personsComment);
            }
            guadagno = (log_like + log_comment) / n_iteration;
            n_iteration++;
            new_personsComment = 0;
            new_persons_like = 0;
            return guadagno;
        }
    }
}
