package clientprog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Client pour les programmeurs de services BRi
 * Se connecte au port PORT_PROG pour gérer leurs services
 */
public class ApplicationProg {
    private final static int PORT_PROG = 3001;
    private final static String HOST = "localhost";

    public static void main(String[] args) {
        Socket s = null;
        try {
            s = new Socket(HOST, PORT_PROG);

            BufferedReader sin = new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter sout = new PrintWriter(s.getOutputStream(), true);
            BufferedReader clavier = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Connecté au serveur BRi Programmeurs " + s.getInetAddress() + ":" + s.getPort());

            String line;
            
            // Boucle de communication avec le serveur
            while (true) {
                // Lire le message du serveur
                line = sin.readLine();
                if (line == null) {
                    break; // Connexion fermée par le serveur
                }
                
                // Afficher le message (remplacer ## par des retours à la ligne)
                System.out.println(line.replaceAll("##", "\n"));
                
                // Si le message se termine par ":", c'est une demande de saisie
                if (line.endsWith(":") || line.contains("Votre choix :") || 
                    line.contains("Login :") || line.contains("Mot de passe :") ||
                    line.contains("Nom de") || line.contains("Nouvelle") || 
                    line.contains("classe")) {
                    
                    // Lire la réponse de l'utilisateur et l'envoyer
                    String reponse = clavier.readLine();
                    sout.println(reponse);
                    
                    // Si l'utilisateur tape "0" ou "quit", on sort
                    if (reponse.equals("0") || reponse.equalsIgnoreCase("quit")) {
                        break;
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Erreur de connexion : " + e.getMessage());
        } finally {
            // Refermer dans tous les cas la socket
            try {
                if (s != null) s.close();
            } catch (IOException e2) {
                System.err.println("Erreur lors de la fermeture : " + e2.getMessage());
            }
        }
    }
}