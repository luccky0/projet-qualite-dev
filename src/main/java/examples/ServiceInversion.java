package examples;

import java.io.*;
import java.net.*;
import bri.Service;

/**
 * Service d'exemple : inversion de texte
 * Implémente la norme BRi pour servir d'exemple et de test
 */
public class ServiceInversion implements Service {
    
    private final Socket client;
    
    /**
     * Constructeur requis par la norme BRi
     * @param socket Socket de connexion du client
     */
    public ServiceInversion(Socket socket) {
        this.client = socket;
    }
    
    /**
     * Méthode principale du service - inverse le texte fourni par le client
     */
    @Override
    public void run() {
        BufferedReader in = null;
        PrintWriter out = null;
        
        try {
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(client.getOutputStream(), true);
            
            // Demander le texte à inverser
            out.println("=== Service d'Inversion de Texte ===");
            out.println("Tapez un texte à inverser :");
            
            // Lire le texte du client
            String line = in.readLine();
            if (line == null || line.trim().isEmpty()) {
                out.println("ERREUR: Aucun texte reçu");
                return;
            }
            
            // Inverser le texte
            String invLine = new StringBuilder(line).reverse().toString();
            
            // Envoyer le résultat
            out.println("Texte original : " + line);
            out.println("Texte inversé : " + invLine);
            out.println("Service terminé.");
            
        } catch (IOException e) {
            System.err.println("Erreur dans ServiceInversion : " + e.getMessage());
        } finally {
            // Fermer la connexion
            try {
                if (client != null && !client.isClosed()) {
                    client.close();
                }
            } catch (IOException e) {
                System.err.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
            }
        }
    }
    
    /**
     * Méthode statique requise par la norme BRi
     * Retourne la description du service
     * @return Description du service
     */
    public static String toStringue() {
        return "Inversion de texte";
    }
}
