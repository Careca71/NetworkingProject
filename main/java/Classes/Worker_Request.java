package main.java.Classes;
import main.java.Server.Winserver;
import main.java.Utils.ResponseManager;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.util.regex.Pattern;

//Classe Runnable per l'elaborazione delle richieste da parte del client.
//Una volta presa una richiesta la elebora passando al winserver l'operazione da eseguire con i
//relativi parametri.
//Infine restituisce la risposta al thread main che la inoltrerà al client
public class Worker_Request implements Runnable {
    //Risposta
    public String responce = null;
    //richiesta
    private String request;
    //server
    private Winserver winserver;
    //socketchannel
    private SocketChannel cliet_channel;
    //Selettore
    private Selector selector;
    //Responsemanager , si occupa di elaborare le risposte ai metodi del server
    ResponseManager responseManager = new ResponseManager();

    //Constructor
    public Worker_Request(Winserver server, String request, SocketChannel client_channel, Selector selector) {
        this.winserver = server;
        this.request = request;
        this.cliet_channel = client_channel;
        this.selector = selector;
    }


    //Metodo run()
    @Override
    public void run() {
        //Taglio la richiesta
        String[] token = request.split(" ");
        //Elaborazione login, nel caso di risposta affermativa ritorno
        //indirizzo multicast e porta al client
        if (token[0].equals("login") && token.length == 3) {
            String nickname = token[1];
            String pass = token[2];
            int ris = this.winserver.login(nickname, pass);
            if (ris == 1) {
                responce = responseManager.send_response(ris, token[0]) + "\nMcaAddress=" + winserver.getMulticast() + "-Mcaport=" + winserver.getMcaport() + "\n";
            } else {
                responce = responseManager.send_response(ris, token[0]);

            }


            return;
        }
        //metodo logout
        if (token[0].equals("logout") && token.length == 2) {
            String nickname = token[1];
            int ris = this.winserver.logout(nickname);
            responce = responseManager.send_response(ris, token[0]);
            return;
        }
        //metodo per ottenere la lista degli utenti
        if (request.equals("list_user")) {
            responce = this.winserver.list_user();
            return;
        }
        //metodo per seguire un utente, semplicemente va a modificare tramite RMI il database locale
        //del client con la lista dei followers e dei follow
        if (token[0].equals("follow") && token.length == 2) {
            if (token[1] != null) {
                try {
                    int ris;
                    if (this.winserver.current_user.getNickname() == null) {
                        ris = -2;
                        responce = responseManager.send_response(ris, token[0]);
                        return;
                    } else {
                        ris = this.winserver.notifyfollower(this.winserver.current_user.getNickname(), token[1], 1);
                        if (ris == 1) {
                            responce = responseManager.send_response(ris, token[0]) + token[1] + "\n";
                        } else responce = responseManager.send_response(ris, token[0]);
                        return;
                    }

                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (AlreadyBoundException e) {
                    e.printStackTrace();
                }
            }
        }
        //ALlo stesso modo di follow
        if (token[0].equals("unfollow") && token.length == 2) {
            if (token[1] != null) {
                try {
                    int ris;
                    if (this.winserver.current_user.getNickname() == null) {
                        ris = -2;
                        responce = responseManager.send_response(ris, token[0]);
                        return;
                    } else {
                        System.out.println(this.winserver.current_user.getNickname() + token[1]);
                        ris = this.winserver.notifyfollower(this.winserver.current_user.getNickname(), token[1], 2);
                        if (ris == 1) {
                            responce = responseManager.send_response(ris, token[0]) + token[1] + "\n";
                        } else responce = responseManager.send_response(ris, token[0]);
                        return;
                    }

                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (AlreadyBoundException e) {
                    e.printStackTrace();
                }
            }
        }
        //Metodo post
        if (token[0].equals("post")) {
            String sep = "\"";
            String text = request.replace(token[0], "");
            String[] finals = text.split(Pattern.quote(sep));
            if (finals.length == 4) {
                int ris = this.winserver.create_post(finals[1], finals[3]);
                responce = responseManager.send_response(ris, token[0]) + "(id=" + ris + ")";
                return;
            }

        }
        //Metodo blog
        if (token[0].equals("blog") && token.length == 1) {
            responce = this.winserver.show_blog();
            return;
        }
        //Metodo feed
        if (token[0].equals("feed") && token.length == 1) {
            responce = this.winserver.show_feed();
            return;
        }
        //Metodo show
        if (token[0].equals("show") && token.length == 2) {
            int id = Integer.parseInt(token[1]);
            responce = this.winserver.show_post(id);
            return;
        }
        //Metodo delete
        if (token[0].equals("delete") && token.length == 2) {
            int id = Integer.parseInt(token[1]);
            responce = this.winserver.delete_post(id);
            return;
        }
        //Metodo delete
        if (token[0].equals("rewin") && token.length == 2) {
            int id = Integer.parseInt(token[1]);
            responce = this.winserver.rewin_post(id);
            return;
        }
        //Metodo vote
        if (token[0].equals("vote") && token.length == 3) {
            int id = Integer.parseInt(token[1]);
            int vote = Integer.parseInt(token[2]);
            responce = this.winserver.rate_post(id, vote);
            return;
        }
        //Metodo comment
        if (token[0].equals("comment") && token.length >= 1) {
            int id = Integer.parseInt(token[1]);
            String sep = "\"";
            String text = request.replace(token[0], "");
            String[] finals = text.split(Pattern.quote(sep));
            if (finals.length == 2) {
                responce = this.winserver.comment_post(id, finals[1]);
                return;
            }
        }
        //Metodo wallet
        if (token[0].equals("wallet")) {
            responce = this.winserver.get_wallet();
            return;
        }
        //metodo wallet bitcoin
        if (token[0].equals("walletbit")) {
            responce = this.winserver.get_walletbit();
            return;
        }
        //operazione non corretta
        responce = "Operazione non corretta";
        return;
    }

}