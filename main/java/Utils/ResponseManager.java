package main.java.Utils;

//Classe di utilità con un solo metodo per interpretare il risultato di
// alcune operazioni del server
public class ResponseManager {

    public String send_response(int ris, String op) {
        String response = "Inserire una operazione corretta!\n";
        if (op.equals("login")) {
            switch (ris) {
                case -2:
                    response = "Un è utente già loggato\n";
                    return response;

                case -1:
                    response.concat("Inserire un nome utente valido\n");
                    return response;

                case 0:
                    response = "Utente non registrato, effettuare registrazione!\n";
                    return response;
                case 1:
                    response = "Utente loggato correttamente\n";
                    return response;
            }
        }
        if (op.equals("logout")) {
            switch (ris) {
                case 0:
                    response = "Utente non loggato\n!\n";
                    return response;
                case 1:
                    response = "Goodbye\n";
                    return response;
            }
        }
        if (op.equals("follow")) {
            switch (ris) {
                case -2:
                    response = "Utente non loggato\n!\n";
                    return response;
                case -1:
                    response = "utente non trovato\n";
                    return response;
                case 0:
                    response = "non puoi seguire te stesso\n";
                    return response;
                case 1:
                    response = "Ora segui ";
                    return response;
            }

        }
        if (op.equals("unfollow")) {
            switch (ris) {
                case -2:
                    response = "Utente non loggato\n!\n";
                    return response;
                case -1:
                    response = "utente non trovato\n";
                    return response;
                case 0:
                    response = "non puoi rimuovere  te stesso\n";
                    return response;
                case 1:
                    response = "Ora non segui più ";
                    return response;
            }

        }
        if (op.equals("post")) {
            switch (ris) {
                case -2:
                    response = "Post troppo lungo!\n";
                    return response;
                case -1:
                    response = "utente non loggato\n";
                    return response;
                default:
                    response = "Post creato ";
                    return response;
            }

        }

        return response;
    }
}
