package main.java.Server;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import main.java.Classes.Post;
import main.java.Classes.Post_list;
import main.java.Interfaces.Rmi_interface;
import main.java.Interfaces.Server_Interface;
import main.java.Classes.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import main.java.Classes.Userslist;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Math.random;

//Classe principale per La gestione e l'elaborazione del winserver
public class Winserver implements Server_Interface {

    //File json locale per la lista degli utenti
    private final File user_db;
    //lock per accesso ai file
    private Lock file_lock;
    //File json locale per la lista dei post
    private File post_db;
    //porta per la registrazione RMI
    private int rmi_registrationport;
    //Porta tcp
    private int tcp_port;
    //indirizzo ip
    private String address;
    //porta per il servizio di notifiche RMI
    private int notify_port;
    //indirizzo multicast
    private String multicast;
    //porta multicast
    private int mcaport;
    //timeout per invio messaggi multicast
    private int timeout;
    //mappa nickname-utente
    private ConcurrentHashMap<String, User> users;
    //Lista utenti per da modificare/salvare sul file json
    private Userslist userslist;
    //utente corrente
    public User current_user = new User();
    //variabile per sapere se al momento qualcuno è collegato al server
    private boolean someone_online = false;
    //Varibile RMI
    private Register_rmi notify_server;
    //lista dei post
    private ArrayList<Post> posts;
    //variabile per modificare/salvere i post sul file json
    private Post_list post_list;
    private Double like_update=0.0;


    //Metodi get and set
    public void setAddress(String address) {
        this.address = address;
    }
    public String getAddress() {
        return address;
    }
    public void setTcp_port(int tcp_port) {
        this.tcp_port = tcp_port;
    }
    public void setRmi_registrationport(int rmi_registrationport) {
        this.rmi_registrationport = rmi_registrationport;
    }
    public void setMulticast(String multicast) {this.multicast = multicast;}
    public void setNotify_port(int notify_port) {this.notify_port = notify_port;}
    public void setMcaport(int mcaport) {this.mcaport = mcaport;}
    public void setTimeout(int timeout) {this.timeout = timeout;}
    public int getNotify_port() {return notify_port;}
    public String getMulticast() {return multicast;}
    public int getMcaport() {return mcaport;}
    public int getTimeout() {return timeout;}
    public int getTcp_port() {
        return tcp_port;
    }
    public int getRmi_registrationport() {
        return rmi_registrationport;
    }

