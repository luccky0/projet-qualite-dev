package bri;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServeurAma implements Runnable {
    
    private int port;
    
    public ServeurAma(int port) {
        this.port = port;
    }

    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Serveur Amateurs démarré sur le port " + port);
            
            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("Nouvelle connexion amateur : " + client.getInetAddress());
                
                ServiceBRi serviceBRi = new ServiceBRi(client);
                serviceBRi.start();
            }
        } catch (IOException e) {
            System.err.println("Erreur serveur amateurs : " + e.getMessage());
        }
    }
}