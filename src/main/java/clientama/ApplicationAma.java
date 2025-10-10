package clientama;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Client pour les amateurs de services BRi
 * Se connecte au port PORT_AMA pour utiliser les services disponibles
 */
public class ApplicationAma {
    private final static int PORT_AMA = 3000;
    private final static String HOST = "localhost";

    public static void main(String[] args) {
        Socket s = null;
        try {
            s = new Socket(HOST, PORT_AMA);

            BufferedReader sin = new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter sout = new PrintWriter(s.getOutputStream(), true);
            BufferedReader clavier = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Connecté au serveur BRi Amateurs " + s.getInetAddress() + ":" + s.getPort());

            String line;

            // 1. Recevoir et afficher le menu des services
            line = sin.readLine();
            if (line != null) {
                System.out.println(line.replaceAll("##", "\n"));
                
                // 2. Saisir et envoyer le choix du service
                String choix = clavier.readLine();
                sout.println(choix);

                // 3. Boucle d'interaction avec le service choisi
                while (true) {
                    // Lire le message du service
                    String message = sin.readLine();
                    if (message == null) {
                        System.out.println("Service terminé.");
                        break;
                    }
                    
                    System.out.println(message);
                    
                    // Si c'est une question du service, répondre
                    if (message.contains("Tapez") || message.contains("Entrez") || 
                        message.contains("Saisissez") || message.contains("?")) {
                        
                        String reponse = clavier.readLine();
                        if (reponse == null) {
                            break;
                        }
                        sout.println(reponse);
                        
                        // Pour certains services, il peut y avoir une réponse finale
                        String resultat = sin.readLine();
                        if (resultat != null) {
                            System.out.println("Résultat : " + resultat);
                        }
                        break; // Service simple terminé après un échange
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