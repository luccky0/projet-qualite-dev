package bri;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServeurProg implements Runnable {
    
    private int port;
    
    public ServeurProg(int port) {
        this.port = port;
    }

    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Serveur Programmeurs démarré sur le port " + port);
            
            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("Nouvelle connexion programmeur : " + client.getInetAddress());
                
                ServiceProg serviceProg = new ServiceProg(client);
                serviceProg.start();
            }
        } catch (IOException e) {
            System.err.println("Erreur serveur programmeurs : " + e.getMessage());
        }
    }
}