    //constructor
    public Winserver() {
        this.user_db = new File("main/java/Server/User_db.json");
        this.post_db = new File("main/java/Server/Post_db.json");
        this.file_lock = new ReentrantLock();
        this.users = new ConcurrentHashMap<>();
        //carico la lista utenti
        this.userslist = load_db();
        //carico la lista dei post
        this.post_list = load_post_db();
        this.posts = new ArrayList<>();
    }
    //metodo per caricare il post dal file json
    private Post_list load_post_db() {
        ObjectMapper objectMapper = new ObjectMapper();
        file_lock.lock();
        try {
            this.post_list = objectMapper.readValue(post_db, Post_list.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        file_lock.unlock();
        return post_list;
    }

    //metodo per caricare la lista degli utenti dal file json
    private Userslist load_db() {
        ObjectMapper objectMapper = new ObjectMapper();
        file_lock.lock();
        try {
            userslist = objectMapper.readValue(user_db, Userslist.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        file_lock.unlock();
        return userslist;
    }

    //Metodo per effettuare avviare il servizio remoto per la registrazione
    @Override
    public void registration_service() {
        try {
            Register_rmi register = new Register_rmi(this.userslist, user_db, file_lock);
            Rmi_interface stub = (Rmi_interface) UnicastRemoteObject.exportObject(register, this.rmi_registrationport);
            LocateRegistry.createRegistry(rmi_registrationport);
            Registry registry = LocateRegistry.getRegistry(rmi_registrationport);
            registry.bind("registry_service", stub);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Metodo per il login
    public int login(String nickname, String password) {
        //carico la lista utenti
        userslist = load_db();
        users = userslist.getUsers();
        System.out.println(users.size());
        if (nickname == null || password == null) {
            return -1;
        }
        if (!users.containsKey(nickname)) {
            return 0;
        } else {
            if (someone_online == false) {
                if (users.get(nickname).pass_control(password) == 1) {
                    current_user = users.get(nickname);
                    someone_online = true;
                    userslist.setUsers(users);
                    return 1;
                } else {
                    return 0;
                }
            } else {
                return -2;
            }
        }
    }

    //metodo per effettuare il logout
    public int logout(String nickname) {
        if (current_user == null && someone_online == false || (!current_user.getNickname().equals(nickname))) {
            return 0;
        } else {
            save_db();
            someone_online = false;
            current_user = null;
            like_update=0.0;
            return 1;
        }
    }

    //Metodo per salvare il db dei post sul file json
    private void save_db() {
        this.file_lock.lock();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(this.post_db, this.post_list);
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        file_lock.unlock();
    }

    //Metodo per ottenere la lista utenti
    public String list_user() {
        if (someone_online == false) return "Eseguire Login\n";
        ConcurrentHashMap<String, User> users = this.userslist.getUsers();
        users.remove(current_user.getNickname());
        ArrayList<String> list = new ArrayList<>();
        users.forEach((n, u) -> {
            ArrayList<String> tags = current_user.getTags();
            for (int i = 0; i < tags.size(); i++) {
                ArrayList<String> tag_line = new ArrayList<>();
                if (u.getTags().contains(tags.get(i))) {
                    tag_line.add(tags.get(i));
                }
                if (tag_line.size() != 0) {
                    list.add(u.getNickname() + tag_line.toString());
                }

            }
        });
        if (list.size() != 0) {
            return list.toString();
        } else {
            return "Nessun utente con tag simili";
        }

    }

    //Metodo per mandare le notifiche sui followers
    public void notify_sevice() throws RemoteException, AlreadyBoundException {
        this.notify_server = new Register_rmi(this.current_user.getNickname());
        Rmi_interface stub = (Rmi_interface) UnicastRemoteObject.exportObject(this.notify_server, 3001);
        LocateRegistry.createRegistry(3001);
        Registry registry = LocateRegistry.getRegistry(3001);
        registry.bind("notify_service", stub);
    }
    //funzione remota per l'aggiornamento dei followers
    public int notifyfollower(String nickname, String follower, int op) throws RemoteException, AlreadyBoundException {
        if (current_user.getNickname() == null) return -2;
        this.users = userslist.getUsers();
        if (!users.containsKey(follower)) {
            System.out.println(users.containsKey(nickname) + "  " + users.contains(follower) + nickname + follower);
            return -1;
        }
        if (current_user.getNickname().equals(follower)) {
            return 0;
        } else {
            //Avvio metodo remoto
            this.notify_server.update(current_user.getNickname(), follower, op);
            return 1;
        }
    }

    //Metodo per creare il post
    public int create_post(String title, String text) {
        if (title.length() > 20 || text.length() > 500)
            if (someone_online == false) return -1;
        Random rand=new Random();
        int id=Math.abs(this.post_list.getPosts().size()+(rand.nextInt()%1000));
        Post post = new Post(id, current_user.getNickname(), title, text);
        posts = post_list.getPosts();
        posts.add(post);
        this.post_list.setPosts(posts);
        //Aggiorno il database
        save_db();
        return post.id;
    }



    //Metodo per mostrare il blog
    public String show_blog() {
        if (someone_online == false) return "Utente non loggato!\n";
        posts = post_list.blog(current_user.getNickname());
        if (posts.size() == 0) return "non hai post nel tuo blog";
        else {
            String responce = "\n id \t | author\t | title \n ---------- \n";
            for (int i = 0; i < posts.size(); i++) {
                responce += posts.get(i).id + "\t|" + posts.get(i).author + "\t|" + posts.get(i).title + "\n";
            }
            return responce;
        }
    }
    //Metodo per mostrare il feed
    public String show_feed() {
        if (someone_online == false) return "Utente non loggato!\n";
        posts = post_list.feed(current_user.getNickname());
        if (posts.size() == 0) return "non hai post nel tuo feed, segui qualcuno!\n";
        else {
            String responce = "\nid \t | author\t | title \n ---------- \n";
            for (int i = 0; i < posts.size(); i++) {
                responce += posts.get(i).id + "\t|" + posts.get(i).author + "\t|" + posts.get(i).title + "\n";
            }
            return responce;
        }
    }
    //Metodo per mostrare il post
    public String show_post(int id) {
        if (someone_online == false) return "Utente non loggato!\n";
        posts = post_list.getPosts();
        for (int i = 0; i < posts.size(); i++) {
            if (posts.get(i).getId() == id) {
                if(post_list.check_follow(posts.get(i).author,current_user.getNickname())==false)return "Non puoi vedere i post di persone che non segui\n";
                String responce = "id: " + posts.get(i).getId() +
                        "\nauthor:\t" + posts.get(i).getAuthor() +
                        "\nTitle:\t" + posts.get(i).getTitle() +
                        "\nText:\t" + posts.get(i).getText() +
                        "\nLike:\t" + posts.get(i).getLike() +
                        "\t," + posts.get(i).getList_userLike().toString() +
                        "\nComments\t" + posts.get(i).getComments().toString();
                return responce;
            }
        }
        return "Post non trovato\n";
    }
    //metodo per eliminare un post
    public String delete_post(int id) {
        if (someone_online == false) return "utente non loggato!\n";
        posts = post_list.getPosts();
        for (int i = 0; i < posts.size(); i++) {
            if (posts.get(i).getId() == id && posts.get(i).author.equals(current_user.getNickname())) {
                posts.remove(i);
                this.post_list.setPosts(posts);
                save_db();
                return "post eliminato correttamente " + id;

            }
            if (posts.get(i).getId() == id && !(posts.get(i).author.equals(current_user.getNickname()))) {
                return "non puoi eliminare un post di cui non sei l'autore!";
            }
        }
        return "Post non trovato";
    }
    //metodo per effettuare il rewin di un post
    public String rewin_post(int id) {
        if (someone_online == false) return "Utente non loggato!\n";
        for (int i = 0; i < posts.size(); i++) {
            if (posts.get(i).id == id) {
                if(post_list.check_follow(posts.get(i).author,current_user.getNickname())==false)return "Non puoi fare il rewin di post di persone che non segui\n";
                posts.get(i).rewin_user.add(current_user.getNickname());
                return "Rewin post " + id + ", ora è presente nel tuo blog";
            }
        }
        return "Post non trovato";
    }
    //metodo per valutare un post
    public String rate_post(int id, int vote) {
        if (someone_online == false) return "Utente non loggato!\n";
        if (vote != 1 && vote != -1) return "voto non valido, vote <idpost> <vote(+1/-1)> \n";
        for (int i = 0; i < posts.size(); i++) {
            if (posts.get(i).id == id) {
                if (posts.get(i).author.equals(current_user.getNickname()))
                    return "non puoi aggiungere like e/o commenti ai tuoi post\n";
                if(post_list.check_follow(posts.get(i).author,current_user.getNickname())==false)return "Non puoi votare i post di persone che non segui\n";
                if (posts.get(i).getList_userLike().contains(current_user.getNickname())) return "non puoi aggiungere più volte like allo stesso post\n";
                posts.get(i).add_vote(current_user.getNickname(), vote);
                like_update+=0.15;
                post_list.setPosts(posts);
                return "voto inserito correttamente";
            }
        }

        return "Post non trovato\n";
    }

    //Metodo per commentare un post
    public String comment_post(int id, String comment) {
        if (someone_online == false) return "Utente non loggato!\n";
        for (int i = 0; i < posts.size(); i++) {
            if (posts.get(i).id == id) {
                if(post_list.check_follow(posts.get(i).author,current_user.getNickname())==false)return "Non puoi votare i post di persone che non segui\n";
                posts.get(i).add_Comment(current_user.getNickname(), comment);
                this.like_update+=0.05;
                post_list.setPosts(posts);
                return "Commento aggiunto correttamente,eseguire 'show' per visualizzare";
            }
        }
        return "Post non trovato\n";
    }


    //Metodo che fa partire il server udp multicast per l'invio di messaggi remoti, creando un nuovo thread
    public void startMcaServer() {
        McaServer mcaServer=new McaServer(this.multicast,this.mcaport,this.timeout,this);
        Thread thread= new Thread(mcaServer);
        thread.start();
    }

    //Metodo  fare l'upload del portafoglio corrente degli utenti
    public String send_mCawallet() {
        posts=post_list.getPosts();
        userslist=load_db();
        users=userslist.getUsers();
        for(int i=0;i<posts.size();i++){
            double increase=posts.get(i).calculate_increase();
            users.get(posts.get(i).author).increaseWallet(increase,posts.get(i).id,posts.get(i).n_iteration);
        }
        users.get(current_user).increaseWallet(like_update,0,0);
        ArrayList<String> responce =new ArrayList<>();
        responce.add( "User\t|Wallet \n");
        users.forEach((k,v)->{
            responce.add(k+"-"+ v.wallet+"\n");
        });
        userslist.setUsers(users);
        save_db();
        save_user_db();
        return responce.toString();
    }
    //Metodo per ottenere il proprio portafoglio
    public String get_wallet(){
        return "Wallet: "+current_user.wallet +"\n"+current_user.transation.toString();
    }
    public  String get_walletbit(){
        double value= getRandomvalue();
        if(value!=-1){
            double change=current_user.wallet*(1/value);
            return "currentchange:"+change;
        }else{
            return "cambio valuta non disponibile";
        }
    }

    //Metodo per ottenere un valore randomico dal sito RANDOM.ORG
    private double getRandomvalue() {
        InputStream in =null;
        double value=1;
        try{
            URL randomorg= new URL("https://www.random.org/decimal-fractions/?num=1&dec=2&col=1&format=plain&rnd=new");
            in=randomorg.openStream();

            Reader reader= new InputStreamReader(in);
            StringBuilder stringBuilder=new StringBuilder();

            int c;
            while ((c = reader.read())!=-1){
                stringBuilder.append((char) c);
            }
            value=Double.parseDouble(stringBuilder.toString());
            return value;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        value=-1;
        return  value;
    }

    //Metodo per l'aggionrnamento del file json contente la lista degli utenti
    private void save_user_db() {
        file_lock.lock();
        ObjectMapper objectMapper=new ObjectMapper();
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(user_db,userslist);
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
