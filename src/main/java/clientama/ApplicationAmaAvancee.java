package clientama;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Version avancée du client amateur avec possibilité de reconnexion
 */
public class ApplicationAmaAvancee {
    private final static int PORT_AMA = 3000;
    private final static String HOST = "localhost";

    public static void main(String[] args) {
        BufferedReader clavier = new BufferedReader(new InputStreamReader(System.in));
        
        System.out.println("=== CLIENT AMATEUR BRi ===");
        
        while (true) {
            System.out.println("\n1 - Se connecter à un service");
            System.out.println("2 - Quitter");
            System.out.print("Votre choix : ");
            
            try {
                String choixMenu = clavier.readLine();
                
                if (choixMenu.equals("2")) {
                    System.out.println("Au revoir !");
                    break;
                } else if (choixMenu.equals("1")) {
                    connecterService();
                } else {
                    System.out.println("Choix invalide !");
                }
            } catch (IOException e) {
                System.err.println("Erreur de saisie : " + e.getMessage());
                break;
            }
        }
    }
    
    private static void connecterService() {
        Socket s = null;
        try {
            s = new Socket(HOST, PORT_AMA);

            BufferedReader sin = new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter sout = new PrintWriter(s.getOutputStream(), true);
            BufferedReader clavier = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("\nConnecté au serveur BRi : " + s.getInetAddress() + ":" + s.getPort());

            // 1. Recevoir et afficher le menu des services
            String line = sin.readLine();
            if (line != null) {
                System.out.println("\n" + line.replaceAll("##", "\n"));
                
                // 2. Saisir et envoyer le choix du service
                String choix = clavier.readLine();
                sout.println(choix);

                // 3. Interaction avec le service
                boolean serviceActif = true;
                while (serviceActif) {
                    String message = sin.readLine();
                    if (message == null) {
                        System.out.println("Service terminé.");
                        break;
                    }
                    
                    System.out.println(message);
                    
                    // Services interactifs
                    if (message.toLowerCase().contains("tapez") || 
                        message.toLowerCase().contains("entrez") || 
                        message.toLowerCase().contains("saisissez") ||
                        message.contains("?")) {
                        
                        System.out.print("> ");
                        String reponse = clavier.readLine();
                        if (reponse == null || reponse.equalsIgnoreCase("quit")) {
                            serviceActif = false;
                            break;
                        }
                        sout.println(reponse);
                    }
                    
                    // Pour certains services simples, récupérer le résultat
                    if (message.contains("inverser") || message.contains("analyser")) {
                        try {
                            Thread.sleep(100); // Petit délai pour s'assurer que la réponse arrive
                            if (sin.ready()) {
                                String resultat = sin.readLine();
                                if (resultat != null) {
                                    System.out.println("Résultat : " + resultat);
                                }
                            }
                        } catch (Exception e) {
                            // Continue
                        }
                        serviceActif = false; // Service simple terminé
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Erreur de connexion : " + e.getMessage());
        } finally {
            try {
                if (s != null) s.close();
            } catch (IOException e2) {
                System.err.println("Erreur lors de la fermeture : " + e2.getMessage());
            }
        }
    }
}