package bri;

import java.io.*;
import java.net.*;

/**
 * Serveur TCP pour les amateurs (consommateurs de services)
 * Les amateurs se connectent pour utiliser les services disponibles
 */
public class ServeurAmateur implements Runnable {
    private ServerSocket listen_socket;
    private final int port;
    
    /**
     * Crée un serveur TCP pour les amateurs
     * @param port Port d'écoute pour les amateurs
     */
    public ServeurAmateur(int port) {
        this.port = port;
        try {
            listen_socket = new ServerSocket(port);
            System.out.println("Serveur amateur démarré sur le port " + port);
        } catch (IOException e) {
            throw new RuntimeException("Impossible de créer le serveur amateur sur le port " + port, e);
        }
    }
    
    /**
     * Boucle principale du serveur - accepte les connexions et crée un thread par client
     */
    @Override
    public void run() {
        try {
            System.out.println("Serveur amateur en écoute sur le port " + port);
            while (true) {
                Socket clientSocket = listen_socket.accept();
                System.out.println("Nouvelle connexion amateur depuis : " + 
                                 clientSocket.getRemoteSocketAddress());
                
                // Créer un thread pour traiter ce client amateur
                new ServiceAmateur(clientSocket).start();
            }
        } catch (IOException e) {
            if (!listen_socket.isClosed()) {
                System.err.println("Erreur sur le port d'écoute amateur : " + e.getMessage());
            }
        } finally {
            try {
                if (listen_socket != null && !listen_socket.isClosed()) {
                    listen_socket.close();
                }
            } catch (IOException e) {
                System.err.println("Erreur lors de la fermeture du serveur amateur : " + e.getMessage());
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
                System.out.println("Serveur amateur arrêté");
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de l'arrêt du serveur amateur : " + e.getMessage());
        }
    }
    
    /**
     * Lance le serveur dans un thread séparé
     */
    public void start() {
        new Thread(this, "ServeurAmateur-" + port).start();
    }
}
