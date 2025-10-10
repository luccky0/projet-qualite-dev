package bri;

import java.io.*;
import java.net.*;

/**
 * Serveur TCP pour les programmeurs
 * Les programmeurs se connectent pour gérer leurs services
 */
public class ServeurProgrammeur implements Runnable {
    private ServerSocket listen_socket;
    private final int port;
    
    /**
     * Crée un serveur TCP pour les programmeurs
     * @param port Port d'écoute pour les programmeurs
     */
    public ServeurProgrammeur(int port) {
        this.port = port;
        try {
            listen_socket = new ServerSocket(port);
            System.out.println("Serveur programmeur démarré sur le port " + port);
        } catch (IOException e) {
            throw new RuntimeException("Impossible de créer le serveur programmeur sur le port " + port, e);
        }
    }
    
    /**
     * Boucle principale du serveur - accepte les connexions et crée un thread par client
     */
    @Override
    public void run() {
        try {
            System.out.println("Serveur programmeur en écoute sur le port " + port);
            while (true) {
                Socket clientSocket = listen_socket.accept();
                System.out.println("Nouvelle connexion programmeur depuis : " + 
                                 clientSocket.getRemoteSocketAddress());
                
                // Créer un thread pour traiter ce client programmeur
                new ServiceProgrammeur(clientSocket).start();
            }
        } catch (IOException e) {
            if (!listen_socket.isClosed()) {
                System.err.println("Erreur sur le port d'écoute programmeur : " + e.getMessage());
            }
        } finally {
            try {
                if (listen_socket != null && !listen_socket.isClosed()) {
                    listen_socket.close();
                }
            } catch (IOException e) {
                System.err.println("Erreur lors de la fermeture du serveur programmeur : " + e.getMessage());
            }
        }
    }
    
    /**
     * Arrête le serveur
     */
    public void stop() {
        try {
            if (listen_socket != null && !listen_socket.isClosed()) {
                listen_socket.close();
                System.out.println("Serveur programmeur arrêté");
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de l'arrêt du serveur programmeur : " + e.getMessage());
        }
    }
    
    /**
     * Lance le serveur dans un thread séparé
     */
    public void start() {
        new Thread(this, "ServeurProgrammeur-" + port).start();
    }
}
