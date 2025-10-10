package appli;

import bri.ServeurProg;
import bri.ServeurAma;
import bri.ServiceRegistry;

public class BRiLaunch {
    private final static int PORT_PROG = 3001;
    private final static int PORT_AMA = 3000;
    
    public static void main(String[] args) {
        System.out.println("=== PLATEFORME BRi ===");
        System.out.println("Démarrage de la plateforme de services dynamiques BRi");
        
        // Démarrer les serveurs
        new Thread(new ServeurProg(PORT_PROG)).start();
        new Thread(new ServeurAma(PORT_AMA)).start();
        
        System.out.println("Serveur Programmeurs : port " + PORT_PROG);
        System.out.println("Serveur Amateurs : port " + PORT_AMA);
        System.out.println("Plateforme BRi opérationnelle !");
        
        // Garder le main thread actif
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.out.println("Arrêt de la plateforme BRi");
        }
    }
}
