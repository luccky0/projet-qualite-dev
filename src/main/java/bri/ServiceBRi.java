package bri;

import java.io.*;
import java.lang.reflect.Constructor;
import java.net.*;

class ServiceBRi implements Runnable {
    
    private Socket client;
    
    ServiceBRi(Socket socket) {
        client = socket;
    }

    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            
            String menu = ServiceRegistry.toStringue() + "##Tapez le numero de service desire :";
            out.println(menu);
            
            String choixStr = in.readLine();
            if (choixStr == null) {
                client.close();
                return;
            }
            
            int choix = Integer.parseInt(choixStr.trim());
            
            Class<?> serviceClass = ServiceRegistry.getServiceClass(choix);
            if (serviceClass != null) {
                // Créer une instance du service par réflexion
                Constructor<?> constructor = serviceClass.getConstructor(Socket.class);
                Object serviceInstance = constructor.newInstance(client);
                
                // Vérifier que c'est bien un Service
                if (serviceInstance instanceof Service) {
                    Service service = (Service) serviceInstance;
                    // Lancer le service dans un thread séparé
                    new Thread(service).start();
                } else {
                    out.println("Erreur: le service ne respecte pas l'interface Service");
                    client.close();
                }
            } else {
                out.println("Service non trouvé");
                client.close();
            }
        } catch (NumberFormatException e) {
            try {
                PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                out.println("Numéro de service invalide");
                client.close();
            } catch (IOException e2) {}
        } catch (Exception e) {
            System.err.println("Erreur dans ServiceBRi: " + e.getMessage());
            e.printStackTrace();
            try { 
                client.close(); 
            } catch (IOException e2) {}
        }
    }
    
    protected void finalize() throws Throwable {
         client.close(); 
    }

    public void start() {
        (new Thread(this)).start();		
    }
}
