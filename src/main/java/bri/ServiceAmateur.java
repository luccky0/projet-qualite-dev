package bri;

import java.io.*;
import java.lang.reflect.Constructor;
import java.net.*;

/**
 * Service pour traiter les demandes des clients amateurs
 * Chaque client amateur a son propre thread ServiceAmateur
 */
public class ServiceAmateur implements Runnable {
    
    private final Socket client;
    
    /**
     * Constructeur pour un client amateur
     * @param socket Socket de connexion du client
     */
    public ServiceAmateur(Socket socket) {
        this.client = socket;
    }
    
    /**
     * Traite la demande du client amateur
     */
    @Override
    public void run() {
        BufferedReader in = null;
        PrintWriter out = null;
        
        try {
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(client.getOutputStream(), true);
            
            // Envoyer le menu des services disponibles
            String menu = ServiceRegistry.toStringue() + "##Tapez le numéro de service désiré :";
            out.println(menu);
            
            // Lire le choix du client
            String choixStr = in.readLine();
            if (choixStr == null || choixStr.trim().isEmpty()) {
                out.println("Aucun choix reçu, connexion fermée.");
                return;
            }
            
            try {
                int choix = Integer.parseInt(choixStr.trim());
                
                // Récupérer la classe du service demandé
                Class<?> serviceClass = ServiceRegistry.getServiceClass(choix);
                if (serviceClass != null) {
                    // Créer une instance du service par réflexion
                    Constructor<?> constructor = serviceClass.getConstructor(Socket.class);
                    Object serviceInstance = constructor.newInstance(client);
                    
                    // Vérifier que c'est bien un Service
                    if (serviceInstance instanceof Service) {
                        Service service = (Service) serviceInstance;
                        System.out.println("Lancement du service " + serviceClass.getSimpleName() + 
                                         " pour le client " + client.getRemoteSocketAddress());
                        
                        // Lancer le service dans le thread courant
                        // (le socket sera fermé par le service lui-même)
                        service.run();
                        return; // Le service gère la fermeture
                    } else {
                        out.println("ERREUR: Le service ne respecte pas l'interface Service");
                    }
                } else {
                    out.println("ERREUR: Service non trouvé");
                }
            } catch (NumberFormatException e) {
                out.println("ERREUR: Numéro de service invalide");
            }
            
        } catch (Exception e) {
            System.err.println("Erreur dans ServiceAmateur pour " + 
                             client.getRemoteSocketAddress() + " : " + e.getMessage());
            if (out != null) {
                out.println("ERREUR: " + e.getMessage());
            }
        } finally {
            // Fermer la connexion si elle n'a pas été fermée par le service
            try {
                if (client != null && !client.isClosed()) {
                    client.close();
                }
            } catch (IOException e) {
                System.err.println("Erreur lors de la fermeture de la connexion amateur : " + e.getMessage());
            }
        }
    }
    
    /**
     * Lance ce service dans un nouveau thread
     */
    public void start() {
        new Thread(this, "ServiceAmateur-" + client.getRemoteSocketAddress()).start();
    }
}
